# Agricultural Intelligence Hub (Agrometis)

Agrometis is a comprehensive desktop platform designed to assist farmers with modern, data-driven agriculture. It combines a sleek, modern **JavaFX Desktop Application** with a secure and robust **Spring Boot Backend REST API**.

The application utilizes **Google Gemini AI** to provide personalized crop planning, disease identification, and expert advice, dynamically tailored to each farmer's specific profile (soil type, location, crops, and area).

---

## 🌟 Key Features

### Frontend (JavaFX)
- **Modern & Dynamic UI**: Features a premium design with a split-panel authentication screen, an animated 8-card dashboard with hover-scale effects, and seamless fade transitions between screens.
- **AI Assistant Hub**: Chat with AgriBot (powered by Gemini AI) for expert farming advice. Your farm profile is automatically injected into the AI context for personalized answers.
- **Yield & Financial Analytics**: Calculate estimated yield, gross revenue, costs, and net profit for your crops, visualized with interactive `BarChart` and `PieChart` components.
- **My Farm Profile**: Save your farm's identity (location, soil type, primary crops, size) which is seamlessly integrated into your workflow.
- **Weather Dashboard**: Access live weather forecasts tailored for your farm location.
- **📓 Crop Diary**: Track planting cycles and log daily farming activities (watering, fertilizing, harvesting) with cost tracking per activity.
- **🔔 Smart Alerts**: Receive weather warnings and farm notifications with severity-based color coding (Critical, Warning, Info) and unread count tracking.
- **📊 Yield History**: Record harvest data over multiple seasons, visualized with a `LineChart` trend graph and aggregate statistics (avg yield per crop, revenue by year).
- **📤 Export Report**: Generate and download a comprehensive farm summary report with preview functionality and `FileChooser` save dialog.

### Backend (Spring Boot 3)
- **Robust Security Layer**:
  - API Key protection for service endpoints.
  - JWT Authentication for users (short-lived Access Tokens + 7-day Refresh Tokens).
  - Gemini API key sent via `x-goog-api-key` header (not URL params).
  - Sanitized error messages — internal details logged via SLF4J, generic responses to clients.
  - CORS configured for localized security. Frame options set to `SAMEORIGIN`.
  - `@Valid` Request DTO validation across all endpoints.
- **AI Integration with Rate Limiting**: Features seamless integration with the Google Gemini API. Endpoints are protected by **Resilience4j Rate Limiting** to prevent quota exhaustion, complete with graceful fallbacks.
- **Automated Database Migrations**: Uses **Flyway** with 6 versioned migrations (users, crops, farm profiles, crop diary, alerts, harvest records).
- **Embedded Database**: Uses an **H2 Database** for effortless setup—no external installation required (console disabled by default in production).
- **Per-User Data Isolation**: All CRUD endpoints verify resource ownership before allowing access.
- **Data Aggregation**: Stream-based analytics for average yield per crop, total revenue by year, and yield trends.
- **Unit Tested**: Core services (`JwtService`, `MarketPriceService`, `CropMapper`) are covered by 15 automated unit tests.

---

## 📁 Project Structure

```
Java_SpringBoot_Project/
├── pom.xml                          # Frontend Maven config (com.rubaet.agrihub)
├── build_installer.bat              # Generates a Windows .exe installer via jpackage
├── src/main/java/
│   ├── module-info.java             # Java module descriptor (10 package exports)
│   └── com/rubaet/agrihub/
│       ├── Start.java               # Application entry point (splash → auth → dashboard)
│       ├── SceneTransition.java     # Fade-animated scene navigator
│       ├── Functions.java           # Dashboard controller (8-card grid)
│       ├── SplashController.java    # Animated splash screen
│       ├── service/
│       │   └── ApiService.java      # Centralized async HTTP client (20+ API methods)
│       ├── state/
│       │   └── AppState.java        # Global singleton (stage, JWT, user email)
│       └── ui/
│           ├── ai/AskAi.java        # AI chat controller (3 modes)
│           ├── auth/AuthController.java
│           ├── weather/WeatherController.java
│           ├── profile/FarmProfileController.java
│           ├── analytics/AnalyticsController.java
│           ├── diary/CropDiaryController.java       # Crop cycle & activity CRUD
│           ├── alerts/AlertsController.java          # Alert list with severity UI
│           ├── history/YieldHistoryController.java   # Harvest recording + LineChart
│           └── report/ExportReportController.java    # Report preview + file download
├── src/main/resources/com/rubaet/agrihub/
│   ├── Auth.fxml, Functions.fxml, Splash.fxml
│   ├── styles/                      # CSS design system (variables, components, animations)
│   └── views/                       # 8 feature FXML screens
└── backend-springboot/
    ├── pom.xml                      # Backend Maven config (com.rubaet:agri-backend)
    ├── src/main/java/com/rubaet/agri/
    │   ├── controller/              # REST controllers (9 controllers)
    │   │   ├── AiController         # Gemini AI (ask, plan-crop, identify-disease)
    │   │   ├── AuthController       # Register, login, refresh tokens
    │   │   ├── WeatherController    # OpenWeatherMap proxy with URL encoding
    │   │   ├── CropDiaryController  # Crop cycles + activity logs (paginated)
    │   │   ├── AlertController      # Alerts with unread count + test endpoint
    │   │   ├── HarvestController    # Harvest CRUD + aggregate summary
    │   │   ├── ReportController     # Farm summary report (text file download)
    │   │   ├── AnalyticsController  # Yield/revenue estimation
    │   │   └── FarmProfileController, CropController, SupportController
    │   ├── security/                # JWT + API Key dual-filter chain
    │   ├── service/                 # Business logic (CropService, MarketPriceService)
    │   ├── entity/                  # JPA entities (User, Crop, FarmProfile, CropCycle, ActivityLog, Alert, HarvestRecord)
    │   ├── repository/              # Spring Data JPA repositories (8 interfaces)
    │   ├── dto/                     # Request/Response DTOs with @Valid
    │   ├── config/                  # Data seeder, OpenAPI config
    │   └── exception/               # Global exception handler (sanitized errors)
    ├── src/main/resources/
    │   ├── application.properties   # Externalized config with env-var overrides
    │   └── db/migration/            # 6 Flyway migrations (V1–V6)
    └── src/test/java/com/rubaet/agri/
        ├── security/JwtServiceTest.java        # 5 tests
        ├── service/MarketPriceServiceTest.java  # 6 tests
        └── util/CropMapperTest.java             # 4 tests
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven (`mvnw` is included in the project)

### 1. Setup Environment Variables & Secrets
The backend relies on API keys for AI and Weather features. In the `backend-springboot` directory, create a `secrets.properties` file and add your keys:

```properties
app.weather.api-key=YOUR_OPENWEATHER_API_KEY
app.ai.gemini-api-key=YOUR_GEMINI_API_KEY
app.jwt.secret=YOUR_LONG_RANDOM_SECRET_FOR_JWT
app.api-key=agri_hub_desktop_client_secret_2026
```
*(Note: `secrets.properties` is git-ignored for security.)*

The desktop frontend can also load the API key from an environment variable instead of using the compiled default:
```bash
set AGRIHUB_API_KEY=agri_hub_desktop_client_secret_2026
```

### 2. Start the Backend Server
The Spring Boot backend must be running before you open the desktop app. It runs on `http://localhost:8080`.

```bash
cd backend-springboot
# Windows
..\mvnw.cmd spring-boot:run
# Mac/Linux
../mvnw spring-boot:run
```
*(Flyway will automatically create the schema and seed the database on startup.)*

### 3. Launch the JavaFX Frontend
In a new terminal window, return to the project root and start the desktop client:

```bash
# Windows
.\mvnw.cmd javafx:run
# Mac/Linux
./mvnw javafx:run
```

### 4. Run Tests
```bash
cd backend-springboot
# Windows
..\mvnw.cmd test
# Mac/Linux
../mvnw test
```

---

## 🏗️ Architecture

- **Backend**: Layered Architecture (Controller → Service → Repository). Built with Spring Boot 3, Spring Data JPA, Hibernate, Flyway, and Resilience4j.
- **Frontend**: JavaFX using FXML for views. Navigation is handled centrally via a `SceneTransition` utility that provides smooth fade animations. Uses a global `AppState` singleton to manage authentication tokens and user context.
- **Package Identity**: `com.rubaet.agrihub` (Desktop) / `com.rubaet.agri` (Backend).

---

## 🛠️ Security Hardening
- Secrets are externalized via `secrets.properties` (git-ignored) with environment variable overrides.
- Gemini API key transmitted via HTTP header (`x-goog-api-key`), not URL query parameters.
- Passwords encrypted with BCrypt.
- Error messages sanitized — internal details logged via SLF4J, generic responses sent to clients.
- H2 console disabled by default (`H2_CONSOLE_ENABLED=false`).
- Frame options set to `SAMEORIGIN` (clickjacking prevention).
- URL parameters encoded via `UriComponentsBuilder` (injection prevention).
- Strict `@Valid` input validation on all DTOs and Controller parameters.
- Dual-filter chain architecture (`ApiKeyAuthFilter` + `JwtAuthFilter`) securing different scopes of the API.
- Per-user data isolation — all endpoints verify resource ownership before returning data.

---

*v3.0 — Powered by Google Gemini AI*
