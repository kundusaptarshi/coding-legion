package com.codinglegion.model;

/**
 * Severity levels for detected violations in Coding Legion
 */
public enum ViolationSeverity {
    /**
     * Definite coding standard violations that must be fixed
     */
    ERROR,
    
    /**
     * Potential violations that should be reviewed and fixed when appropriate
     */
    WARNING
}

