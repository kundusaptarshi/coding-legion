package com.codinglegion.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.ui.JBUI;
import com.codinglegion.model.Violation;
import com.codinglegion.model.ViolationSeverity;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.DefaultCaret;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Main panel for displaying violations in Coding Legion tool window
 */
public class ViolationTreePanel extends JPanel {
    
    private static final Logger LOG = Logger.getInstance(ViolationTreePanel.class);
    public static final Key<ViolationTreePanel> KEY = Key.create("CodingLegion.ViolationTreePanel");
    
    private final Project project;
    private final JEditorPane errorsPane;
    private final JEditorPane warningsPane;
    private final JLabel statusLabel;
    private final JLabel headerLabel;
    private final JTabbedPane tabbedPane;
    private final JButton rerunButton;
    private final JPanel warningsFooter;
    private boolean hasRunAnalysis = false;
    private boolean isRerun = false; // Track if this is a re-run
    private List<Violation> currentErrors;
    private List<Violation> currentWarnings;
    
    public ViolationTreePanel(Project project) {
        this.project = project;
        setLayout(new BorderLayout());
        setBackground(JBColor.background());
        
        // Create header panel with proper layout
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new JBColor(new Color(43, 43, 43), new Color(43, 43, 43)));
        headerPanel.setBorder(JBUI.Borders.empty(6, 10, 6, 10));
        
        // Create left side of header (logo + title + counts)
        headerLabel = new JLabel();
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 14f));
        headerLabel.setIcon(loadIcon());
        headerLabel.setForeground(new JBColor(new Color(187, 187, 187), new Color(187, 187, 187)));
        
        // Create right side of header (Documentation link and Re-run button)
        JLabel docsLabel = new JLabel("📄 Docs");
        docsLabel.setFont(docsLabel.getFont().deriveFont(Font.PLAIN, 11f));
        docsLabel.setForeground(new JBColor(new Color(120, 150, 200), new Color(120, 150, 200)));
        docsLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        docsLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        docsLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openReadmeInEditor();
            }
        });
        
        // Create re-run button for header
        rerunButton = new JButton("🔄");
        rerunButton.setToolTipText("Re-run analysis");
        rerunButton.setFont(rerunButton.getFont().deriveFont(Font.PLAIN, 14f));
        rerunButton.setFocusable(false);
        rerunButton.setContentAreaFilled(false);
        rerunButton.setBorderPainted(false);
        rerunButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        rerunButton.setVisible(false); // Initially hidden
        rerunButton.setPreferredSize(new Dimension(30, 20));
        rerunButton.addActionListener(e -> rerunAnalysis());
        
        // Create right panel with both docs and re-run button
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.X_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.add(rerunButton);
        rightPanel.add(Box.createHorizontalStrut(10)); // Space between buttons
        rightPanel.add(docsLabel);
        
        // Add to header panel
        headerPanel.add(headerLabel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        // Create Errors pane
        errorsPane = createViolationPane();
        errorsPane.setText("<html><body style='padding: 10px;'>Analysis not run yet. Run Legion Analysis from Tools menu or right-click project.</body></html>");
        
        // Create Warnings pane
        warningsPane = createViolationPane();
        warningsPane.setText("<html><body style='padding: 10px;'>Analysis not run yet. Run Legion Analysis from Tools menu or right-click project.</body></html>");
        
        // Create footer for warnings tab
        warningsFooter = createWarningsFooter();
        
        // Create warnings tab panel with footer fixed at bottom
        JPanel warningsTabPanel = new JPanel(new BorderLayout());
        warningsTabPanel.setBackground(JBColor.background());
        warningsTabPanel.add(new JBScrollPane(warningsPane), BorderLayout.CENTER);
        warningsTabPanel.add(warningsFooter, BorderLayout.SOUTH);
        
        // Create tabbed pane
        tabbedPane = new JBTabbedPane();
        tabbedPane.setBackground(JBColor.background());
        String errorsTitle = String.format("<html><b><font color='#FF5555'>⚠ Errors (%d)</font></b></html>", 0);
        String warningsTitle = String.format("<html><b><font color='#FFB86C'>⚡ Warnings (%d)</font></b></html>", 0);
        tabbedPane.addTab(errorsTitle, new JBScrollPane(errorsPane));
        tabbedPane.addTab(warningsTitle, warningsTabPanel);
        
        // Create status bar
        statusLabel = new JLabel("Ready. Run Legion Analysis from Tools menu or right-click project to scan for violations.");
        statusLabel.setBorder(JBUI.Borders.empty(5));
        statusLabel.setForeground(new JBColor(new Color(150, 150, 150), new Color(150, 150, 150)));
        
        // Layout
        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        
        updateHeader(0, 0);
    }
    
    private Icon loadIcon() {
        try {
            // Load pre-scaled 16x16 icon
            java.net.URL iconUrl = getClass().getResource("/icons/logo_16.png");
            if (iconUrl != null) {
                return new ImageIcon(iconUrl);
            }
        } catch (Exception e) {
            // Fallback to no icon
        }
        return null;
    }
    
    /**
     * Create the fixed footer panel for warnings tab
     */
    private JPanel createWarningsFooter() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new JBColor(new Color(43, 43, 43), new Color(43, 43, 43)));
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, new JBColor(new Color(255, 184, 108), new Color(255, 184, 108))),
            JBUI.Borders.empty(6, 10)
        ));
        
        JEditorPane footerText = new JEditorPane();
        footerText.setContentType("text/html");
        footerText.setEditable(false);
        footerText.setOpaque(false);
        footerText.setBackground(new JBColor(new Color(43, 43, 43), new Color(43, 43, 43)));
        
        // IMPORTANT: Set editor kit and styles BEFORE adding listener and content
        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet styleSheet = kit.getStyleSheet();
        Color linkColor = new JBColor(new Color(88, 166, 255), new Color(88, 166, 255));
        String linkColorHex = String.format("#%02x%02x%02x", linkColor.getRed(), linkColor.getGreen(), linkColor.getBlue());
        styleSheet.addRule("a { color: " + linkColorHex + " !important; text-decoration: underline !important; }");
        styleSheet.addRule("a:hover { text-decoration: underline !important; }");
        footerText.setEditorKit(kit);
        
        // Hyperlink listener for Best Practices link
        footerText.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                String desc = e.getDescription();
                if (desc != null && desc.startsWith("docs:")) {
                    String violationTypeName = desc.substring("docs:".length());
                    openViolationDocumentation(violationTypeName);
                }
            }
        });
        
        String footerHTML = 
            "<html><body style='margin: 0; padding: 0; font-size: 9px; color: #CCCCCC;'>" +
            "<b>ℹ️ Note:</b> Not all violations are auto-detected. " +
            "Review <a href='docs:BEST_PRACTICES'>Best Practices</a> for manual guidelines." +
            "</body></html>";
        
        footerText.setText(footerHTML);
        footerPanel.add(footerText, BorderLayout.CENTER);
        
        return footerPanel;
    }
    
    private void openReadmeInEditor() {
        openViolationDocumentation(null);
    }
    
    private void openViolationDocumentation(String violationTypeName) {
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                // ALWAYS recreate temp file to ensure latest README content
                File tempFile = new File(System.getProperty("java.io.tmpdir"), "Coding-Legion-Documentation.md");
                
                java.io.InputStream is = getClass().getResourceAsStream("/README.md");
                if (is == null) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Plugin documentation not found.",
                        "Coding Legion",
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
                
                java.nio.file.Files.copy(is, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                is.close();
                
                VirtualFile vFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(tempFile);
                
                if (vFile != null) {
                    // If specific violation type requested, ALWAYS navigate to that section
                    if (violationTypeName != null) {
                        int sectionLine = findViolationSection(vFile, violationTypeName);
                        if (sectionLine >= 0) {
                            // WORKAROUND: To force re-navigation to the same line, we briefly navigate elsewhere first
                            // This ensures IntelliJ recognizes the navigation as a "change" even if file is already open
                            
                            // Step 1: Navigate to line 0 (top of file) first
                            OpenFileDescriptor topDescriptor = new OpenFileDescriptor(project, vFile, 0, 0);
                            topDescriptor.navigate(false);  // Don't focus yet
                            
                            // Step 2: Small delay then navigate to target line
                            ApplicationManager.getApplication().invokeLater(() -> {
                                OpenFileDescriptor targetDescriptor = new OpenFileDescriptor(project, vFile, sectionLine, 0);
                                targetDescriptor.navigate(true);  // Now focus and navigate
                            });
                        } else {
                            // Section not found, just open the file
                            FileEditorManager.getInstance(project).openFile(vFile, true);
                        }
                    } else {
                        // Just open the file at top
                        FileEditorManager.getInstance(project).openFile(vFile, true);
                    }
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "Documentation not available.\n\nPlease check the Code Review Guidelines link.",
                        "Coding Legion",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                }
            } catch (Exception e) {
                LOG.error("Error opening documentation", e);
            }
        });
    }
    
    private int findViolationSection(VirtualFile readmeFile, String violationTypeName) {
        try {
            String content = new String(readmeFile.contentsToByteArray());
            String[] lines = content.split("\n");
            
            // Get search patterns for this violation type
            String[] searchPatterns = getSearchPatterns(violationTypeName);
            if (searchPatterns == null || searchPatterns.length == 0) {
                return -1;
            }
            
            // Search for patterns IN ORDER (first pattern has priority)
            // This ensures we find headers before text mentions
            for (String pattern : searchPatterns) {
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i].trim();
                    
                    // For header patterns (starting with # or ##), require line to START with it
                    if (pattern.startsWith("#")) {
                        if (line.startsWith(pattern)) {
                            return i;
                        }
                    } else {
                        // For non-header patterns, just check if line contains it
                        if (line.contains(pattern)) {
                            return i;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Error finding violation section", e);
        }
        return -1;
    }
    
    /**
     * Get flexible search patterns for finding violation sections
     * Uses multiple patterns to handle README changes without breaking navigation
     */
    private String[] getSearchPatterns(String violationTypeName) {
        switch (violationTypeName) {
            case "STRING_EQUALS":
                return new String[]{"String Equals Without Null Safety", "String.equals()", "#### 1."};
            
            case "STRING_EMPTY_CHECK":
                return new String[]{"Manual String Null/Empty Check", "String Empty Check", "String empty", "#### 2."};
            
            case "COLLECTION_NULL_CHECK":
            case "COLLECTION_SIZE_CHECK":
                return new String[]{"Collection Checks (Combined", "Collection Check", "Manual Collection", "Collection Size", "#### 3-4.", "#### 3."};
            
            case "TERNARY_NULL_DEFAULT":
                return new String[]{"Null Default Pattern", "Ternary Null Default", "Ternary or If-Else", "#### 5."};
            
            case "BOOLEAN_UNBOXING":
                return new String[]{"Boolean Auto-Unboxing", "Boolean Unboxing", "#### 6."};
            
            case "NULL_VALUE_IN_CONTEXT":
                return new String[]{"Null Value in Context", "Null in Context", "Context/Attributes", "#### 7."};
            
            case "LOG_NULL_DEREFERENCE":
                return new String[]{"Null Dereference in Log", "Log Null Dereference", "Log Statement", "#### 8."};
            
            case "DTO_INITIALIZATION_CHECK":
                return new String[]{"DTO Property Access", "DTO Initialization", "isInitialized", "#### 9."};
            
            case "NULL_VALUE_IN_MAP":
                return new String[]{"Null Value in Map", "Null in Map", "Map.put", "#### 10."};
            
            case "BEST_PRACTICES":
                return new String[]{"## 🎯 Best Practices", "Best Practices", "Important Coding Principles"};
            
            default:
                return null;
        }
    }
    
    private JEditorPane createViolationPane() {
        JEditorPane pane = new JEditorPane();
        pane.setContentType("text/html");
        pane.setEditable(false);
        pane.setOpaque(false);
        pane.setBackground(JBColor.background());
        pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        
        // Use a custom caret that's never visible
        pane.setCaret(new DefaultCaret() {
            @Override
            public void setVisible(boolean visible) {
                // Always override to invisible
                super.setVisible(false);
            }
            
            @Override
            public boolean isVisible() {
                return false;
            }
        });
        pane.getCaret().setBlinkRate(0);
        
        // Hyperlink listener for both violation navigation and documentation
        pane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                String desc = e.getDescription();
                if (desc == null) return;
                
                if (desc.startsWith("violation:")) {
                    // Navigate to violation in source code
                    int violationIndex = Integer.parseInt(desc.substring("violation:".length()));
                    boolean isErrorsPane = (pane == errorsPane);
                    List<Violation> targetList = isErrorsPane ? currentErrors : currentWarnings;
                    if (targetList != null && violationIndex < targetList.size()) {
                        navigateToPosition(targetList.get(violationIndex));
                    }
                } else if (desc.startsWith("docs:")) {
                    // Navigate to documentation section for this violation type
                    String violationTypeName = desc.substring("docs:".length());
                    openViolationDocumentation(violationTypeName);
                }
            }
        });
        
        // Detect theme colors
        Color linkColor = new JBColor(new Color(26, 13, 171), new Color(88, 166, 255));
        Color visitedColor = new JBColor(new Color(104, 29, 168), new Color(158, 111, 216));
        Color secondaryTextColor = new JBColor(new Color(95, 99, 104), new Color(154, 160, 166));
        
        // Custom CSS with theme-adaptive colors
        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet styleSheet = kit.getStyleSheet();
        
        String linkColorHex = String.format("#%02x%02x%02x", linkColor.getRed(), linkColor.getGreen(), linkColor.getBlue());
        String visitedColorHex = String.format("#%02x%02x%02x", visitedColor.getRed(), visitedColor.getGreen(), visitedColor.getBlue());
        String secondaryColorHex = String.format("#%02x%02x%02x", secondaryTextColor.getRed(), secondaryTextColor.getGreen(), secondaryTextColor.getBlue());
        
        styleSheet.addRule("body { " +
            "font-family: 'Consolas', 'Monaco', 'Courier New', monospace; " +
            "font-size: 11px; " +
            "padding: 8px 10px; " +
            "line-height: 1.6; " +
            "background: transparent; " +
            "}");
        
        // Hyperlink styles - always underlined for consistency (no hover glitch)
        styleSheet.addRule("a { " +
            "color: " + linkColorHex + " !important; " +
            "text-decoration: underline !important; " +
            "cursor: pointer !important; " +
            "}");
        
        styleSheet.addRule("a:visited { " +
            "color: " + visitedColorHex + " !important; " +
            "text-decoration: underline !important; " +
            "}");
        
        pane.setEditorKit(kit);
        pane.setCursor(Cursor.getDefaultCursor());
        
        return pane;
    }
    
    public void updateViolations(List<Violation> violations) {
        // Show re-run button after first analysis
        if (!hasRunAnalysis) {
            hasRunAnalysis = true;
            ApplicationManager.getApplication().invokeLater(() -> {
                rerunButton.setVisible(true);
                rerunButton.revalidate();
                rerunButton.repaint();
            });
        }
        
        ApplicationManager.getApplication().invokeLater(() -> {
            if (violations.isEmpty()) {
                errorsPane.setText("<html><body style='padding: 10px;'>No errors found - Great job! ✓</body></html>");
                warningsPane.setText("<html><body style='padding: 10px;'>No warnings found - Great job! ✓</body></html>");
                updateHeader(0, 0);
                updateTabTitles(0, 0);
                statusLabel.setText("Analysis complete. No violations detected.");
            } else {
                // Group by severity
                Map<ViolationSeverity, List<Violation>> grouped = violations.stream()
                    .collect(Collectors.groupingBy(Violation::getSeverity));
                
                List<Violation> errors = grouped.getOrDefault(ViolationSeverity.ERROR, Collections.emptyList());
                List<Violation> warnings = grouped.getOrDefault(ViolationSeverity.WARNING, Collections.emptyList());
                
                currentErrors = errors;
                currentWarnings = warnings;
                
                int errorCount = errors.size();
                int warningCount = warnings.size();
                
                // Generate HTML for errors
                if (!errors.isEmpty()) {
                    errorsPane.setText(generateViolationsHTML(errors));
                    errorsPane.setCaretPosition(0);  // Scroll to top
                } else {
                    errorsPane.setText("<html><body style='padding: 10px;'>No errors found - Great job! ✓</body></html>");
                }
                
                // Generate HTML for warnings (footer is now separate, always visible)
                if (!warnings.isEmpty()) {
                    warningsPane.setText(generateViolationsHTML(warnings));
                    warningsPane.setCaretPosition(0);  // Scroll to top
                } else {
                    warningsPane.setText("<html><body style='padding: 10px;'>No warnings found - Great job! ✓</body></html>");
                }
                
                updateHeader(errorCount, warningCount);
                updateTabTitles(errorCount, warningCount);
                statusLabel.setText(String.format(
                    "Analysis complete. Found %d violation(s). Click to navigate.",
                    violations.size()
                ));
            }
        });
    }
    
    private String generateViolationsHTML(List<Violation> violations) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><style>");
        html.append("body { margin: 0; padding: 0; font-size: 11px; }");
        html.append(".violation { margin-bottom: 18px; padding-bottom: 0px; }");
        html.append(".violation-line { padding-left: 20px; text-indent: -20px; }");
        html.append(".suggested-line { padding-left: 20px; }");
        html.append(".number { font-weight: normal; font-size: 11px; margin-right: 4px; }");
        html.append(".location { font-weight: normal; font-size: 11px; }");
        html.append(".rule-label { font-weight: bold; margin-left: 6px; font-size: 11px; }");
        html.append(".rule-text { font-weight: normal; font-size: 11px; }");
        html.append(".suggested { display: inline; font-size: 11px; }");
        html.append("</style></head>");
        html.append("<body>");
        
        // Get theme-adaptive colors
        Color linkColor = new JBColor(new Color(26, 13, 171), new Color(88, 166, 255));
        Color visitedColor = new JBColor(new Color(104, 29, 168), new Color(158, 111, 216));
        Color secondaryTextColor = new JBColor(new Color(95, 99, 104), new Color(154, 160, 166));
        
        String linkColorHex = String.format("#%02x%02x%02x", linkColor.getRed(), linkColor.getGreen(), linkColor.getBlue());
        String visitedColorHex = String.format("#%02x%02x%02x", visitedColor.getRed(), visitedColor.getGreen(), visitedColor.getBlue());
        String secondaryColorHex = String.format("#%02x%02x%02x", secondaryTextColor.getRed(), secondaryTextColor.getGreen(), secondaryTextColor.getBlue());
        
        for (int i = 0; i < violations.size(); i++) {
            Violation violation = violations.get(i);
            
            String locationLink = String.format(
                "%s.%s:%d:%d",
                violation.getPackageName(),
                violation.getClassName() + ".java",
                violation.getLineNumber(),
                violation.getColumnNumber()
            );
            
            // Professional formatted violation with proper indentation
            html.append(String.format(
                "<div class='violation'>" +
                "<div class='violation-line'>" +
                "<span class='number'>%d.</span> " +
                "<a href='violation:%d' class='location'>%s</a>" +
                "<span class='rule-label'> - Rule:</span>" +
                "<span class='rule-text'> %s.</span> " +
                "<a href='docs:%s' style='color: %s; text-decoration: none; font-size: 12px;' title='View full documentation'>ℹ️</a>" +
                "</div>" +
                "<div class='suggested-line'>" +
                "<span class='suggested' style='color: %s;'>Suggested: %s</span>" +
                "</div>" +
                "</div>",
                i + 1,
                i,
                locationLink,
                violation.getType().getCompactDescription(),
                violation.getType().name(),
                linkColorHex,
                secondaryColorHex,
                violation.getType().getSuggestedFix()
            ));
        }
        
        html.append("</body></html>");
        return html.toString();
    }
    
    private void navigateToPosition(Violation violation) {
        if (violation.getFile() != null && violation.getFile().getVirtualFile() != null) {
            // Navigate to exact line and column
            int line = Math.max(0, violation.getLineNumber() - 1); // Convert to 0-based
            int column = Math.max(0, violation.getColumnNumber() - 1); // Convert to 0-based
            
            FileEditorManager.getInstance(project).openTextEditor(
                new OpenFileDescriptor(
                    project,
                    violation.getFile().getVirtualFile(),
                    line,
                    column
                ),
                true // focus editor
            );
        }
    }
    
    private void updateTabTitles(int errors, int warnings) {
        // Create HTML formatted tab titles with better styling (compact to fit in one line)
        String errorsTitle = String.format("<html><b><font color='#FF5555'>⚠ Errors (%d)</font></b></html>", errors);
        String warningsTitle = String.format("<html><b><font color='#FFB86C'>⚡ Warnings (%d)</font></b></html>", warnings);
        
        tabbedPane.setTitleAt(0, errorsTitle);
        tabbedPane.setTitleAt(1, warningsTitle);
        
        // Only auto-switch tabs on initial run, not on re-run
        if (!isRerun) {
            // Always switch to Errors tab for initial analysis
            tabbedPane.setSelectedIndex(0); // Always show Errors tab
        }
        
        // Reset the re-run flag
        isRerun = false;
    }
    
    private void updateHeader(int errors, int warnings) {
        String text = "  Coding Legion";
        headerLabel.setText(text);
    }
    
    private void rerunAnalysis() {
        // Mark this as a re-run to preserve tab selection
        isRerun = true;
        
        // Get the RunAnalysisAction and execute it
        AnAction runAnalysisAction = ActionManager.getInstance().getAction("com.codinglegion.RunLegionAnalysis");
        if (runAnalysisAction != null) {
            // Create a simple data context with the project
            DataContext dataContext = new DataContext() {
                @Override
                public Object getData(String dataId) {
                    if (CommonDataKeys.PROJECT.getName().equals(dataId)) {
                        return project;
                    }
                    return null;
                }
            };
            AnActionEvent event = AnActionEvent.createFromAnAction(runAnalysisAction, null, ActionPlaces.TOOLBAR, dataContext);
            runAnalysisAction.actionPerformed(event);
        }
    }
    
    /**
     * Clear all violations from the UI
     * Called when there are no files to analyze (no changes, deleted files, wrong branch)
     */
    public void clearViolations() {
        ApplicationManager.getApplication().invokeLater(() -> {
            // Clear current violations
            currentErrors = null;
            currentWarnings = null;
            
            // Reset panes to initial state
            errorsPane.setText("<html><body style='padding: 10px;'>No violations. Either no Java changes detected or analysis not run yet.</body></html>");
            warningsPane.setText("<html><body style='padding: 10px;'>No violations. Either no Java changes detected or analysis not run yet.</body></html>");
            
            // Reset header and tabs
            updateHeader(0, 0);
            updateTabTitles(0, 0);
            
            // Update status
            statusLabel.setText("No Java changes detected. Previous violations cleared.");
        });
    }
    
    public void showError(String message) {
        ApplicationManager.getApplication().invokeLater(() -> {
            String errorHTML = String.format("<html><body style='padding: 10px;'>⚠ %s</body></html>", message);
            errorsPane.setText(errorHTML);
            warningsPane.setText(errorHTML);
            statusLabel.setText(message);
            updateTabTitles(0, 0);
        });
    }
}

