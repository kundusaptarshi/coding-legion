package com.codinglegion.analyzer.detectors;

import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.codinglegion.analyzer.ViolationDetector;
import com.codinglegion.model.Violation;
import com.codinglegion.model.ViolationType;

import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive collection check detector (combines old rules 3 & 4)
 * Detects:
 * 1. Manual null checks: collection == null || collection.isEmpty()
 * 2. Size comparisons: collection.size() == 0 or > 0
 * 3. Unsafe isEmpty/size calls without null checks (NPE risk)
 */
public class CollectionCheckDetector extends BaseDetector implements ViolationDetector {
    
    @Override
    public List<Violation> detect(PsiElement element) {
        List<Violation> violations = new ArrayList<>();
        
        // Pattern 1: collection == null || collection.isEmpty()
        if (element instanceof PsiBinaryExpression) {
            PsiBinaryExpression binary = (PsiBinaryExpression) element;
            
            // Check for OR pattern: collection == null || collection.isEmpty()
            if (binary.getOperationTokenType() == JavaTokenType.OROR) {
                checkNullOrEmptyPattern(binary, violations);
            }
            
            // Check for size comparisons: collection.size() == 0, > 0, etc.
            checkSizeComparisonPattern(binary, violations);
        }
        
        // Pattern 2: Unsafe isEmpty() or size() call without null check
        if (element instanceof PsiMethodCallExpression) {
            checkUnsafeCollectionMethod((PsiMethodCallExpression) element, violations);
        }
        
        return violations;
    }
    
    private void checkNullOrEmptyPattern(PsiBinaryExpression binary, List<Violation> violations) {
        PsiExpression left = binary.getLOperand();
        PsiExpression right = binary.getROperand();
        
        if (right == null) {
            return;
        }
        
        // Check if left is null check
        boolean leftIsNullCheck = isNullCheck(left);
        
        // Check if right is isEmpty() or size() == 0 check
        boolean rightIsEmptyCheck = isIsEmptyCall(right) || isSizeEqualsZero(right);
        
        if (leftIsNullCheck && rightIsEmptyCheck) {
            // Check if both sides refer to the same collection variable
            String leftVar = getVariableFromNullCheck(left);
            String rightVar = getVariableFromCollectionCheck(right);
            
            if (leftVar != null && leftVar.equals(rightVar) && isCollectionExpression(right)) {
                addViolation(binary, ViolationType.COLLECTION_NULL_CHECK, violations);
            }
        }
    }
    
    private void checkSizeComparisonPattern(PsiBinaryExpression binary, List<Violation> violations) {
        IElementType op = binary.getOperationTokenType();
        
        // Check for comparison operators: ==, !=, >, <, >=, <=
        if (op != JavaTokenType.EQEQ && op != JavaTokenType.NE && 
            op != JavaTokenType.GT && op != JavaTokenType.LT &&
            op != JavaTokenType.GE && op != JavaTokenType.LE) {
            return;
        }
        
        PsiExpression left = binary.getLOperand();
        PsiExpression right = binary.getROperand();
        
        if (right == null) {
            return;
        }
        
        // Check if either side is size() method call and other is numeric literal (0, 1, etc.)
        boolean leftIsSize = isSizeCall(left);
        boolean rightIsSize = isSizeCall(right);
        boolean leftIsNumeric = isNumericLiteral(left);
        boolean rightIsNumeric = isNumericLiteral(right);
        
        if ((leftIsSize && rightIsNumeric) || (rightIsSize && leftIsNumeric)) {
            PsiExpression sizeExpr = leftIsSize ? left : right;
            
            // Check if it's a Collection type
            if (isCollectionSizeCall(sizeExpr)) {
                addViolation(binary, ViolationType.COLLECTION_SIZE_CHECK, violations);
            }
        }
    }
    
    private void checkUnsafeCollectionMethod(PsiMethodCallExpression methodCall, List<Violation> violations) {
        String methodName = methodCall.getMethodExpression().getReferenceName();
        
        // Only check isEmpty() and size() calls
        if (!"isEmpty".equals(methodName) && !"size".equals(methodName)) {
            return;
        }
        
        PsiExpression qualifier = methodCall.getMethodExpression().getQualifierExpression();
        
        // Skip if no qualifier (shouldn't happen) or if it's a known safe pattern
        if (qualifier == null || !isCollectionType(qualifier)) {
            return;
        }
        
        // Check if this call is already protected by a null check
        if (isProtectedByNullCheck(methodCall, qualifier)) {
            return;
        }
        
        // Check if the qualifier is a literal or guaranteed non-null
        if (isGuaranteedNonNull(qualifier)) {
            return;
        }
        
        // This is an unsafe collection method call - could NPE
        // Use COLLECTION_NULL_CHECK type for consistency
        addViolation(methodCall, ViolationType.COLLECTION_NULL_CHECK, violations);
    }
    
    private boolean isNullCheck(PsiExpression expr) {
        if (!(expr instanceof PsiBinaryExpression)) {
            return false;
        }
        
        PsiBinaryExpression binary = (PsiBinaryExpression) expr;
        IElementType op = binary.getOperationTokenType();
        
        if (op != JavaTokenType.EQEQ && op != JavaTokenType.NE) {
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
        
        if (left instanceof PsiReferenceExpression && !(left instanceof PsiMethodCallExpression)) {
            return left.getText();
        } else if (right instanceof PsiReferenceExpression && !(right instanceof PsiMethodCallExpression)) {
            return right.getText();
        }
        
        return null;
    }
    
    private boolean isIsEmptyCall(PsiExpression expr) {
        if (!(expr instanceof PsiMethodCallExpression)) {
            return false;
        }
        
        PsiMethodCallExpression methodCall = (PsiMethodCallExpression) expr;
        String methodName = methodCall.getMethodExpression().getReferenceName();
        
        return "isEmpty".equals(methodName);
    }
    
    private boolean isSizeEqualsZero(PsiExpression expr) {
        if (!(expr instanceof PsiBinaryExpression)) {
            return false;
        }
        
        PsiBinaryExpression binary = (PsiBinaryExpression) expr;
        PsiExpression left = binary.getLOperand();
        PsiExpression right = binary.getROperand();
        
        if (right == null) {
            return false;
        }
        
        boolean leftIsSize = isSizeCall(left);
        boolean rightIsSize = isSizeCall(right);
        boolean leftIsZero = isZeroLiteral(left);
        boolean rightIsZero = isZeroLiteral(right);
        
        return (leftIsSize && rightIsZero) || (rightIsSize && leftIsZero);
    }
    
    private String getVariableFromCollectionCheck(PsiExpression expr) {
        if (expr instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression methodCall = (PsiMethodCallExpression) expr;
            PsiExpression qualifier = methodCall.getMethodExpression().getQualifierExpression();
            
            if (qualifier instanceof PsiReferenceExpression) {
                return qualifier.getText();
            }
        } else if (expr instanceof PsiBinaryExpression) {
            // Handle size() == 0 pattern
            PsiBinaryExpression binary = (PsiBinaryExpression) expr;
            PsiExpression left = binary.getLOperand();
            PsiExpression right = binary.getROperand();
            
            PsiExpression sizeExpr = isSizeCall(left) ? left : (isSizeCall(right) ? right : null);
            if (sizeExpr instanceof PsiMethodCallExpression) {
                PsiMethodCallExpression methodCall = (PsiMethodCallExpression) sizeExpr;
                PsiExpression qualifier = methodCall.getMethodExpression().getQualifierExpression();
                if (qualifier instanceof PsiReferenceExpression) {
                    return qualifier.getText();
                }
            }
        }
        
        return null;
    }
    
    private boolean isSizeCall(PsiExpression expr) {
        if (!(expr instanceof PsiMethodCallExpression)) {
            return false;
        }
        
        PsiMethodCallExpression methodCall = (PsiMethodCallExpression) expr;
        String methodName = methodCall.getMethodExpression().getReferenceName();
        
        return "size".equals(methodName) || "length".equals(methodName);
    }
    
    private boolean isZeroLiteral(PsiExpression expr) {
        if (!(expr instanceof PsiLiteralExpression)) {
            return false;
        }
        
        PsiLiteralExpression literal = (PsiLiteralExpression) expr;
        Object value = literal.getValue();
        
        return value instanceof Integer && (Integer) value == 0;
    }
    
    private boolean isNumericLiteral(PsiExpression expr) {
        if (!(expr instanceof PsiLiteralExpression)) {
            return false;
        }
        
        PsiLiteralExpression literal = (PsiLiteralExpression) expr;
        Object value = literal.getValue();
        
        return value instanceof Number;
    }
    
    private boolean isCollectionExpression(PsiExpression expr) {
        if (expr instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression methodCall = (PsiMethodCallExpression) expr;
            PsiExpression qualifier = methodCall.getMethodExpression().getQualifierExpression();
            return qualifier != null && isCollectionType(qualifier);
        } else if (expr instanceof PsiBinaryExpression) {
            PsiBinaryExpression binary = (PsiBinaryExpression) expr;
            PsiExpression left = binary.getLOperand();
            PsiExpression right = binary.getROperand();
            
            if (isSizeCall(left)) {
                return isCollectionSizeCall(left);
            } else if (right != null && isSizeCall(right)) {
                return isCollectionSizeCall(right);
            }
        }
        return false;
    }
    
    private boolean isCollectionSizeCall(PsiExpression expr) {
        if (!(expr instanceof PsiMethodCallExpression)) {
            return false;
        }
        
        PsiMethodCallExpression methodCall = (PsiMethodCallExpression) expr;
        PsiExpression qualifier = methodCall.getMethodExpression().getQualifierExpression();
        
        return qualifier != null && isCollectionType(qualifier);
    }
    
    private boolean isCollectionType(PsiExpression expr) {
        PsiType type = expr.getType();
        if (type == null) {
            return false;
        }
        
        String typeName = type.getCanonicalText();
        
        // Check if it's a Collection, List, Set, Map, etc.
        return typeName.contains("Collection") || typeName.contains("List") || 
               typeName.contains("Set") || typeName.contains("Map") ||
               typeName.contains("Queue") || typeName.contains("Deque");
    }
    
    private boolean isProtectedByNullCheck(PsiMethodCallExpression methodCall, PsiExpression qualifier) {
        // Look for parent if statement with null check
        PsiElement parent = methodCall.getParent();
        
        while (parent != null && !(parent instanceof PsiMethod)) {
            if (parent instanceof PsiIfStatement) {
                PsiIfStatement ifStmt = (PsiIfStatement) parent;
                PsiExpression condition = ifStmt.getCondition();
                
                if (condition != null && containsNullCheckFor(condition, qualifier.getText())) {
                    return true;
                }
            }
            
            // Check for ternary with null check
            if (parent instanceof PsiConditionalExpression) {
                PsiConditionalExpression ternary = (PsiConditionalExpression) parent;
                PsiExpression condition = ternary.getCondition();
                
                if (condition != null && containsNullCheckFor(condition, qualifier.getText())) {
                    return true;
                }
            }
            
            parent = parent.getParent();
        }
        
        return false;
    }
    
    private boolean containsNullCheckFor(PsiExpression condition, String varName) {
        if (condition instanceof PsiBinaryExpression) {
            PsiBinaryExpression binary = (PsiBinaryExpression) condition;
            IElementType op = binary.getOperationTokenType();
            
            if (op == JavaTokenType.NE || op == JavaTokenType.EQEQ) {
                PsiExpression left = binary.getLOperand();
                PsiExpression right = binary.getROperand();
                
                if (right == null) {
                    return false;
                }
                
                boolean hasNull = "null".equals(left.getText()) || "null".equals(right.getText());
                boolean hasVar = left.getText().equals(varName) || right.getText().equals(varName);
                
                return hasNull && hasVar;
            }
            
            // Recursively check AND conditions
            if (op == JavaTokenType.ANDAND) {
                return containsNullCheckFor(binary.getLOperand(), varName) ||
                       (binary.getROperand() != null && containsNullCheckFor(binary.getROperand(), varName));
            }
        }
        
        return false;
    }
    
    private boolean isGuaranteedNonNull(PsiExpression expr) {
        // Newly created objects are non-null
        if (expr instanceof PsiNewExpression) {
            return true;
        }
        
        // Method calls like Collections.emptyList() are typically non-null
        if (expr instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression methodCall = (PsiMethodCallExpression) expr;
            String methodName = methodCall.getMethodExpression().getReferenceName();
            
            // Common factory methods that return non-null
            if ("emptyList".equals(methodName) || "emptySet".equals(methodName) || 
                "emptyMap".equals(methodName) || "singleton".equals(methodName) ||
                "singletonList".equals(methodName) || "asList".equals(methodName)) {
                return true;
            }
        }
        
        return false;
    }
    
    private void addViolation(PsiElement element, ViolationType type, List<Violation> violations) {
        int lineNumber = getLineNumber(element);
        int columnNumber = getColumnNumber(element);
        String className = getClassName(element);
        String packageName = getPackageName(element);
        String code = element.getText();
        
        Violation violation = new Violation(
            type,
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
        return "CollectionCheckDetector";
    }
}

