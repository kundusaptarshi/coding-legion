package com.codinglegion.analyzer.detectors;

import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.codinglegion.analyzer.ViolationDetector;
import com.codinglegion.model.Violation;
import com.codinglegion.model.ViolationType;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects null default patterns (ternary and if-else) that should use ObjectUtils.defaultIfNull()
 * Rule: Use ObjectUtils.defaultIfNull(obj, default) instead of:
 *   - obj != null ? obj : defaultValue (ternary)
 *   - if (obj == null) { x = default; } else { x = obj; } (if-else assignment)
 *   - if (obj == null) { return default; } else { return obj; } (if-else return)
 */
public class TernaryNullCheckDetector extends BaseDetector implements ViolationDetector {
    
    @Override
    public List<Violation> detect(PsiElement element) {
        List<Violation> violations = new ArrayList<>();
        
        // Check for ternary pattern
        if (element instanceof PsiConditionalExpression) {
            checkTernaryPattern((PsiConditionalExpression) element, violations);
        }
        
        // Check for if-else pattern
        if (element instanceof PsiIfStatement) {
            checkIfElsePattern((PsiIfStatement) element, violations);
        }
        
        return violations;
    }
    
    /**
     * Check ternary expression: obj != null ? obj : default
     */
    private void checkTernaryPattern(PsiConditionalExpression ternary, List<Violation> violations) {
        PsiExpression condition = ternary.getCondition();
        PsiExpression thenExpr = ternary.getThenExpression();
        PsiExpression elseExpr = ternary.getElseExpression();
        
        if (thenExpr == null || elseExpr == null) {
            return;
        }
        
        // Check if condition is a null check
        NullCheckInfo nullCheck = extractNullCheck(condition);
        if (nullCheck == null) {
            return;
        }
        
        // Check if then/else matches the pattern
        boolean isNullDefaultPattern = false;
        
        if (nullCheck.isNotNullCheck) {
            // obj != null ? obj : default -> thenExpr should be obj
            isNullDefaultPattern = thenExpr.getText().equals(nullCheck.varName);
        } else {
            // obj == null ? default : obj -> elseExpr should be obj
            isNullDefaultPattern = elseExpr.getText().equals(nullCheck.varName);
        }
        
        if (isNullDefaultPattern) {
            addViolation(ternary, violations);
        }
    }
    
    /**
     * Check if-else statement: if (obj == null) { x = default; } else { x = obj; }
     */
    private void checkIfElsePattern(PsiIfStatement ifStatement, List<Violation> violations) {
        PsiExpression condition = ifStatement.getCondition();
        PsiStatement thenBranch = ifStatement.getThenBranch();
        PsiStatement elseBranch = ifStatement.getElseBranch();
        
        // Must have both branches
        if (condition == null || thenBranch == null || elseBranch == null) {
            return;
        }
        
        // Check if condition is a null check
        NullCheckInfo nullCheck = extractNullCheck(condition);
        if (nullCheck == null) {
            return;
        }
        
        // Unwrap block statements to get the actual statement
        PsiStatement thenStmt = unwrapBlockStatement(thenBranch);
        PsiStatement elseStmt = unwrapBlockStatement(elseBranch);
        
        if (thenStmt == null || elseStmt == null) {
            return;
        }
        
        // Check for assignment pattern
        if (thenStmt instanceof PsiExpressionStatement && elseStmt instanceof PsiExpressionStatement) {
            checkAssignmentPattern(ifStatement, nullCheck, 
                (PsiExpressionStatement) thenStmt, 
                (PsiExpressionStatement) elseStmt, 
                violations);
        }
        
        // Check for return pattern
        if (thenStmt instanceof PsiReturnStatement && elseStmt instanceof PsiReturnStatement) {
            checkReturnPattern(ifStatement, nullCheck,
                (PsiReturnStatement) thenStmt,
                (PsiReturnStatement) elseStmt,
                violations);
        }
    }
    
    /**
     * Check assignment pattern: if (obj == null) { x = default; } else { x = obj; }
     */
    private void checkAssignmentPattern(PsiIfStatement ifStatement, NullCheckInfo nullCheck,
                                       PsiExpressionStatement thenStmt, PsiExpressionStatement elseStmt,
                                       List<Violation> violations) {
        PsiExpression thenExpr = thenStmt.getExpression();
        PsiExpression elseExpr = elseStmt.getExpression();
        
        if (!(thenExpr instanceof PsiAssignmentExpression) || !(elseExpr instanceof PsiAssignmentExpression)) {
            return;
        }
        
        PsiAssignmentExpression thenAssign = (PsiAssignmentExpression) thenExpr;
        PsiAssignmentExpression elseAssign = (PsiAssignmentExpression) elseExpr;
        
        // Both must assign to the same variable
        PsiExpression thenTarget = thenAssign.getLExpression();
        PsiExpression elseTarget = elseAssign.getLExpression();
        
        if (!thenTarget.getText().equals(elseTarget.getText())) {
            return;
        }
        
        PsiExpression thenValue = thenAssign.getRExpression();
        PsiExpression elseValue = elseAssign.getRExpression();
        
        if (thenValue == null || elseValue == null) {
            return;
        }
        
        // Check the pattern based on null check type
        boolean isNullDefaultPattern = false;
        
        if (nullCheck.isNotNullCheck) {
            // if (obj != null) { x = obj; } else { x = default; }
            isNullDefaultPattern = thenValue.getText().equals(nullCheck.varName);
        } else {
            // if (obj == null) { x = default; } else { x = obj; }
            isNullDefaultPattern = elseValue.getText().equals(nullCheck.varName);
        }
        
        if (isNullDefaultPattern) {
            addViolation(ifStatement, violations);
        }
    }
    
    /**
     * Check return pattern: if (obj == null) { return default; } else { return obj; }
     */
    private void checkReturnPattern(PsiIfStatement ifStatement, NullCheckInfo nullCheck,
                                    PsiReturnStatement thenReturn, PsiReturnStatement elseReturn,
                                    List<Violation> violations) {
        PsiExpression thenValue = thenReturn.getReturnValue();
        PsiExpression elseValue = elseReturn.getReturnValue();
        
        if (thenValue == null || elseValue == null) {
            return;
        }
        
        // Check the pattern based on null check type
        boolean isNullDefaultPattern = false;
        
        if (nullCheck.isNotNullCheck) {
            // if (obj != null) { return obj; } else { return default; }
            isNullDefaultPattern = thenValue.getText().equals(nullCheck.varName);
        } else {
            // if (obj == null) { return default; } else { return obj; }
            isNullDefaultPattern = elseValue.getText().equals(nullCheck.varName);
        }
        
        if (isNullDefaultPattern) {
            addViolation(ifStatement, violations);
        }
    }
    
    /**
     * Extract null check information from condition
     */
    private NullCheckInfo extractNullCheck(PsiExpression condition) {
        if (!(condition instanceof PsiBinaryExpression)) {
            return null;
        }
        
        PsiBinaryExpression binaryCondition = (PsiBinaryExpression) condition;
        IElementType op = binaryCondition.getOperationTokenType();
        
        // Check for != or ==
        if (op != JavaTokenType.NE && op != JavaTokenType.EQEQ) {
            return null;
        }
        
        PsiExpression left = binaryCondition.getLOperand();
        PsiExpression right = binaryCondition.getROperand();
        
        if (right == null) {
            return null;
        }
        
        // Check if one side is null
        boolean leftIsNull = left instanceof PsiLiteralExpression && "null".equals(left.getText());
        boolean rightIsNull = right instanceof PsiLiteralExpression && "null".equals(right.getText());
        
        if (!leftIsNull && !rightIsNull) {
            return null;
        }
        
        // Get the variable being checked
        String varName = leftIsNull ? right.getText() : left.getText();
        boolean isNotNullCheck = (op == JavaTokenType.NE);
        
        return new NullCheckInfo(varName, isNotNullCheck);
    }
    
    /**
     * Unwrap block statement to get single statement inside
     */
    private PsiStatement unwrapBlockStatement(PsiStatement statement) {
        if (statement instanceof PsiBlockStatement) {
            PsiBlockStatement block = (PsiBlockStatement) statement;
            PsiStatement[] statements = block.getCodeBlock().getStatements();
            
            // Only process single-statement blocks
            if (statements.length == 1) {
                return statements[0];
            }
            return null;
        }
        return statement;
    }
    
    /**
     * Add violation
     */
    private void addViolation(PsiElement element, List<Violation> violations) {
        int lineNumber = getLineNumber(element);
        int columnNumber = getColumnNumber(element);
        String className = getClassName(element);
        String packageName = getPackageName(element);
        String code = element.getText();
        
        Violation violation = new Violation(
            ViolationType.TERNARY_NULL_DEFAULT,
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
    
    /**
     * Helper class to store null check information
     */
    private static class NullCheckInfo {
        final String varName;
        final boolean isNotNullCheck;
        
        NullCheckInfo(String varName, boolean isNotNullCheck) {
            this.varName = varName;
            this.isNotNullCheck = isNotNullCheck;
        }
    }
    
    @Override
    public String getName() {
        return "TernaryNullCheckDetector";
    }

}
