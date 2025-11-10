package com.codinglegion.analyzer.detectors;

import com.intellij.psi.*;

/**
 * Base class for all detectors with common utility methods
 */
public abstract class BaseDetector {
    
    protected int getLineNumber(PsiElement element) {
        PsiFile file = element.getContainingFile();
        if (file == null) return -1;
        
        int offset = element.getTextOffset();
        return getLineNumberFromOffset(file, offset);
    }
    
    protected int getColumnNumber(PsiElement element) {
        PsiFile file = element.getContainingFile();
        if (file == null) return -1;
        
        int offset = element.getTextOffset();
        String text = file.getText();
        if (text == null) return -1;
        
        // Find the start of the line
        int lineStart = offset;
        while (lineStart > 0 && text.charAt(lineStart - 1) != '\n') {
            lineStart--;
        }
        
        // Column is the distance from line start
        return offset - lineStart + 1;
    }
    
    protected int getLineNumberFromOffset(PsiFile file, int offset) {
        String text = file.getText();
        if (text == null) return -1;
        
        int line = 1;
        for (int i = 0; i < Math.min(offset, text.length()); i++) {
            if (text.charAt(i) == '\n') {
                line++;
            }
        }
        return line;
    }
    
    protected String getClassName(PsiElement element) {
        PsiClass psiClass = findParentClass(element);
        if (psiClass != null && psiClass.getName() != null) {
            return psiClass.getName();
        }
        return "Unknown";
    }
    
    protected String getPackageName(PsiElement element) {
        PsiFile file = element.getContainingFile();
        if (file instanceof PsiJavaFile) {
            PsiJavaFile javaFile = (PsiJavaFile) file;
            String packageName = javaFile.getPackageName();
            return packageName != null && !packageName.isEmpty() ? packageName : "default";
        }
        return "default";
    }
    
    protected PsiClass findParentClass(PsiElement element) {
        PsiElement current = element;
        while (current != null) {
            if (current instanceof PsiClass) {
                return (PsiClass) current;
            }
            current = current.getParent();
        }
        return null;
    }
}

