# FreeRoom: Real-Time Campus Room Availability Tracker

![Language](https://img.shields.io/badge/language-Java%2FJavaScript-blue.svg)
![Framework](https://img.shields.io/badge/framework-Spring%20Boot-green.svg)
![License](https://img.shields.io/badge/license-MIT-lightgrey.svg)

**FreeRoom** is a real-time, interactive campus map web application built to solve the universal problem of finding a free room to study or hold meetings. It aggregates official university lecture schedules and ad-hoc student check-ins to display room availability instantly.

***

## Key Features

* **Interactive Live Map:** Users can zoom and pan across a campus floor plan to see real-time room statuses, with intuitive color-coded overlays (Green = Free, Red = Occupied).
* **Official Schedule Sync:** A scheduled task periodically fetches official lecture times from Google Calendar and automatically blocks out officially occupied rooms.
* **Ad-Hoc Check-in:** For rooms not officially booked, students can check-in directly through the website to claim the room and declare their expected departure time.
* **Telegram Waitlists & Alerts:** Users link their web account to a Telegram bot using a secure sync code. If a room is full, they can join a waitlist, receive impending expiry warnings, and get notified immediately when the room frees up.
* **Gamification & Honor System:** Users gain reputation points for accurately checking out early, and lose points if their time expires while another user reports the room as empty.
* **Missed lecture viewer:** Users can view when current week's lecture topic will be held again if there will be any. Useful if user missed their class and want to catch up.
***

## Technical Architecture

### 1. Backend & Data Management
The core logic is driven by a **Java (Spring Boot)** backend, managing REST APIs, scheduled tasks, and database connections while strictly adhering to Object-Oriented Programming (OOP) principles. Data persistence is handled by **PostgreSQL**, which stores user accounts, room metadata, schedule logs, waitlist requests, and gamification scores.

### 2. Frontend & Interactive Mapping
The user interface is built with **React, JavaScript, and Tailwind CSS**. 

### 3. External Integrations
* **Google Calendar API:** Enables automated syncing of official university lecture schedules.
* **Telegram Bot API:** Handles waitlist operations, expiry warnings, and availability alerts directly to users without requiring phone numbers.

***

## Usage Options

* **Live Status:** Check the map before heading to campus to spot free areas.
* **Reserve on the Go:** Check-in via the website when entering a room to mark it as occupied.
* **Waitlist & Updates:** Connect to the Telegram bot to get in line for highly requested spaces.

***

## License
MIT License. Free for educational and research use.

***

## Running the Project

### Prerequisites
- Node.js (for the frontend client)
- Java 17+ (for the Spring Boot server)
- PostgreSQL Database

### Client (Frontend)
Navigate to the `client` directory to install dependencies and start the Vite development server:
```bash
cd client
npm install
npm run dev
```

### Server (Backend)
Navigate to the `server` directory. Make sure to configure your database settings first (refer to `.env.example`).
```bash
cd server
./mvnw spring-boot:run
```

### Database Connection & Credentials (PostgreSQL)

The Spring Boot backend connects directly to a cloud-hosted PostgreSQL instance provisioned via Supabase in the EU (Frankfurt) region.

* **Database Type:** PostgreSQL 15+
* **API URL:** `https://lahucjwdhglaxwdkiroz.supabase.co`
* **Client Anon Key:** (Shared privately via team chat)
[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/skmUAHf8)
# Final-Project
OOP ფინალური პროექტი
