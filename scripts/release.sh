#!/usr/bin/env bash
set -euo pipefail

# ================================================================
# Manual Release Helper Script
# ================================================================
# Usage:
#   ./scripts/release.sh backend patch
#   ./scripts/release.sh frontend minor
#   ./scripts/release.sh both major
# ================================================================

COMPONENT="${1:-}"
BUMP_TYPE="${2:-}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# ================================================================
# Validate Input
# ================================================================
if [ -z "$COMPONENT" ] || [ -z "$BUMP_TYPE" ]; then
  echo -e "${RED}Error: Missing required arguments${NC}"
  echo ""
  echo "Usage: $0 <component> <bump-type>"
  echo ""
  echo "Components:"
  echo "  backend   - Release backend only"
  echo "  frontend  - Release frontend only"
  echo "  both      - Release both backend and frontend"
  echo ""
  echo "Bump Types:"
  echo "  patch     - Bug fixes (0.0.X)"
  echo "  minor     - New features (0.X.0)"
  echo "  major     - Breaking changes (X.0.0)"
  echo ""
  exit 1
fi

if [[ ! "$COMPONENT" =~ ^(backend|frontend|both)$ ]]; then
  echo -e "${RED}Error: Invalid component '$COMPONENT'${NC}"
  echo "Must be: backend, frontend, or both"
  exit 1
fi

if [[ ! "$BUMP_TYPE" =~ ^(patch|minor|major)$ ]]; then
  echo -e "${RED}Error: Invalid bump type '$BUMP_TYPE'${NC}"
  echo "Must be: patch, minor, or major"
  exit 1
fi

# ================================================================
# Helper Functions
# ================================================================
bump_version() {
  local current_version="$1"
  local bump_type="$2"

  IFS='.' read -r major minor patch <<< "$current_version"

  case "$bump_type" in
    major)
      major=$((major + 1))
      minor=0
      patch=0
      ;;
    minor)
      minor=$((minor + 1))
      patch=0
      ;;
    patch)
      patch=$((patch + 1))
      ;;
  esac

  echo "$major.$minor.$patch"
}

release_component() {
  local component="$1"
  local bump_type="$2"

  local version_file="${component}/VERSION"

  if [ ! -f "$version_file" ]; then
    echo -e "${RED}Error: $version_file not found${NC}"
    exit 1
  fi

  local current_version
  current_version=$(cat "$version_file")

  local new_version
  new_version=$(bump_version "$current_version" "$bump_type")

  echo -e "${GREEN}Releasing $component${NC}"
  echo "  Current version: $current_version"
  echo "  New version: $new_version"
  echo "  Bump type: $bump_type"
  echo ""

  # Update VERSION file
  echo "$new_version" > "$version_file"

  # Stage VERSION file
  git add "$version_file"

  # Commit VERSION file
  git commit -m "chore($component): bump version to $new_version"

  # Create annotated tag
  git tag -a "${component}-v${new_version}" -m "${component^} v${new_version}

Released via manual script
Bump type: $bump_type"

  echo -e "${GREEN}✓ Created ${component}-v${new_version}${NC}"
  echo ""
}

# ================================================================
# Pre-flight Checks
# ================================================================
echo -e "${YELLOW}Pre-flight Checks${NC}"

# Check if we're on main branch
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [ "$CURRENT_BRANCH" != "main" ]; then
  echo -e "${RED}Warning: You are not on the main branch (current: $CURRENT_BRANCH)${NC}"
  read -p "Continue anyway? (y/N): " -n 1 -r
  echo
  if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Aborted."
    exit 1
  fi
fi

# Check for uncommitted changes
if ! git diff-index --quiet HEAD --; then
  echo -e "${RED}Error: You have uncommitted changes${NC}"
  echo "Please commit or stash your changes before releasing."
  exit 1
fi

# Check if we can push
if ! git remote -v | grep -q origin; then
  echo -e "${RED}Error: No 'origin' remote configured${NC}"
  exit 1
fi

echo -e "${GREEN}✓ Pre-flight checks passed${NC}"
echo ""

# ================================================================
# Release
# ================================================================
if [ "$COMPONENT" = "both" ]; then
  release_component "backend" "$BUMP_TYPE"
  release_component "frontend" "$BUMP_TYPE"
elif [ "$COMPONENT" = "backend" ]; then
  release_component "backend" "$BUMP_TYPE"
elif [ "$COMPONENT" = "frontend" ]; then
  release_component "frontend" "$BUMP_TYPE"
fi

# ================================================================
# Push Changes
# ================================================================
echo -e "${YELLOW}Ready to push changes and tags${NC}"
echo ""
echo "This will:"
echo "  1. Push commits to origin/$CURRENT_BRANCH"
echo "  2. Push new tags to origin"
echo "  3. Trigger GitHub Actions workflows"
echo ""
read -p "Push now? (y/N): " -n 1 -r
echo

if [[ $REPLY =~ ^[Yy]$ ]]; then
  # Push commits
  git push origin "$CURRENT_BRANCH"

  # Push tags
  git push origin --tags

  echo ""
  echo -e "${GREEN}✓ Release complete!${NC}"
  echo ""
  echo "Next steps:"
  echo "  1. Monitor builds: https://github.com/mabdulwasii/retail-manager/actions"
  echo "  2. Verify images: https://hub.docker.com/r/princely/shop-manager/tags"
  echo "  3. Check releases: https://github.com/mabdulwasii/retail-manager/releases"
else
  echo ""
  echo -e "${YELLOW}Changes committed locally but not pushed${NC}"
  echo "To push manually:"
  echo "  git push origin $CURRENT_BRANCH"
  echo "  git push origin --tags"
fi
