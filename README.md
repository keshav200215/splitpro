# SplitPro – Expense Sharing Platform

SplitPro is a full-stack expense sharing platform inspired by Splitwise that allows users to track shared expenses, split costs across groups, and settle balances with the minimum number of transactions.

The platform supports group expense tracking, dynamic balance computation, email reminders for outstanding debts, and secure authentication.

---

## 🚀 Features

* **Group Expense Management**

  * Create and manage expense groups
  * Add members to groups
  * Track all shared expenses

* **Smart Expense Splitting**

  * Split expenses equally or by custom amounts
  * Real-time balance calculation between users
  * Settlement functionality to clear debts

* **Activity Timeline**

  * View a chronological history of all expenses and settlements
  * Edit or delete expenses when necessary

* **Secure Authentication**

  * JWT-based stateless authentication
  * Protected API routes using Spring Security

* **Email Notifications**

  * Automated weekly debt reminder emails
  * Implemented using Spring scheduled jobs and SMTP

* **Optimized User Experience**

  * Parallel API fetching for faster dashboard loading
  * Debounced user search when adding group members

---

## 🏗 Architecture

SplitPro follows a **modern full-stack architecture**:

Frontend (React)
⬇
REST APIs (Spring Boot)
⬇
PostgreSQL Database

Key backend components:

* Spring Boot REST Controllers
* Service Layer for business logic
* JPA/Hibernate for ORM
* JWT Authentication filter
* Scheduled background jobs for reminders

---

## 🛠 Tech Stack

### Frontend

* React
* React Router
* Axios
* Tailwind CSS

### Backend

* Spring Boot
* Spring Security
* JWT Authentication
* Spring Data JPA / Hibernate

### Database

* PostgreSQL

### Infrastructure / Deployment

* Railway (Backend)
* Vercel (Frontend)

### Additional Tools

* Scheduled Jobs (Spring @Scheduled)
* SMTP Email Integration

---

## 📂 Project Structure

```
splitpro
│
├── backend
│   ├── controller
│   ├── service
│   ├── repository
│   ├── entity
│   ├── config
│   └── security
│
├── frontend
│   ├── components
│   ├── pages
│   ├── config
│   └── styles
```

---

## ⚙️ Environment Variables

Backend requires the following environment variables:

```
DB_URL=your_postgres_url
DB_USER=your_db_user
DB_PASSWORD=your_db_password

JWT_SECRET=your_jwt_secret

MAIL_USER=your_email
MAIL_PASS=your_app_password
```

Frontend requires:

```
REACT_APP_API_URL=https://your-backend-url
```

---

## ▶️ Running Locally

### 1. Clone the repository

```
git clone https://github.com/your-username/splitpro.git
cd splitpro
```

---

### 2. Start Backend

```
cd backend
mvn spring-boot:run
```

Backend runs at:

```
http://localhost:8080
```

---

### 3. Start Frontend

```
cd frontend
npm install
npm start
```

Frontend runs at:

```
http://localhost:3000
```

---

## 📊 Key Functionalities Implemented

* Expense splitting and balance computation
* Minimum transaction settlement
* Secure JWT authentication
* Group member management
* Activity timeline for expenses
* Scheduled background jobs for debt reminders


## 📈 Future Improvements

* Push notifications for new expenses
* Graph-based visualization of group balances
* Mobile responsive UI improvements
* Aggregated group details API to reduce network calls

---

## 👨‍💻 Author

**Keshav Bhotika**

GitHub: https://github.com/keshav200215

---

## ⭐ Acknowledgements

Inspired by Splitwise and built as a full-stack system design project to explore scalable expense management workflows.
