# 🚀 TNEA Smart College Predictor

A **full-stack intelligent college recommendation system** that helps students find the best engineering colleges based on cutoff, budget, district, and preferences.

---

## 🔥 Features

### 🎯 Smart Recommendation Engine

* Suggests **Dream / Moderate / Safe colleges**
* Based on:

  * Cutoff
  * Budget
  * District
  * Category
  * Branch
* Uses **weighted scoring algorithm**

---

### ⚡ Dynamic Preference System

Users can prioritize:

* 💼 High Placement
* 💰 Low Fees
* 🏆 Top Tier
* ⚖️ Balanced (default)

---

### 📊 Analytics Dashboard

* 📈 Placement comparison chart
* 🥧 Tier distribution chart
* Visual insights for better decision-making

---

### 🆚 College Comparison

* Compare up to **3 colleges**
* Side-by-side comparison:

  * Cutoff
  * Fees
  * Placement %
  * Tier
  * District

---

### 🌐 Full Stack Architecture

* Backend: Spring Boot (Java)
* Database: MySQL
* Frontend: HTML + CSS + JavaScript
* Charts: Chart.js

---

## 📁 Project Structure

```
tneaguider/
│
├── backend/        → Spring Boot API
├── frontend/       → UI (HTML, CSS, JS)
├── database/       → SQL schema + data
├── cleaned_dataset_final.csv → Final dataset
└── README.md
```

---

## 🧠 Recommendation Logic

Score is calculated using:

* Cutoff match
* Placement rate
* Fees affordability
* Tier importance

Dynamic weights based on user preference.

---

## 🗄️ Database Design

Table: `colleges`

Columns:

* id
* name
* district
* tier
* branch
* category
* cutoff
* fees
* placement_rate

Optimized with indexes:

* cutoff
* district
* tier

---

## ⚙️ Setup Instructions

### 1️⃣ Database Setup (MySQL Workbench)

Run:

```
database/schema.sql
database/insert_data.sql
```

---

### 2️⃣ Backend Setup

Navigate to backend:

```
cd backend
./mvnw spring-boot:run
```

Runs on:

```
http://localhost:8081
```

---

### 3️⃣ Frontend Setup

Open:

```
frontend/index.html
```

Or run via Live Server:

```
http://127.0.0.1:5500/frontend/index.html
```

---

## 🔌 API Endpoints

### 🔹 Recommend Colleges

```
POST /api/recommend
```

### 🔹 Compare Colleges

```
GET /api/compare?ids=1,2,3
```

---

## 🧪 Validation

✔ Clean dataset (400+ colleges)
✔ No duplicates
✔ Proper constraints
✔ Backend tested
✔ Error handling implemented

---

## ⚠️ Note About Dataset

* Based on real Tamil Nadu colleges
* Some values (fees/placement) are approximated
* Designed for **recommendation simulation**

---

## 🚀 Future Enhancements

* 🌐 Live fee scraping (real-time data)
* 🤖 AI-based prediction tuning
* 📍 Map-based college visualization
* 🎯 Personalized recommendations

---

## 👨‍💻 Author

**Vijayaragavan**
CSE Student | Full Stack Developer

---

## 🏁 Final Status

✅ Production-ready
✅ Hackathon-ready
✅ Scalable architecture
🔥 High-impact project

---

## 💡 Tagline

**"Find your perfect college in seconds with data-driven intelligence."**
