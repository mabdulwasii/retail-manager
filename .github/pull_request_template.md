## Description

<!-- Provide a brief description of the changes in this PR -->

## Type of Change

<!-- Check all that apply -->

- [ ] Bug fix (non-breaking change which fixes an issue)
- [ ] New feature (non-breaking change which adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] Refactoring (code change that neither fixes a bug nor adds a feature)
- [ ] Documentation update
- [ ] Performance improvement
- [ ] Test improvements

## Components Changed

<!-- Check all that apply -->

- [ ] Backend
- [ ] Frontend
- [ ] Helm Chart
- [ ] GitHub Actions
- [ ] Documentation

## Testing

<!-- Describe the tests you ran to verify your changes -->

- [ ] Unit tests pass (`./mvnw test`)
- [ ] Integration tests pass (`./mvnw verify -Pintegration-tests`)
- [ ] Manual testing completed
- [ ] Added new tests for new functionality

## Release Labels

<!-- **IMPORTANT**: Add ONE of these labels to trigger automatic release when this PR is merged -->

### Backend Release (if backend code changed)
- `release:backend-patch` - Bug fixes, minor updates (0.0.X)
- `release:backend-minor` - New features, backwards compatible (0.X.0)
- `release:backend-major` - Breaking changes (X.0.0)

### Frontend Release (if frontend code changed)
- `release:frontend-patch` - Bug fixes, minor UI updates (0.0.X)
- `release:frontend-minor` - New features, new pages (0.X.0)
- `release:frontend-major` - Complete redesign, breaking changes (X.0.0)

### Both Components Release (if both changed)
- `release:both-patch` - Bug fixes in both (0.0.X)
- `release:both-minor` - New features in both (0.X.0)
- `release:both-major` - Breaking changes in both (X.0.0)

### No Release
- If this PR doesn't require a release (docs only, tests only, etc.), **don't add any release label**

## Checklist

<!-- Check all that apply before requesting review -->

- [ ] My code follows the project's style guidelines
- [ ] I have performed a self-review of my code
- [ ] I have commented my code, particularly in hard-to-understand areas
- [ ] I have updated the documentation accordingly
- [ ] My changes generate no new warnings
- [ ] I have added tests that prove my fix is effective or that my feature works
- [ ] New and existing unit tests pass locally with my changes
- [ ] Any dependent changes have been merged and published
- [ ] I have updated the permission-matrix.csv if adding/modifying API endpoints
- [ ] I have created a migration file if database schema changed (never modified existing migrations)

## Breaking Changes

<!-- If this is a breaking change, describe the migration path for existing users -->

N/A

## Additional Notes

<!-- Add any additional context, screenshots, or information here -->
