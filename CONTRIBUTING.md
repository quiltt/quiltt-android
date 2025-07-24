# Contributing to Quiltt Android SDK

First off, thank you for considering contributing to the Quiltt Android SDK. It's people like you that make Quiltt such a great tool for developers in the fintech space. We welcome contributions from everyone as part of our mission to build powerful, user-friendly tools for financial technology applications on Android.

## Getting Started

Before you begin, please ensure you have:

- A GitHub account
- Android development environment set up (Android Studio 2024.1+, Android API 26+)
- Familiarity with the Quiltt documentation at [https://quiltt.dev](https://quiltt.dev)
- Understanding of Gradle build system and WebView integration

Understanding Quiltt's core concepts and the Android SDK's architecture will help you make meaningful contributions.

## Development Setup

1. **Clone the repository:**

   ```bash
   git clone https://github.com/quiltt/quiltt-android.git
   cd quiltt-android
   ```

2. **Open in Android Studio:**

   ```bash
   # Open the project in Android Studio
   studio .
   # or
   open -a "Android Studio" .
   ```

3. **Set up example apps:**

   The project includes two example applications:
   - `app` - Traditional View-based example
   - `app_jetpack_compose` - Jetpack Compose example

4. **Install Fastlane (for releases):**

   ```bash
   bundle install
   ```

## Project Structure

```txt
├── connector/                    # Main SDK library module
│   ├── src/main/java/app/quiltt/connector/
│   │   ├── QuilttConnector.kt           # Main SDK entry point
│   │   ├── QuilttConnectorConfiguration.kt # Configuration classes
│   │   ├── QuilttConnectorEvent.kt      # Event callback definitions
│   │   ├── QuilttConnectorWebView.kt    # WebView implementation
│   │   ├── UrlUtils.kt                  # URL encoding utilities
│   │   └── QuilttSdkVersion.kt          # Version information
│   └── build.gradle.kts          # Library build configuration
├── app/                          # Example app (View-based)
├── app_jetpack_compose/          # Example app (Compose)
├── fastlane/                     # Release automation
└── .github/workflows/            # CI/CD pipelines
```

## Ways to Contribute

There are many ways to contribute to the Quiltt Android SDK:

### Reporting Bugs

- **Use the GitHub Issues tracker** to submit bug reports
- **Search existing issues** to avoid duplicates
- **Provide detailed information:**
  - Android Studio and Gradle versions
  - Android API levels being tested
  - Device models and manufacturers
  - Steps to reproduce the issue
  - Expected vs actual behavior
  - Logcat output or error messages
  - Connector ID (if applicable) for debugging

**Bug Report Template:**

```md
**Describe the bug**
A clear description of what the bug is.

**To Reproduce**
Steps to reproduce the behavior:
1. Configure SDK with '...'
2. Call method '...'
3. See error

**Expected behavior**
What you expected to happen.

**Environment:**
- Android Studio version: [e.g. 2024.1.1]
- Gradle version: [e.g. 8.7]
- Android Gradle Plugin: [e.g. 8.5.0]
- Kotlin version: [e.g. 1.9.20]
- Target Android API: [e.g. API 34]
- Min Android API: [e.g. API 26]
- Device: [e.g. Pixel 7, Samsung Galaxy S23]
- Android version: [e.g. Android 14]
- SDK version: [e.g. 1.0.3]

**Logcat output**
```
Include relevant logcat output here
```

**Additional context**
Any other context about the problem.
```

### Feature Requests

- **Use the GitHub Issues tracker** for feature requests
- **Search existing requests** to avoid duplicates
- **Provide clear explanations:**
  - Use case and business justification
  - Proposed API design
  - Examples of how it would work
  - Consideration for different Android versions

### Platform Support

We welcome contributions to extend platform support:

- **Jetpack Compose improvements** - Enhanced Compose integration patterns
- **Material Design 3** - Modern Material You theming support
- **Foldable devices** - Large screen and foldable device optimizations
- **Android TV** - Android TV platform support
- **Wear OS** - Smartwatch integration possibilities

## Submitting Code Changes

### Development Workflow

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:

   ```bash
   git clone https://github.com/YOUR_USERNAME/quiltt-android.git
   ```

3. **Create a feature branch:**

   ```bash
   git checkout -b feature/your-feature-name
   ```

4. **Make your changes** following our coding standards
5. **Test your changes** thoroughly
6. **Commit with descriptive messages:**

   ```bash
   git commit -m "Add support for foldable device configurations"
   ```

7. **Push to your fork:**

   ```bash
   git push origin feature/your-feature-name
   ```

8. **Submit a pull request** against the `main` branch

### Coding Standards

- **Follow Kotlin conventions** from the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- **Use meaningful variable names** and add KDoc comments for public APIs
- **Maintain consistency** with existing code patterns
- **Handle errors gracefully** with proper exception handling
- **Add documentation** for public APIs using KDoc
- **Test on multiple Android versions** (API 26+)
- **Follow Android development best practices**

### Kotlin Coding Conventions

```kotlin
// Use clear, descriptive names
class QuilttConnectorWebView(context: Context) : WebView(context) {
    // Use proper visibility modifiers
    private var config: QuilttConnectorConfiguration? = null
    
    /**
     * Loads the connector with the provided configuration
     * @param token The session token for authentication
     * @param config The connector configuration
     */
    fun load(
        token: String?,
        config: QuilttConnectorConfiguration,
        onEvent: ConnectorSDKOnEventCallback? = null
    ) {
        // Implementation
    }
    
    // Use proper error handling
    private fun handleOAuthUrl(url: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url)
            context.startActivity(intent)
        } catch (error: Exception) {
            Log.e(TAG, "Failed to open URL", error)
        }
    }
}
```

### Commit Message Format

We prefer clear, descriptive commit messages that start with action verbs:

- `Add` - New features or functionality
- `Fix` - Bug fixes
- `Update` - Changes to existing features
- `Refactor` - Code restructuring without functional changes
- `Remove` - Deleting code or features
- `Improve` - Enhancements to existing functionality

**Examples:**

- `Add support for Android 14 predictive back gestures`
- `Fix WebView memory leak in configuration changes`
- `Update UrlUtils to handle edge cases`
- `Refactor connector initialization logic`

**Note:** Release versioning is handled automatically via PR labels, not commit message conventions.

### Testing

- **Test your changes** on multiple Android versions (API 26+)
- **Run the example apps** to verify functionality
- **Test with multiple connectors** if possible (Plaid, Finicity, etc.)
- **Verify App Link flows** work correctly
- **Check for memory leaks** in WebView handling
- **Test on physical devices** when possible
- **Validate accessibility** features work properly
- **Test configuration changes** (rotation, multi-window)

### Pull Request Guidelines

**Pull Request Template:**

See [pull_request_template.md](./.github/pull_request_template.md)

## Release Process

The project uses **automated label-based releases**:

- **Patch releases** (`release:patch` label) - Bug fixes, documentation updates
- **Minor releases** (`release:minor` label) - New features, enhancements  
- **Major releases** (`release:major` label) - Breaking changes, major API changes

### How to Trigger a Release

1. **Create your PR** with changes
2. **Add appropriate release label** before merging:
   - `release:patch` for bug fixes
   - `release:minor` for new features
   - `release:major` for breaking changes
3. **Merge the PR** - Release happens automatically!

The automation will:

- Calculate new version number
- Update `build.gradle.kts` and `QuilttSdkVersion.kt`
- Publish to Maven Central
- Create GitHub release with release notes
- Tag the release

**No manual commands needed!** See [RELEASING.md](RELEASING.md) for detailed instructions.

Contributors should indicate the type of change in their PRs using the pull request template.

## Code Review Process

1. **Automated checks** run via GitHub Actions
2. **Build verification** on multiple Android API levels
3. **Gradle build validation**
4. **Maintainer review** for code quality and architecture
5. **Testing verification** on Android devices and emulators
6. **Feedback and iteration** if changes are needed
7. **Approval and merge** once requirements are met

Reviews help ensure code quality, consistency, and maintainability. Please be open to feedback and discussion.

## Android Development Guidelines

### Gradle Configuration

- **Keep dependencies minimal** - Avoid unnecessary external dependencies
- **Use version catalogs** - Manage dependency versions centrally
- **Target latest stable API** - Stay current with Android releases
- **Proper module organization** - Separate library from example apps

### WebView Integration

- **Follow WebView best practices** - Proper lifecycle management
- **Handle navigation carefully** - Security considerations for financial apps
- **Memory management** - Avoid memory leaks with proper cleanup
- **JavaScript injection** - Secure script evaluation practices

### Android Architecture

- **Follow Android architecture patterns** - Proper separation of concerns
- **Handle configuration changes** - Support device rotation and multi-window
- **Lifecycle awareness** - Proper Activity/Fragment lifecycle handling
- **Background processing** - Proper handling of background tasks

## Community Guidelines

We want to foster an inclusive and friendly community around the Quiltt Android SDK. We expect everyone to:

- **Be respectful** in all interactions
- **Provide constructive feedback** during code reviews
- **Help newcomers** get started with contributions
- **Share knowledge** about Android development and fintech integration
- **Follow Google's Android community guidelines** and development best practices

## Questions and Support

- **GitHub Issues** - For bugs and feature requests
- **GitHub Discussions** - For questions and community support
- **Pull Request comments** - For code-specific questions
- **Documentation** - Check [quiltt.dev](https://quiltt.dev) for SDK guides

## Android Developer Resources

- **Kotlin Documentation** - [kotlinlang.org/docs](https://kotlinlang.org/docs/)
- **Android Developer Guides** - [developer.android.com/guide](https://developer.android.com/guide)
- **Material Design** - [material.io/develop/android](https://material.io/develop/android)
- **App Links** - [developer.android.com/training/app-links](https://developer.android.com/training/app-links)
- **WebView** - [developer.android.com/guide/webapps/webview](https://developer.android.com/guide/webapps/webview)

## Thank You

Thank you for your interest in contributing to the Quiltt Android SDK! Together, we can make financial technology integration easier and more accessible for Android developers worldwide.
