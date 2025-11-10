package com.codinglegion.analyzer.detectors;

import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.codinglegion.analyzer.ViolationDetector;
import com.codinglegion.model.Violation;
import com.codinglegion.model.ViolationType;
import com.codinglegion.settings.CodingLegionSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Detects potential null dereferences in log statements
 * Rule: Check for null before calling methods or accessing fields in log statements (warning only - not always a violation)
 * Detects: client.getName(), list.get(0), client.name
 * Ignores: client (simple reference with implicit toString())
 */
public class LogNullDereferenceDetector extends BaseDetector implements ViolationDetector {
    
    private static final List<String> LOG_METHODS = Arrays.asList(
        "debug", "info", "warn", "error", "trace", "fatal", "log"
    );
    
    @Override
    public List<Violation> detect(PsiElement element) {
        List<Violation> violations = new ArrayList<>();
        
        if (!(element instanceof PsiMethodCallExpression)) {
            return violations;
        }
        
        PsiMethodCallExpression methodCall = (PsiMethodCallExpression) element;
        String methodName = methodCall.getMethodExpression().getReferenceName();
        
        // Check if this is a log method
        if (methodName == null || !LOG_METHODS.contains(methodName)) {
            return violations;
        }
        
        // Check if the method is called on a logger object
        PsiExpression qualifier = methodCall.getMethodExpression().getQualifierExpression();
        if (qualifier != null) {
            String qualifierText = qualifier.getText();
            if (!qualifierText.toLowerCase().contains("log")) {
                return violations;
            }
        }
        
        // Check arguments for method calls or field access (potential null dereference)
        PsiExpression[] args = methodCall.getArgumentList().getExpressions();
        
        for (PsiExpression arg : args) {
            if (containsMethodCall(arg)) {
                int columnNumber = getColumnNumber(element);
                String packageName = getPackageName(element);
                // This argument contains a method call or field access - potential null dereference
                int lineNumber = getLineNumber(element);
                String className = getClassName(element);
                String code = element.getText();
                
                Violation violation = new Violation(
                    ViolationType.LOG_NULL_DEREFERENCE,
                    element.getContainingFile(),
                    element,
                    lineNumber,
                    columnNumber,
                    className,
                    packageName,
                    code
                );
                
                violations.add(violation);
                break; // Only report once per log statement
            }
        }
        
        return violations;
    }
    
    private boolean containsMethodCall(PsiExpression expr) {
        // If this is a ternary operator with a null check, it's safe
        if (expr instanceof PsiConditionalExpression) {
            PsiConditionalExpression conditional = (PsiConditionalExpression) expr;
            PsiExpression condition = conditional.getCondition();
            
            // Check if the condition is a null check
            if (isNullCheckCondition(condition)) {
                return false; // Safe - wrapped in null check
            }
        }
        
        // Check if expression is a method call (e.g., client.getName(), list.get(0))
        if (expr instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression methodCall = (PsiMethodCallExpression) expr;
            PsiExpression qualifier = methodCall.getMethodExpression().getQualifierExpression();
            String methodName = methodCall.getMethodExpression().getReferenceName();
            
            // Check if this is a whitelisted null-safe method
            if (isNullSafeMethod(methodCall, methodName, qualifier)) {
                return false;
            }
            
            // Only flag if called on an object (has qualifier), not static calls
            if (qualifier != null) {
                return true;
            }
        }
        
        // Check if expression is field access (e.g., client.name, user.address)
        if (expr instanceof PsiReferenceExpression) {
            PsiReferenceExpression ref = (PsiReferenceExpression) expr;
            PsiExpression qualifier = ref.getQualifierExpression();
            
            // Only flag if it's field access (has qualifier)
            // Simple references like "client" without field access are safe (just toString())
            if (qualifier != null && !(qualifier instanceof PsiThisExpression) && !(qualifier instanceof PsiSuperExpression)) {
                // Check if it's actually a field, not a method call (already handled above)
                PsiElement resolved = ref.resolve();
                if (resolved instanceof PsiField) {
                    return true;
                }
            }
        }
        
        // Recursively check child expressions for method calls or field access
        for (PsiElement child : expr.getChildren()) {
            if (child instanceof PsiExpression && containsMethodCall((PsiExpression) child)) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isNullSafeMethod(PsiMethodCallExpression methodCall, String methodName, PsiExpression qualifier) {
        if (methodName == null) {
            return false;
        }
        
        // Get the class name and variable name if qualifier exists
        String qualifierClass = null;
        String qualifierName = null;
        if (qualifier != null) {
            qualifierName = qualifier.getText();
            PsiType type = qualifier.getType();
            if (type != null) {
                qualifierClass = type.getCanonicalText();
            }
        }
        
        // Skip common utility bean patterns (autowired beans) based on user settings
        if (qualifierName != null) {
            CodingLegionSettings settings = CodingLegionSettings.getInstance();
            String lowerName = qualifierName.toLowerCase();
            
            for (String pattern : settings.utilityBeanPatterns) {
                if (lowerName.endsWith(pattern.toLowerCase())) {
                    return true;  // Assume autowired beans are non-null
                }
            }
        }
        
        // String class null-safe methods
        if ("java.lang.String".equals(qualifierClass) || "String".equals(qualifierClass)) {
            if ("valueOf".equals(methodName) || "format".equals(methodName)) {
                return true;
            }
        }
        
        // Objects class null-safe methods (Java 7+)
        if ("java.util.Objects".equals(qualifierClass) || "Objects".equals(qualifierClass)) {
            if ("toString".equals(methodName) || "isNull".equals(methodName) || 
                "nonNull".equals(methodName) || "hash".equals(methodName) ||
                "equals".equals(methodName)) {
                return true;
            }
        }
        
        // Arrays class null-safe methods
        if ("java.util.Arrays".equals(qualifierClass) || "Arrays".equals(qualifierClass)) {
            if ("toString".equals(methodName) || "deepToString".equals(methodName) ||
                "asList".equals(methodName)) {
                return true;
            }
        }
        
        // Optional class null-safe methods (Java 8+)
        if ("java.util.Optional".equals(qualifierClass) || "Optional".equals(qualifierClass)) {
            if ("ofNullable".equals(methodName) || "empty".equals(methodName)) {
                return true;
            }
        }
        
        // Apache Commons Lang - StringUtils
        if (qualifierClass != null && qualifierClass.contains("StringUtils")) {
            if ("toString".equals(methodName) || "isEmpty".equals(methodName) ||
                "isNotEmpty".equals(methodName) || "isBlank".equals(methodName) ||
                "isNotBlank".equals(methodName) || "defaultString".equals(methodName) ||
                "defaultIfEmpty".equals(methodName) || "equals".equals(methodName) ||
                "equalsIgnoreCase".equals(methodName)) {
                return true;
            }
        }
        
        // Apache Commons Lang - ObjectUtils
        if (qualifierClass != null && qualifierClass.contains("ObjectUtils")) {
            if ("toString".equals(methodName) || "defaultIfNull".equals(methodName) ||
                "firstNonNull".equals(methodName)) {
                return true;
            }
        }
        
        // Apache Commons Lang - BooleanUtils
        if (qualifierClass != null && qualifierClass.contains("BooleanUtils")) {
            if ("isTrue".equals(methodName) || "isFalse".equals(methodName) ||
                "toBoolean".equals(methodName) || "toBooleanObject".equals(methodName) ||
                "isNotTrue".equals(methodName) || "isNotFalse".equals(methodName)) {
                return true;
            }
        }
        
        // Apache Commons Collections - CollectionUtils
        if (qualifierClass != null && qualifierClass.contains("CollectionUtils")) {
            if ("isEmpty".equals(methodName) || "isNotEmpty".equals(methodName) ||
                "size".equals(methodName) || "emptyIfNull".equals(methodName)) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isNullCheckCondition(PsiExpression condition) {
        // Check for patterns like: obj != null, obj == null, null != obj, null == obj
        if (condition instanceof PsiBinaryExpression) {
            PsiBinaryExpression binary = (PsiBinaryExpression) condition;
            IElementType operatorType = binary.getOperationTokenType();
            
            // Must be == or != operator
            if (operatorType != JavaTokenType.EQEQ && operatorType != JavaTokenType.NE) {
                return false;
            }
            
            PsiExpression left = binary.getLOperand();
            PsiExpression right = binary.getROperand();
            
            if (right == null) {
                return false;
            }
            
            // Check if one side is null literal
            boolean leftIsNull = left instanceof PsiLiteralExpression && "null".equals(left.getText());
            boolean rightIsNull = right instanceof PsiLiteralExpression && "null".equals(right.getText());
            
            // Must have exactly one null and one reference
            return (leftIsNull && !rightIsNull) || (!leftIsNull && rightIsNull);
        }
        
        return false;
    }
    
    @Override
    public String getName() {
        return "LogNullDereferenceDetector";
    }

}
