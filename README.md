# WalletWay

WalletWay is a personal budgeting Android application developed for our final POE project. The app enables users to manage their daily expenses, set financial goals, and analyze their spending habits through a simple and intuitive interface.

---

##  Features

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

- **Spending Chart (New)**
  - Interactive bar graph showing category spending trends over time.
  - Min and Max goal lines visualized.
  - Date range filters and color-coded bars per category.

- **Custom Feature: Chart Export**
  - Export the spending chart as an image to your device gallery with a confirmation message.

- **UI Improvements**
  - DatePicker dialogs for easier date input.
  - Snackbar error feedback for incomplete input.
  - Improved layout, consistent Material Design across screens.
  - Scrollable UI for accessibility on all screen sizes.

---

##  Tech Stack

- **Language**: Kotlin  
- **Framework**: Android Jetpack Compose  
- **Database**: Firebase Firestore (replaced RoomDB)  
- **Image Loading**: Coil  
- **Charting**: MPAndroidChart  
- **IDE**: Android Studio

---

##  Setup Instructions

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

##  App Structure

```
com.example.walletway
│
├── MainActivity.kt                  # Navigation logic and screen state
├── AuthScreen.kt                   # Login/Register UI
├── BudgetScreen.kt                 # Add expenses, view goals
├── ExpenseListScreen.kt            # Filter and manage expenses
├── CategoryScreen.kt               # Manage expense categories
├── CategorySummaryScreen.kt        # View summary by category
├── CategorySpendingChartScreen.kt  # Bar graph visualization and export
├── GoalScreen.kt                   # Set spending goals
│
├── firestore/
│   ├── FirestoreService.kt
│   ├── FirestoreCategoryService.kt
│   └── FirestoreGoalService.kt
│
├── entity/
│   ├── ExpenseEntity.kt
│   ├── CategoryEntity.kt
│   └── GoalEntity.kt
```

---

##  Feature Enhancements in Part 3

-  **Firestore Integration** – Replaced local RoomDB with online Firestore database.  
-  **Category Spending Chart** – Bar graph showing spending by category and timeline.  
-  **Custom Feature** – Export your spending chart to the device gallery with confirmation feedback.

---

##  Developer Info

- **Student Members**:  
  ST10300672: Brayden Pillay  
  ST10302369: Lethabo Penniston  
  ST10288560: Luke James Lutchmiah

- **Module**: PROG7313 - Programming 3C  
- **Submission**: Part 2 – App Prototype

- **GitHub Repo**: [Wallet Way Final](https://github.com/ST10300672/PROG7311_WalletWay.git)
- **YouTube Demo**: [Youtube Demo](https://youtu.be/5qsFBbtmg9s?si=jbj17FPmWFzd5Pw0)
- **FireBase Console**: [FireBase Console](https://console.firebase.google.com/project/walletway-2c665/firestore/databases/-default-/data/~2Fcategories~2F01b52aad-562d-4b59-b4cb-1a767370416d)
---


