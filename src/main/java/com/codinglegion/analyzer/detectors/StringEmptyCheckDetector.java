package com.codinglegion.analyzer.detectors;

import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.codinglegion.analyzer.ViolationDetector;
import com.codinglegion.model.Violation;
import com.codinglegion.model.ViolationType;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects manual string null/empty checks that should use StringUtils.isEmpty()
 * Rule: Use StringUtils.isEmpty(str) instead of str == null || str.isEmpty() or str.equals("")
 */
public class StringEmptyCheckDetector extends BaseDetector implements ViolationDetector {
    
    @Override
    public List<Violation> detect(PsiElement element) {
        List<Violation> violations = new ArrayList<>();
        
        if (!(element instanceof PsiBinaryExpression)) {
            return violations;
        }
        
        PsiBinaryExpression binary = (PsiBinaryExpression) element;
        
        // Check for pattern: str == null || str.isEmpty()
        // or str == null || str.equals("")
        if (binary.getOperationTokenType() != JavaTokenType.OROR) {
            return violations;
        }
        
        PsiExpression left = binary.getLOperand();
        PsiExpression right = binary.getROperand();
        
        if (right == null) {
            return violations;
        }
        
        // Check if left is null check
        boolean leftIsNullCheck = isNullCheck(left);
        
        // Check if right is isEmpty(), equals(""), or length() == 0 check on same variable
        boolean rightIsEmptyCheck = isEmptyCheck(right) || isEqualsEmptyStringCheck(right) || isLengthZeroCheck(right);
        
        if (leftIsNullCheck && rightIsEmptyCheck) {
            // Check if both sides refer to the same string variable
            String leftVar = getVariableFromNullCheck(left);
            String rightVar = getVariableFromEmptyCheck(right);
            
            if (leftVar != null && leftVar.equals(rightVar)) {
                int lineNumber = getLineNumber(element);
                int columnNumber = getColumnNumber(element);
                String className = getClassName(element);
                String packageName = getPackageName(element);
                String code = element.getText();
                
                Violation violation = new Violation(
                    ViolationType.STRING_EMPTY_CHECK,
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
        }
        
        return violations;
    }
    
    private boolean isNullCheck(PsiExpression expr) {
        if (!(expr instanceof PsiBinaryExpression)) {
            return false;
        }
        
        PsiBinaryExpression binary = (PsiBinaryExpression) expr;
        IElementType op = binary.getOperationTokenType();
        
        if (op != JavaTokenType.EQEQ) {
            return false;
        }
        
        PsiExpression left = binary.getLOperand();
        PsiExpression right = binary.getROperand();
        
        return (left instanceof PsiLiteralExpression && "null".equals(left.getText())) ||
               (right != null && right instanceof PsiLiteralExpression && "null".equals(right.getText()));
    }
    
    private String getVariableFromNullCheck(PsiExpression expr) {
        if (!(expr instanceof PsiBinaryExpression)) {
            return null;
        }
        
        PsiBinaryExpression binary = (PsiBinaryExpression) expr;
        PsiExpression left = binary.getLOperand();
        PsiExpression right = binary.getROperand();
        
        if (left instanceof PsiReferenceExpression) {
            return left.getText();
        } else if (right instanceof PsiReferenceExpression) {
            return right.getText();
        }
        
        return null;
    }
    
    private boolean isEmptyCheck(PsiExpression expr) {
        if (!(expr instanceof PsiMethodCallExpression)) {
            return false;
        }
        
        PsiMethodCallExpression methodCall = (PsiMethodCallExpression) expr;
        String methodName = methodCall.getMethodExpression().getReferenceName();
        
        if (!"isEmpty".equals(methodName)) {
            return false;
        }
        
        // Check if the qualifier is a String type
        PsiExpression qualifier = methodCall.getMethodExpression().getQualifierExpression();
        if (qualifier != null) {
            PsiType type = qualifier.getType();
            if (type != null) {
                // Only match if it's java.lang.String
                return type.getCanonicalText().equals("java.lang.String");
            }
        }
        
        return false;
    }
    
    private boolean isEqualsEmptyStringCheck(PsiExpression expr) {
        if (!(expr instanceof PsiMethodCallExpression)) {
            return false;
        }
        
        PsiMethodCallExpression methodCall = (PsiMethodCallExpression) expr;
        String methodName = methodCall.getMethodExpression().getReferenceName();
        
        if (!"equals".equals(methodName)) {
            return false;
        }
        
        PsiExpression[] args = methodCall.getArgumentList().getExpressions();
        if (args.length != 1) {
            return false;
        }
        
        // Check if argument is empty string ""
        PsiExpression arg = args[0];
        if (arg instanceof PsiLiteralExpression) {
            Object value = ((PsiLiteralExpression) arg).getValue();
            return value instanceof String && ((String) value).isEmpty();
        }
        
        return false;
    }
    
    private boolean isLengthZeroCheck(PsiExpression expr) {
        // Check for pattern: str.length() == 0
        if (!(expr instanceof PsiBinaryExpression)) {
            return false;
        }
        
        PsiBinaryExpression binary = (PsiBinaryExpression) expr;
        IElementType op = binary.getOperationTokenType();
        
        if (op != JavaTokenType.EQEQ) {
            return false;
        }
        
        PsiExpression left = binary.getLOperand();
        PsiExpression right = binary.getROperand();
        
        if (right == null) {
            return false;
        }
        
        // Check if one side is length() call and other is 0
        boolean leftIsLength = isLengthCall(left);
        boolean rightIsLength = isLengthCall(right);
        boolean leftIsZero = isZeroLiteral(left);
        boolean rightIsZero = isZeroLiteral(right);
        
        return (leftIsLength && rightIsZero) || (rightIsLength && leftIsZero);
    }
    
    private boolean isLengthCall(PsiExpression expr) {
        if (!(expr instanceof PsiMethodCallExpression)) {
            return false;
        }
        
        PsiMethodCallExpression methodCall = (PsiMethodCallExpression) expr;
        String methodName = methodCall.getMethodExpression().getReferenceName();
        
        if (!"length".equals(methodName)) {
            return false;
        }
        
        // Check if it's called on a String
        PsiExpression qualifier = methodCall.getMethodExpression().getQualifierExpression();
        if (qualifier != null) {
            PsiType type = qualifier.getType();
            if (type != null) {
                return type.getCanonicalText().equals("java.lang.String");
            }
        }
        
        return false;
    }
    
    private boolean isZeroLiteral(PsiExpression expr) {
        if (!(expr instanceof PsiLiteralExpression)) {
            return false;
        }
        
        PsiLiteralExpression literal = (PsiLiteralExpression) expr;
        Object value = literal.getValue();
        
        return value instanceof Integer && (Integer) value == 0;
    }
    
    private String getVariableFromEmptyCheck(PsiExpression expr) {
        if (expr instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression methodCall = (PsiMethodCallExpression) expr;
            PsiExpression qualifier = methodCall.getMethodExpression().getQualifierExpression();
            
            if (qualifier instanceof PsiReferenceExpression) {
                return qualifier.getText();
            }
        } else if (expr instanceof PsiBinaryExpression) {
            // Handle length() == 0 pattern
            PsiBinaryExpression binary = (PsiBinaryExpression) expr;
            PsiExpression left = binary.getLOperand();
            PsiExpression right = binary.getROperand();
            
            PsiExpression lengthExpr = isLengthCall(left) ? left : (right != null && isLengthCall(right) ? right : null);
            if (lengthExpr instanceof PsiMethodCallExpression) {
                PsiMethodCallExpression methodCall = (PsiMethodCallExpression) lengthExpr;
                PsiExpression qualifier = methodCall.getMethodExpression().getQualifierExpression();
                if (qualifier instanceof PsiReferenceExpression) {
                    return qualifier.getText();
                }
            }
        }
        
        return null;
    }
    
    @Override
    public String getName() {
        return "StringEmptyCheckDetector";
    }
}

