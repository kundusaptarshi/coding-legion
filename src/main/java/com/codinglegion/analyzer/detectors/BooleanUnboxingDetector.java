package com.codinglegion.analyzer.detectors;

import com.intellij.psi.*;
import com.codinglegion.analyzer.ViolationDetector;
import com.codinglegion.model.Violation;
import com.codinglegion.model.ViolationType;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects Boolean wrapper auto-unboxing to primitive boolean
 * Rule: Avoid boolean boolVal = boolObj (may cause NPE). Use BooleanUtils.isTrue() or Boolean.TRUE.equals()
 */
public class BooleanUnboxingDetector extends BaseDetector implements ViolationDetector {
    
    @Override
    public List<Violation> detect(PsiElement element) {
        List<Violation> violations = new ArrayList<>();
        
        // Check for variable declarations with assignment
        if (element instanceof PsiLocalVariable) {
            PsiLocalVariable variable = (PsiLocalVariable) element;
            checkVariableAssignment(variable, violations);
        }
        
        // Check for assignment expressions
        if (element instanceof PsiAssignmentExpression) {
            PsiAssignmentExpression assignment = (PsiAssignmentExpression) element;
            checkAssignment(assignment, violations);
        }
        
        // Check for Boolean wrapper used directly in conditionals
        if (element instanceof PsiIfStatement) {
            PsiIfStatement ifStmt = (PsiIfStatement) element;
            PsiExpression condition = ifStmt.getCondition();
            if (condition != null && isBooleanWrapperType(condition)) {
                addViolation(element, condition.getText(), violations);
            }
        }
        
        // Check for return statements
        if (element instanceof PsiReturnStatement) {
            PsiReturnStatement returnStmt = (PsiReturnStatement) element;
            checkReturnStatement(returnStmt, violations);
        }
        
        // Check for method call arguments
        if (element instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression methodCall = (PsiMethodCallExpression) element;
            checkMethodArguments(methodCall, violations);
        }
        
        return violations;
    }
    
    private void checkVariableAssignment(PsiLocalVariable variable, List<Violation> violations) {
        PsiType varType = variable.getType();
        
        // Check if variable is primitive boolean
        if (!varType.equals(PsiType.BOOLEAN)) {
            return;
        }
        
        PsiExpression initializer = variable.getInitializer();
        if (initializer == null) {
            return;
        }
        
        // Check if initializer is Boolean wrapper type
        if (isBooleanWrapperType(initializer)) {
            addViolation(variable, initializer.getText(), violations);
        }
    }
    
    private void checkAssignment(PsiAssignmentExpression assignment, List<Violation> violations) {
        PsiExpression lhs = assignment.getLExpression();
        PsiExpression rhs = assignment.getRExpression();
        
        if (rhs == null) {
            return;
        }
        
        PsiType lhsType = lhs.getType();
        if (lhsType == null || !lhsType.equals(PsiType.BOOLEAN)) {
            return;
        }
        
        // Check if RHS is Boolean wrapper type
        if (isBooleanWrapperType(rhs)) {
            addViolation(assignment, rhs.getText(), violations);
        }
    }
    
    private boolean isBooleanWrapperType(PsiExpression expr) {
        PsiType type = expr.getType();
        if (type == null) {
            return false;
        }
        
        String typeName = type.getCanonicalText();
        return "java.lang.Boolean".equals(typeName) || "Boolean".equals(typeName);
    }
    
    private void checkReturnStatement(PsiReturnStatement returnStmt, List<Violation> violations) {
        PsiExpression returnValue = returnStmt.getReturnValue();
        if (returnValue == null) {
            return;
        }
        
        // Check if return value is Boolean wrapper but method returns primitive boolean
        if (!isBooleanWrapperType(returnValue)) {
            return;
        }
        
        // Find the containing method
        PsiMethod method = findContainingMethod(returnStmt);
        if (method == null) {
            return;
        }
        
        PsiType returnType = method.getReturnType();
        if (returnType != null && returnType.equals(PsiType.BOOLEAN)) {
            addViolation(returnStmt, returnValue.getText(), violations);
        }
    }
    
    private void checkMethodArguments(PsiMethodCallExpression methodCall, List<Violation> violations) {
        PsiExpression[] args = methodCall.getArgumentList().getExpressions();
        
        // Try to resolve the method to check parameter types
        PsiMethod method = methodCall.resolveMethod();
        if (method == null) {
            return;
        }
        
        PsiParameter[] params = method.getParameterList().getParameters();
        
        for (int i = 0; i < args.length && i < params.length; i++) {
            PsiExpression arg = args[i];
            PsiParameter param = params[i];
            
            // Check if argument is Boolean wrapper but parameter is primitive boolean
            if (isBooleanWrapperType(arg) && param.getType().equals(PsiType.BOOLEAN)) {
                addViolation(arg, arg.getText(), violations);
            }
        }
    }
    
    private PsiMethod findContainingMethod(PsiElement element) {
        PsiElement current = element.getParent();
        while (current != null) {
            if (current instanceof PsiMethod) {
                return (PsiMethod) current;
            }
            current = current.getParent();
        }
        return null;
    }
    
    private void addViolation(PsiElement element, String code, List<Violation> violations) {
        int lineNumber = getLineNumber(element);
        int columnNumber = getColumnNumber(element);
        String className = getClassName(element);
        String packageName = getPackageName(element);
        
        Violation violation = new Violation(
            ViolationType.BOOLEAN_UNBOXING,
            element.getContainingFile(),
            element,
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
        return "BooleanUnboxingDetector";
    }

}
