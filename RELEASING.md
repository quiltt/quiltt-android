# Releasing the Quiltt Android SDK

This guide explains how to cut new releases of the Android SDK using our automated release process.

## Overview

The Quiltt Android SDK uses **label-based automated releases**. When you merge a PR with a release label, the system automatically:

- Bumps the version number
- Updates the version files
- Publishes to Maven Central
- Creates a GitHub release
- Tags the release

**No manual commands needed!** 🎉

## Release Types

We follow [Semantic Versioning](https://semver.org/) (semver):

| Label | Version Change | When to Use |
|-------|---------------|-------------|
| `release:patch` | `1.0.2 → 1.0.3` | Bug fixes, documentation updates |
| `release:minor` | `1.0.2 → 1.1.0` | New features, enhancements (backward compatible) |
| `release:major` | `1.0.2 → 2.0.0` | Breaking changes, major API changes |

## How to Release

### Step 1: Create Your PR

Create a pull request with your changes as usual:

```bash
git checkout -b fix/webview-memory-leak
# Make your changes...
git commit -m "fix: resolve WebView memory leak in configuration changes"
git push origin fix/webview-memory-leak
```

### Step 2: Add Release Label

**Before merging**, add the appropriate release label to your PR:

1. Go to your PR on GitHub
2. Click the **Labels** section on the right side
3. Select the appropriate release label:
   - 🐛 **`release:patch`** - for bug fixes
   - ✨ **`release:minor`** - for new features  
   - 💥 **`release:major`** - for breaking changes

### Step 3: Get PR Reviewed and Merged

Follow normal review process, then merge the PR. **The release happens automatically on merge!**

### Step 4: Verify Release

After merging, check:

- ✅ **GitHub Actions**: Go to Actions tab, verify "Automated Release and Publish" succeeds
- ✅ **Maven Central**: Check [Maven Central](https://search.maven.org/artifact/app.quiltt/connector) for new version
- ✅ **GitHub Releases**: Check the [Releases page](../../releases) for the new release
- ✅ **PR Comment**: The automation will comment on your PR with success details

## Examples

### Bug Fix Release

```md
PR: "Fix WebView memory leak in configuration changes"
Label: release:patch
Result: 1.0.2 → 1.0.3
```

### New Feature Release  

```md
PR: "Add support for Jetpack Compose integration"
Label: release:minor  
Result: 1.0.2 → 1.1.0
```

### Breaking Change Release

```md
PR: "Remove deprecated authenticate() method signature"
Label: release:major
Result: 1.0.2 → 2.0.0
```

## What Happens Automatically

When you merge a labeled PR, the automation:

1. **Detects the release label** on your merged PR
2. **Calculates new version** based on current version + label type
3. **Updates version files**:
   - `connector/build.gradle.kts`
   - `connector/src/main/java/app/quiltt/connector/QuilttSdkVersion.kt`
4. **Commits changes** back to main branch
5. **Creates Git tag** (e.g., `v1.0.3`)
6. **Builds and validates** Android library
7. **Publishes to Maven Central** automatically
8. **Creates GitHub release** with release notes
9. **Comments on your PR** with success details

## Troubleshooting

### No Release Happened

**Problem**: Merged PR but no release was created.
**Solution**: Check that your PR had a `release:*` label before merging.

### Release Failed

**Problem**: GitHub Actions shows red X on release workflow.
**Solution**:

1. Check the Actions logs for specific error
2. Common issues:
   - Gradle build failures
   - Missing Maven Central credentials
   - Android Gradle Plugin compatibility issues
   - ProGuard/R8 configuration problems

### Maven Central Publishing Failed

**Problem**: GitHub release created but package not on Maven Central.
**Solution**:

1. Check Sonatype staging repository status
2. Verify signing keys and credentials are valid
3. Check for naming conflicts or validation errors
4. May need manual intervention in Sonatype OSSRH

### Wrong Version Number

**Problem**: Released wrong version type (e.g., minor instead of patch).
**Solution**:

1. Create a new PR to fix any issues
2. Use the correct label for the follow-up release
3. The version number will continue from the previous release

### Manual Release Needed

**Problem**: Need to release without a PR (hotfix, emergency).
**Solution**: Create a minimal PR with the fix and proper label, then merge.

## Best Practices

### ✅ Do

- **Always add release labels** before merging
- **Use descriptive PR titles** (they become release notes)
- **Test your changes** thoroughly on multiple Android versions before labeling for release
- **Use patch releases** for bug fixes and documentation
- **Use minor releases** for new features and Android enhancements
- **Use major releases** sparingly, only for breaking API changes

### ❌ Don't

- **Don't merge without a release label** if you intend to release
- **Don't use major releases** for non-breaking changes
- **Don't manually edit version files** (automation handles this)
- **Don't create manual Git tags** (automation handles this)
- **Don't release without testing** on physical Android devices when possible

## Emergency Releases

For urgent hotfixes:

1. **Create hotfix branch** from main
2. **Make minimal fix** focused on the critical issue
3. **Create PR** with clear title describing the emergency
4. **Add `release:patch` label**
5. **Get expedited review** from team
6. **Merge immediately**

The automation will handle the emergency release within minutes.

## Platform-Specific Considerations

### Android Release Checklist

- ✅ **Test on multiple Android versions** (API 26+)
- ✅ **Verify on physical devices** when possible
- ✅ **Check App Link handling** for OAuth flows
- ✅ **Validate memory management** in WebView usage
- ✅ **Test configuration changes** (rotation, multi-window)
- ✅ **Test accessibility features**
- ✅ **Verify ProGuard/R8 compatibility**

### Gradle Considerations

- ✅ **Ensure Gradle builds successfully**
- ✅ **Verify all Android API levels build**
- ✅ **Check dependency compatibility**
- ✅ **Validate AAR generation**
- ✅ **Test with different AGP versions**

### Maven Central Requirements

- ✅ **Valid POM file generation**
- ✅ **Proper artifact signing**
- ✅ **Complete Javadoc/KDoc**
- ✅ **Source JAR inclusion**
- ✅ **License and developer information**

## Getting Help

- **GitHub Actions failing?** Check the [Actions tab](../../actions) for detailed logs
- **Maven Central issues?** Check [Sonatype OSSRH](https://oss.sonatype.org/) status
- **Gradle build problems?** Verify your Android development environment
- **Questions about semver?** See [Semantic Versioning](https://semver.org/)
- **Need help?** Ask in #engineering-android or create an issue

## Current Version

You can always check the current version:

- **build.gradle.kts**: `version = "X.Y.Z"`
- **QuilttSdkVersion.kt**: `val quilttSdkVersion = "X.Y.Z"`
- **Maven Central**: [Latest version](https://search.maven.org/artifact/app.quiltt/connector)
- **GitHub**: [Latest release](../../releases/latest)

## Release History

Major releases and their breaking changes:

### v2.0.0 (Future)

- TBD - Major API restructuring

### v1.0.0 (Current)

- Initial stable release
- Android API 26+ support
- View-based and Jetpack Compose integration
- App Links support
- Maven Central distribution

## Maven Central Publication

The SDK is published to Maven Central with these coordinates:

```gradle
dependencies {
    implementation("app.quiltt:connector:X.Y.Z")
}
```

### Publication Details

- **Group ID**: `app.quiltt`
- **Artifact ID**: `connector`
- **Repository**: [Maven Central](https://search.maven.org/artifact/app.quiltt/connector)
- **License**: MIT
- **Source Code**: Included as source JAR
- **Documentation**: Included as Javadoc JAR

---

Happy releasing! 🚀
