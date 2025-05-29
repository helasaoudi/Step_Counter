# Step Counter App

This is a native Android application developed using **Android Studio**. It functions as a personal step counter, allowing users to set fitness goals, track their activity, and view progress on a map.

---

## 🚀 Features

The app is structured around **4 main fragments** accessible via a bottom menu:

### 1. 🎯 Target Fragment
- Allows the user to set personal fitness goals:
  - Number of steps
  - Distance (in meters)
  - Duration (in minutes)
  - Calories

### 2. 🏃 Running Fragment
- Displays the user's live progress:
  - Steps taken
  - Time elapsed
  - Distance covered
  - Calories burned
- Uses the device’s **accelerometer sensor** to detect and count steps in real-time.

### 3. 📊 Statistics Fragment
- Shows activity statistics for the **last 7 days**, including:
  - Total steps per day
  - Calories burned
  - Duration and distance trends

### 4. 🗺️ Map Fragment
- Integrates **Google Maps API**.
- Uses **current location** to allow users to select a destination.
- Displays:
  - Estimated travel time
  - Distance between current location and selected destination

---

## 🧠 Technologies Used

- **Android Native (Java/Kotlin)**
- **Google Maps API**
- **Accelerometer Sensor** for step detection
- **Local Storage** (e.g., `SharedPreferences` or `Room`) for data persistence
- **Fragments & Navigation Components**
- **MVVM/MVC pattern** (if applicable)

---

## 📱 How to Run

1. Clone this repository to your local machine:
   ```bash
   git clone https://github.com/your-username/step-counter-app.git
