package com.codinglegion.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.codinglegion.analyzer.CodingStandardsAnalyzer;
import com.codinglegion.model.Violation;
import com.codinglegion.ui.ViolationTreePanel;
import com.codinglegion.utils.GitBranchChecker;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.util.List;

/**
 * Action to run null check analysis in Coding Legion
 * Triggered by menu action from Tools or right-click
 */
public class RunAnalysisAction extends AnAction {
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        
        // Check if we're on a feature branch
        if (!GitBranchChecker.isFeatureBranch(project)) {
            String currentBranch = GitBranchChecker.getCurrentBranchName(project);
            
            // Clear previous violations from UI
            clearUI(project);
            
            showErrorDialog(
                project,
                "Cannot Run Analysis on Protected Branch",
                String.format(
                    "Coding Legion only analyzes code in feature branches.\n\n" +
                    "Current branch: %s\n\n" +
                    "Please checkout a feature branch (not main/master/develop) to run the analysis.",
                    currentBranch
                )
            );
            return;
        }
        
        // Check if there are any changed Java files
        if (!GitBranchChecker.hasChangedJavaFiles(project)) {
            // Clear previous violations from UI since there are no changes to analyze
            clearUI(project);
            
            showErrorDialog(
                project,
                "No Changed Java Files Found",
                "Coding Legion only analyzes changed/added Java files in your feature branch.\n\n" +
                "No Java file changes were detected.\n\n" +
                "Make some changes to Java files and try again."
            );
            return;
        }
        
        // Get changed Java files
        List<VirtualFile> changedFiles = GitBranchChecker.getChangedJavaFiles(project);
        
        // Show and activate tool window
        ToolWindow toolWindow = ToolWindowManager.getInstance(project)
            .getToolWindow("Coding Legion");
        
        if (toolWindow != null) {
            toolWindow.show(() -> {});
        }
        
        // Run analysis in background
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Coding Legion: Analyzing Files", true) {
            private List<Violation> violations;
            
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(false);
                indicator.setText("Initializing Coding Legion...");
                
                CodingStandardsAnalyzer analyzer = new CodingStandardsAnalyzer(project);
                
                indicator.setText(String.format("Analyzing %d changed file(s)...", changedFiles.size()));
                violations = analyzer.analyzeFiles(changedFiles, indicator);
                
                indicator.setText("Analysis complete");
            }
            
            @Override
            public void onSuccess() {
                updateUI(project, violations, changedFiles.size());
            }
            
            @Override
            public void onCancel() {
                ViolationTreePanel panel = project.getUserData(ViolationTreePanel.KEY);
                if (panel != null) {
                    panel.showError("Analysis was cancelled by user");
                }
            }
            
            @Override
            public void onThrowable(@NotNull Throwable error) {
                ViolationTreePanel panel = project.getUserData(ViolationTreePanel.KEY);
                if (panel != null) {
                    panel.showError("Analysis failed: " + error.getMessage());
                }
                
                showErrorDialog(
                    project,
                    "Analysis Error",
                    "An error occurred during analysis:\n\n" + error.getMessage()
                );
            }
        });
    }
    
    private void updateUI(Project project, List<Violation> violations, int fileCount) {
        ApplicationManager.getApplication().invokeLater(() -> {
            ViolationTreePanel panel = project.getUserData(ViolationTreePanel.KEY);
            if (panel != null) {
                panel.updateViolations(violations);
            }
            
            // No dialog boxes - just update the tabs silently for all cases
        });
    }
    
    /**
     * Clear previous violations from UI
     * Called when there are no files to analyze (no changes, wrong branch, deleted files)
     */
    private void clearUI(Project project) {
        ApplicationManager.getApplication().invokeLater(() -> {
            ViolationTreePanel panel = project.getUserData(ViolationTreePanel.KEY);
            if (panel != null) {
                panel.clearViolations();
            }
        });
    }
    
    private void showErrorDialog(Project project, String title, String message) {
        ApplicationManager.getApplication().invokeLater(() -> {
            // Create a custom panel with selectable text
            JPanel errorPanel = new JPanel(new BorderLayout());
            JTextArea textArea = new JTextArea(message);
            textArea.setEditable(false);
            textArea.setOpaque(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setFont(UIManager.getFont("Label.font"));
            textArea.setBorder(null);
            
            // Use custom invisible caret
            textArea.setCaret(new DefaultCaret() {
                @Override
                public void setVisible(boolean visible) {
                    super.setVisible(false);
                }
                
                @Override
                public boolean isVisible() {
                    return false;
                }
            });
            textArea.getCaret().setBlinkRate(0);
            
            textArea.setBackground(UIManager.getColor("Panel.background"));
            errorPanel.add(textArea, BorderLayout.CENTER);
            errorPanel.setPreferredSize(new Dimension(400, 150));
            
            JOptionPane.showMessageDialog(
                null,
                errorPanel,
                title,
                JOptionPane.ERROR_MESSAGE
            );
        });
    }
}

