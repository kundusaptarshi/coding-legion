package com.codinglegion.analyzer.detectors;

import com.intellij.psi.*;
import com.codinglegion.analyzer.ViolationDetector;
import com.codinglegion.model.Violation;
import com.codinglegion.model.ViolationType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Detects null values being passed to context/map setter methods
 * Rule: Don't add null values to context objects (e.g., context.setTransactionAttribute(ns, key, null))
 */
public class NullValueInContextDetector extends BaseDetector implements ViolationDetector {
    
    // Common setter method patterns
    private static final List<String> SETTER_METHOD_PATTERNS = Arrays.asList(
        "set", "put", "add", "insert", "append"
    );
    
    // Methods that should be excluded (safe to pass null)
    private static final List<String> EXCLUDED_METHODS = Arrays.asList(
        "assertEquals", "assertNull", "assertNotNull", "assertTrue", "assertFalse",
        "setNull"  // JDBC setNull is intentional
    );
    
    @Override
    public List<Violation> detect(PsiElement element) {
        List<Violation> violations = new ArrayList<>();
        
        if (!(element instanceof PsiMethodCallExpression)) {
            return violations;
        }
        
        PsiMethodCallExpression methodCall = (PsiMethodCallExpression) element;
        PsiReferenceExpression methodRef = methodCall.getMethodExpression();
        String methodName = methodRef.getReferenceName();
        
        if (methodName == null) {
            return violations;
        }
        
        // Skip excluded methods (assertions, etc.)
        if (isExcludedMethod(methodName)) {
            return violations;
        }
        
        // Check if this is a setter-like method
        if (!isSetterLikeMethod(methodName)) {
            return violations;
        }
        
        // Check if the object is a context/map-like type (not a regular POJO)
        PsiExpression qualifier = methodRef.getQualifierExpression();
        if (!isContextOrMapType(qualifier)) {
            return violations;  // Skip regular POJO setters like product.setPrice(null)
        }
        
        // Get method arguments
        PsiExpression[] args = methodCall.getArgumentList().getExpressions();
        
        if (args.length == 0) {
            return violations;
        }
        
        // Check each argument for null literals
        // For context setters, the value is typically the last argument
        // e.g., setTransactionAttribute(namespace, key, value)
        //       setAttribute(key, value)
        //       put(key, value)
        
        int valueArgIndex = getValueArgumentIndex(methodName, args.length);
        
        if (valueArgIndex >= 0 && valueArgIndex < args.length) {
            PsiExpression valueArg = args[valueArgIndex];
            
            if (isNullLiteral(valueArg)) {
                // Determine violation type based on object type (qualifier already declared above)
                boolean isMap = isMapType(qualifier);
                
                addViolation(methodCall, methodName, valueArgIndex, isMap, violations);
            }
        }
        
        return violations;
    }
    
    /**
     * Check if method name matches setter patterns AND is on a context/map-like object
     */
    private boolean isSetterLikeMethod(String methodName) {
        for (String pattern : SETTER_METHOD_PATTERNS) {
            if (methodName.startsWith(pattern)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if the object is a context/map-like type (not a regular POJO)
     */
    private boolean isContextOrMapType(PsiExpression qualifier) {
        return isMapType(qualifier) || isContextType(qualifier);
    }
    
    /**
     * Check if the object is a Map type
     */
    private boolean isMapType(PsiExpression qualifier) {
        if (qualifier == null) {
            return false;
        }
        
        PsiType type = qualifier.getType();
        if (type == null) {
            return false;
        }
        
        String typeName = type.getCanonicalText();
        
        // Check if it's a Map type
        if (typeName.contains("java.util.Map") || typeName.contains("Map<")) {
            return true;
        }
        
        // Check if it's a known Map implementation
        if (typeName.contains("java.util.HashMap") || typeName.contains("java.util.LinkedHashMap") ||
            typeName.contains("java.util.TreeMap") || typeName.contains("java.util.Hashtable") ||
            typeName.contains("java.util.concurrent.ConcurrentHashMap")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if the object is a Context or container type (not a Map)
     */
    private boolean isContextType(PsiExpression qualifier) {
        if (qualifier == null) {
            return false;
        }
        
        PsiType type = qualifier.getType();
        if (type == null) {
            return false;
        }
        
        String typeName = type.getCanonicalText();
        String simpleTypeName = type.getPresentableText();
        
        // Check if class/type name contains "Context" (case-insensitive)
        if (typeName.toLowerCase().contains("context")) {
            return true;
        }
        
        if (simpleTypeName.toLowerCase().contains("context")) {
            return true;
        }
        
        // Check for common context-like class names
        if (typeName.contains("Attributes") || typeName.contains("Properties") ||
            typeName.contains("Configuration") || typeName.contains("Settings") ||
            typeName.contains("Registry") || typeName.contains("Cache")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if method is excluded (assertions, etc.)
     */
    private boolean isExcludedMethod(String methodName) {
        for (String excluded : EXCLUDED_METHODS) {
            if (methodName.contains(excluded) || methodName.equalsIgnoreCase(excluded)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Determine which argument index is the value argument
     */
    private int getValueArgumentIndex(String methodName, int argCount) {
        // For most setters, the value is the last argument
        // setTransactionAttribute(namespace, key, value) -> index 2
        // setAttribute(key, value) -> index 1
        // put(key, value) -> index 1
        // add(value) -> index 0
        
        // Special cases for specific method patterns
        if (methodName.matches("set.*Attribute")) {
            // setXxxAttribute methods typically have (namespace, key, value) or (key, value)
            return argCount - 1; // Last argument is the value
        }
        
        if (methodName.equals("put") || methodName.equals("putIfAbsent")) {
            // Map.put(key, value) -> value is index 1
            return argCount >= 2 ? 1 : -1;
        }
        
        if (methodName.equals("add") || methodName.equals("append")) {
            // Collection.add(value) or StringBuilder.append(value) -> value is index 0
            // But List.add(index, value) -> value is index 1
            if (argCount == 1) {
                return 0;
            } else if (argCount == 2) {
                // Could be add(index, value) - check if first arg is int
                return 1;
            }
        }
        
        if (methodName.startsWith("set")) {
            // Generic setters: setValue(value), setName(value), etc.
            // Typically single argument
            if (argCount == 1) {
                return 0;
            }
            // Multi-arg setters: value is usually last
            return argCount - 1;
        }
        
        // Default: value is the last argument
        return argCount > 0 ? argCount - 1 : -1;
    }
    
    /**
     * Check if expression is a null literal
     */
    private boolean isNullLiteral(PsiExpression expr) {
        // Direct null literal
        if (expr instanceof PsiLiteralExpression) {
            return "null".equals(expr.getText());
        }
        
        // Parenthesized null
        if (expr instanceof PsiParenthesizedExpression) {
            PsiParenthesizedExpression paren = (PsiParenthesizedExpression) expr;
            PsiExpression inner = paren.getExpression();
            return inner != null && isNullLiteral(inner);
        }
        
        // Typecast null: (String) null
        if (expr instanceof PsiTypeCastExpression) {
            PsiTypeCastExpression cast = (PsiTypeCastExpression) expr;
            PsiExpression operand = cast.getOperand();
            return operand != null && isNullLiteral(operand);
        }
        
        return false;
    }
    
    /**
     * Add violation with appropriate type based on object type
     */
    private void addViolation(PsiMethodCallExpression methodCall, String methodName,
                             int nullArgIndex, boolean isMap, List<Violation> violations) {
        int lineNumber = getLineNumber(methodCall);
        int columnNumber = getColumnNumber(methodCall);
        String className = getClassName(methodCall);
        String packageName = getPackageName(methodCall);
        String code = methodCall.getText();
        
        // Use different violation type based on object type
        ViolationType violationType = isMap ? ViolationType.NULL_VALUE_IN_MAP : ViolationType.NULL_VALUE_IN_CONTEXT;
        
        Violation violation = new Violation(
            violationType,
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
        return "NullValueInContextDetector";
    }
}

