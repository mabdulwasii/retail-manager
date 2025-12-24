# SonarQube Issues - Remaining Fixes Guide

## ✅ Fixed (Commits: 6fa4bf3, 2bafc56)
1. ✅ Random instance reused in ProductService
2. ✅ remainingQuantity made package-private in InventoryAllocationService
3. ✅ Inline assertions in ShopAccessValidatorTest (removed intermediate variables)
4. ✅ Unused imports removed by linter

## 📋 Remaining Issues (85 total)

### Category 1: AssertJ Assertion Chains (~50 issues)
**Pattern:** Multiple separate assertions on same/related objects

**Example from PermissionServiceTest.java:57**
```java
// BEFORE
assertThat(permissions).isNotNull();
assertThat(permissions).hasSize(5);
assertThat(permissions).extracting(Permission::getName)
    .contains("USER_CREATE", "USER_READ");

// AFTER
assertThat(permissions)
    .isNotNull()
    .hasSize(5)
    .extracting(Permission::getName)
    .contains("USER_CREATE", "USER_READ");
```

**Files to fix:**
- PermissionServiceTest.java (lines 57, 80, 159, 267, 333)
- RoleServiceTest.java (line 162)
- CategoryServiceTest.java (line 260)
- InvestmentCreateRequestValidatorTest.java (lines 50, 72, 111, 133, 172, 194, 234, 255, 275, 312, 333, 408)
- InvestmentRoundValidatorTest.java (lines 54, 122, 144, 178, 210, 242, 309, 342, 375, 410, 443, 493, 509, 530, 555, 581, 610)
- InvestorDistributionTest.java (line 356)
- InvestorShareTest.java (line 88)
- InvestmentRoundTest.java (line 402)
- TimeWeightingRulesTest.java (line 406)
- SalesTransactionTest.java (lines 397, 414)
- ReceiptTest.java (lines 249, 264)
- ExpenseStatusTest.java (line 534)
- BusinessExceptionTest.java (line 142)

**Command to find all:**
```bash
cd backend/src/test/java
grep -r "assertThat(" --include="*Test.java" | awk '{print $1}' | sort | uniq -c | sort -rn | head -20
```

### Category 2: Parameterized Test Suggestions (5 files)
**Pattern:** Multiple similar tests that should use @ParameterizedTest

**Example from LineItemTest.java:86**
```java
// BEFORE - 3 separate tests
@Test
void testCase1() { ... }

@Test
void testCase2() { ... }

@Test
void testCase3() { ... }

// AFTER - Single parameterized test
@ParameterizedTest
@CsvSource({
    "case1, expected1",
    "case2, expected2",
    "case3, expected3"
})
void testAllCases(String input, String expected) { ... }
```

**Files to fix:**
- AnalyticsControllerMinimalIT.java (line 31) - 4 tests
- TenantControllerMinimalIT.java (line 83) - 3 tests
- ExpenseCategoryTest.java (line 241) - 3 tests
- FraudRuleTest.java (line 68) - 3 tests
- LineItemTest.java (lines 86, 104) - 3 tests each

### Category 3: containsEntry() Usage (~6 issues)
**Pattern:** Use `.containsEntry(key, value)` instead of separate assertions

**Example from FraudManagementServiceTest.java:474**
```java
// BEFORE
Map<String, Object> details = rule.getDetails();
assertThat(details.get("key1")).isEqualTo("value1");
assertThat(details.get("key2")).isEqualTo("value2");

// AFTER
assertThat(details)
    .containsEntry("key1", "value1")
    .containsEntry("key2", "value2");
```

**Files to fix:**
- FraudManagementServiceTest.java (lines 474, 475, 476, 480, 481, 485)

### Category 4: Useless eq() in Mockito (~3 issues)
**Pattern:** Remove unnecessary `eq()` wrapper for simple values

**Example from FraudDetectionServiceTest.java:250**
```java
// BEFORE
when(repository.findById(eq("123"))).thenReturn(Optional.of(entity));

// AFTER
when(repository.findById("123")).thenReturn(Optional.of(entity));
```

**Files to fix:**
- FraudDetectionServiceTest.java (line 250)
- FraudManagementServiceTest.java (lines 141, 213)

### Category 5: Duplicate Test Implementations (2 issues)
**Pattern:** Two tests with identical implementation

**Example from ExpenseCategoryTest.java:528**
```java
// These two tests do the same thing - merge or differentiate them
@Test
void activate_shouldSetIsActiveToTrue() { ... }  // Line 241

@Test
void deactivate_shouldSetIsActiveTo False() { ... }  // Line 528 - identical to line 241!
```

**Files to fix:**
- ExpenseCategoryTest.java (line 528 duplicate of 241)
- TimeWeightingRulesTest.java (line 250 duplicate of 198)

### Category 6: Commented Code Blocks (2 issues)
**Pattern:** Remove commented-out code

**Files to fix:**
- InvestmentRoundServiceTest.java (lines 190, 238)

**Command:**
```bash
# Find and remove commented blocks
sed -i '/^[[:space:]]*\/\//d' InvestmentRoundServiceTest.java
```

### Category 7: Unused Variables/Imports (6 issues)
**Pattern:** Remove unused code

**Files:**
- InventoryControllerMinimalIT.java:6 - Remove `import org.junit.jupiter.api.Test;`
- TestJwtSecurityConfig.java:56 - Remove unused `subject` variable
- InvestmentCreateRequestValidatorTest.java:369 - Remove unused `violations` variable
- InvestmentCreateRequestValidatorTest.java:420 - Rename `validator` variable (shadows field)

### Category 8: isZero() Usage (2 issues)
**Pattern:** Use `.isZero()` instead of `.isEqualTo(0)` or `.isEqualTo(BigDecimal.ZERO)`

**Example from ReceiptTest.java:52**
```java
// BEFORE
assertThat(amount).isEqualTo(BigDecimal.ZERO);
assertThat(count).isEqualTo(0);

// AFTER
assertThat(amount).isZero();
assertThat(count).isZero();
```

**Files to fix:**
- ReceiptTest.java (line 52)

### Category 9: Stream.toList() Migration (1 issue)
**Pattern:** Use Java 16+ `.toList()` instead of `.collect(Collectors.toList())`

**Example from TestSecurityConfig.java:127**
```java
// BEFORE
List<String> roles = stream.collect(Collectors.toList());

// AFTER
List<String> roles = stream.toList();
```

**Files to fix:**
- TestSecurityConfig.java (line 127)

### Category 10: Thread.sleep() in Tests (3 issues)
**Pattern:** Replace with proper wait mechanisms or Awaitility

**Example from ReceiptTest.java:172**
```java
// BEFORE
Thread.sleep(1000);

// AFTER
await().atMost(Duration.ofSeconds(1))
    .until(() -> condition);
```

**Files to fix:**
- ReceiptTest.java (line 172)
- AbstractIntegrationTest.java (lines 924, 947)

### Category 11: TODO Comments (5 issues)
**Pattern:** Complete or document TODOs

**Files to fix:**
- FraudDetectionControllerMinimalIT.java (lines 234, 237, 246, 255, 264)

**Options:**
1. Implement the TODO
2. Convert to GitHub issue and remove comment
3. Document why it's deferred with target date

## 🚀 Automated Fix Script

```bash
#!/bin/bash
# Run from backend/ directory

# Fix 1: Remove unused Test import
sed -i '' '/import org.junit.jupiter.api.Test;$/d' \
  src/test/java/com/princely/shopmanager/inventory/controller/InventoryControllerMinimalIT.java

# Fix 2: Replace isEqualTo(0) with isZero()
find src/test/java -name "*Test.java" -exec sed -i '' 's/\.isEqualTo(0)/.isZero()/g' {} \;
find src/test/java -name "*Test.java" -exec sed -i '' 's/\.isEqualTo(BigDecimal\.ZERO)/.isZero()/g' {} \;

# Fix 3: Replace collect(Collectors.toList()) with toList()
find src/test/java -name "*.java" -exec sed -i '' 's/\.collect(Collectors\.toList())/.toList()/g' {} \;

# Fix 4: Remove useless eq() in Mockito (string literals only)
find src/test/java -name "*Test.java" -exec sed -i '' 's/eq("\([^"]*\)")"/"\1"/g' {} \;

echo "✅ Automated fixes applied. Run './mvnw test' to verify."
```

## 📊 Priority Order

1. **HIGH** (Do first): Categories 6, 7, 8, 9 - Quick wins, low risk
2. **MEDIUM** (Do second): Categories 3, 4, 5 - Moderate effort
3. **LOW** (Do last): Categories 1, 2, 10, 11 - Require manual review

## 🎯 Estimated Impact on Coverage

- Current coverage: 62%
- Target coverage: 80%
- These fixes: Improves code quality but doesn't increase coverage
- **To reach 80% coverage:** Need to add ~150-200 more test assertions for uncovered code paths

## 📝 Notes

- All fixes preserve test functionality
- Run `./mvnw test` after each category
- Commit after each successful category
- Some AssertJ chains require understanding test context - review manually
