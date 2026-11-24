# Expense Art — Product Requirements Document (PRD) & Product Brief

**Product Name:** Expense Art  
**Category:** Personal Finance & Money Management  
**Target Platform:** Native Android (Kotlin & Jetpack Compose)  
**Secondary Targets:** Android Tablets, Foldables, Desktop-sized windows  
**Primary Market:** India (Default Currency: ₹ INR, Indian numbering scale: e.g., ₹1,25,000)  
**Design Reference:** Samsung One UI 6.0+ Design Language  
**Document Version:** 1.0 (Production Specification)  
**Status:** Approved for Implementation

---

## 1. Executive Summary & Vision

### 1.1 Problem Statement
Most modern personal finance apps either overwhelm users with automated bank SMS syncs, account scraping, aggressive ad placements, and confusing credit score upsells, or they feature clunky, spreadsheet-like interfaces that make manual tracking tedious. Furthermore, many apps merge borrowed and lent funds into ambiguous net balances, leading to poor debt tracking.

### 1.2 Product Vision
**Expense Art** is a privacy-first, manual-entry personal finance manager tailored for individual users in India. Inspired by Samsung One UI’s ergonomic layout, the app optimizes for single-handed usability, transparent financial categorization, instant entry workflows (< 5 seconds per transaction), and crystal-clear separation of debts, savings, recurring bill reminders, and monthly budgets.

### 1.3 Target Audience
- Working professionals, freelancers, and students in India managing personal budgets.
- Users who value data privacy and intentionally avoid granting banking SMS or read access.
- Individuals actively tracking peer-to-peer lending/borrowing (friends, colleagues, flatmates) alongside regular household bills and savings goals.

---

## 2. Core Architectural & Product Principles

1. **Zero Unapproved Automation / Privacy First:**
    - No bank API integrations, account aggregators, or automated SMS scraping.
    - All financial ledgers and settings are strictly local (Room DB + Encrypted SharedPreferences).
2. **One UI Ergonomic Layout:**
    - Upper screen area dedicated to viewing and large heading context; lower 60% dedicated to comfortable interaction and thumb reachability.
    - Consistent 4dp spatial grid, soft rounded cards (16–20dp), and subdued divider lines.
3. **Strict Financial Clarity:**
    - Borrowed money ("I Owe") and Lent money ("Owed to Me") must **never** be merged into a single net debt figure.
    - Tabular, monospaced-style figures for amounts to ensure scanability.
4. **Native Android First-Class Citizen:**
    - Built 100% in Jetpack Compose, targeting Material 3 / One UI design standards, complete with dynamic Dark Theme (AMOLED `#101114`) and Light Theme (`#F7F8FA`).

---

## 3. Information Architecture & Navigation

The application strictly maintains **four primary bottom navigation destinations** plus a central context-aware Floating Action Button (`+`):

```
                     ┌───────────────────────────────┐
                     │          Expense Art          │
                     └───────────────┬───────────────┘
                                     │
    ┌────────────────┬───────────────┼───────────────┬────────────────┐
    ▼                ▼               ▼               ▼                ▼
[1. Home]    [2. Expenses]   [3. Debt & Goals] [4. Reminders]   [Add Entry (+)]
 - Financial  - Expense Ledg. - Debt & Lend     - Overdue Bills  - Bottom Sheet
   Overview   - Income Ledg.  - Savings Goals   - Upcoming Due   - 5-Way Mode
 - Budget Bar - Category Bar  - Settlement Log  - Auto-mandates  - Quick Chips
 - Reminders  - Month Picker  - Progress Bars   - Push Pings     - Keypad Focus
```

---

## 4. Detailed Feature Specifications

### 4.1 Home Dashboard
- **Header:** Personalized greeting (`"Good morning, Rahul ✨"`), operational health badge (`On Track`), and persistent filter dropdowns (Reporting period: *This Month (Oct 2024)*, Category filter).
- **4-Card Metric Matrix (2x2 Grid):**
    1. *Total Income:* Monthly inflow with comparative delta (`+12% vs last mo`).
    2. *Total Expenses:* Incurred spend with percentage of total income (`38% of income`).
    3. *Net Debt Summary:* Dual breakdown preview (`Owe ₹18k · Owed ₹12k`).
    4. *Total Savings:* Accumulated liquid and goal reserves (`3 active goals`).
- **Monthly Budget Plan Bar:** Visual track meter (e.g., ₹32,450 spent of ₹45,000; ₹12,550 left) with top spending tags (Food, Groceries, Transport, Utilities).
- **Actionable Reminder & Activity Feed:** High-priority upcoming dues and recent chronological transaction rows.

### 4.2 Expense & Income Ledger
- **Segmented Control:** Seamless tab toggle between *Expense* (default) and *Income*.
- **Expense View:**
    - Date navigator with period selector (e.g., *October 2024*).
    - Category filter chips: *All, Food & Dining, Groceries, Transport, Utilities, Shopping*.
    - Visual category allocation bar with month-over-month savings benchmark (`₹4,120 less than September`).
    - Chronological transaction lists grouped by day (`Today`, `Yesterday`, `23 Oct`) with payment method tags (UPI, GPay, Credit Card, Net Banking).
- **Income View:**
    - Aggregated inflow summary with source attribution: *Salary, Freelance, Business, Investments, Other*.

### 4.3 Debt & Savings (Peer Ledger & Goals)
- **Segmented Navigation:** *Debt & Lend* vs. *Savings Goals*.
- **Debt & Lend Ledger:**
    - Two prominent non-conflicting metrics: **"I Owe (Borrowed)"** (Red) vs. **"Owed to Me (Lent)"** (Green).
    - Status filters: *All Records, I Owe (Borrowed), Owed to Me (Lent), Overdue, Settled*.
    - Individual contact cards with repayment completion bars, due dates, and single-tap `"Record Repayment"` bottom sheet.
- **Savings Goals:**
    - Target amount, current balance, deadline, and progress meter.
    - Standard archetypes: Emergency Fund (6 months buffer), Gadgets/Electronics, Vacation/Travel.

### 4.4 Payment Reminders & Scheduled Bills
- **Filters & Triage:** *Upcoming, Overdue, Completed*.
- **Urgent Action Warning:** High-contrast alert card for bills past due date with `"Mark Paid"` and `"Reschedule"` actions.
- **Due Date Timeline:** Grouped recurring commitments (Electricity, Broadband, Rent, Credit Card Auto-mandates) with countdown tags (`Due in 2 days`, `Due Nov 5`).
- **Notification Opt-In:** Explanatory banner highlighting local notification benefits prior to prompting OS permission.

### 4.5 Global "Add Entry" Bottom Sheet
- **Modality:** One UI 24dp top-radii bottom sheet with handle, overlaying a dimmed canvas.
- **5 Transaction Modes:** *Expense, Income, Debt/Lend, Savings, Reminder*.
- **Instant Numeric Input:** Tabular currency display (`₹ 1,450.00`) with one-tap increment pills (`+₹100`, `+₹500`, `+₹1,000`, `Clear`).
- **Contextual Attributes:** Category picker, transaction date/time picker, payment method selector (Cash, UPI, Credit/Debit Card, Net Banking), and optional note field.

---

## 5. UI/UX Design System Specification

### 5.1 Color Palette
| Token | Light Theme | Dark Theme (One UI Dark Canvas) | Usage |
|---|---|---|---|
| **Primary Brand** | `#2563EB` | `#76A7FF` | Key buttons, active tabs, FAB, progress bars |
| **Canvas Background** | `#F7F8FA` | `#101114` | Global background canvas |
| **Card Surface** | `#FFFFFF` | `#1B1D22` | Summary cards, ledger rows, bottom sheets |
| **Input / Wells** | `#F0F2F5` | `#15161A` | Input fields, filter chip inactive states |
| **Text Primary** | `#17191D` | `#F4F5F7` | Headings, amounts, primary labels |
| **Text Secondary** | `#68707D` | `#A7ADB8` | Timestamps, payment methods, captions |
| **Positive / Income** | `#16875D` | `#55C89A` | Income figures, settled debts, lent amounts |
| **Negative / Expense** | `#D94A4A` | `#FF7777` | Expense values, overdue notices, borrowed sums |
| **Warning / Pending** | `#D68A17` | `#F2B95C` | Budget warnings, pending reminders |

### 5.2 Typography System
- **Typeface:** Roboto / Roboto Flex (Android system native).
- **Scale:**
    - *Large Display / Heading:* 28sp–32sp, Bold (One UI Screen Titles).
    - *Section Headings:* 18sp–20sp, Semi-Bold.
    - *Financial Amounts:* 20sp–26sp, Bold, Tabular Figures (`font-feature-settings: "tnum"`).
    - *Body / List Items:* 14sp–15sp, Regular & Medium.
    - *Captions & Badges:* 11sp–12sp, Medium.

### 5.3 Spacing & Shape Metrics
- **Base Grid:** 4dp. (Padding increments: 8dp, 12dp, 16dp, 24dp, 32dp).
- **Container Radii:** 16dp–20dp for content cards; 24dp for modal bottom sheets; 999dp (pill) for action chips and buttons.
- **Touch Target:** Minimum 48x48dp for all interactive touch zones.

---

## 6. Technical Stack & Implementation Architecture

- **Language:** Kotlin 2.0+
- **UI Framework:** Jetpack Compose (Material 3 + custom One UI design token wrappers)
- **Architecture Pattern:** MVI / MVVM with Clean Architecture:
    - `UI Layer`: Stateless Composables consuming `StateFlow<ViewState>` and emitting `UserIntent`
    - `Domain Layer`: UseCases (e.g., `AddExpenseUseCase`, `CalculateDebtSummaryUseCase`)
    - `Data Layer`: Local Room Database with SQLite indexes on `timestamp`, `category_id`, and `type`
- **Local Key-Value Storage:** Jetpack DataStore / EncryptedSharedPreferences (for dark theme, user preferences, and profile name)
- **Background Scheduling:** Android `WorkManager` for daily 09:00 AM local reminder notification triggers
- **No Third-Party Analytics / Trackers:** Zero telemetry for pure privacy assurance.

---

## 7. Success Metrics & Release Criteria

1. **Transaction Logging Velocity:** Complete end-to-end expense logging in < 5 seconds.
2. **Crash-Free Sessions:** > 99.9% crash-free rate across Android 10 (API 29) through Android 15 (API 35).
3. **Contrast Compliance:** All text and critical financial UI elements meet WCAG 2.1 AA standards (minimum 4.5:1 ratio) in both Light and Dark themes.
4. **Cold Start Time:** Initial launch to interactive dashboard state in < 700ms on mid-tier Android devices.
