# Changelog

## Version 1.6.0 - Major Update

### 🆕 New Rules (3 Added)
- **Rule 8: DTO Initialization Check (WARNING)** - Warns when accessing DTO properties without checking isInitialized() first
- **Rule 9: Null Values in Context (ERROR)** - Prevents adding null literals to context objects and shared containers
- **Rule 10: Null Values in Map (WARNING)** - Warns about null literals in Map.put() - review for legitimacy

### ✨ Rule Improvements
- **Rule 5 (Null Defaults) - Extended!** Now detects if-else blocks in addition to ternary operators:
  - if (obj == null) { x = default; } else { x = obj; }
  - if (obj == null) { return default; } else { return obj; }

- **Rule 6 (Boolean Unboxing) - Now ERROR!** Changed from WARNING to ERROR + detects more cases:
  - Return statements with unboxing
  - Method arguments with unboxing
  - If conditions with unboxing

- **Rule 7 (Log Null Dereference) - Smarter!**
  - Now detects field access (client.name)
  - Comprehensive null-safe method whitelist (Objects, Arrays, Optional, StringUtils, CollectionUtils, etc.)
  - Doesn't flag simple object references

### 🔧 Detector Enhancements
- **Combined Collection Detectors** - Rules 3 & 4 merged into one comprehensive detector:
  - Detects unsafe isEmpty()/size() calls without null checks
  - Recognizes safe patterns (new ArrayList(), Collections.emptyList())
  - Checks for existing null checks in parent conditions

- **Rule 1 (String.equals) - Fewer False Positives!** Now ignores string literals: "CONSTANT".equals(variable)
- **Rule 2 (String Empty) - More Patterns!** Now detects: str == null || str.length() == 0
- **Rules 9 & 10 - Smart Severity Levels!**
  - Context/Attributes: ERROR (always a bug)
  - Maps: WARNING (might be legitimate)
  - Regular POJOs: Not flagged (product.setPrice(null) is fine)

### 🎨 UI Improvements
- **Warning Tab Footer** - Clickable link to Best Practices documentation for manual code review principles
- **Bug Fix:** Stale violations now properly cleared when no Java changes detected or files deleted
- Improved violation descriptions with detailed examples and context

### 📚 Documentation Updates
- **New Section: Manual Code Review Principles**
  - Error handling & null check guidelines
  - Boolean testing requirements (true/false/null coverage)
  - Clear separation of automated vs manual rules
- Comparison table: Automated Detection vs Manual Review Required
- Updated all rule descriptions with better examples

### 📊 Summary
- **Total Rules:** 10 automated detectors (was 7)
- **Errors:** 7 rules (was 5)
- **Warnings:** 4 rules (was 2)
- **False Positives:** Significantly reduced across all rules
- **Detection Coverage:** Expanded to cover more violation patterns
- **Smarter Severity:** Context/Map null values now have appropriate severity levels based on risk

---

## Version 1.0.0 - Initial Release

### Features
- Null check coding standards enforcement
- String handling violations detection (StringUtils)
- Collection handling violations detection (CollectionUtils)
- Object null defaults detection (ObjectUtils)
- Boolean auto-unboxing warnings
- Analyzes only changed files in feature branches
- Clickable navigation links with exact line:column
- Tabbed interface for errors and warnings
- Selectable text for copying violations
- Fast background analysis

