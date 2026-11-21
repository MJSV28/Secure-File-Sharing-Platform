# Secure File Sharing Platform

A full-stack web application built using **Java, Spring Boot, React.js, and MySQL** that provides secure file management and sharing through an authenticated system.

## About the Project

The Secure File Sharing Platform allows users to securely access and manage files through a web-based application.

The backend is developed using Spring Boot and provides REST APIs for file operations and authentication. The frontend is developed using React.js to provide a user-friendly interface. MySQL is used to store application data.

## Features

- User registration and login
- JWT-based authentication
- Secure access to protected APIs
- File upload
- File download
- File deletion
- User-based file management
- React-based user interface
- Backend and frontend integration

## Technologies Used

### Backend
- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- JWT
- MySQL
- Maven

### Frontend
- React.js
- JavaScript
- HTML5
- CSS3
- Vite
- Axios (if used for API requests)

### Tools
- Eclipse IDE
- MySQL Workbench
- Postman
- Git
- GitHub

## Project Structure

```text
Secure-File-Sharing-Platform/
│
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── frontend/
│   ├── src/
│   ├── public/
│   ├── package.json
│   ├── vite.config.js
│   └── README.md
│
└── README.md
```

*The structure above is an example. Keep the folder names consistent with your actual repository.*

## How It Works

1. A user registers or logs in through the frontend.
2. The backend handles authentication and generates a JWT token.
3. The frontend uses the token when accessing protected APIs.
4. Authenticated users can perform supported file operations.
5. The backend processes requests and interacts with the database and file storage.

## Backend Setup

1. Clone the repository:

```bash
git clone https://github.com/MJSV28/Secure-File-Sharing-Platform.git
```

2. Open the backend project in Eclipse or another Java IDE.

3. Configure your MySQL database and update the database settings in `application.properties`.

4. Run the Spring Boot application.

Backend URL:

```text
http://localhost:8888
```

## Frontend Setup

1. Open the frontend folder in VS Code or another editor.

2. Install dependencies:

```bash
npm install
```

3. Start the development server:

```bash
npm run dev
```

4. Open the local URL displayed in the terminal.

## Learning Outcomes

- Developed REST APIs using Spring Boot.
- Implemented JWT-based authentication.
- Worked with Spring Security.
- Integrated Spring Data JPA with MySQL.
- Connected a React frontend with a Spring Boot backend.
- Practiced full-stack application development.

## Developer

**Malledi J.S.V. Praneeth**

- GitHub: [MJSV28](https://github.com/MJSV28)
- Email: praneethmjsv@gmail.com

---

⭐ If you find this project useful, feel free to explore the repository.
