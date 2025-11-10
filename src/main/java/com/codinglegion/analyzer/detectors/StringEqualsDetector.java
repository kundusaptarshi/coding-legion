package com.codinglegion.analyzer.detectors;

import com.intellij.psi.*;
import com.codinglegion.analyzer.ViolationDetector;
import com.codinglegion.model.Violation;
import com.codinglegion.model.ViolationType;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects string.equals() calls that should use StringUtils.equals()
 * Rule: Use StringUtils.equals(a, b) instead of a.equals(b)
 */
public class StringEqualsDetector extends BaseDetector implements ViolationDetector {
    
    @Override
    public List<Violation> detect(PsiElement element) {
        List<Violation> violations = new ArrayList<>();
        
        if (!(element instanceof PsiMethodCallExpression)) {
            return violations;
        }
        
        PsiMethodCallExpression methodCall = (PsiMethodCallExpression) element;
        PsiReferenceExpression methodRef = methodCall.getMethodExpression();
        
        // Check if method name is "equals"
        if (!"equals".equals(methodRef.getReferenceName())) {
            return violations;
        }
        
        // Get the qualifier (the object on which equals is called)
        PsiExpression qualifier = methodRef.getQualifierExpression();
        if (qualifier == null) {
            return violations;
        }
        
        // Skip if qualifier is a string literal (null-safe pattern: "CONSTANT".equals(variable))
        if (qualifier instanceof PsiLiteralExpression) {
            PsiLiteralExpression literal = (PsiLiteralExpression) qualifier;
            if (literal.getValue() instanceof String) {
                return violations; // This is already null-safe
            }
        }
        
        // Check if qualifier is a String type
        PsiType qualifierType = qualifier.getType();
        if (qualifierType == null) {
            return violations;
        }
        
        String qualifierTypeName = qualifierType.getCanonicalText();
        if (!qualifierTypeName.equals("java.lang.String") && !qualifierTypeName.equals("String")) {
            return violations;
        }
        
        // This is a variable.equals() call on String - create violation
        int lineNumber = getLineNumber(element);
        int columnNumber = getColumnNumber(element);
        String className = getClassName(element);
        String packageName = getPackageName(element);
        String code = element.getText();
        
        Violation violation = new Violation(
            ViolationType.STRING_EQUALS,
            element.getContainingFile(),
            element,
            lineNumber,
            columnNumber,
            className,
            packageName,
            code
        );
        
        violations.add(violation);
        return violations;
    }
    
    @Override
    public String getName() {
        return "StringEqualsDetector";
    }
}

