
# WalletWay

WalletWay is a personal budgeting Android application developed as a prototype for PROG7313 Part 2. The app enables users to manage their daily expenses, set financial goals, and analyze their spending habits through a simple and intuitive interface.

---

## Features

- **User Authentication**
  - Register and login functionality with clean UI.
- **Expense Management**
  - Add expenses with description, amount, category, date (via DatePicker), and optional image.
  - View, edit, and delete expenses.
  - Category and date range filters.
  - Confirmation dialog before deletion.
- **Category Management**
  - Create and delete custom categories.
- **Budget Goals**
  - Set minimum and maximum budget goals.
  - Visual progress bar for budget monitoring.
- **Category Summary**
  - View total spending by category over a selected period.
  - Filter category summaries by category and date.
- **UI Improvements**
  - DatePicker dialogs for easier date input.
  - Snackbar error feedback for incomplete input.
  - Improved login screen layout and branding.
  - Material Design and consistent layout across screens.

---

## Tech Stack

- **Language**: Kotlin
- **Framework**: Android Jetpack Compose
- **Database**: Room (SQLite)
- **Image Loading**: Coil
- **Architecture**: Compose + ViewModel-style state handling
- **IDE**: Android Studio

---

## Setup Instructions

### Requirements

- Android Studio Giraffe / Hedgehog or newer
- Android SDK 24 or above

### Installation Steps

1. Clone the repository:

```bash
git clone https://github.com/VCDN-2025/prog7313-part-2-ST10300672.git
```

2. Open the project in **Android Studio**.

3. Let Gradle sync automatically. If it doesn’t, click **File > Sync Project with Gradle Files**.

4. Click **Run > Run App** to deploy the app on an emulator or physical Android device.

---

## App Structure

```
com.example.walletway
│
├── MainActivity.kt                # Navigation logic and screen state
├── AuthScreen.kt                 # Login/Register UI
├── BudgetScreen.kt               # Add expenses, view goals
├── ExpenseListScreen.kt          # Filter and manage expenses
├── CategoryScreen.kt             # Manage expense categories
├── CategorySummaryScreen.kt      # View summary by category
├── GoalScreen.kt                 # Set spending goals
│
├── data/
│   ├── AppDatabase.kt
│   ├── dao/
│   │   ├── ExpenseDao.kt
│   │   ├── CategoryDao.kt
│   │   └── GoalDao.kt
│   └── entity/
│       ├── ExpenseEntity.kt
│       ├── CategoryEntity.kt
│       └── GoalEntity.kt
```

---

## Future Enhancements

- Firebase Authentication and Realtime Database
- Charting with Jetpack Compose Graphs
- Monthly summary exports (PDF/CSV)
- Gamified badges or user streaks for saving

---

## Developer

- **Student Members**: ST10300672: Brayden Pillay, ST10302369: Lethabo Penniston, ST10288560: Luke James Lutchmiah 
- **Module**: PROG7313 - Programming 3C  
- **Submission**: Part 2 – App Prototype  
- **GitHub Repo**: [WalletWay Prototype](https://github.com/VCDN-2025/prog7313-part-2-ST10300672)
- **YouTube Link**: https://youtu.be/ZMbWNDCHyCE

---


