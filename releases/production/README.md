# Production Release Builds

This directory will contain production-ready builds for Google Play Store submission.

## Current Status

⚠️ Production builds require a release keystore for proper signing. Once configured, production builds will be available here.

## What's in a Production Build?

- Optimized with ProGuard/R8 (smaller size ~3MB)
- Signed with release keystore
- Android App Bundle (AAB) for Play Store
- Release APK for sideloading
- All Play Store compliance checks passed

## Release Process

Production releases are created when a version tag is pushed:

```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

This triggers the release pipeline which:
1. Runs all tests and security checks
2. Builds signed release APK and AAB
3. Creates a GitHub Release with artifacts
4. (Future) Uploads to Google Play Console

## Play Store Preparation

See [PLAY_STORE_PREPARATION.md](../PLAY_STORE_PREPARATION.md) for the complete checklist before submitting to the Play Store.
