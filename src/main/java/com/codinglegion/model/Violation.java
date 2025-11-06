package com.codinglegion.model;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.Objects;

/**
 * Represents a detected null check violation in the codebase
 */
public class Violation {
    private final ViolationType type;
    private final PsiFile file;
    private final int lineNumber;
    private final int columnNumber;
    private final String className;
    private final String packageName;
    private final String violatingCode;
    private final PsiElement element;
    
    public Violation(ViolationType type, PsiFile file, PsiElement element, 
                     int lineNumber, int columnNumber, String className, String packageName, String violatingCode) {
        this.type = type;
        this.file = file;
        this.element = element;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        this.className = className;
        this.packageName = packageName;
        this.violatingCode = violatingCode;
    }
    
    public ViolationType getType() {
        return type;
    }
    
    public PsiFile getFile() {
        return file;
    }
    
    public PsiElement getElement() {
        return element;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
    
    public int getColumnNumber() {
        return columnNumber;
    }
    
    public String getClassName() {
        return className;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public String getViolatingCode() {
        return violatingCode;
    }
    
    public ViolationSeverity getSeverity() {
        return type.getSeverity();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Violation violation = (Violation) o;
        return lineNumber == violation.lineNumber &&
               type == violation.type &&
               Objects.equals(file.getVirtualFile().getPath(), violation.file.getVirtualFile().getPath());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(type, file.getVirtualFile().getPath(), lineNumber);
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s:%d - %s", 
            type.getSeverity(), 
            className, 
            lineNumber, 
            type.getTitle());
    }
}

