# CI/CD and Testing Framework for Google Play Store Preparation

This directory contains the GitHub Actions workflows for automated testing, building, and deployment preparation for Google Play Store.

## Workflows

1. **main.yml** - Main CI/CD pipeline for testing and building
2. **release.yml** - Release pipeline for Play Store deployment
3. **security.yml** - Security and code quality checks

## Test Coverage Requirements

- Unit tests: 80% minimum coverage
- Integration tests for all user interaction flows
- Edge case testing for game mechanics
- Network/multiplayer functionality validation
- Audio system validation
- Save/load system validation

## Play Store Preparation Checklist

- [ ] All tests passing
- [ ] Release build optimized
- [ ] ProGuard/R8 configuration
- [ ] Signing configuration
- [ ] Privacy policy compliance
- [ ] Content rating requirements
- [ ] Performance benchmarks met