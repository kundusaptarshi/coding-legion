package com.codinglegion.analyzer.detectors;

import com.intellij.psi.*;
import com.codinglegion.analyzer.ViolationDetector;
import com.codinglegion.model.Violation;
import com.codinglegion.model.ViolationType;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects DTO property access without checking isInitialized() first
 * Rule: Before accessing DTO properties, check if they're initialized using isInitialized("PropertyName")
 * Example: dto.isInitialized("Day") && dto.getDay() != null
 */
public class DtoInitializationCheckDetector extends BaseDetector implements ViolationDetector {
    
    @Override
    public List<Violation> detect(PsiElement element) {
        List<Violation> violations = new ArrayList<>();
        
        // Only check method call expressions
        if (!(element instanceof PsiMethodCallExpression)) {
            return violations;
        }
        
        PsiMethodCallExpression methodCall = (PsiMethodCallExpression) element;
        PsiReferenceExpression methodRef = methodCall.getMethodExpression();
        String methodName = methodRef.getReferenceName();
        
        if (methodName == null) {
            return violations;
        }
        
        // Check if this is a getter method (starts with "get" or "is")
        if (!isGetterMethod(methodName)) {
            return violations;
        }
        
        // Get the qualifier (the DTO object)
        PsiExpression qualifier = methodRef.getQualifierExpression();
        if (qualifier == null) {
            return violations;
        }
        
        // Check if the qualifier's class has isInitialized() method
        if (!hasIsInitializedMethod(qualifier)) {
            return violations; // Not a DTO with initialization tracking
        }
        
        // Extract property name from getter
        String propertyName = extractPropertyName(methodName);
        if (propertyName == null) {
            return violations;
        }
        
        // Check if this getter call is protected by isInitialized check
        if (isProtectedByInitializationCheck(methodCall, qualifier.getText(), propertyName)) {
            return violations; // Already has proper check
        }
        
        // This is an unprotected DTO property access
        addViolation(methodCall, qualifier.getText(), propertyName, violations);
        
        return violations;
    }
    
    /**
     * Check if method name is a getter (getXxx or isXxx)
     */
    private boolean isGetterMethod(String methodName) {
        return (methodName.startsWith("get") && methodName.length() > 3) ||
               (methodName.startsWith("is") && methodName.length() > 2);
    }
    
    /**
     * Extract property name from getter method
     * getDay -> Day
     * isActive -> Active
     */
    private String extractPropertyName(String methodName) {
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return methodName.substring(3);
        } else if (methodName.startsWith("is") && methodName.length() > 2) {
            return methodName.substring(2);
        }
        return null;
    }
    
    /**
     * Check if the qualifier's class has isInitialized() method
     */
    private boolean hasIsInitializedMethod(PsiExpression qualifier) {
        PsiType type = qualifier.getType();
        if (type == null) {
            return false;
        }
        
        // Get the PsiClass from the type
        if (type instanceof PsiClassType) {
            PsiClass psiClass = ((PsiClassType) type).resolve();
            if (psiClass == null) {
                return false;
            }
            
            // Look for isInitialized method
            PsiMethod[] methods = psiClass.findMethodsByName("isInitialized", true);
            
            // Check if any of these methods takes a String parameter
            for (PsiMethod method : methods) {
                PsiParameter[] params = method.getParameterList().getParameters();
                if (params.length == 1) {
                    PsiType paramType = params[0].getType();
                    if (paramType.equalsToText("java.lang.String") || paramType.equalsToText("String")) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Check if the getter call is protected by isInitialized() check
     */
    private boolean isProtectedByInitializationCheck(PsiMethodCallExpression methodCall, 
                                                      String dtoVarName, String propertyName) {
        // Look for parent if statement or conditional expression
        PsiElement parent = methodCall.getParent();
        
        while (parent != null && !(parent instanceof PsiMethod)) {
            // Check if we're inside an if statement
            if (parent instanceof PsiIfStatement) {
                PsiIfStatement ifStmt = (PsiIfStatement) parent;
                PsiExpression condition = ifStmt.getCondition();
                
                if (condition != null && containsInitializationCheck(condition, dtoVarName, propertyName)) {
                    return true;
                }
            }
            
            // Check if we're inside a ternary expression
            if (parent instanceof PsiConditionalExpression) {
                PsiConditionalExpression ternary = (PsiConditionalExpression) parent;
                PsiExpression condition = ternary.getCondition();
                
                if (condition != null && containsInitializationCheck(condition, dtoVarName, propertyName)) {
                    return true;
                }
            }
            
            // Check if we're inside a while loop
            if (parent instanceof PsiWhileStatement) {
                PsiWhileStatement whileStmt = (PsiWhileStatement) parent;
                PsiExpression condition = whileStmt.getCondition();
                
                if (condition != null && containsInitializationCheck(condition, dtoVarName, propertyName)) {
                    return true;
                }
            }
            
            parent = parent.getParent();
        }
        
        return false;
    }
    
    /**
     * Check if condition contains isInitialized("PropertyName") call
     */
    private boolean containsInitializationCheck(PsiExpression condition, String dtoVarName, String propertyName) {
        // Direct method call check
        if (condition instanceof PsiMethodCallExpression) {
            if (isInitializedCallFor(condition, dtoVarName, propertyName)) {
                return true;
            }
        }
        
        // Check in binary expressions (AND/OR conditions)
        if (condition instanceof PsiBinaryExpression) {
            PsiBinaryExpression binary = (PsiBinaryExpression) condition;
            
            // Check left side
            if (containsInitializationCheck(binary.getLOperand(), dtoVarName, propertyName)) {
                return true;
            }
            
            // Check right side
            PsiExpression right = binary.getROperand();
            if (right != null && containsInitializationCheck(right, dtoVarName, propertyName)) {
                return true;
            }
        }
        
        // Check in parenthesized expressions
        if (condition instanceof PsiParenthesizedExpression) {
            PsiParenthesizedExpression paren = (PsiParenthesizedExpression) condition;
            PsiExpression inner = paren.getExpression();
            if (inner != null) {
                return containsInitializationCheck(inner, dtoVarName, propertyName);
            }
        }
        
        // Check in prefix/postfix expressions (like !expression)
        if (condition instanceof PsiPrefixExpression) {
            PsiPrefixExpression prefix = (PsiPrefixExpression) condition;
            PsiExpression operand = prefix.getOperand();
            if (operand != null) {
                return containsInitializationCheck(operand, dtoVarName, propertyName);
            }
        }
        
        return false;
    }
    
    /**
     * Check if expression is dto.isInitialized("PropertyName")
     */
    private boolean isInitializedCallFor(PsiExpression expr, String dtoVarName, String propertyName) {
        if (!(expr instanceof PsiMethodCallExpression)) {
            return false;
        }
        
        PsiMethodCallExpression methodCall = (PsiMethodCallExpression) expr;
        PsiReferenceExpression methodRef = methodCall.getMethodExpression();
        
        // Check method name
        if (!"isInitialized".equals(methodRef.getReferenceName())) {
            return false;
        }
        
        // Check if called on the same DTO
        PsiExpression qualifier = methodRef.getQualifierExpression();
        if (qualifier == null || !qualifier.getText().equals(dtoVarName)) {
            return false;
        }
        
        // Check if the argument is the property name
        PsiExpression[] args = methodCall.getArgumentList().getExpressions();
        if (args.length != 1) {
            return false;
        }
        
        PsiExpression arg = args[0];
        if (arg instanceof PsiLiteralExpression) {
            Object value = ((PsiLiteralExpression) arg).getValue();
            if (value instanceof String) {
                String argValue = (String) value;
                // Match property name (case-sensitive)
                return argValue.equals(propertyName);
            }
        }
        
        return false;
    }
    
    /**
     * Add violation
     */
    private void addViolation(PsiMethodCallExpression methodCall, String dtoVarName, 
                             String propertyName, List<Violation> violations) {
        int lineNumber = getLineNumber(methodCall);
        int columnNumber = getColumnNumber(methodCall);
        String className = getClassName(methodCall);
        String packageName = getPackageName(methodCall);
        String code = methodCall.getText();
        
        Violation violation = new Violation(
            ViolationType.DTO_INITIALIZATION_CHECK,
            methodCall.getContainingFile(),
            methodCall,
            lineNumber,
            columnNumber,
            className,
            packageName,
            code
        );
        
        violations.add(violation);
    }
    
    @Override
    public String getName() {
        return "DtoInitializationCheckDetector";
    }
}

