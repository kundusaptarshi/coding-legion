package com.codinglegion.startup;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.codinglegion.settings.CodingLegionSettings;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Properties;

/**
 * Detects first run after installation and resets settings on version change
 */
public class FirstRunDetector implements StartupActivity {
    
    private static String getCurrentVersion() {
        try {
            InputStream is = FirstRunDetector.class.getResourceAsStream("/version.properties");
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                is.close();
                return props.getProperty("version", "Unknown");
            }
        } catch (Exception e) {
            // Fallback
        }
        return "Unknown";
    }
    
    @Override
    public void runActivity(@NotNull Project project) {
        CodingLegionSettings settings = CodingLegionSettings.getInstance();
        String currentVersion = getCurrentVersion();
        
        // Check if this is first run (no version stored) or version changed
        if (settings.installedVersion == null || settings.installedVersion.isEmpty()) {
            // First time installation - settings should already be empty by default
            settings.installedVersion = currentVersion;
        } else if (!currentVersion.equals(settings.installedVersion)) {
            // Version upgrade - auto-reset settings
            settings.resetToDefaults();  // Clear all settings on upgrade
            settings.installedVersion = currentVersion;
        }
    }
}

