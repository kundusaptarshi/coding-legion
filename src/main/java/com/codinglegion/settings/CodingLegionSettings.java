package com.codinglegion.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Persistent settings for Coding Legion plugin
 */
@State(
    name = "CodingLegionSettings",
    storages = @Storage("CodingLegionSettings.xml")
)
public class CodingLegionSettings implements PersistentStateComponent<CodingLegionSettings> {
    
    // Plugin version tracking (for detecting first install)
    public String installedVersion = "";
    
    // Analysis scope
    public boolean analyzeChangedLinesOnly = false;
    
    // Utility bean patterns for log null dereference whitelist (empty by default)
    public List<String> utilityBeanPatterns = new ArrayList<>();
    
    // Protected branch names (empty by default)
    public List<String> protectedBranches = new ArrayList<>();
    
    // Enable/disable specific rules (all disabled by default)
    public boolean enableStringEqualsCheck = false;
    public boolean enableStringEmptyCheck = false;
    public boolean enableCollectionCheck = false;
    public boolean enableTernaryNullDefaultCheck = false;
    public boolean enableBooleanUnboxingCheck = false;
    public boolean enableNullInContextCheck = false;
    public boolean enableLogNullDereferenceCheck = false;
    public boolean enableDtoInitializationCheck = false;
    public boolean enableNullInMapCheck = false;
    
    public static CodingLegionSettings getInstance() {
        return ServiceManager.getService(CodingLegionSettings.class);
    }
    
    @Nullable
    @Override
    public CodingLegionSettings getState() {
        return this;
    }
    
    @Override
    public void loadState(@NotNull CodingLegionSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
    
    /**
     * Reset to empty state (all settings cleared)
     */
    public void resetToDefaults() {
        // Don't clear installedVersion - keep it to track reinstalls
        analyzeChangedLinesOnly = false;
        
        utilityBeanPatterns.clear();
        protectedBranches.clear();
        
        enableStringEqualsCheck = false;
        enableStringEmptyCheck = false;
        enableCollectionCheck = false;
        enableTernaryNullDefaultCheck = false;
        enableBooleanUnboxingCheck = false;
        enableNullInContextCheck = false;
        enableLogNullDereferenceCheck = false;
        enableDtoInitializationCheck = false;
        enableNullInMapCheck = false;
    }
    
    /**
     * Clear all settings including version (for fresh install simulation)
     */
    public void clearAllSettings() {
        installedVersion = "";
        resetToDefaults();
    }
    
    /**
     * Check if a rule is enabled
     */
    public boolean isRuleEnabled(String ruleName) {
        switch (ruleName) {
            case "StringEqualsDetector":
                return enableStringEqualsCheck;
            case "StringEmptyCheckDetector":
                return enableStringEmptyCheck;
            case "CollectionCheckDetector":
                return enableCollectionCheck;
            case "TernaryNullCheckDetector":
                return enableTernaryNullDefaultCheck;
            case "BooleanUnboxingDetector":
                return enableBooleanUnboxingCheck;
            case "NullValueInContextDetector":
                return enableNullInContextCheck || enableNullInMapCheck;
            case "LogNullDereferenceDetector":
                return enableLogNullDereferenceCheck;
            case "DtoInitializationCheckDetector":
                return enableDtoInitializationCheck;
            default:
                return true;
        }
    }
}

