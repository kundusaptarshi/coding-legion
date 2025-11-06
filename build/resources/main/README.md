# Coding Legion

**Enforce coding standards in your Java projects automatically!**

Coding Legion is a powerful IntelliJ IDEA plugin that helps development teams maintain consistent coding standards by detecting and reporting violations in real-time. The plugin intelligently analyzes only changed files in feature branches, making it fast and efficient for daily development workflows.

**Current Focus**: Null check coding standards using Apache Commons and Spring utilities.

**Designed for Extensibility**: The plugin architecture supports easy addition of new coding standard rules beyond null checks.

---

## 📋 Table of Contents

- [Features](#-features)
- [Requirements](#-requirements)
- [Installation](#-installation)
- [Usage Guide](#-usage-guide)
- [Violations Reference](#-violations-reference) - 10 Automated Rules
- [Best Practices](#-best-practices) - Manual Review Guidelines
- [Configuration](#-configuration)
- [Development](#-development)
- [Team Distribution](#-team-distribution)
- [Troubleshooting](#-troubleshooting)
- [Future Improvements](#-future-improvements)
- [Copyright](#-copyright)

---

## ✨ Features

### Smart Analysis
- **Feature Branch Detection**: Only runs on feature branches (automatically skips main/master/develop)
- **Changed Files Only**: Analyzes only modified/added Java files for maximum performance
- **Git-Aware**: New files must be staged (`git add`) to be included in analysis
- **Background Processing**: Non-blocking, fast analysis that doesn't interrupt your workflow
- **Incremental Scanning**: Efficient detection focused on your changes

### Comprehensive Detection
- **Errors**: Detects definite coding standard violations that must be fixed (7 rules)
- **Warnings**: Flags potential coding standard concerns that should be reviewed (4 rules)
- **10 Automated Rules**: String handling, collections, objects, booleans, logging, DTOs, and context/map management
- **Extensible Architecture**: Easily add new rules for other coding standards
- **Smart Severity Levels**: Context-aware ERROR vs WARNING classification

### Developer-Friendly UI
- **Clickable Violations**: Click blue links to jump directly to exact code location (line:column)
- **Tabbed Results**: Separate tabs for Errors and Warnings
- **Quick Re-run**: 🔄 reload button appears after first analysis for instant re-analysis
- **Smart Tab Behavior**: Re-run preserves current tab, initial run shows Errors tab
- **Theme-Adaptive**: Beautiful UI that works in both light and dark IntelliJ themes
- **Selectable Text**: Copy violation details for documentation (no cursor interference)
- **Bundled Documentation**: Access full docs within IntelliJ

### Team Collaboration
- **No External Dependencies**: Self-contained plugin with no runtime dependencies
- **Easy Distribution**: Share as a single JAR file across your team
- **Version Compatible**: Works with IntelliJ IDEA 2020.1+ and all future versions
- **Professional Display**: Custom logo and branding

---

## 📦 Requirements

- **IntelliJ IDEA**: Version 2020.1 or later (Community or Ultimate Edition)
- **Java Project**: With Git version control
- **Java**: Java 8 or higher (plugin is built with Java 8 for maximum compatibility)
- **Git**: Project must be under Git version control

**For using suggested fixes, your project needs**:
```xml
<!-- Apache Commons Lang3 -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
    <version>3.12.0</version>
</dependency>

        <!-- Apache Commons Collections4 -->
<dependency>
<groupId>org.apache.commons</groupId>
<artifactId>commons-collections4</artifactId>
<version>4.4</version>
</dependency>
```

---

## 🚀 Installation

### Quick Install (Recommended)

1. **Get the Plugin ZIP**:
   ```bash
   # Build from source:
   ./gradlew buildPlugin
   # Output: build/distributions/coding-legion-<version>.zip
   ```

2. **Install in IntelliJ**:
    - Open IntelliJ IDEA
    - Go to `File` → `Settings` → `Plugins`
    - Click gear icon (⚙️) → `Install Plugin from Disk...`
    - Select `coding-legion-<version>.zip`
    - Click `OK` and **restart IntelliJ**

3. **Verify Installation**:
    - After restart, go to `Tools` menu
    - You should see "Run Legion Analysis" option
    - Right-click on project should also show "Run Legion Analysis"
    - Check bottom panel for "Coding Legion" tool window

### Building from Source

1. **Clone and Build**:
   ```bash
   git clone <repository-url>
   cd coding-legion
   chmod +x gradlew  # Linux/Mac only
   ./gradlew buildPlugin
   ```

2. **Development Mode**:
   ```bash
   ./gradlew runIde  # Launches IntelliJ with plugin pre-installed
   ```

---

## 📖 Usage Guide

### Quick Start

1. **Checkout a Feature Branch**:
   ```bash
   git checkout -b feature/my-new-feature
   ```
   ⚠️ Plugin only works on feature branches, not on main/master/develop

2. **Make Code Changes**:
    - Modify or create Java files
    - Write your feature code
    - **Important**: New Java files must be staged (`git add`) to be analyzed

3. **Run Analysis**:
    - **Method 1**: `Tools` → `Run Legion Analysis`
    - **Method 2**: Right-click on project → `Run Legion Analysis`

4. **Review Violations**:
    - "Coding Legion" tool window opens at bottom
    - Switch between **⚠ Errors** and **⚡ Warnings** tabs
    - Each violation shows: `1. package.Class.java:line:column - Rule: Description`
    - Click the blue underlined link to jump to exact location

5. **Fix Violations**:
    - Navigate to violation location
    - Apply the suggested fix (shown in grey text)
    - Copy text by selecting with mouse
    - Re-run analysis to verify

### Understanding the UI

**Tool Window Header**:
```
🏛️ Coding Legion                    🔄  📄 Docs
```
- **🔄 Re-run**: Appears after first analysis, click to instantly re-analyze (stays on current tab)
- **📄 Docs**: Click to open this comprehensive README in IntelliJ editor
- **ℹ️ Info Icon**: Click next to any rule in violations to jump to that rule's documentation

**Tabs**:
- **⚠ Errors (X)** - Definite violations that must be fixed
- **⚡ Warnings (X)** - Potential issues to review
- **Tab Behavior**: Initial analysis always shows Errors tab, re-run via 🔄 preserves current tab

**Violation Display Format**:
```
1. com.example.service.UserService.java:46:21 - Rule: Use StringUtils.equals(a, b) instead of a.equals(b) to prevent NullPointerException when 'a' is null. ℹ️
   Suggested: StringUtils.equals(a, b)
```

### Workflow Integration

```
1. Create Feature Branch
   ↓
2. Write Code  
   ↓
3. Stage New Files (git add)
   ↓
4. Run Analysis (Tools menu or right-click)
   ↓
5. Review Violations in Tool Window
   ↓
6. Fix and Re-analyze (click 🔄 for quick re-run)
   ↓
7. Commit Clean Code
```

---

## 🔍 Violations Reference

**Complete reference for all violations that are AUTOMATICALLY DETECTED by the plugin.**

These rules are automatically enforced during analysis. Violations will appear in the Errors or Warnings tabs.

### Current Coverage: Null Check Standards

The plugin currently enforces null-safe coding practices using Apache Commons and Spring utilities. Additional coding standards can be added easily using the extensible detector framework.

**Note:** Some important coding principles cannot be automatically detected and require manual review. See [Best Practices & Coding Principles](#-best-practices) section below for manual guidelines.

### ⚠ ERRORS (Must Fix)

#### 1. String Equals Without Null Safety

**What it detects**: `a.equals(b)` where `a` could be null

**Why**: Throws NullPointerException if `a` is null

**Bad**:
```java
if (username.equals("admin")) {  // NPE if username is null!
grantAccess();
}
```

**Good**:
```java
import org.apache.commons.lang3.StringUtils;

if (StringUtils.equals(username, "admin")) {  // Safe
grantAccess();
}
```

**Fix**: `StringUtils.equals(a, b)`

---

#### 2. Manual String Null/Empty Check

**What it detects**: `str == null || str.isEmpty()`

**Why**: Verbose, violates team standard

**Bad**:
```java
if (name == null || name.isEmpty()) {
showError();
}
```

**Good**:
```java
import org.apache.commons.lang3.StringUtils;

if (StringUtils.isEmpty(name)) {
showError();
}
```

**Fix**: `StringUtils.isEmpty(str)`

---

#### 3-4. Collection Checks (Combined Detector)

**What it detects**: 
- `collection == null || collection.isEmpty()` (manual pattern)
- `collection.size() == 0` or `> 0` (size comparisons)
- Unsafe `collection.isEmpty()` or `collection.size()` without null check

**Why**: Verbose manual checks, NPE risk if not null-checked, less semantic

**Bad**:
```java
// Manual null check (verbose)
if (list == null || list.isEmpty()) {
    return;
}

// Size comparison (not null-safe!)
if (items.size() == 0) {  // NPE if items is null!
    return "No items";
}
```

**Good**:
```java
import org.apache.commons.collections4.CollectionUtils;

// One method handles all cases (null-safe!)
if (CollectionUtils.isEmpty(list)) {
    return;
}

if (CollectionUtils.isNotEmpty(items)) {
    processItems();
}
```

**Fix**: `CollectionUtils.isEmpty(collection)` or `CollectionUtils.isNotEmpty(collection)`

**Note**: The detector is smart - it recognizes safe patterns like `new ArrayList<>().isEmpty()` and `Collections.emptyList().size()` and won't flag them.

---

#### 6. Boolean Auto-Unboxing Risk

**What it detects**: Boolean wrapper being auto-unboxed to primitive boolean

**Why**: Auto-unboxing throws NPE if wrapper is null

**Bad**:
```java
Boolean isActive = getStatus();
boolean active = isActive;  // NPE if isActive is null!

public boolean isValid() {
    Boolean result = checkValidity();
    return result;  // NPE if result is null!
}

void setFlag(boolean flag) { }
Boolean flagObj = getFlag();
setFlag(flagObj);  // NPE if flagObj is null!
```

**Good**:
```java
import org.apache.commons.lang3.BooleanUtils;

Boolean isActive = getStatus();
boolean active = BooleanUtils.isTrue(isActive);  // Safe, defaults to false

public boolean isValid() {
    Boolean result = checkValidity();
    return BooleanUtils.isTrue(result);  // Safe
}

void setFlag(boolean flag) { }
Boolean flagObj = getFlag();
if (flagObj != null) {
    setFlag(flagObj);
}
```

**Fix**: `BooleanUtils.isTrue(boolObj)` or `Boolean.TRUE.equals(boolObj)`

**Note**: Changed to ERROR in v1.6.0 - the detector confirms Boolean→boolean conversion, so there's real NPE risk.

---

#### 7. Null Value in Context/Attributes

**What it detects**: Null literals passed to Context/Attributes/Properties/Settings setters

**Why**: Almost always a bug in shared contexts

**Bad**:
```java
context.setTransactionAttribute(namespace, key, null);
settings.setAttribute("name", null);
properties.setProperty("config", null);
```

**Good**:
```java
// Only add non-null values
if (value != null) {
    context.setTransactionAttribute(namespace, key, value);
}

// To remove, use remove() method
context.removeTransactionAttribute(namespace, key);
```

**Fix**: `if (value != null) { context.set...(key, value); }` or use `remove()`

**Note**: Regular POJO setters like `product.setPrice(null)` are NOT flagged - those are legitimate for optional fields.

---

### ⚡ WARNINGS (Should Review)

#### 5. Null Default Pattern (Ternary or If-Else)

**What it detects**: 
- Ternary: `obj != null ? obj : defaultValue`
- If-else assignment: `if (obj == null) { x = default; } else { x = obj; }`
- If-else return: `if (obj == null) { return default; } else { return obj; }`

**Why**: Less expressive than utility method (though functionally correct)

**Warning**:
```java
// Ternary
String value = config != null ? config : "default";

// If-else assignment
String result;
if (user == null) {
    result = "Guest";
} else {
    result = user;
}

// If-else return
if (config != null) {
    return config;
} else {
    return DEFAULT_CONFIG;
}
```

**Better**:
```java
import org.apache.commons.lang3.ObjectUtils;

// All three cases become:
String value = ObjectUtils.defaultIfNull(config, "default");
String result = ObjectUtils.defaultIfNull(user, "Guest");
return ObjectUtils.defaultIfNull(config, DEFAULT_CONFIG);
```

**Fix**: `ObjectUtils.defaultIfNull(obj, defaultValue)`

---

#### 8. Potential Null Dereference in Log Statement

**What it detects**: 
- Method calls in log arguments: `client.getName()`
- Field access in log arguments: `client.name`
- Does NOT flag: Simple object references like `client`

**Why**: May throw NPE if object is null

**Warning**:
```java
log.info("Client: {}", client.getName());  // NPE if client is null
log.info("Name: {}", user.name);  // NPE if user is null
log.info("Item: {}", list.get(0));  // NPE if list is null
```

**Better**:
```java
// Option 1: Ternary null check
log.info("Client: {}", client != null ? client.getName() : null);

// Option 2: Null-safe utility
log.info("Client: {}", Objects.toString(client));

// Already safe (not flagged):
log.info("Client: {}", client);  // Just toString(), safe
log.info("Value: {}", String.valueOf(obj));  // Null-safe method
```

**Fix**: `log.info("...", obj != null ? obj.method() : null)`

**Note**: Comprehensive whitelist of null-safe methods (String.valueOf, Objects.toString, StringUtils, CollectionUtils, etc.)

---

#### 9. DTO Property Access Without Initialization Check

**What it detects**: Accessing DTO getters without checking `isInitialized("PropertyName")` first

**Why**: Uninitialized DTO properties may cause issues (Hibernate lazy loading, etc.)

**Warning**:
```java
if (dto.getDay() != null && dto.getDay().equals(schedulingDate)) {
    // Missing initialization check!
}
```

**Better**:
```java
if (dto.isInitialized("Day") && 
    dto.getDay() != null && 
    dto.getDay().equals(schedulingDate)) {
    // Proper check
}
```

**Fix**: `dto.isInitialized("PropertyName") && dto.getPropertyName() != null`

**Note**: Only applies to DTOs with `isInitialized(String)` method. Regular POJOs are not flagged.

---

#### 10. Null Value in Map

**What it detects**: Null literals passed to `Map.put()` methods

**Why**: Creates ambiguity - `map.get(key)` returns null for both "key absent" and "key has null value"

**Warning**:
```java
map.put("key", null);
hashMap.put("id", null);
```

**Better**:
```java
// If removing an entry:
map.remove("key");

// If value might be null, add guard:
if (value != null) {
    map.put("key", value);
}

// If null is intentional (rare):
// Document why null value is needed
map.put("disabledFeature", null);  // Comment: null means disabled
```

**Fix**: `if (value != null) { map.put(key, value); }` or `map.remove(key)`

**Note**: This is WARNING (not ERROR) because Maps support null values and some use cases are legitimate (sparse data, disable flags).

---

## 📊 Violations Summary

| # | Violation | Severity | Fix Complexity | Frequency |
|---|-----------|----------|----------------|-----------|
| 1 | String.equals() without null safety | ⚠ ERROR | Easy | Very Common |
| 2 | Manual string null/empty check | ⚠ ERROR | Easy | Common |
| 3-4 | Collection checks (combined) | ⚠ ERROR | Easy | Common |
| 5 | Null default pattern (ternary/if-else) | ⚡ WARNING | Easy | Occasional |
| 6 | Boolean auto-unboxing | ⚠ ERROR | Easy | Rare |
| 7 | Null value in context/attributes | ⚠ ERROR | Easy | Rare |
| 8 | Log null dereference | ⚡ WARNING | Medium | Occasional |
| 9 | DTO initialization check | ⚡ WARNING | Easy | Rare |
| 10 | Null value in map | ⚡ WARNING | Easy | Occasional |

**Totals:** 10 rules (7 ERRORS, 4 WARNINGS) | All automatically detected

---

## ⚙️ Configuration

### Protected Branches

The plugin automatically blocks analysis on these branches:
- `master`
- `main`
- `develop`
- `development`

To customize, edit `src/main/java/com/codinglegion/utils/GitBranchChecker.java`:

```java
private static final Set<String> PROTECTED_BRANCHES = new HashSet<>(Arrays.asList(
        "master", "main", "develop", "development"
        // Add your custom protected branches here
));
```

---

## 🛠️ Development

### Project Structure

```
src/main/java/com/codinglegion/
├── actions/
│   └── RunAnalysisAction.java               # Main action handler
├── analyzer/
│   ├── CodingStandardsAnalyzer.java         # Orchestrates all detectors
│   ├── ViolationDetector.java               # Base detector interface
│   └── detectors/
│       ├── BaseDetector.java                # Shared utility methods
│       ├── StringEqualsDetector.java        # Rule 1: String.equals()
│       ├── StringEmptyCheckDetector.java    # Rule 2: String empty checks
│       ├── CollectionCheckDetector.java     # Rules 3-4: Collection checks (combined)
│       ├── TernaryNullCheckDetector.java    # Rule 5: Null defaults (ternary/if-else)
│       ├── BooleanUnboxingDetector.java     # Rule 6: Boolean unboxing
│       ├── LogNullDereferenceDetector.java  # Rule 8: Log null dereference
│       ├── DtoInitializationCheckDetector.java  # Rule 9: DTO initialization
│       └── NullValueInContextDetector.java  # Rules 7 & 10: Null in context/map
├── model/
│   ├── Violation.java                       # Violation data model
│   ├── ViolationType.java                   # All 10 violation types
│   └── ViolationSeverity.java               # ERROR or WARNING
├── startup/
│   └── OpenReadmeOnStartup.java             # Auto-open README on project load
├── ui/
│   ├── CodingLegionToolWindowFactory.java   # Tool window factory
│   └── ViolationTreePanel.java              # Main UI panel with violations display
└── utils/
    └── GitBranchChecker.java                # Git branch utilities
```

### Adding New Coding Standard Rules

The plugin is designed for easy extensibility. Adding new rules for any coding standard is simple:

**Step 1**: Create detector class extending `BaseDetector`:

```java
package com.codinglegion.analyzer.detectors;

import com.codinglegion.analyzer.ViolationDetector;
import com.codinglegion.model.Violation;
import com.intellij.psi.PsiElement;
import java.util.List;

public class MyCustomDetector extends BaseDetector implements ViolationDetector {
    
    @Override
    public List<Violation> detect(PsiElement element) {
        List<Violation> violations = new ArrayList<>();
        
        // Your detection logic here
        // Use inherited methods: getLineNumber(), getColumnNumber(), 
        // getClassName(), getPackageName()
        
        return violations;
    }
    
    @Override
    public String getName() {
        return "MyCustomDetector";
    }
}
```

**Step 2**: Add violation type to `ViolationType.java`:

```java
MY_CUSTOM_RULE(
    "Rule Title",
    "Detailed description explaining what's wrong and why",
    ViolationSeverity.ERROR,  // or WARNING
    "Suggested fix code"
)
```

**Step 3**: Register in `CodingLegionAnalyzer.java`:

```java
private List<ViolationDetector> initializeDetectors() {
    return Arrays.asList(
        // ... existing detectors ...
        new MyCustomDetector()  // Add here
    );
}
```

That's it! Your new rule is now active.

---

## 🏗️ Building the Plugin

### Build Plugin ZIP

```bash
./gradlew buildPlugin
```

Output: `build/distributions/coding-legion-<version>.zip`

### Run in Development

```bash
./gradlew runIde  # Launches test IntelliJ with plugin
```

### Clean Build

```bash
./gradlew clean build
```

---

## 👥 Team Distribution

### Method 1: Direct JAR Sharing (Easiest)

1. **Build once**:
   ```bash
   ./gradlew buildPlugin
   ```

2. **Share ZIP** from `build/distributions/` via:
    - Shared network drive
    - Internal repository
    - Email/Slack/Teams

3. **Team installs**:
    - `Settings` → `Plugins` → ⚙️ → `Install Plugin from Disk...`
    - Select ZIP file
    - Restart IntelliJ

### Method 2: Source Distribution

1. Share the repository
2. Team members build locally:
   ```bash
   ./gradlew clean buildPlugin
   ```
3. Install from `build/distributions/coding-legion-<version>.zip`

---

## 🚫 Troubleshooting

### "Cannot Run Analysis on Protected Branch"

**Cause**: You're on main/master/develop branch

**Solution**: Checkout a feature branch
```bash
git checkout -b feature/my-feature
```

### "No Changed Java Files Found"

**Cause**: No modified Java files in current branch or new files not staged

**Solution**:
- Make changes to at least one Java file
- For new files, stage them first: `git add YourNewFile.java`
- Then run the analysis again

### Tool Window Not Visible

**Solution**:
- `View` → `Tool Windows` → `Coding Legion`
- Or click "Coding Legion" tab at bottom
- Or run analysis (auto-opens window)

### Icons Not Appearing in Marketplace

**Solution**:
- Ensure `pluginIcon.svg` and `pluginIcon_dark.svg` are in `src/main/resources/META-INF/`
- Icons must be exactly 40x40 pixels
- Clear IntelliJ icon cache:
  ```bash
  rm -rf ~/Library/Caches/JetBrains/IntelliJIdea*/icons  # macOS
  ```
- Reinstall plugin after clearing cache

### Build Fails - Permission Denied

**Solution** (Linux/Mac):
```bash
chmod +x gradlew
```

### Build Fails - JAVA_HOME not set

**Solution**:
```bash
# Mac/Linux
export JAVA_HOME=/path/to/jdk-11

# Windows
set JAVA_HOME=C:\Path\To\jdk-11
```

---

## 🔮 Future Improvements

### Planned Features

#### Phase 1: Additional Null Check Rules
- [x] DTO initialization checks (isInitialized() pattern) - ✅ **Implemented in v1.6.0**
- [x] Context object null value detection - ✅ **Implemented in v1.6.0**
- [ ] Method parameter null validation (@NonNull, @Nullable annotations)
- [ ] Quick fixes (one-click apply suggestions)

#### Phase 2: Other Coding Standards
- [ ] Exception handling standards
- [ ] Logging standards
- [ ] Naming conventions
- [ ] Code complexity rules
- [ ] Documentation requirements

#### Phase 3: Advanced Features
- [ ] Data flow analysis for smarter detection
- [ ] Real-time inspection (as you type)
- [ ] Batch fix multiple violations
- [ ] Export violations to HTML/PDF/CSV

#### Phase 3: Configuration
- [ ] Enable/disable individual detectors
- [ ] Custom severity levels
- [ ] Project-specific rule configuration
- [ ] Custom rule builder GUI

#### Phase 4: Testing Integration
- [ ] Test case coverage analysis
- [ ] Boolean attribute test coverage (true/false/null)
- [ ] Missing null test detection

---

## 🎯 Best Practices

### ⚠️ Important Coding Principles (MANUAL REVIEW REQUIRED)

**IMPORTANT**: The principles in this section **CANNOT be automatically detected** by the plugin. They require manual code review, testing, and developer judgment.

The plugin automatically detects 9 types of violations (see [Violations Reference](#-violations-reference) above). However, some critical coding principles are too context-dependent or complex for automated detection. You must review these manually.

---

**Quick Reference: Automated vs Manual**

| Category | Automated Detection | Manual Review Required |
|----------|-------------------|----------------------|
| **Null Safety Patterns** | ✅ String.equals()<br/>✅ String empty checks<br/>✅ Collection checks<br/>✅ Ternary null defaults<br/>✅ DTO initialization<br/>✅ Null in context/maps | ❌ Error handling intent<br/>❌ Downstream logic effects<br/>❌ When to fail-fast vs defensive |
| **Boolean Handling** | ✅ Auto-unboxing detection | ❌ Test coverage (true/false/null)<br/>❌ Appropriate null defaults |
| **Logging** | ✅ Null dereference in logs | |

---

#### 🔴 Error Handling & Null Checks (NOT Auto-Detected)

**Why this isn't automated:** Requires understanding business logic and intent - static analysis cannot determine if error handling is correct or a bug.

When implementing null checks or error-handling guards, ensure their downstream effects are well understood. Neglecting this can unintentionally alter logic flow and cause unexpected behavior.

**Key Considerations:**
- **Intent**: Should this fail-fast (throw error) or handle gracefully (return/log)?
- **Downstream Impact**: Does this change existing behavior or fix a bug?
- **Logging**: Add appropriate logging when handling errors silently
- **Documentation**: Document why the null check/error handling exists

**Examples:**

```java
// ❌ BAD: Silent failure that hides bugs
public void chargeCustomer(Customer customer) {
    if (customer == null) return;  // Hides bug - should never be null!
    processPayment(customer.getAccount());
}

// ✅ GOOD: Explicit handling with logging
public void logActivity(User user) {
    if (user == null) {
        logger.warn("logActivity called with null user");
        return;  // Optional logging, so graceful handling is OK
    }
    logger.info("User: " + user.getName());
}

// ✅ GOOD: Fail-fast for critical operations
public void chargeCustomer(Customer customer) {
    Objects.requireNonNull(customer, "Customer cannot be null");
    processPayment(customer.getAccount());
}
```

---

#### 🔴 Boolean Handling & Testing (NOT Auto-Detected)

**Why this isn't automated:** Test coverage and default value appropriateness require business context that static analysis cannot determine.

**Rule**: Always treat boolean values strictly as `true` or `false`. If the value is `null`, handle it by defaulting to either `true` or `false` as appropriate.

**Testing Requirements:**
- Add test cases to handle null scenarios
- Any Boolean attribute must have test cases covering:
  - `true` scenario
  - `false` scenario
  - `null` scenario

**Examples:**

```java
// ✅ GOOD: Explicit null handling with default
public boolean isFeatureEnabled(Boolean featureFlag) {
    return BooleanUtils.isTrue(featureFlag);  // null → false
}

public boolean isOptOut(Boolean optOutFlag) {
    return BooleanUtils.isNotFalse(optOutFlag);  // null → true (opt-out by default)
}

// ✅ GOOD: Test coverage for all scenarios
@Test
public void testFeatureFlag() {
    assertTrue(isFeatureEnabled(Boolean.TRUE));    // true case
    assertFalse(isFeatureEnabled(Boolean.FALSE));   // false case
    assertFalse(isFeatureEnabled(null));            // null case → default to false
}
```

**Why This Matters:**
- Prevents unexpected `NullPointerException` from auto-unboxing
- Makes default behavior explicit and testable
- Ensures consistent behavior across the application
- Documents intent clearly (null means true vs null means false)

---

### When to Run

✅ **Good Times**:
- Before committing code
- After implementing a feature
- During code review
- Before creating pull request

❌ **Won't Work**:
- On main/master/develop branches
- When no Java files changed
- Outside Git repositories

### Fixing Efficiently

1. **Stage New Files**: Always `git add` new Java files before analysis
2. **Errors First**: Fix all errors in ⚠ tab before warnings
3. **Use Suggested Fixes**: Copy-paste the grey suggested code
4. **Batch Similar**: Fix all of same type together
5. **Quick Re-run**: Click 🔄 button in header for instant re-analysis (preserves current tab)
6. **Zero Violations**: Keep violations at zero before commit

**💡 Pro Tip**: After fixing violations, use the 🔄 button for quick re-analysis without losing your place. It keeps you on the same tab (Errors or Warnings) you're currently working with!

---

## 📧 Support & Contact

- **Issues**: Report in repository issues
- **Author**: Saptarshi Kundu
- **Guidelines**: Check team coding guidelines for standards

---

## 🤝 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Make changes
4. Test: `./gradlew test`
5. Build: `./gradlew buildPlugin`
6. Commit: `git commit -m 'Add amazing feature'`
7. Push and create Pull Request

### Code Style

- Follow existing patterns
- Add JavaDoc for public methods
- Keep detectors focused
- Update this README for new features

---

## 📄 License

This project is licensed under the MIT License.

---

## 🙏 Acknowledgments

- Apache Commons Lang3 - For StringUtils, ObjectUtils, BooleanUtils patterns
- Apache Commons Collections4 - For CollectionUtils patterns
- IntelliJ Platform SDK - For plugin framework
- JetBrains - For the amazing IDE

---

## 📜 Copyright

**© 2025 Saptarshi Kundu. All Rights Reserved.**

Developed by Saptarshi Kundu for enforcing coding standards in Java projects.

---

<div align="center">

**Coding Legion**  
*Making your code null-safe, one violation at a time.*

Developed by Saptarshi Kundu

</div>
