package com.codinglegion.model;

/**
 * Types of null check violations detected by Coding Legion
 */
public enum ViolationType {
    STRING_EQUALS(
        "String Equals Without Null Safety",
        "Detected: variable.equals(other) - This will throw NullPointerException if 'variable' is null. " +
        "Solution: Use StringUtils.equals(a, b) which safely handles null values on both sides (returns false if either is null). " +
        "Note: String literals are safe - \"CONSTANT\".equals(variable) won't cause NPE and is allowed.",
        ViolationSeverity.ERROR,
        "StringUtils.equals(a, b)"
    ),
    
    STRING_EMPTY_CHECK(
        "Manual String Null/Empty Check",
        "Detected: str == null || str.isEmpty() OR str == null || str.length() == 0 OR str == null || str.equals(\"\") - While these patterns are safe, they're verbose. " +
        "Solution: Use StringUtils.isEmpty(str) - a single method that checks both null and empty in one call. Cleaner, more maintainable, and follows team coding standards. " +
        "Benefit: Reduces code duplication and improves readability across the codebase.",
        ViolationSeverity.ERROR,
        "StringUtils.isEmpty(str)"
    ),
    
    COLLECTION_NULL_CHECK(
        "Manual or Unsafe Collection Check",
        "Detected: collection == null || collection.isEmpty() OR unsafe collection.isEmpty() without null check. " +
        "Problem: Manual null checks are verbose; unsafe isEmpty()/size() calls will throw NullPointerException if collection is null. " +
        "Solution: Use CollectionUtils.isEmpty(collection) - one method that safely handles null, empty, and zero-size collections. Returns true for null or empty collections. " +
        "For non-empty checks, use CollectionUtils.isNotEmpty(collection).",
        ViolationSeverity.ERROR,
        "CollectionUtils.isEmpty(collection)"
    ),
    
    COLLECTION_SIZE_CHECK(
        "Collection Size Comparison",
        "Detected: collection.size() == 0 OR collection.size() > 0 (or other numeric comparisons). " +
        "Problem: If collection is null, calling size() throws NullPointerException. Also less readable than intent-revealing methods. " +
        "Solution: Use CollectionUtils.isEmpty(collection) for zero-size checks, or CollectionUtils.isNotEmpty(collection) for non-zero checks. " +
        "These methods are null-safe and clearly express intent: checking if collection has elements, not counting them.",
        ViolationSeverity.ERROR,
        "CollectionUtils.isEmpty(collection) or !CollectionUtils.isEmpty(collection)"
    ),
    
    TERNARY_NULL_DEFAULT(
        "Null Default Pattern (Ternary or If-Else)",
        "Detected: Null default pattern using ternary (obj != null ? obj : defaultValue) OR if-else blocks (if (obj == null) { result = defaultValue; } else { result = obj; } or similar return statements). " +
        "While these patterns are functionally correct and null-safe, they're verbose and require mental parsing. " +
        "Suggestion: Use ObjectUtils.defaultIfNull(obj, defaultValue) for improved readability and conciseness. " +
        "Benefit: The method name explicitly conveys intent ('provide default if null'), making code self-documenting. Works for assignments, returns, and any expression. " +
        "This is a WARNING (not ERROR) because your code works correctly - it's about code clarity, consistency, and maintainability.",
        ViolationSeverity.WARNING,
        "ObjectUtils.defaultIfNull(obj, defaultValue)"
    ),
    
    BOOLEAN_UNBOXING(
        "Boolean Auto-Unboxing Risk",
        "Detected: Boolean wrapper being auto-unboxed to primitive boolean (e.g., boolean val = boolObj, return boolObj, method(boolObj), or if(boolObj)). " +
        "Problem: Java automatically unboxes Boolean to boolean, but if the Boolean is null, this throws NullPointerException at runtime. " +
        "Solution: Use BooleanUtils.isTrue(boolObj) which returns false for null (safe default), or Boolean.TRUE.equals(boolObj) for strict true checks. " +
        "Why this is ERROR: The detector only flags confirmed Boolean→boolean conversions, so there's a real NPE risk that must be addressed.",
        ViolationSeverity.ERROR,
        "BooleanUtils.isTrue(boolObj) or Boolean.TRUE.equals(boolObj)"
    ),
    
    LOG_NULL_DEREFERENCE(
        "Potential Null Dereference in Log Statement",
        "Detected: Method call or field access on potentially null object in log arguments (e.g., log.info('Name: {}', client.getName()) or log.info('Name: {}', client.name)). " +
        "Risk: If the object is null, calling methods or accessing fields will throw NullPointerException. " +
        "Safe patterns NOT flagged: Simple object references (log.info('Client: {}', client) uses toString), null-safe utility methods (String.valueOf(), Objects.toString(), StringUtils methods), or expressions already wrapped in null checks. " +
        "Solution: Add ternary null check: client != null ? client.getName() : null, or ensure object is non-null before logging. " +
        "Why this is WARNING: Context-dependent - you may know the object is non-null from prior logic. Review and apply fix where appropriate.",
        ViolationSeverity.WARNING,
        "log.info('Name: {}', client != null ? client.getName() : null)"
    ),
    
    DTO_INITIALIZATION_CHECK(
        "DTO Property Access Without Initialization Check",
        "Detected: Accessing DTO property (e.g., dto.getDay()) without first checking if it's initialized using dto.isInitialized(\"Day\"). " +
        "Risk: Uninitialized DTO properties may cause unexpected behavior or NullPointerException depending on the framework (Hibernate lazy loading, custom DTOs, etc.). " +
        "Solution: Always check initialization before accessing DTO properties: if (dto.isInitialized(\"PropertyName\") && dto.getPropertyName() != null) { ... } " +
        "Pattern: dto.isInitialized(\"Day\") && dto.getDay() != null && dto.getDay().equals(value). " +
        "Why this is WARNING: Context-dependent - you may know the DTO is already fully initialized from prior logic, or the framework guarantees initialization. Review based on your DTO framework's behavior.",
        ViolationSeverity.WARNING,
        "dto.isInitialized(\"PropertyName\") && dto.getPropertyName() != null"
    ),
    
    NULL_VALUE_IN_CONTEXT(
        "Null Value Added to Context/Attributes",
        "Detected: Passing null literal as value to Context/Attributes/Properties/Settings setter methods (e.g., context.setTransactionAttribute(ns, key, null), settings.setAttribute(name, null)). " +
        "Problem: Explicitly adding null values to shared context objects, attributes, or configuration containers is almost always a bug. This causes NullPointerException or unexpected behavior when other components retrieve the value. " +
        "Solution: Only add non-null values. To remove entries, use remove() method. If value might be null: if (value != null) { context.set...(key, value); } " +
        "Targets: Context types and container-like classes (Attributes, Properties, Configuration, Settings, Registry, Cache). " +
        "NOT flagged: Regular POJO setters (product.setPrice(null)) or Map.put() - those have legitimate null use cases. " +
        "Why this is ERROR: For shared contexts/attributes, null literals indicate a definite bug. Use remove() to delete, not set to null.",
        ViolationSeverity.ERROR,
        "if (value != null) { context.setXxx(..., value); } or context.remove(key)"
    ),
    
    NULL_VALUE_IN_MAP(
        "Null Value Added to Map",
        "Detected: Passing null literal as value to Map.put() method (e.g., map.put(key, null), hashMap.put(\"key\", null)). " +
        "Consideration: While Java Maps support null values, this creates ambiguity: map.get(key) returns null for both 'key not found' AND 'key has null value'. " +
        "Problem: Often indicates a logic error where a variable unexpectedly became null, leading to NPE when the value is used later. " +
        "Solution: Consider if null is intentional. If removing an entry, use map.remove(key). If value might be null: if (value != null) { map.put(key, value); } " +
        "Why this is WARNING (not ERROR): Maps explicitly support null values in Java, and there are legitimate use cases (sparse data, explicit disable flags). Review each case based on your intent.",
        ViolationSeverity.WARNING,
        "if (value != null) { map.put(key, value); } or map.remove(key)"
    );
    
    private final String title;
    private final String description;
    private final ViolationSeverity severity;
    private final String suggestedFix;
    
    ViolationType(String title, String description, ViolationSeverity severity, String suggestedFix) {
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.suggestedFix = suggestedFix;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Get compact description for UI display (full description is in README)
     */
    public String getCompactDescription() {
        switch (this) {
            case STRING_EQUALS:
                return "Use StringUtils.equals(a, b) to prevent NPE when 'a' is null";
            case STRING_EMPTY_CHECK:
                return "Use StringUtils.isEmpty(str) instead of manual 'str == null || str.isEmpty()' pattern";
            case COLLECTION_NULL_CHECK:
            case COLLECTION_SIZE_CHECK:
                return "Use CollectionUtils.isEmpty(collection) - handles null, empty, and size checks safely";
            case TERNARY_NULL_DEFAULT:
                return "Consider ObjectUtils.defaultIfNull(obj, default) for better readability";
            case BOOLEAN_UNBOXING:
                return "Boolean auto-unboxing will throw NPE if null. Use BooleanUtils.isTrue(boolObj)";
            case NULL_VALUE_IN_CONTEXT:
                return "Never add null to Context/Attributes. Use remove() or add null check";
            case LOG_NULL_DEREFERENCE:
                return "Method call or field access on potentially null object in log arguments";
            case DTO_INITIALIZATION_CHECK:
                return "Check dto.isInitialized(\"Property\") before accessing DTO properties";
            case NULL_VALUE_IN_MAP:
                return "Map.put(key, null) creates ambiguity. Consider using remove() or null check";
            default:
                return description;
        }
    }
    
    public ViolationSeverity getSeverity() {
        return severity;
    }
    
    public String getSuggestedFix() {
        return suggestedFix;
    }
}

