package com.codinglegion.analyzer;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.codinglegion.analyzer.detectors.*;
import com.codinglegion.model.Violation;
import com.codinglegion.settings.CodingLegionSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * Main analyzer class for Coding Legion
 * Orchestrates all violation detectors and analyzes files
 */
public class CodingStandardsAnalyzer {
    
    private final List<ViolationDetector> detectors;
    private final Project project;
    
    public CodingStandardsAnalyzer(Project project) {
        this.project = project;
        this.detectors = initializeDetectors();
    }
    
    /**
     * Initialize all violation detectors
     * Only includes enabled detectors based on user settings
     */
    private List<ViolationDetector> initializeDetectors() {
        CodingLegionSettings settings = CodingLegionSettings.getInstance();
        List<ViolationDetector> detectors = new ArrayList<>();
        
        if (settings.enableStringEqualsCheck) {
            detectors.add(new StringEqualsDetector());
        }
        if (settings.enableStringEmptyCheck) {
            detectors.add(new StringEmptyCheckDetector());
        }
        if (settings.enableCollectionCheck) {
            detectors.add(new CollectionCheckDetector());
        }
        if (settings.enableTernaryNullDefaultCheck) {
            detectors.add(new TernaryNullCheckDetector());
        }
        if (settings.enableBooleanUnboxingCheck) {
            detectors.add(new BooleanUnboxingDetector());
        }
        if (settings.enableLogNullDereferenceCheck) {
            detectors.add(new LogNullDereferenceDetector());
        }
        if (settings.enableDtoInitializationCheck) {
            detectors.add(new DtoInitializationCheckDetector());
        }
        if (settings.enableNullInContextCheck || settings.enableNullInMapCheck) {
            detectors.add(new NullValueInContextDetector());
        }
        
        return detectors;
    }
    
    /**
     * Analyze a list of files and return all violations found
     */
    public List<Violation> analyzeFiles(List<VirtualFile> files, ProgressIndicator indicator) {
        List<Violation> allViolations = new ArrayList<>();
        
        int fileCount = files.size();
        int currentFile = 0;
        
        for (VirtualFile file : files) {
            currentFile++;
            
            if (indicator != null) {
                indicator.setFraction((double) currentFile / fileCount);
                indicator.setText("Analyzing " + file.getName() + " (" + currentFile + "/" + fileCount + ")");
                
                if (indicator.isCanceled()) {
                    break;
                }
            }
            
            // Analyze this file in a read action
            List<Violation> fileViolations = ReadAction.compute(() -> {
                PsiManager psiManager = PsiManager.getInstance(project);
                PsiFile psiFile = psiManager.findFile(file);
                
                if (psiFile == null || !(psiFile instanceof PsiJavaFile)) {
                    return new ArrayList<>();
                }
                
                return analyzeFile(psiFile, indicator);
            });
            
            allViolations.addAll(fileViolations);
        }
        
        return allViolations;
    }
    
    /**
     * Analyze a single file
     */
    private List<Violation> analyzeFile(PsiFile psiFile, ProgressIndicator indicator) {
        List<Violation> violations = new ArrayList<>();
        
        // Visit all elements in the file
        psiFile.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if (indicator != null && indicator.isCanceled()) {
                    return;
                }
                
                // Run all detectors on this element
                for (ViolationDetector detector : detectors) {
                    List<Violation> detected = detector.detect(element);
                    violations.addAll(detected);
                }
                
                // Continue visiting children
                super.visitElement(element);
            }
        });
        
        return violations;
    }
    
    /**
     * Get the list of all detectors (for reporting)
     */
    public List<ViolationDetector> getDetectors() {
        return detectors;
    }
}

