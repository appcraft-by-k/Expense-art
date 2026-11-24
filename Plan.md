# Expense Art — Implementation Plan (Feature Session Source of Truth)

**Project:** `com.appcraftbyk.expense.art` (`C:\Users\kanis\AndroidStudioProjects\Expenseart`)
**Source docs:** `PRD.md` (v1.0) + `AGENTS.md`
**Plan version:** 2.0 — locked after design review
**Status:** Approved for implementation. No phase started yet.

---

## 0. How to use this file (session protocol)

This file drives **feature sessions**. Each session implements exactly one phase.

1. Read §1–§12 once at the start of the session (they are the contracts).
2. Run `.\gradlew :app:assembleDebug` to confirm the baseline is green **before** changing anything.
3. Implement only the current phase in §13. Do not pull work forward from later phases.
4. Follow §7 invariants — they are correctness rules, not suggestions.
5. Update §16 Progress tracker (status + date + short note) and append one line to §17 when done.
6. **Locked decisions (§2) may only be changed with explicit user approval.** Record any such change in the decision log with a date.
7. A phase is done only when its *Acceptance criteria* and *Verification* are both satisfied. Never claim a test/build passed unless it was actually run.
8. Nothing in this plan overrides `AGENTS.md` (Compose-only UI, MVVM, Room for structured data, no main-thread DB work, no secrets in plain prefs, no unapproved decorative UI).

---

## 1. Scope, constraints, non-goals

**Product:** privacy-first, manual-entry personal finance manager for India. Local-only data, no analytics, no bank/SMS integration, ₹ INR with Indian digit grouping (₹1,25,000).

**v1 includes**
- Home dashboard: 4 metric cards, monthly budget bar, top-spend tags, upcoming dues, recent activity.
- Expense & Income ledger with period + category filters, day grouping, payment-method tags.
- Debt & Lend: separate "I Owe" / "Owed to Me", contact cards, repayment log, settle flow.
- Savings: user-managed **savings types** + savings pots with target/deadline/progress.
- Global Add Entry bottom sheet: **4 modes** (Expense, Income, Debt/Lend, Savings).
- Category management (system + user categories).
- Theme (light/dark, PRD palette), profile name, persistent preferences.

**Non-goals for v1 (explicitly deferred)**
- Payment reminders / scheduled bills / notifications / WorkManager (v2 — see §8.3).
- Bank sync, SMS parsing, account aggregation, credit scores, cloud sync, multi-currency.
- Charts/animations beyond required progress bars (PRD + `AGENTS.md` approval rule).
- Data export/import, app lock/biometric, tablet-specific navigation rails (**revisit after v1**; layouts must still be responsive).

**Platform:** `minSdk 26`, `targetSdk 37`, `compileSdk 37`, Kotlin 2.2.10, AGP 9.5.2 (built-in Kotlin), Compose BOM 2026.02.01, Material 3.

---

## 2. Locked decisions (decision log)

| # | Decision | Choice | Date |
|---|---|---|---|
| D1 | Dependency injection | **Hilt** (`@HiltAndroidApp`, `@HiltViewModel`, KSP) | 2026-11-24 |
| D2 | Navigation | **androidx navigation-compose** with per-tab back stacks | 2026-11-24 |
| D3 | Minimum SDK | **26** (no `java.time` desugaring needed) | 2026-11-24 |
| D4 | Preferences | **Preferences DataStore** + **Android Keystore** AES-GCM for PII | 2026-11-24 |
| D5 | Android auto-backup | **Left enabled** (`allowBackup="true"`, default rules) | 2026-11-24 |
| D6 | Categories | System **and** user-managed, with archive/reassign rules | 2026-11-24 |
| D7 | Savings types | **Separate user-managed table** (`savings_types`), pots reference a type | 2026-11-24 |
| D8 | Reminders | **Deferred to v2**; 4th nav tab stays as a "coming soon" placeholder | 2026-11-24 |
| D9 | Money representation | `Long` **paise** (minor units) everywhere; never `Double`/`Float` | 2026-11-24 |
| D10 | Architecture | MVVM + unidirectional flow (intents → VM → repo → Room Flow) | 2026-11-24 |
| D11 | Backup encryption | DB stays unencrypted in cloud backup (accepted under D5) | 2026-11-24 |
| D12 | Payment methods | **User-managed `payment_methods` table** (system rows seeded, archive/reassign rules mirror categories); `transactions.paymentMethodId` FK RESTRICT | 2026-11-24 |
| D13 | Row identity | Every syncable table has `uid TEXT UNIQUE`: deterministic for system rows, UUID for user rows. Long `id` stays the local storage key | 2026-11-24 |

---

## 3. Dependencies

Add to `gradle/libs.versions.toml` (use a coherent version set; verify resolution during P0):

```toml
[versions]
room = "<current 2.8.x>"
ksp  = "<matches kotlin 2.2.10>"
hilt = "<current 2.5x>"
hiltNavigationCompose = "<current>"
navigationCompose = "<current>"
datastore = "<current 1.1.x>"
lifecycle = "<current 2.9.x>"

[libraries]
androidx-room-runtime        = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx            = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler       = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-room-testing        = { group = "androidx.room", name = "room-testing", version.ref = "room" }
hilt-android                 = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler                = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
androidx-hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigationCompose" }
androidx-navigation-compose  = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-lifecycle-runtime-compose   = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }

[plugins]
ksp  = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

**Not to be added:** Hilt test artifacts (until Hilt-injected instrumented tests exist), SQLCipher, chart libraries, androidx `security-crypto` (deprecated).

**Resolved in P0 (2026-11-24):** room `2.8.5`, ksp `2.2.10-2.0.2`, hilt `2.60.1`, hiltNavigationCompose `1.4.0`, navigationCompose `2.9.8`, datastore `1.1.7`, lifecycle `2.9.4`. AGP 9 built-in Kotlin needs `android.disallowKotlinSourceSets=false` in `gradle.properties` because KSP still registers generated sources through the legacy `kotlin.sourceSets` DSL.

---

## 4. Build configuration changes

**`app/build.gradle.kts`**
```kotlin
plugins { alias(libs.plugins.ksp); alias(libs.plugins.hilt) }

android {
    defaultConfig { minSdk = 26 }            // changed from 24
    ...
}
ksp { arg("room.schemaLocation", "$projectDir/schemas") }

dependencies {
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    androidTestImplementation(libs.androidx.room.testing)
}
```

**Manifest:** add `android:name=".ExpenseArtApplication"`. Keep `allowBackup="true"` and the existing backup XMLs untouched (D5).

**Commit** `app/schemas/<db>/1.json` once generated — v2 reminder migration depends on it.

---

## 5. Package layout

```
com.appcraftbyk.expense.art
├─ ExpenseArtApplication.kt            @HiltAndroidApp
├─ MainActivity.kt                     @AndroidEntryPoint, edge-to-edge, theme host
├─ core/
│  ├─ id/Uids.kt                        stable row uid generator (cloud merge)
│  ├─ money/Money.kt                   value class over Long minor units + arithmetic
│  ├─ money/IndianCurrencyFormatter.kt ₹1,25,000 grouping, unit-tested
│  ├─ time/DateRanges.kt               month/day ranges from Clock, Today/Yesterday labels
│  └─ time/AppClock.kt                 thin wrapper over java.time.Clock (injectable, testable)
├─ di/  DatabaseModule.kt  DataModule.kt  RepositoryModule.kt
├─ data/
│  ├─ local/AppDatabase.kt  Converters.kt  DatabaseSeeder.kt
│  ├─ local/entity/…          (10 entities)
│  ├─ local/dao/…             (6 DAOs)
│  ├─ local/projection/…      (aggregate result POJOs)
│  ├─ prefs/PreferencesRepository.kt  UserPreferences.kt
│  ├─ security/KeystoreCipher.kt  EncryptedValueStore.kt
│  ├─ repository/…Impl.kt
│  └─ mapper/…
├─ domain/
│  ├─ model/…                        (Transaction, Category, Debt, SavingsPot, …)
│  ├─ repository/…                   (interfaces, @Binds targets)
│  └─ usecase/…
└─ ui/
   ├─ App.kt  navigation/Destinations.kt  navigation/ExpenseArtNavHost.kt
   ├─ main/MainViewModel.kt             app-scoped: period, prefs, FAB sheet
   ├─ home/  expenses/  categories/  debt/  savings/  addentry/
   ├─ reminders/RemindersPlaceholderScreen.kt   (v2 stub)
   └─ theme/Color.kt (PRD tokens) Type.kt Theme.kt (dynamicColor = false)
```

---

## 6. Data model — 10 tables, schema version 1

Conventions: every table has `uid: String` (unique index) for cloud backup/merge — deterministic `sys-…` values for seeded rows, UUIDs for user rows; `uid` is immutable once created. `amountMinor: Long` (positive; sign comes from `kind`), timestamps `epochMillis: Long`, calendar-only dates `epochDay: Long`, enums stored as `TEXT` via `Converters`, all FK columns indexed. Local FKs use `id`; the backup layer resolves parent `uid` → local `id` on restore.

### 6.1 `categories`
| column | type | notes |
|---|---|---|
| `id` PK autoGenerate | Long | system rows use fixed IDs 1–N |
| `name` | String | unique per `kind` (case-insensitive, enforced in repo) |
| `kind` | `EXPENSE` \| `INCOME` | |
| `iconKey` | String | fixed icon set in UI |
| `sortOrder` | Int | |
| `isSystem` | Boolean | seeded rows; not hard-deletable |
| `isArchived` | Boolean | hides from pickers, keeps history |
| `createdAt` | Long | |

Indexes: unique `(name, kind)`, `(kind, sortOrder)`.

### 6.2 `transactions`
| column | type | notes |
|---|---|---|
| `id` PK | Long | |
| `kind` | `EXPENSE` \| `INCOME` | |
| `amountMinor` | Long | `> 0` (validated) |
| `categoryId` | Long? | FK → `categories` **RESTRICT** |
| `occurredAt` | Long | epoch millis |
| `paymentMethodId` | Long | FK → `payment_methods` **RESTRICT** (D12) |
| `note` | String? | |
| `createdAt`, `updatedAt` | Long | |

Indexes (PRD §6): `(occurredAt)`, `(categoryId)`, `(paymentMethodId)`, `(kind, occurredAt)`.

### 6.3 `monthly_budgets`
`id` PK, `yearMonth` Int (`202611`), `limitMinor` Long, `createdAt`. **Unique** `(yearMonth)`.
Per-category budgets: out of scope for v1.

### 6.4 `debt_contacts`
`id` PK, `name` String, `note` String?, `isArchived` Boolean, `createdAt` Long. Index `(name)`.

### 6.5 `debts`
| column | type | notes |
|---|---|---|
| `id` PK | Long | |
| `contactId` | Long | FK → `debt_contacts` RESTRICT |
| `direction` | `BORROWED` (I Owe) \| `LENT` (Owed to Me) | never netted |
| `principalMinor` | Long | |
| `startDate` | Long | epochDay |
| `dueDate` | Long? | epochDay; drives Overdue |
| `note` | String? | |
| `status` | `OPEN` \| `SETTLED` | |
| `settledAt` | Long? | |
| `createdAt`, `updatedAt` | Long | |

Indexes `(direction, status)`, `(contactId)`, `(dueDate)`. **Repaid total is never stored.**

### 6.6 `debt_repayments`
`id` PK, `debtId` FK → `debts` **CASCADE**, `amountMinor` Long, `paidAt` Long, `note` String?. Indexes `(debtId, paidAt)`, `(paidAt)`.

### 6.7 `savings_types` — user-managed (D7)
| column | type | notes |
|---|---|---|
| `id` PK | Long | system rows use fixed IDs 1–N |
| `name` | String | e.g. "Bank FD", "Bank RD", "Stocks", "Mutual Fund", "Cash", "Gold", "PPF" |
| `iconKey` | String | fixed icon set |
| `isSystem` | Boolean | seeded; not deletable |
| `isArchived` | Boolean | |
| `sortOrder` | Int | |
| `createdAt` | Long | |

Index: unique `(name)` (case-insensitive enforced in repo), `(sortOrder)`.

### 6.8 `savings_goals` (pots)
| column | type | notes |
|---|---|---|
| `id` PK | Long | |
| `name` | String | "HDFC FD 2026", "Emergency Fund" |
| `typeId` | Long | FK → `savings_types` **RESTRICT** |
| `institution` | String? | optional label ("HDFC") |
| `targetMinor` | Long? | nullable — instrument pots often have no target |
| `deadline` | Long? | epochDay |
| `isArchived` | Boolean | |
| `sortOrder` | Int | |
| `createdAt` | Long | |

Indexes `(typeId)`, `(isArchived, sortOrder)`.

### 6.9 `savings_entries`
| column | type | notes |
|---|---|---|
| `id` PK | Long | |
| `goalId` | Long | FK → `savings_goals` **CASCADE** |
| `kind` | `CONTRIBUTION` \| `WITHDRAWAL` | |
| `amountMinor` | Long | positive |
| `occurredAt` | Long | epoch millis |
| `note` | String? | |

Index `(goalId, occurredAt)`.

### 6.10 `payment_methods` — user-managed (D12)
| column | type | notes |
|---|---|---|
| `id` PK | Long | system rows use fixed IDs 1–N |
| `name` | String | "Cash", "UPI", "Credit Card", … (unique, case-insensitive enforced in repo) |
| `iconKey` | String | fixed icon set |
| `isSystem` | Boolean | seeded; not deletable |
| `isArchived` | Boolean | hides from pickers, keeps history |
| `sortOrder` | Int | |
| `createdAt` | Long | |

Index: unique `(name)`, unique `(uid)`, `(sortOrder)`.

### Deferred to v2 (documented, not created)
`reminders`, `reminder_occurrences` — recurring bill template + per-occurrence payment rows (unique `(reminderId, dueDate)`), plus `notifyDaysBefore` and `notificationsEnabled`.

---

## 7. Invariants (enforce in repositories / use cases)

1. `amountMinor > 0` for transactions, repayments, savings entries; validate before insert (Room can't express `CHECK` from annotations).
2. Money never enters the DB as floating point; UI converts only at format/parse boundaries.
3. Debt balances: `outstanding = principalMinor − Σ repayments`; `Σ repayments ≤ principalMinor`; reaching equality sets `status = SETTLED`, `settledAt` in the **same** `withTransaction`. No over-payment.
4. "I Owe" and "Owed to Me" are always displayed and summed separately; never a single net figure.
5. Savings balance per pot = `Σ(CONTRIBUTION) − Σ(WITHDRAWAL)`; withdrawals may not make it negative; goal progress capped at 100% in UI.
6. Debt and savings entries **never** write into `transactions` (they would distort budgets/income totals). Cash-flow coupling is out of scope.
7. Category rules: system categories may be archived, not deleted; user categories may be deleted only when unused, otherwise reassign transactions to another category inside one transaction. Name uniqueness is case-insensitive per `kind`.
8. Savings-type rules mirror categories: seeded types are non-deletable; user types are deletable only when unused, otherwise archive or reassign pots to `Other`.
9. Archived categories/types/goals are excluded from pickers and totals but remain valid for historical rows.
10. All SQL that feeds a screen must aggregate in SQL (`SUM`, `GROUP BY`), never by loading full tables into memory.
11. Payment-method rules mirror categories: seeded methods are non-deletable (archive only); user methods are deletable only when unused, otherwise reassign transactions inside one `withTransaction`. Name uniqueness is case-insensitive.
12. `uid` is generated once at row creation and never changed. System rows keep their seeded `sys-…` uid forever; repositories must never regenerate it on edit.

---

## 8. Seeding, schema export, migration policy

**8.1 Seeding** (`DatabaseSeeder`, invoked from `RoomDatabase.Callback.onCreate`, fixed IDs so identity survives reinstall/restore):
- Expense categories: Food & Dining, Groceries, Transport, Utilities, Shopping, Health, Entertainment, Rent, Other.
- Income categories: Salary, Freelance, Business, Investments, Other.
- Savings types: Bank FD, Bank RD, Stocks, Mutual Fund, Cash, Gold, Other.
- Payment methods: Cash, UPI, Credit Card, Debit Card, Net Banking.
- No transactions, debts, pots, or budgets are seeded.
- Seeded rows carry deterministic uids (`sys-category-…`, `sys-savings-type-…`, `sys-payment-method-…`) so a future cloud merge matches them instead of duplicating.

**8.2 Policy:** `exportSchema = true`; never `fallbackToDestructiveMigration`; no main-thread queries; every schema change bumps `version` and adds an explicit `Migration` + `MigrationTestHelper` test.

**8.3 Planned v1 → v2 migration:** create `reminders` + `reminder_occurrences`, add the 5th Add Entry mode, replace `RemindersPlaceholderScreen`, add WorkManager daily 09:00 check.

**8.4 Cloud backup readiness (reviewed 2026-11-24):**
- **Identity:** every table has a unique immutable `uid`; system rows use deterministic uids. Local `Long` PKs remain the storage key, so a backup snapshot restores as-is and a merge restores by matching `uid` and remapping parent FKs.
- **Natural-key collisions:** merge must match on `uid` first. The secondary unique indexes (`categories(name,kind)`, `savings_types(name)`, `payment_methods(name)`, `monthly_budgets(yearMonth)`) can still collide when two devices independently create the "same" row; the merge policy must merge or rename instead of blindly inserting.
- **Portable values:** money is `Long` paise, enums are `TEXT` names, timestamps are epoch millis/days — all backup-safe. No floats, no ordinals, no device-local IDs in payloads.
- **Full-snapshot backup:** self-consistent (FKs inside the file); no tombstones needed.
- **Merge sync (future):** still needs `updatedAt` on every table and deletion tombstones (`deletedAt` or a sync log) to resolve conflicts and propagate deletes; add as an explicit v2 schema change with a migration.
- **Restore safety:** a backup must record schema `version`; never restore a newer schema into an older app — run migrations forward instead. Restore writes must run in one `withTransaction`.
- **Privacy:** DB is unencrypted in Android cloud backup today (D5/D11). Any custom upload must encrypt the payload client-side before leaving the device.
- **Pre-release note:** the 2026-11-24 schema revision (uid + payment methods) redefined v1 before any release; no migration was shipped for the earlier dev-only v1.

---

## 9. DI graph (Hilt)

- `DatabaseModule`: `@Singleton` `AppDatabase` (lazy, seeded callback) + all DAOs.
- `DataModule`: `DataStore<Preferences>`, `KeystoreCipher`, `EncryptedValueStore`, `IndianCurrencyFormatter`, `AppClock` (`Clock.systemDefaultZone()`).
- `RepositoryModule`: `@Binds` repository interfaces → impls (interfaces; impls `@Inject constructor`).
- ViewModels: `@HiltViewModel`, consumed via `hiltViewModel()`; `MainViewModel` activity-scoped via `by viewModels()`.

---

## 10. Preferences & secure storage (D4)

- `PreferencesRepository` → `DataStore<Preferences>("user_prefs")` exposing `Flow<UserPreferences>`: `themeMode` (SYSTEM/LIGHT/DARK), `profileName`, `lastSelectedMonth`, `onboardingDone`, `notificationsEnabled` (reserved).
- PII (`profileName`) is stored encrypted: `KeystoreCipher` holds an AES-256/GCM key in `AndroidKeyStore` (alias `expense_art_pii`, no user-auth requirement); `EncryptedValueStore` persists `Base64(iv || ciphertext)`.
- Decryption failure (`AEADBadTagException`, `KeyPermanentlyInvalidatedException`, restored device) → treat value as absent, clear it, never log content. Non-sensitive settings stay plaintext so a lost key can't lock the user out.
- Never store passwords, tokens, or bank credentials anywhere.

---

## 11. State flow, navigation, UI contracts

**Shell**
```
MainActivity @AndroidEntryPoint
└─ ExpenseArtApp(mainViewModel)
   ├─ Scaffold(bottomBar = 4 destinations, floatingActionButton = FAB)
   └─ NavHost("home")
        home | expenses | debt_goals | reminders(placeholder)
```

- Routes: `home`, `expenses`, `debt_goals`, `reminders`. Top-level nav uses `saveState/restoreState`, `launchSingleTop`, `popUpTo(startDestination)`.
- Segmented tabs (Expense/Income, Debt/Goals, reminder triage) and dialogs are **local Compose state**, not routes.
- `MainViewModel` (activity-scoped) owns: `selectedYearMonth: StateFlow<YearMonth>`, `userPreferences: StateFlow<UserPreferences>`, `addEntrySheet: StateFlow<AddEntrySheetState>` (open + preset mode). Home and Expenses share the month; chips/filters stay screen-local.
- FAB preset mapping: Home→Expense, Expenses→Expense, Debt & Goals→Debt/Lend, Reminders→hidden (v2).

**Per-screen contract**
- `data class XUiState(status: LoadStatus, …, errorMessage: String?)` — immutable; `LoadStatus = Loading | Content | Empty | Error`; all four states rendered explicitly.
- Effects (snackbar, sheet close, navigate): `Channel<UiEffect>` → `receiveAsFlow()`.
- ViewModels: `combine(roomFlows, prefsFlow, periodFlow).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)`.
- Writes: UI intent → VM validation → use case → `repository.withTransaction { }` → Room invalidates flows → screens recompute. **No manual refresh calls.**
- Threading: `suspend`/`Flow` DAOs only; formatting on `Dispatchers.Default`; no blocking work on main.
- Compose collects with `collectAsStateWithLifecycle()`.

**Key Intents (per screen)**
`HomeIntent`: ChangePeriod, OpenBudgetEditor, OpenEntry, Retry.
`ExpensesIntent`: SelectKind, ChangePeriod, SelectCategory, OpenCategoryManager, OpenEntry(fab).
`DebtGoalsIntent`: SelectSegment, SelectDebtFilter, OpenDebtEditor, RecordRepayment, MarkSettled, OpenSavingsPot, OpenTypeManager, Contribute.
`AddEntryIntent`: SelectMode, UpdateAmountKey, AddQuickAmount(Long), ClearAmount, SelectCategory, SelectDate, SelectPaymentMethod, UpdateNote, SelectDebtDirection, Save, Dismiss.

**Reference `HomeUiState`**
```kotlin
data class HomeUiState(
    val status: LoadStatus,
    val greetingName: String?,
    val period: YearMonth,
    val incomeMinor: Long,
    val expenseMinor: Long,
    val incomeDeltaPercent: Int?,
    val expenseShareOfIncomePercent: Int?,
    val oweMinor: Long,            // never netted with owedToMeMinor
    val owedToMeMinor: Long,
    val totalSavingsMinor: Long,
    val activeGoalCount: Int,
    val budgetLimitMinor: Long?,
    val topCategories: List<CategorySpend>,
    val upcomingDues: List<DueItem>,   // debt due dates in v1
    val recentTransactions: List<TxnRow>,
    val errorMessage: String?
)
```

---

## 12. Screen → data mapping

| Screen / element | Data source |
|---|---|
| Home income/expense totals | `TransactionDao.observeTotalsByKind(from,to)` (`GROUP BY kind`) |
| Home "% of income", deltas | ViewModel math on current + previous month aggregates |
| Home debt card | `DebtDao.observeOutstandingByDirection()` (`SUM(principal − repaid) GROUP BY direction`) |
| Home savings card | `SavingsDao.observePots()` balances + count of `targetMinor != null` |
| Home budget bar | `BudgetDao.observe(yearMonth)` + expense total |
| Home top categories | `TransactionDao.observeSpendByCategory(from,to)` ordered desc, limit 4 |
| Home upcoming dues | `DebtDao.observeUpcomingDue(from, to)` |
| Home activity feed | `TransactionDao.observeRecent(limit = 5)` |
| Expenses ledger | `TransactionDao.observeRange(kind, from, to)` ordered `occurredAt DESC`; day grouping in VM via `DateRanges` |
| Category filter chips | `CategoryDao.observeByKind(kind, includeArchived = false)` |
| Category manager | `CategoryDao.observeAllByKind()` + `countUsage(categoryId)` |
| Payment method chips | `PaymentMethodDao.observeAll(includeArchived = false)` |
| Payment method manager | `PaymentMethodDao.observeAll(includeArchived = true)` + `countUsage(paymentMethodId)` |
| Debt list + filters | `DebtDao.observeDebtsWithContact(filter)` incl. repaid sum, overdue computed vs `AppClock.today()` |
| Repayment sheet | `DebtDao.observeRepayments(debtId)` + outstanding |
| Savings pots | `SavingsDao.observePotsWithBalance()` (JOIN + `SUM(CASE …)`) |
| Savings type manager | `SavingsDao.observeTypes(includeArchived = true)` + `countUsage(typeId)` |

---

## 13. Phases

### P0 — Build & DI spike (blocking)
- [ ] Add KSP + Hilt plugins, Room/Hilt/nav/DataStore/lifecycle deps to catalog and module.
- [ ] `minSdk = 26`; `ksp { arg("room.schemaLocation", …) }`.
- [ ] `ExpenseArtApplication` + manifest `android:name`; `MainActivity` `@AndroidEntryPoint`.
- [ ] Throwaway entity + DAO injected into a ViewModel; verify Hilt + KSP + Room + AGP 9 **built-in Kotlin** cooperate.

**Acceptance:** `assembleDebug` green; app launches; no KSP/Hilt errors.
**Verification:** `.\gradlew :app:assembleDebug` (and `testDebugUnitTest` if a test exists).
**Fallback if Hilt's transform fights AGP 9:** declare `org.jetbrains.kotlin.android` explicitly; if still blocked, stop and report — do not silently switch to manual DI.

### P1 — Data layer
- [ ] 10 entities per §6 with indices/FKs exactly as specified; `Converters` (enums ⇄ String); stable `uid` on every table.
- [ ] 6 DAOs (`Transaction`, `Category`, `Budget`, `Debt`, `Savings`, `PaymentMethod`) with the queries in §12.
- [ ] `AppDatabase(version = 1, exportSchema = true)`; `DatabaseSeeder`; commit `schemas/1.json`.
- [ ] Instrumented DAO test: insert → read → aggregate; FK RESTRICT behaviour for category/payment-method delete in use; uid uniqueness.

**Acceptance:** schema JSON committed; DAO tests pass; no main-thread access.
**Verification:** `.\gradlew :app:connectedDebugAndroidTest` (or unit-level DAO test with in-memory DB if no device).

### P2 — DI + domain
- [ ] Hilt modules (§9); repository interfaces + impls; mappers.
- [ ] Use cases: `AddTransaction`, `ObserveHomeSummary`, `ObserveLedger`, `SaveCategory`/`DeleteCategory`(reassign), `RecordRepayment`, `SettleDebt`, `SaveSavingsPot`, `ContributeToPot`, `SaveSavingsType`/`DeleteSavingsType`, `SetBudget`.
- [ ] Unit tests with fake repositories: over-payment rejection, settle transition, withdrawal-below-zero rejection, category reassign.

**Acceptance:** all listed invariants (§7) covered by tests; `assembleDebug` green.

### P3 — Settings, theme, app shell
- [ ] DataStore + Keystore implementation; `UserPreferences` flow; profile name editable.
- [ ] PRD color tokens in `Color.kt`; `Theme.kt` **`dynamicColor = false`**; light/dark + AMOLED canvas per PRD §5.1.
- [ ] `IndianCurrencyFormatter` (₹1,25,000) with unit tests.
- [ ] Scaffold: 4-destination bottom bar, center FAB, `NavHost`, placeholder screens (including Reminders "coming soon").

**Acceptance:** theme/name persist across restart; tabs switch with preserved state; no dynamic-color override.

### P4 — Home dashboard
- [ ] Aggregates + `HomeUiState` + `HomeViewModel`; 2×2 metric matrix, budget bar + editor sheet, top-spend tags, upcoming-dues card, activity feed.
- [ ] Loading/empty/error states; month selector shared with Expenses.

**Acceptance:** numbers reconcile with manual entries; empty month shows empty state, not zeros-only confusion.

### P5 — Expense & Income ledger + categories
- [ ] Segmented Expense/Income; period picker; category chips; day-grouped list with payment tags; income source summary.
- [ ] Category manager: add/rename/reorder/archive/delete-with-reassign.

**Acceptance:** category deletion of an in-use category requires reassignment and preserves all transactions.

### P6 — Debt & Savings
- [ ] Debt ledger with separate "I Owe"/"Owed to Me" totals, filters (All/I Owe/Owed to Me/Overdue/Settled), contact cards with progress, repayment sheet.
- [ ] Savings: pot list with progress, create/edit pot (type, institution, target?, deadline?), contribution/withdrawal sheet, **savings type manager**.

**Acceptance:** partial repayment moves progress without settling; full repayment settles; deleting a used type requires archive/reassign.

### P7 — Add Entry sheet
- [ ] 4 modes; numeric keypad with `+₹100/+₹500/+₹1,000/Clear`; tabular amount display; category/date/payment pickers; note; validation errors inline.
- [ ] Writes through the same use cases as the dedicated screens.

**Acceptance:** expense logged in < 5 s and visible on Home without manual refresh; debt/savings modes create ledger rows, not transactions.

### P8 — Reminders (v2, not v1)
v1→v2 migration, template + occurrence tables, triage screen, WorkManager 09:00 notification, permission banner + opt-in flow, 5th entry mode. Requires a migration test and notification permission handling.

---

## 14. Testing strategy

- **Unit (JVM):** use cases with fake repositories, `IndianCurrencyFormatter`, `DateRanges` (month boundaries, Today/Yesterday across DST/timezones).
- **Instrumented:** DAO queries (aggregates, JOINs, FK RESTRICT/CASCADE), seeding idempotence, in-memory `Room.inMemoryDatabaseBuilder`.
- **Migration:** `MigrationTestHelper` from v2 onward (queue it with P8).
- **UI:** Compose tests only for critical flows (add entry, repayment) once screens stabilize.
- No Hilt test runner until needed.

---

## 15. Risks

| Risk | Impact | Mitigation |
|---|---|---|
| Hilt/KSP vs AGP 9.5.2 built-in Kotlin | Blocks P0 | Spike first; explicit `kotlin-android` fallback; report if blocked |
| Compose BOM 2026.02.01 + navigation/DataStore version skew | Build breakage | Pin a single coherent set; verify resolution in P0 |
| `Locale("en","IN")` currency grouping differences | Wrong formatting | Own `IndianCurrencyFormatter` + unit tests |
| Timezone change shifting a transaction into an adjacent month | Report drift | Ranges computed from `AppClock`; document; acceptable for v1 |
| Room DB in cloud backup unencrypted (D5/D11) | Privacy gap vs PRD | Accepted; revisit with one `data_extraction_rules.xml` edit |
| Keystore key lost after restore | Missing profile name | Detect + clear + prompt re-entry |
| Hilt + Room init cost | Cold start > 700 ms | Lazy singletons, SQL aggregates, measure in P4 |

---

## 16. Progress tracker

| Phase | Status | Date | Notes |
|---|---|---|---|
| P0 Build & DI spike | Done | 2026-11-24 | KSP + Hilt + Room on AGP 9 built-in Kotlin; required `android.disallowKotlinSourceSets=false`; validated with real DAOs injected via Hilt instead of a throwaway entity |
| P1 Data layer | Done | 2026-11-24 | Revised pre-release: 10 tables incl. `payment_methods` + `uid` on every table; 6 DAOs, seeder (14 categories + 7 types + 5 payment methods), `schemas/…/1.json`; 10 instrumented DAO tests green |
| P2 DI + domain | Not started | — | — |
| P3 Settings/theme/shell | Not started | — | — |
| P4 Home | Not started | — | — |
| P5 Ledger + categories | Not started | — | — |
| P6 Debt & Savings | Not started | — | — |
| P7 Add Entry | Not started | — | — |
| P8 Reminders (v2) | Deferred | — | — |

---

## 17. Session log

_(append one line per feature session: date — phase — summary — build/test result)_

2026-11-24 — P0+P1 — Room DB setup: KSP/Hilt/minSdk 26 build config, 9 entities + converters, 5 DAOs with SQL aggregates, seeder, schema v1 export, DAO tests — `assembleDebug` OK, `testDebugUnitTest` OK, `connectedDebugAndroidTest` 8/8 pass, app launches without crash.
2026-11-24 — P1 revision (pre-release) — Added stable `uid` to all tables (deterministic system uids), replaced `PaymentMethod` enum with `payment_methods` table + FK, reviewed schema for cloud backup (§8.4), regenerated schema v1 — `assembleDebug` OK, `connectedDebugAndroidTest` 11/11 pass, app launches.
