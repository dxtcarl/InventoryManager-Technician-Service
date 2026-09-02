# Teltech Inventory Manager 🔧✨

**Teltech Inventory Manager** is a premium, all-in-one ERP solution designed specifically for phone and laptop repair technicians. It bridges the gap between hardware inventory and service job management using modern Android technologies and AI-driven insights.

## 🚀 Features

- **Inventory Control**: Track parts (LCDs, batteries, etc.) with image support, auto-expiring URI handling, and smart category shortcuts.
- **Repair Service Bridge**: Link inventory parts directly to repair jobs. Automated stock deduction upon job creation.
- **AI Business Assistant**: Integrated **Gemini 1.5 Flash** that analyzes your real-time local database to provide stock alerts, earning summaries, and technical advice.
- **Advanced Analytics**: Dynamic charts visualizing 7-day stock flow, VIP customer ranking, and popular part usage.
- **Modern UX**: Built with Jetpack Compose following Material 3 guidelines, featuring floating premium dialogs and a sleek right-side navigation drawer.
- **Barcode Integration**: Instant SKU tracking using Google ML Kit.

## 🛠️ Technical Stack

- **UI**: Jetpack Compose (100% Declarative UI)
- **Language**: Kotlin + Coroutines/Flow
- **Architecture**: Clean Architecture (Domain, Data, Presentation layers)
- **Dependency Injection**: Dagger Hilt
- **Local Database**: Room (SQLite) with relational mapping
- **AI Engine**: Google Generative AI SDK (Gemini)
- **Vision**: Google ML Kit (Barcode Scanning)
- **Image Loading**: Coil
- **Charts**: Compose Charts (ir.ehsannarmani)

## 📁 Architecture Overview

The project follows **Clean Architecture** principles to ensure scalability and testability:
- **Presentation**: MVI/MVVM pattern with StateFlow for reactive UI updates.
- **Domain**: Pure Kotlin layer containing UseCases (e.g., `GetDashboardDataUseCase`) and Model definitions.
- **Data**: Repository implementations, Room DAOs, and the Gemini AI client.

## 🛠️ Setup

1. Clone the repository.
2. Create a `local.properties` file in the root directory.
3. Add your Gemini API key: `GEMINI_API_KEY=AQ.Ab8RN6INi3k5iK0WBFfT6hAbN61b_T7om0NzP6PSPFpTMo1OCg`.
4. Build and run the app.

