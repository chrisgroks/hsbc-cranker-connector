# Security CI/CD Setup Guide
## For hsbc-cranker-connector

This guide will walk you through setting up enterprise-grade security and quality checks for this repository. **You've never used these tools before**, so I'll explain everything step-by-step.

---

## 🎯 What We're Setting Up

### New Security & Quality Tools:
1. **SonarCloud** - Analyzes Java code for bugs, vulnerabilities, code smells
2. **Snyk** - Scans Maven dependencies for known vulnerabilities (CVEs)
3. **Semgrep** - Fast security pattern detection (OWASP Top 10, etc.)
4. **Dependabot** - Automated dependency updates (already built into GitHub)

### Existing Tools (Already Working):
- ✅ **Basic CI** - Build and test on Java 11, 17, 21
- ✅ **CodeQL** - GitHub's code scanning (already configured)

---

## 🔒 Security Best Practices

### ✅ What We Did Right:

1. **Secrets in GitHub Secrets** - Tokens are stored securely, never in code
2. **Minimal Permissions** - Workflows only get the permissions they need
3. **Pull Request Triggers** - Security checks run before code is merged
4. **SARIF Upload** - Results go to GitHub Security tab for centralized viewing
5. **No Token Exposure** - Tokens are masked in logs automatically

### ❌ What to NEVER Do:

- ❌ Never commit API tokens or secrets to the repository
- ❌ Never echo or print secrets in workflow logs
- ❌ Never use tokens from untrusted sources
- ❌ Never give workflows more permissions than needed

---

## 📋 Step-by-Step Setup

### Step 1: Set Up SonarCloud (10 minutes)

**What is SonarCloud?**
A cloud-based code quality platform that analyzes your Java code for bugs, security vulnerabilities, and code maintainability issues.

**Setup Instructions:**

1. **Create Account**
   - Go to [https://sonarcloud.io](https://sonarcloud.io)
   - Click **"Log in"** → **"Sign up with GitHub"**
   - Authorize SonarCloud to access your GitHub account

2. **Import Your Repository**
   - Click **"+"** button → **"Analyze new project"**
   - Select **"hsbc-cranker-connector"** from the list
   - Click **"Set Up"**

3. **Choose Analysis Method**
   - Select **"With GitHub Actions"**
   - SonarCloud will show you organization and project keys

4. **Get Your Keys**
   - You'll see something like:
     ```
     Organization: chrisgroks
     Project Key: chrisgroks_hsbc-cranker-connector
     ```
   - **IMPORTANT**: Copy these! You need them for the next step

5. **Generate Token (THIS IS THE SECURITY-SENSITIVE PART)**
   - Click your profile picture → **"My Account"**
   - Go to **"Security"** tab
   - Under **"Generate Tokens"**:
     - Name: `hsbc-cranker-connector-ci`
     - Type: Select **"User Token"** or **"Project Token"**
     - Click **"Generate"**
   - **COPY THE TOKEN IMMEDIATELY** - You won't see it again!
   - ⚠️ **NEVER paste this token in code or share it publicly**

6. **Add Token to GitHub Secrets (SECURE METHOD)**
   - Go to your GitHub repository: https://github.com/chrisgroks/hsbc-cranker-connector
   - Click **"Settings"** tab
   - Left sidebar → **"Secrets and variables"** → **"Actions"**
   - Click **"New repository secret"**
   - Name: `SONAR_TOKEN` (must be exactly this)
   - Value: Paste the token you copied
   - Click **"Add secret"**
   - ✅ Token is now securely stored and encrypted by GitHub

7. **Update Configuration**
   - Open `sonar-project.properties` in your repository
   - Update these lines with your actual values:
     ```properties
     sonar.organization=chrisgroks
     sonar.projectKey=chrisgroks_hsbc-cranker-connector
     ```
   - Also update in `.github/workflows/sonarcloud.yml` (lines 42-43) if different

---

### Step 2: Set Up Snyk (10 minutes)

**What is Snyk?**
A security platform that scans your Maven dependencies (like junit, slf4j, etc.) for known vulnerabilities (CVEs) and provides fix recommendations.

**Setup Instructions:**

1. **Create Account**
   - Go to [https://snyk.io](https://snyk.io)
   - Click **"Sign up"** → **"Continue with GitHub"**
   - Authorize Snyk to access your GitHub account

2. **Import Your Repository (Optional but Recommended)**
   - Click **"Add project"**
   - Select **"GitHub"**
   - Find **"hsbc-cranker-connector"**
   - Click **"Add selected repository"**
   - This sets up the Snyk dashboard for viewing results

3. **Get Your API Token (SECURITY-SENSITIVE)**
   - Click your profile icon → **"Account settings"**
   - Scroll to **"General"** section
   - Find **"Auth Token"**
   - Click **"Click to show"**
   - **COPY THE TOKEN**
   - ⚠️ **NEVER commit this to your repository**

4. **Add Token to GitHub Secrets (SECURE METHOD)**
   - Go to: https://github.com/chrisgroks/hsbc-cranker-connector/settings/secrets/actions
   - Click **"New repository secret"**
   - Name: `SNYK_TOKEN` (must be exactly this)
   - Value: Paste the token
   - Click **"Add secret"**
   - ✅ Token is now securely stored

---

### Step 3: Set Up Semgrep (Optional - Works Without Account)

**What is Semgrep?**
A fast static analysis tool that finds security patterns in your code (like SQL injection risks, XSS vulnerabilities, etc.). Works without an account!

**Setup Instructions:**

**Option A: No Account Needed** ✅ Recommended
- Nothing to do! The workflow will run automatically
- Results appear in GitHub Security tab
- No token needed

**Option B: With Semgrep Cloud (Optional)**
- Dashboard for tracking findings over time
- Go to [https://semgrep.dev](https://semgrep.dev)
- Sign up with GitHub
- Create a project and get API token
- Add as `SEMGREP_APP_TOKEN` secret (optional)

---

### Step 4: Enable Dependabot (Already Done!)

**What is Dependabot?**
Built into GitHub, it automatically creates pull requests to update your dependencies and alerts you to security vulnerabilities.

**Setup:**
- ✅ Configuration file already created (`.github/dependabot.yml`)
- GitHub will start scanning automatically
- You'll see PRs for dependency updates weekly

**How to View:**
- Go to **"Security"** tab → **"Dependabot alerts"**
- Enable alerts: **Settings** → **"Code security and analysis"** → Enable all

---

## 🧪 Testing Your Setup

### Test 1: Create a Test Pull Request

1. **Create a new branch:**
   ```bash
   cd hsbc-cranker-connector
   git checkout -b test-security-ci
   ```

2. **Make a small change:**
   ```bash
   echo "# Security CI Test" >> README.md
   git add README.md
   git commit -m "test: Verify security CI setup"
   git push -u origin test-security-ci
   ```

3. **Create Pull Request:**
   - Go to GitHub
   - Click **"Compare & pull request"**
   - Create the PR

4. **Watch the Checks:**
   - Go to **"Checks"** tab
   - You should see:
     - ✅ Build and test (existing)
     - ✅ CodeQL (existing)
     - ⏳ SonarCloud Analysis (new)
     - ⏳ Snyk Vulnerability Scan (new)
     - ⏳ Semgrep Security Scan (new)

### Test 2: Verify Security Tab

1. Go to your repo's **"Security"** tab
2. Click **"Code scanning"**
3. You should see findings from:
   - CodeQL
   - Snyk
   - Semgrep
   - SonarCloud (if it found issues)

---

## 🔍 Understanding the Workflows

### What Runs When?

| Workflow | Trigger | What It Does | Fails Build? |
|----------|---------|--------------|--------------|
| **ci.yaml** | Every push/PR | Builds & tests on Java 11, 17, 21 | Yes |
| **codeql.yml** | Push to master, PRs, weekly | Deep semantic analysis | Yes |
| **sonarcloud.yml** | Push to master, PRs | Code quality & security | Yes (if quality gate fails) |
| **snyk.yml** | Push to master, PRs | Dependency vulnerabilities | Yes (on high+ severity) |
| **semgrep.yml** | Push to master, PRs | Security pattern detection | Yes (if findings) |

### Security Features:

1. **Minimal Permissions:**
   ```yaml
   permissions:
     contents: read        # Can only read code
     pull-requests: read   # Can only read PRs
     security-events: write # Can only write to Security tab
   ```

2. **Secret Handling:**
   - Secrets accessed via `${{ secrets.SECRET_NAME }}`
   - GitHub automatically masks them in logs
   - Never logged or exposed

3. **SARIF Results:**
   - Security findings uploaded to GitHub Security tab
   - Centralized view across all tools
   - Code annotations on PRs

---

## 🛠️ Troubleshooting

### "SONAR_TOKEN not found"

**Problem:** The workflow can't find the SonarCloud token.

**Fix:**
1. Verify you added the secret with exact name: `SONAR_TOKEN`
2. Check: https://github.com/chrisgroks/hsbc-cranker-connector/settings/secrets/actions
3. If missing, add it following Step 1 above

### "SNYK_TOKEN not found"

**Problem:** The workflow can't find the Snyk token.

**Fix:**
1. Verify you added the secret with exact name: `SNYK_TOKEN`
2. Add it following Step 2 above

### "Invalid project key" in SonarCloud

**Problem:** The project key in `sonar-project.properties` doesn't match SonarCloud.

**Fix:**
1. Go to SonarCloud → Your Project → Administration → Update Key
2. Copy the correct key
3. Update in:
   - `sonar-project.properties`
   - `.github/workflows/sonarcloud.yml` (line 42)

### Workflows Not Running

**Problem:** New workflows don't appear in GitHub Actions.

**Fix:**
1. Make sure files are pushed to GitHub
2. Check they're in `.github/workflows/` directory
3. Verify YAML syntax is correct

### Build Failing on Dependencies

**Problem:** "Cannot resolve dependencies" or similar Maven errors.

**Fix:**
1. Make sure you have a network connection
2. Try: `mvn clean install` locally first
3. Check if Maven Central is accessible

---

## 🎨 Customizing Security Thresholds

### Make Checks More Strict

**Snyk - Fail on Medium Severity:**
```yaml
args: --severity-threshold=medium
```

**SonarCloud - Adjust Quality Gate:**
1. Go to SonarCloud → Your Project
2. Administration → Quality Gate
3. Set stricter thresholds

### Make Checks More Lenient

**Snyk - Only Fail on Critical:**
```yaml
args: --severity-threshold=critical
```

**Continue on Error (Not Recommended for Security):**
```yaml
continue-on-error: true  # Won't fail build but still reports
```

---

## 📊 Viewing Results

### In Pull Requests:
- **Checks tab** - Pass/fail status for each workflow
- **Files changed** - Inline annotations on code
- **Conversation** - Bot comments with findings

### In GitHub Security Tab:
- **Code scanning** - All SARIF results from Snyk, Semgrep, CodeQL
- **Dependabot alerts** - Vulnerable dependencies
- **Security advisories** - CVE details

### In Tool Dashboards:
- **SonarCloud**: https://sonarcloud.io/project/overview?id=chrisgroks_hsbc-cranker-connector
- **Snyk**: https://app.snyk.io/org/YOUR-ORG/projects
- **Semgrep**: https://semgrep.dev (if configured)

---

## 🚀 Next Steps

1. ✅ Complete Steps 1-2 above (add secrets)
2. ✅ Test with a pull request
3. ✅ Review any findings in Security tab
4. ✅ Set up branch protection rules (optional)
5. ✅ Configure quality gates in SonarCloud

### Optional: Branch Protection

Require security checks before merging:
1. Settings → Branches
2. Add rule for `master`
3. Check "Require status checks to pass"
4. Select all security workflows
5. Save

---

## ❓ Questions?

### "Is this secure?"
Yes! All tokens are stored in GitHub Secrets (encrypted), never in code.

### "Will this slow down my builds?"
Slightly. Security scans add 2-5 minutes per PR. Worth it for security!

### "Can I disable a check?"
Yes, just delete or rename the workflow file.

### "What if I find a vulnerability?"
The tools will show how to fix it. Many provide automated fixes.

### "Do I need to pay?"
No! All tools have free tiers for public repos or open source.

---

## 📞 Support

- **SonarCloud**: https://community.sonarsource.com
- **Snyk**: https://support.snyk.io
- **Semgrep**: https://semgrep.dev/docs
- **GitHub Actions**: https://docs.github.com/actions

---

**Created**: Oct 31, 2025
**For**: hsbc-cranker-connector CI/CD pipeline
**Security**: All tokens stored as GitHub Secrets ✅
