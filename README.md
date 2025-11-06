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
- [Violations Reference](#-violations-reference)
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
- **Background Processing**: Non-blocking, fast analysis that doesn't interrupt your workflow
- **Incremental Scanning**: Efficient detection focused on your changes

### Comprehensive Detection
- **Errors**: Detects definite coding standard violations that must be fixed
- **Warnings**: Flags potential coding standard concerns that should be reviewed
- **7 Null Check Rules**: Currently implements string handling, collections, objects, booleans, and logging
- **Extensible Architecture**: Easily add new rules for other coding standards

### Developer-Friendly UI
- **Clickable Violations**: Click blue links to jump directly to exact code location (line:column)
- **Tabbed Results**: Separate tabs for Errors and Warnings
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
   # Output: build/distributions/manh-coding-legion-1.0.0.zip
   ```

2. **Install in IntelliJ**:
   - Open IntelliJ IDEA
   - Go to `File` → `Settings` → `Plugins`
   - Click gear icon (⚙️) → `Install Plugin from Disk...`
   - Select `manh-coding-legion-1.0.0.zip`
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
   cd manh-coding-legion
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
🏛️ Coding Legion                    📄 Docs
```
- Click **📄 Docs** to open this comprehensive README in IntelliJ editor

**Tabs**:
- **⚠ Errors (X)** - Definite violations that must be fixed
- **⚡ Warnings (X)** - Potential issues to review

**Violation Display Format**:
```
1. com.manh.cp.promising.metrics.ShipmentBucket.java:46:21 - Rule: Use StringUtils.equals(a, b) instead of a.equals(b) to prevent NullPointerException when 'a' is null.
   Suggested: StringUtils.equals(a, b)
```

### Workflow Integration

```
1. Create Feature Branch
   ↓
2. Write Code  
   ↓
3. Run Analysis (Tools menu or right-click)
   ↓
4. Review Violations in Tool Window
   ↓
5. Fix and Re-analyze
   ↓
6. Commit Clean Code
```

---

## 🔍 Violations Reference

Complete reference for all null check violations currently implemented.

### Current Coverage: Null Check Standards

The plugin currently enforces null-safe coding practices using Apache Commons and Spring utilities. Additional coding standards can be added easily using the extensible detector framework.

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

#### 3. Manual Collection Null Check

**What it detects**: `collection == null || collection.isEmpty()`

**Why**: Verbose when utility method exists

**Bad**:
```java
if (list == null || list.isEmpty()) {
    return;
}
```

**Good**:
```java
import org.apache.commons.collections4.CollectionUtils;

if (CollectionUtils.isEmpty(list)) {
    return;
}
```

**Fix**: `CollectionUtils.isEmpty(collection)`

---

#### 4. Collection Size Comparison

**What it detects**: `collection.size() == 0` or `collection.size() > 0`

**Why**: Less semantic, doesn't handle null

**Bad**:
```java
if (items.size() == 0) {
    return "No items";
}

if (items.size() > 0) {
    processItems();
}
```

**Good**:
```java
import org.apache.commons.collections4.CollectionUtils;

if (CollectionUtils.isEmpty(items)) {
    return "No items";
}

if (!CollectionUtils.isEmpty(items)) {
    processItems();
}
```

**Fix**: `CollectionUtils.isEmpty(collection)` or `!CollectionUtils.isEmpty(collection)`

---

### ⚡ WARNINGS (Should Review)

#### 5. Ternary Null Default Pattern

**What it detects**: `obj != null ? obj : defaultValue`

**Why**: Less expressive than utility method

**Warning**:
```java
String value = config != null ? config : "default";
```

**Better**:
```java
import org.apache.commons.lang3.ObjectUtils;

String value = ObjectUtils.defaultIfNull(config, "default");
```

**Fix**: `ObjectUtils.defaultIfNull(obj, defaultValue)`

---

#### 6. Boolean Auto-Unboxing Risk

**What it detects**: `boolean val = booleanWrapper`

**Why**: Auto-unboxing throws NPE if wrapper is null

**Warning**:
```java
Boolean isActive = getStatus();
boolean active = isActive;  // NPE if isActive is null!
```

**Better**:
```java
import org.apache.commons.lang3.BooleanUtils;

Boolean isActive = getStatus();
boolean active = BooleanUtils.isTrue(isActive);  // Safe, defaults to false
```

**Fix**: `BooleanUtils.isTrue(boolObj)` or `Boolean.TRUE.equals(boolObj)`

---

#### 7. Potential Null Dereference in Log Statement

**What it detects**: Method calls on objects in log statements

**Why**: May throw NPE in logging code

**Warning**:
```java
log.info("Client: {}", client.getName());  // NPE if client is null
```

**Better**:
```java
log.info("Client: {}", client != null ? client.getName() : null);
```

**Fix**: `log.info("...", obj != null ? obj.method() : null)`

---

## 📊 Violations Summary

| # | Violation | Severity | Fix Complexity | Frequency |
|---|-----------|----------|----------------|-----------|
| 1 | String.equals() | ⚠ ERROR | Easy | Very Common |
| 2 | Manual string null/empty | ⚠ ERROR | Easy | Common |
| 3 | Manual collection null | ⚠ ERROR | Easy | Common |
| 4 | Collection.size() | ⚠ ERROR | Easy | Common |
| 5 | Ternary null default | ⚡ WARNING | Easy | Occasional |
| 6 | Boolean unboxing | ⚡ WARNING | Easy | Rare |
| 7 | Log null dereference | ⚡ WARNING | Medium | Occasional |

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
├── actions/RunNullCheckAction.java          # Main action handler
├── analyzer/
│   ├── NullCheckAnalyzer.java               # Orchestrates all detectors
│   ├── ViolationDetector.java               # Base detector interface
│   └── detectors/
│       ├── BaseDetector.java                # Shared utility methods
│       ├── StringEqualsDetector.java        # Detects string.equals()
│       ├── StringEmptyCheckDetector.java    # Detects manual null checks
│       ├── CollectionNullCheckDetector.java # Detects collection null checks
│       ├── CollectionSizeDetector.java      # Detects size() comparisons
│       ├── TernaryNullCheckDetector.java    # Detects ternary patterns
│       ├── BooleanUnboxingDetector.java     # Detects unboxing risks
│       └── LogNullDereferenceDetector.java  # Detects log dereferences
├── model/
│   ├── Violation.java                       # Violation data model
│   ├── ViolationType.java                   # All violation types
│   └── ViolationSeverity.java               # ERROR or WARNING
├── ui/
│   ├── NullCheckToolWindowFactory.java      # Tool window factory
│   └── ViolationTreePanel.java              # Main UI panel
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

**Step 3**: Register in `NullCheckAnalyzer.java`:

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

Output: `build/distributions/manh-coding-legion-1.0.0.zip`

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
2. Team members build locally
3. Install from built ZIP

---

## 🚫 Troubleshooting

### "Cannot Run Analysis on Protected Branch"

**Cause**: You're on main/master/develop branch

**Solution**: Checkout a feature branch
```bash
git checkout -b feature/my-feature
```

### "No Changed Java Files Found"

**Cause**: No modified Java files in current branch

**Solution**: Make changes to at least one Java file

### Tool Window Not Visible

**Solution**:
- `View` → `Tool Windows` → `Coding Legion`
- Or click "Coding Legion" tab at bottom
- Or run analysis (auto-opens window)

### Cursor Appearing in Text

**Solution**: This should not happen. If it does:
- Reinstall the plugin
- Clear IntelliJ caches: `File` → `Invalidate Caches / Restart`

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
- [ ] DTO initialization checks (isInitialized() pattern)
- [ ] Context object null value detection  
- [ ] Method parameter null validation
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

1. **Errors First**: Fix all errors in ⚠ tab before warnings
2. **Use Suggested Fixes**: Copy-paste the grey suggested code
3. **Batch Similar**: Fix all of same type together
4. **Re-run**: Verify fixes worked
5. **Zero Violations**: Keep violations at zero before commit

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

[Documentation](README.md)

</div>
