package com.codinglegion.analyzer;

import com.intellij.psi.PsiElement;
import com.codinglegion.model.Violation;

import java.util.List;

/**
 * Base interface for all violation detectors in Coding Legion
 * Implementing this interface makes it easy to add new coding standard rules
 */
public interface ViolationDetector {
    
    /**
     * Detect violations in the given PSI element
     * 
     * @param element PSI element to analyze
     * @return List of detected violations (empty if none found)
     */
    List<Violation> detect(PsiElement element);
    
    /**
     * Get the name of this detector (for logging/debugging)
     */
    String getName();
}

