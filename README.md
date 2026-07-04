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

### The Easy Way (Docker)
Got Docker? Perfect. You can spin up the entire app in one command.

1. Copy `.env.example` to `.env` and fill in your credentials (ask the team for the values).
2. Create a `secrets` folder in the project root.
3. Drop `clientSecret.json` and `serviceAccountKey.json` into that `secrets` folder.
4. Run `docker compose up --build`.

After the first build you can skip `--build` for faster starts: `docker compose up`.

Open `http://localhost:3000` when the terminal calms down. That's it.

### The Manual Way (For Active Dev)

#### Client (Frontend)
Navigate to the `client` directory to install dependencies and start the Vite development server:
```bash
cd client
npm install
npm run dev
```

#### Server (Backend)
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

## Testing & CI

CI runs automatically on every pull request (`.github/workflows/ci.yml`): backend build + tests, frontend lint + build.

### Where to add backend tests
Tests live in `server/src/test/java/ge/freeroom/freeroom/`, mirroring the main package. Any file ending in `Test` (or `Tests`) is picked up and run automatically, just add the file, CI runs it.

- **Pure logic** (no database): plain JUnit. No Spring context, no annotations needed.
- **Database-dependent** (repositories, queries): use `@DataJpaTest`. This automatically uses the in-memory H2 profile (`src/test/resources/application-test.properties`), never the real Supabase database. Tests run isolated and leave no trace.

Do not write tests that depend on the real database or external credentials, CI runs on a clean machine with no Supabase access.
