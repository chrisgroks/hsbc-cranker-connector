# SonarCloud Quality Gates Setup Guide

## ✅ Code Coverage is Now Enabled!

JaCoCo has been added to your project. The next time tests run, SonarCloud will show coverage metrics.

---

## 📊 Setting Up Quality Gates

### Step 1: Access Your Project

Go to: https://sonarcloud.io/project/overview?id=chrisgroks_hsbc-cranker-connector

### Step 2: Navigate to Quality Gates

1. Click **Administration** (top menu)
2. Click **Quality Gate** (left sidebar)

---

## 🎯 Recommended Quality Gate Settings

### Option A: Use Default "Sonar way" (Recommended for Start)

The default quality gate is already pretty good:
- **Coverage on New Code** ≥ 80%
- **Duplicated Lines on New Code** ≤ 3%
- **Maintainability Rating on New Code** = A
- **Reliability Rating on New Code** = A
- **Security Rating on New Code** = A
- **Security Hotspots Reviewed** = 100%

**To use it:**
1. In Quality Gate settings
2. Select "Sonar way" from the dropdown
3. Click **Save**

---

### Option B: Create Custom "Enterprise" Quality Gate

For stricter requirements:

1. Click **"Create"** button
2. Name it: `Enterprise Quality Gate`
3. Add these conditions:

#### New Code Conditions (Strict):
```
Coverage on New Code          ≥ 80%
Duplicated Lines on New Code  ≤ 3%
Maintainability Rating        = A
Reliability Rating            = A
Security Rating               = A
Security Hotspots Reviewed    = 100%
```

#### Overall Code Conditions (Lenient):
```
Coverage                      ≥ 70%
Duplicated Lines              ≤ 5%
Maintainability Rating        ≤ B
Reliability Rating            ≤ B
```

4. Click **"Set as Default"** to apply to all projects

---

## 🔍 What Each Metric Means

### Coverage
- **What**: Percentage of code executed by tests
- **Good**: ≥ 80% on new code
- **Why**: Ensures new features are tested

### Duplicated Lines
- **What**: Percentage of copy-pasted code
- **Good**: ≤ 3%
- **Why**: Reduces maintenance burden

### Maintainability Rating
- **What**: Based on code smells and technical debt
- **Ratings**: A (best) → E (worst)
- **Why**: Keeps code clean and easy to change

### Reliability Rating
- **What**: Based on bugs found
- **Ratings**: A (no bugs) → E (many bugs)
- **Why**: Prevents runtime errors

### Security Rating
- **What**: Based on security vulnerabilities
- **Ratings**: A (no vulns) → E (many vulns)
- **Why**: Prevents security breaches

### Security Hotspots
- **What**: Code that needs manual security review
- **Good**: 100% reviewed
- **Why**: Ensures sensitive code is checked

---

## 📈 Viewing Coverage in SonarCloud

After the next workflow run with tests:

1. Go to your project dashboard
2. Click **"Measures"** tab
3. Select **"Coverage"** from the dropdown
4. You'll see:
   - Overall coverage percentage
   - Coverage by file
   - Uncovered lines highlighted
   - Coverage trends over time

---

## 🧪 Testing Coverage Locally

To see coverage reports on your machine:

```bash
# Run tests with coverage
mvn clean verify

# View HTML report
open target/site/jacoco/index.html
```

The report shows:
- ✅ Green = Covered lines
- ❌ Red = Uncovered lines
- 🟡 Yellow = Partially covered branches

---

## 🎯 Demo Tips

### Show Coverage in Action:

1. **Before**: Show current coverage in SonarCloud
2. **Add a test**: Write a test for uncovered code
3. **Push**: Commit and push
4. **After**: Show improved coverage in SonarCloud

### Show Quality Gate Failure:

1. **Add bad code**: Introduce a bug or code smell
2. **Push**: Create a PR
3. **Show**: Quality gate fails in PR checks
4. **Fix**: Remove the bad code
5. **Show**: Quality gate passes

---

## 🔧 Adjusting Thresholds

### Make More Strict:
```
Coverage on New Code ≥ 90%  (instead of 80%)
Duplicated Lines ≤ 2%       (instead of 3%)
```

### Make More Lenient:
```
Coverage on New Code ≥ 70%  (instead of 80%)
Duplicated Lines ≤ 5%       (instead of 3%)
```

**To change:**
1. Administration → Quality Gate
2. Click on the condition
3. Edit the threshold
4. Save

---

## 📊 Understanding the Dashboard

### Main Metrics (Top of Dashboard):

- **Bugs**: Logic errors that could cause failures
- **Vulnerabilities**: Security issues
- **Code Smells**: Maintainability issues
- **Coverage**: Test coverage percentage
- **Duplications**: Duplicated code percentage

### Activity Tab:
- Shows history of scans
- Coverage trends over time
- Quality gate pass/fail history

### Issues Tab:
- Lists all bugs, vulnerabilities, code smells
- Filter by severity, type, status
- Assign to team members

---

## 🚀 Next Steps

1. **Wait for next workflow run** - Coverage will appear in ~5 minutes
2. **Review the metrics** - See what your current coverage is
3. **Set quality gate** - Choose default or create custom
4. **Test it** - Create a PR and see quality gate in action

---

## 💡 Pro Tips

### For Demos:
- Show the **"New Code"** tab - highlights recent changes
- Use **"Activity"** to show improvement over time
- Filter issues by **"Since last version"** to show new findings

### For Development:
- Run `mvn verify` before pushing to catch issues early
- Review **Security Hotspots** manually - they need human judgment
- Use **"Assign"** feature to distribute issue fixes across team

---

## 🔗 Useful Links

- **Your Project**: https://sonarcloud.io/project/overview?id=chrisgroks_hsbc-cranker-connector
- **SonarCloud Docs**: https://docs.sonarcloud.io
- **Quality Gates Guide**: https://docs.sonarcloud.io/improving/quality-gates/
- **JaCoCo Documentation**: https://www.jacoco.org/jacoco/trunk/doc/

---

## ❓ Troubleshooting

### "Coverage is 0%"
- Make sure tests are running: `mvn test`
- Check JaCoCo report exists: `ls target/site/jacoco/jacoco.xml`
- Verify workflow ran successfully

### "Quality Gate not showing in PR"
- Check workflow permissions (already set correctly)
- Verify `GITHUB_TOKEN` is available (automatic)
- Wait a few minutes for SonarCloud to process

### "Can't see quality gate settings"
- Make sure you're logged into SonarCloud
- Verify you have admin access to the project
- Try refreshing the page

---

**Coverage is now enabled! 🎉**

The next workflow run will generate coverage reports and send them to SonarCloud.
