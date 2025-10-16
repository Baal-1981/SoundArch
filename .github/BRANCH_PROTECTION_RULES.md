# Branch Protection Rules - Configuration Guide

**Date**: 2025-10-16
**Status**: P0-2 (CI/CD Pipeline Setup)

---

## 📋 Branch Protection Rules (Manual Configuration)

Ces règles doivent être configurées manuellement dans GitHub Settings:

**Settings → Branches → Add Branch Protection Rule**

---

## 🔒 Protection Rules for `main` Branch

### Branch Name Pattern
```
main
```

### Rules to Enable

#### Require Pull Request Reviews
- ✅ **Require a pull request before merging**
  - Required number of approvals: **1**
  - ✅ Dismiss stale pull request approvals when new commits are pushed
  - ✅ Require review from Code Owners (if CODEOWNERS file exists)

#### Require Status Checks
- ✅ **Require status checks to pass before merging**
  - ✅ Require branches to be up to date before merging

  **Required Status Checks** (wait for first CI/CD run to populate):
  - `Unit Tests (JVM)`
  - `Lint Checks`
  - `Build Debug APK`
  - `Instrumented Tests (Android)` (optional - can be slow)
  - `Code Coverage Report`

#### Additional Rules
- ✅ **Require conversation resolution before merging**
- ✅ **Require linear history** (no merge commits, use rebase or squash)
- ✅ **Do not allow bypassing the above settings** (enforce for admins too)

#### Restrictions
- ❌ **Allow force pushes**: Disabled
- ❌ **Allow deletions**: Disabled

---

## 🔒 Protection Rules for `develop` Branch

### Branch Name Pattern
```
develop
```

### Rules to Enable

#### Require Pull Request Reviews
- ✅ **Require a pull request before merging**
  - Required number of approvals: **1**
  - ✅ Dismiss stale pull request approvals when new commits are pushed

#### Require Status Checks
- ✅ **Require status checks to pass before merging**
  - ✅ Require branches to be up to date before merging

  **Required Status Checks**:
  - `Unit Tests (JVM)`
  - `Lint Checks`
  - `Build Debug APK`

#### Additional Rules
- ✅ **Require conversation resolution before merging**
- ✅ **Require linear history**

#### Restrictions
- ❌ **Allow force pushes**: Disabled
- ❌ **Allow deletions**: Disabled

---

## 📝 Workflow Summary

### Feature Development Workflow
```
1. Create feature branch from develop
   git checkout develop
   git pull
   git checkout -b feature/my-feature

2. Make changes and commit
   git add .
   git commit -m "feat: Add new feature"

3. Push and create PR
   git push -u origin feature/my-feature

4. Wait for CI/CD checks to pass (GitHub Actions)
   - Unit tests
   - Lint checks
   - Build verification

5. Request review (minimum 1 approval)

6. Merge to develop (squash merge recommended)

7. After sprint completion, create PR: develop → main
```

### Hotfix Workflow
```
1. Create hotfix branch from main
   git checkout main
   git pull
   git checkout -b hotfix/critical-fix

2. Fix issue and commit
   git commit -m "fix: Critical bug fix"

3. Push and create PR to main
   git push -u origin hotfix/critical-fix

4. Wait for CI/CD checks (all must pass)

5. Request review + approval

6. Merge to main (squash merge)

7. Cherry-pick to develop
   git checkout develop
   git cherry-pick <commit-sha>
   git push
```

---

## ✅ Configuration Steps

### Step 1: Configure Branch Protection (GitHub Web UI)

1. Go to repository on GitHub
2. **Settings** → **Branches**
3. Click **Add branch protection rule**
4. Enter branch name pattern: `main`
5. Enable rules as listed above
6. Click **Create** or **Save changes**
7. Repeat for `develop` branch

### Step 2: Verify CI/CD Integration

1. Create a test PR
2. Verify GitHub Actions run automatically
3. Check that required status checks appear
4. Verify PR cannot be merged until checks pass

### Step 3: Update Required Status Checks

After first CI/CD run:
1. Go to **Settings** → **Branches**
2. Edit `main` branch protection rule
3. Under **Status checks**, search for:
   - `Unit Tests (JVM)`
   - `Lint Checks`
   - `Build Debug APK`
   - `Code Coverage Report`
4. Select each status check to make it required
5. Save changes

---

## 🎯 Quality Gates Enforced by CI/CD

### Automatic Checks (GitHub Actions)

1. **Unit Tests (JVM)**
   - All unit tests must pass
   - Command: `./gradlew testDebugUnitTest`
   - Timeout: 30 minutes

2. **Lint Checks**
   - ktlint must pass (Kotlin code style)
   - Android Lint must pass (code quality)
   - Command: `./gradlew ktlintCheck lintDebug`

3. **Build Verification**
   - Debug APK must build successfully
   - Command: `./gradlew assembleDebug`

4. **Code Coverage**
   - JaCoCo report generated
   - Minimum threshold: 45% (current baseline)
   - Target: 85%+ (Phase 2 goal)
   - Command: `./gradlew jacocoTestReport`

5. **Instrumented Tests** (optional for PRs, mandatory for main merge)
   - Run on Android Emulator (API 30)
   - All instrumented tests must pass
   - Command: `./gradlew connectedDebugAndroidTest`

---

## 🚨 Enforcement

### What Happens if Checks Fail?

- ❌ PR cannot be merged (red "X" next to status check)
- 🔴 GitHub blocks merge button
- 📧 PR author receives notification
- 🔧 Author must fix issues and push new commit
- ♻️ CI/CD re-runs automatically on new commit

### Override (Emergency Only)

**NOT RECOMMENDED** - Branch protection should be enforced for everyone, including admins.

If emergency override is needed:
1. Settings → Branches → Edit rule
2. Temporarily disable "Do not allow bypassing"
3. Merge PR
4. **IMMEDIATELY re-enable** protection
5. Document reason in commit message + Slack/email

---

## 📊 Monitoring

### GitHub Actions Dashboard
- View all workflow runs: **Actions** tab
- Filter by branch, status, workflow
- Download artifacts (test results, coverage reports, APKs)

### Status Badges (Optional)
Add to README.md:

```markdown
![Android CI](https://github.com/YOUR_USERNAME/SoundArch/actions/workflows/android-ci.yml/badge.svg)
```

---

## 🔐 Secrets Configuration (for Release Builds)

Release builds require signing secrets (configured in Settings → Secrets):

### Required Secrets

1. **KEYSTORE_BASE64**
   - Base64-encoded keystore file
   - Generate: `base64 -i app/keystore.jks | pbcopy` (macOS)
   - Or: `base64 app/keystore.jks > keystore.txt` (Linux)

2. **KEYSTORE_PASSWORD**
   - Keystore password

3. **KEY_ALIAS**
   - Key alias (e.g., "soundarch")

4. **KEY_PASSWORD**
   - Key password

### Setup Steps

1. Go to **Settings** → **Secrets and variables** → **Actions**
2. Click **New repository secret**
3. Add each secret above
4. Verify release build workflow can access secrets

---

## ✅ Verification Checklist

After configuring branch protection:

- [ ] Branch protection enabled for `main`
- [ ] Branch protection enabled for `develop`
- [ ] Required status checks configured
- [ ] Minimum 1 approval required for PRs
- [ ] Force push disabled
- [ ] Branch deletion disabled
- [ ] Linear history enforced
- [ ] CI/CD workflows running on every PR
- [ ] Test PR created and verified
- [ ] Status checks appear in PR
- [ ] Merge blocked until checks pass
- [ ] Release signing secrets configured (if applicable)

---

**Status**: Ready for manual configuration on GitHub
**Next**: Test PR creation to verify CI/CD integration
