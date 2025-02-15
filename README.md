# 📝 Task Manager App

## 📖 Overview
The **Task Manager App** is a JavaFX-based application designed to help users efficiently manage their tasks, categories, priorities, and reminders. It follows the **MVC (Model-View-Controller) architecture**, ensuring a clean and scalable structure. Assignment as part of the Mutimedia Class in Electrical and Computer Engineering School of National Technical University of Athens.

## 🚀 Features
### **Task Management**
- Create, edit, and delete tasks.
- Assign a **title, description, category, priority, status, and deadline** to each task.
- Status updates: **In Progress, Completed, Delayed**.

###  **Category Management**
- Add, rename, and delete categories.

###  **Priority Management**
- Assign priorities to tasks.
- Modify priority names, and all related tasks update accordingly.


### **Reminder System**
- Set reminders for tasks.
- Notifications appear when a reminder is due.
- Automatically **removes past-due reminders**.

###  **Search & Filtering**
- Search for tasks using **title, category, and priority** (or any combination).
- View **only relevant tasks** based on applied filters.


---

## 🛠️ Installation
### **1️⃣ Prerequisites**
- **Java Development Kit (JDK 22 or higher)**  
- **JavaFX SDK** (Pre-bundled in `lib/` folder)
- **VS Code or any Java IDE** (Eclipse, IntelliJ, etc.)

### **2️⃣ Setting Up the Project**
1. **Extract the ZIP file**.
2. Open **VS Code** (or your IDE) and **navigate to the extracted folder**.
3. Ensure the **`lib/` folder contains JavaFX dependencies**.

---

## ▶️ Running the App
### **Run from VS Code Terminal**
1. **Compile the application** (if `bin/` folder is missing):
   ```sh
   javac -d bin -cp "lib/*" src/app/Main.java
