package com.codinglegion.startup;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Opens README.md in preview mode when the project is opened
 * Only runs once per project session
 */
public class OpenReadmeOnStartup implements StartupActivity {
    
    private static final String README_OPENED_KEY = "com.codinglegion.readme.opened";
    
    @Override
    public void runActivity(@NotNull Project project) {
        // Only run once per project session
        if (Boolean.TRUE.equals(project.getUserData(com.intellij.openapi.util.Key.create(README_OPENED_KEY)))) {
            return;
        }
        
        // Mark as opened
        project.putUserData(com.intellij.openapi.util.Key.create(README_OPENED_KEY), Boolean.TRUE);
        
        // Open README in preview mode
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                // Look for README.md in project root
                String basePath = project.getBasePath();
                if (basePath == null) {
                    return;
                }
                
                File readmeFile = new File(basePath, "README.md");
                if (!readmeFile.exists()) {
                    return;
                }
                
                VirtualFile vFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(readmeFile);
                if (vFile != null && vFile.isValid()) {
                    // Open in preview mode
                    FileEditorManager.getInstance(project).openFile(vFile, true);
                }
            } catch (Exception e) {
                // Silently fail - don't interrupt project opening
            }
        });
    }
}

