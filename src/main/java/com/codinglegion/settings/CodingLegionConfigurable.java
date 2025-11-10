package com.codinglegion.settings;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;

/**
 * Settings UI for Coding Legion plugin
 */
public class CodingLegionConfigurable implements Configurable {
    
    private JPanel mainPanel;
    private JBCheckBox analyzeChangedLinesOnlyCheckbox;
    private JBTextArea utilityPatternsTextArea;
    private JBTextArea protectedBranchesTextArea;
    
    // Rule checkboxes
    private JBCheckBox enableStringEquals;
    private JBCheckBox enableStringEmpty;
    private JBCheckBox enableCollection;
    private JBCheckBox enableTernaryNull;
    private JBCheckBox enableBooleanUnboxing;
    private JBCheckBox enableNullInContext;
    private JBCheckBox enableLogNull;
    private JBCheckBox enableDtoInit;
    private JBCheckBox enableNullInMap;
    
    private TextFieldWithBrowseButton configFileField;
    
    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Coding Legion";
    }
    
    @Nullable
    @Override
    public JComponent createComponent() {
        mainPanel = new JPanel(new BorderLayout());
        
        // Add header with plugin name and version
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(JBUI.Borders.empty(5, 0, 15, 0));
        
        JLabel headerLabel = new JLabel("Coding Legion Configuration");
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 18f));
        
        // Read version from version.properties
        String version = readVersion();
        JLabel versionLabel = new JLabel("Version " + version);
        versionLabel.setFont(versionLabel.getFont().deriveFont(Font.BOLD, 12f));
        versionLabel.setForeground(new Color(100, 100, 100));
        
        headerPanel.add(headerLabel, BorderLayout.WEST);
        headerPanel.add(versionLabel, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel content = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = JBUI.insets(5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        
        // Analysis Scope Section
        gbc.gridwidth = 2;
        content.add(createSectionLabel("Analysis Scope"), gbc);
        gbc.gridy++;
        
        analyzeChangedLinesOnlyCheckbox = new JBCheckBox("Analyze only changed/added lines (not entire files)");
        content.add(analyzeChangedLinesOnlyCheckbox, gbc);
        gbc.gridy++;
        
        content.add(createHelpLabel("When enabled, only modified lines are analyzed. Faster but may miss context-dependent violations."), gbc);
        gbc.gridy++;
        gbc.insets = JBUI.insetsTop(20);
        
        // Protected Branches Section (before utility patterns) - Read-only with lock icon
        JPanel branchesHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        branchesHeaderPanel.add(createSectionLabel("Protected Branches"));
        JLabel lockIcon = new JLabel("🔒");
        lockIcon.setFont(lockIcon.getFont().deriveFont(12f));
        lockIcon.setToolTipText("Read-only - modify via settings import");
        branchesHeaderPanel.add(lockIcon);
        content.add(branchesHeaderPanel, gbc);
        gbc.gridy++;
        gbc.insets = JBUI.insets(5);
        
        content.add(createHelpLabel("Analysis is blocked on these branches (modify via settings import):"), gbc);
        gbc.gridy++;
        
        protectedBranchesTextArea = new JBTextArea(3, 40);
        protectedBranchesTextArea.setLineWrap(true);
        protectedBranchesTextArea.setEditable(false);  // Read-only
        protectedBranchesTextArea.setFocusable(false);  // No cursor on click
        protectedBranchesTextArea.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));  // Default cursor (not text cursor)
        JScrollPane branchScroll = new JScrollPane(protectedBranchesTextArea);
        branchScroll.setPreferredSize(new Dimension(500, 60));
        content.add(branchScroll, gbc);
        gbc.gridy++;
        gbc.insets = JBUI.insetsTop(20);
        
        // Utility Bean Patterns Section
        content.add(createSectionLabel("Log Null Dereference - Utility Bean Whitelist"), gbc);
        gbc.gridy++;
        gbc.insets = JBUI.insets(5);
        
        content.add(createHelpLabel("Autowired beans ending with these patterns won't trigger log warnings (one per line):"), gbc);
        gbc.gridy++;
        
        utilityPatternsTextArea = new JBTextArea(4, 40);
        utilityPatternsTextArea.setLineWrap(true);
        JScrollPane utilityScroll = new JScrollPane(utilityPatternsTextArea);
        utilityScroll.setPreferredSize(new Dimension(500, 80));
        content.add(utilityScroll, gbc);
        gbc.gridy++;
        gbc.insets = JBUI.insetsTop(20);
        
        // Enable/Disable Rules Section
        content.add(createSectionLabel("Enable/Disable Rules"), gbc);
        gbc.gridy++;
        gbc.insets = JBUI.insets(5);
        
        content.add(createHelpLabel("Uncheck rules you don't want to enforce:"), gbc);
        gbc.gridy++;
        
        JPanel rulesPanel = new JPanel(new GridLayout(5, 2, 10, 5));
        
        enableStringEquals = new JBCheckBox("Rule 1: String.equals() (ERROR)");
        enableStringEmpty = new JBCheckBox("Rule 2: String empty (ERROR)");
        enableCollection = new JBCheckBox("Rules 3-4: Collections (ERROR)");
        enableTernaryNull = new JBCheckBox("Rule 5: Null defaults (WARNING)");
        enableBooleanUnboxing = new JBCheckBox("Rule 6: Boolean unboxing (ERROR)");
        enableNullInContext = new JBCheckBox("Rule 7: Null in context (ERROR)");
        enableLogNull = new JBCheckBox("Rule 8: Log null deref (WARNING)");
        enableDtoInit = new JBCheckBox("Rule 9: DTO init (WARNING)");
        enableNullInMap = new JBCheckBox("Rule 10: Null in map (WARNING)");
        
        rulesPanel.add(enableStringEquals);
        rulesPanel.add(enableStringEmpty);
        rulesPanel.add(enableCollection);
        rulesPanel.add(enableTernaryNull);
        rulesPanel.add(enableBooleanUnboxing);
        rulesPanel.add(enableNullInContext);
        rulesPanel.add(enableLogNull);
        rulesPanel.add(enableDtoInit);
        rulesPanel.add(enableNullInMap);
        rulesPanel.add(new JLabel()); // Spacer for odd number of checkboxes
        
        content.add(rulesPanel, gbc);
        gbc.gridy++;
        gbc.insets = JBUI.insetsTop(20);
        
        // Import/Export Configuration
        content.add(createSectionLabel("Import/Export Configuration"), gbc);
        gbc.gridy++;
        gbc.insets = JBUI.insets(5);
        
        JPanel importExportPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        
        configFileField = new TextFieldWithBrowseButton();
        configFileField.setPreferredSize(new Dimension(280, 30));
        configFileField.addBrowseFolderListener(
            "Select Configuration File",
            "Choose a Coding Legion configuration file (.properties)",
            null,
            new FileChooserDescriptor(true, false, false, false, false, false)
                .withFileFilter(file -> file.getExtension() == null || 
                               file.getExtension().equals("properties") || 
                               file.isDirectory())
        );
        
        JButton importButton = new JButton("Import");
        importButton.addActionListener(e -> importSettings());
        
        JButton exportButton = new JButton("Export");
        exportButton.addActionListener(e -> exportSettings());
        
        JButton resetButton = new JButton("Reset to Defaults");
        resetButton.addActionListener(e -> resetToDefaults());
        
        importExportPanel.add(new JBLabel("Config file:"));
        importExportPanel.add(configFileField);
        importExportPanel.add(importButton);
        importExportPanel.add(exportButton);
        importExportPanel.add(resetButton);
        
        content.add(importExportPanel, gbc);
        gbc.gridy++;
        
        content.add(createHelpLabel("Share settings across team using .properties files"), gbc);
        gbc.gridy++;
        
        // Add vertical glue to push everything to the top
        gbc.weighty = 1.0;
        content.add(Box.createVerticalGlue(), gbc);
        
        // Wrap in scroll pane with proper settings
        JScrollPane scrollPane = new JScrollPane(content,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(JBUI.Borders.empty(10));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(700, 600));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    private JLabel createSectionLabel(String text) {
        JLabel label = new JBLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        return label;
    }
    
    private JLabel createHelpLabel(String text) {
        JLabel label = new JBLabel("<html><i>" + text + "</i></html>");
        label.setForeground(new Color(120, 120, 120));
        return label;
    }
    
    /**
     * Read version from bundled version.properties
     */
    private String readVersion() {
        try {
            InputStream is = getClass().getResourceAsStream("/version.properties");
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
    public boolean isModified() {
        CodingLegionSettings settings = CodingLegionSettings.getInstance();
        
        if (analyzeChangedLinesOnlyCheckbox.isSelected() != settings.analyzeChangedLinesOnly) return true;
        
        String currentUtilPatterns = String.join("\n", settings.utilityBeanPatterns);
        if (!utilityPatternsTextArea.getText().trim().equals(currentUtilPatterns)) return true;
        
        String currentBranches = String.join("\n", settings.protectedBranches);
        if (!protectedBranchesTextArea.getText().trim().equals(currentBranches)) return true;
        
        if (enableStringEquals.isSelected() != settings.enableStringEqualsCheck) return true;
        if (enableStringEmpty.isSelected() != settings.enableStringEmptyCheck) return true;
        if (enableCollection.isSelected() != settings.enableCollectionCheck) return true;
        if (enableTernaryNull.isSelected() != settings.enableTernaryNullDefaultCheck) return true;
        if (enableBooleanUnboxing.isSelected() != settings.enableBooleanUnboxingCheck) return true;
        if (enableNullInContext.isSelected() != settings.enableNullInContextCheck) return true;
        if (enableLogNull.isSelected() != settings.enableLogNullDereferenceCheck) return true;
        if (enableDtoInit.isSelected() != settings.enableDtoInitializationCheck) return true;
        if (enableNullInMap.isSelected() != settings.enableNullInMapCheck) return true;
        
        return false;
    }
    
    @Override
    public void apply() {
        CodingLegionSettings settings = CodingLegionSettings.getInstance();
        
        settings.analyzeChangedLinesOnly = analyzeChangedLinesOnlyCheckbox.isSelected();
        
        settings.utilityBeanPatterns.clear();
        String[] patterns = utilityPatternsTextArea.getText().split("\n");
        for (String pattern : patterns) {
            String trimmed = pattern.trim();
            if (!trimmed.isEmpty()) {
                settings.utilityBeanPatterns.add(trimmed);
            }
        }
        
        settings.protectedBranches.clear();
        String[] branches = protectedBranchesTextArea.getText().split("\n");
        for (String branch : branches) {
            String trimmed = branch.trim();
            if (!trimmed.isEmpty()) {
                settings.protectedBranches.add(trimmed);
            }
        }
        
        settings.enableStringEqualsCheck = enableStringEquals.isSelected();
        settings.enableStringEmptyCheck = enableStringEmpty.isSelected();
        settings.enableCollectionCheck = enableCollection.isSelected();
        settings.enableTernaryNullDefaultCheck = enableTernaryNull.isSelected();
        settings.enableBooleanUnboxingCheck = enableBooleanUnboxing.isSelected();
        settings.enableNullInContextCheck = enableNullInContext.isSelected();
        settings.enableLogNullDereferenceCheck = enableLogNull.isSelected();
        settings.enableDtoInitializationCheck = enableDtoInit.isSelected();
        settings.enableNullInMapCheck = enableNullInMap.isSelected();
    }
    
    @Override
    public void reset() {
        CodingLegionSettings settings = CodingLegionSettings.getInstance();
        
        analyzeChangedLinesOnlyCheckbox.setSelected(settings.analyzeChangedLinesOnly);
        utilityPatternsTextArea.setText(String.join("\n", settings.utilityBeanPatterns));
        protectedBranchesTextArea.setText(String.join("\n", settings.protectedBranches));
        
        enableStringEquals.setSelected(settings.enableStringEqualsCheck);
        enableStringEmpty.setSelected(settings.enableStringEmptyCheck);
        enableCollection.setSelected(settings.enableCollectionCheck);
        enableTernaryNull.setSelected(settings.enableTernaryNullDefaultCheck);
        enableBooleanUnboxing.setSelected(settings.enableBooleanUnboxingCheck);
        enableNullInContext.setSelected(settings.enableNullInContextCheck);
        enableLogNull.setSelected(settings.enableLogNullDereferenceCheck);
        enableDtoInit.setSelected(settings.enableDtoInitializationCheck);
        enableNullInMap.setSelected(settings.enableNullInMapCheck);
    }
    
    private void resetToDefaults() {
        int confirm = JOptionPane.showConfirmDialog(mainPanel,
            "This will clear all settings (checkboxes, branches, and utility patterns).\n" +
            "To restore, you'll need to configure manually or import a settings file.\n\n" +
            "Are you sure?",
            "Reset to Defaults",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            CodingLegionSettings.getInstance().clearAllSettings();
            reset();
            JOptionPane.showMessageDialog(mainPanel,
                "Settings reset to empty state.\nConfigure your preferences or import a settings file.",
                "Reset Complete",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void importSettings() {
        String filePath = configFileField.getText();
        if (filePath == null || filePath.trim().isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Please select a configuration file first.", 
                "Import Settings", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(filePath));
            
            // Load settings from properties
            if (props.containsKey("analyzeChangedLinesOnly")) {
                analyzeChangedLinesOnlyCheckbox.setSelected(Boolean.parseBoolean(props.getProperty("analyzeChangedLinesOnly")));
            }
            
            if (props.containsKey("utilityBeanPatterns")) {
                utilityPatternsTextArea.setText(props.getProperty("utilityBeanPatterns").replace(",", "\n"));
            }
            
            if (props.containsKey("protectedBranches")) {
                protectedBranchesTextArea.setText(props.getProperty("protectedBranches").replace(",", "\n"));
            }
            
            // Load rule enable/disable states
            enableStringEquals.setSelected(Boolean.parseBoolean(props.getProperty("enableStringEqualsCheck", "true")));
            enableStringEmpty.setSelected(Boolean.parseBoolean(props.getProperty("enableStringEmptyCheck", "true")));
            enableCollection.setSelected(Boolean.parseBoolean(props.getProperty("enableCollectionCheck", "true")));
            enableTernaryNull.setSelected(Boolean.parseBoolean(props.getProperty("enableTernaryNullDefaultCheck", "true")));
            enableBooleanUnboxing.setSelected(Boolean.parseBoolean(props.getProperty("enableBooleanUnboxingCheck", "true")));
            enableNullInContext.setSelected(Boolean.parseBoolean(props.getProperty("enableNullInContextCheck", "true")));
            enableLogNull.setSelected(Boolean.parseBoolean(props.getProperty("enableLogNullDereferenceCheck", "true")));
            enableDtoInit.setSelected(Boolean.parseBoolean(props.getProperty("enableDtoInitializationCheck", "true")));
            enableNullInMap.setSelected(Boolean.parseBoolean(props.getProperty("enableNullInMapCheck", "true")));
            
            JOptionPane.showMessageDialog(mainPanel, 
                "Settings imported successfully!", 
                "Import Settings", 
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Failed to import settings: " + e.getMessage(), 
                "Import Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportSettings() {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
        descriptor.setTitle("Select Export Directory");
        
        VirtualFile[] files = FileChooserFactory.getInstance().createFileChooser(descriptor, null, mainPanel).choose(null);
        
        if (files.length > 0) {
        try {
            // Include version in filename (read from version.properties)
            String version = readVersion();
            String baseFileName = "coding-legion-settings-v" + version;
            File exportDir = new File(files[0].getPath());
            
            // Find next available filename with numeric suffix if file exists
            File exportFile = new File(exportDir, baseFileName + ".properties");
            int counter = 1;
            while (exportFile.exists()) {
                exportFile = new File(exportDir, baseFileName + "_" + counter + ".properties");
                counter++;
            }
            
            Properties props = new Properties();
                
                props.setProperty("analyzeChangedLinesOnly", String.valueOf(analyzeChangedLinesOnlyCheckbox.isSelected()));
                props.setProperty("utilityBeanPatterns", utilityPatternsTextArea.getText().replace("\n", ","));
                props.setProperty("protectedBranches", protectedBranchesTextArea.getText().replace("\n", ","));
                
                props.setProperty("enableStringEqualsCheck", String.valueOf(enableStringEquals.isSelected()));
                props.setProperty("enableStringEmptyCheck", String.valueOf(enableStringEmpty.isSelected()));
                props.setProperty("enableCollectionCheck", String.valueOf(enableCollection.isSelected()));
                props.setProperty("enableTernaryNullDefaultCheck", String.valueOf(enableTernaryNull.isSelected()));
                props.setProperty("enableBooleanUnboxingCheck", String.valueOf(enableBooleanUnboxing.isSelected()));
                props.setProperty("enableNullInContextCheck", String.valueOf(enableNullInContext.isSelected()));
                props.setProperty("enableLogNullDereferenceCheck", String.valueOf(enableLogNull.isSelected()));
                props.setProperty("enableDtoInitializationCheck", String.valueOf(enableDtoInit.isSelected()));
                props.setProperty("enableNullInMapCheck", String.valueOf(enableNullInMap.isSelected()));
                
                props.store(new FileOutputStream(exportFile), "Coding Legion Settings");
                
                JOptionPane.showMessageDialog(mainPanel, 
                    "Settings exported to:\n" + exportFile.getAbsolutePath(), 
                    "Export Settings", 
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception e) {
                JOptionPane.showMessageDialog(mainPanel, 
                    "Failed to export settings: " + e.getMessage(), 
                    "Export Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    @Override
    public void disposeUIResources() {
        mainPanel = null;
    }
}

