# 🏠 AI Home Manager

A powerful Java CLI application that helps you manage your home — tracking maintenance tasks, bills, and appliances — with **Claude AI** providing smart diagnostics, seasonal checklists, spending analysis, and personalized home care advice

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔧 **Maintenance Tracking** | Track tasks by category, priority, and due date with overdue alerts |
| 💰 **Bill Management** | Log recurring bills, mark paid, auto-schedule next cycle |
| 🏷️ **Appliance Registry** | Track appliances, warranties, and end-of-life predictions |
| 🤖 **Home Health Report** | AI scores your home health and forecasts maintenance needs |
| 📋 **Seasonal Checklist** | AI generates season-specific maintenance tasks for your home |
| 🔍 **Problem Diagnosis** | Describe any issue — AI gives causes, fixes, and cost estimates |
| 📊 **Bill Analysis** | AI benchmarks your spending and finds savings opportunities |
| ❓ **Home Q&A** | Ask any home question with your full home data as context |
| 🔄 **Recurring Tasks & Bills** | Auto-reschedules recurring tasks and bills on completion |
| 💾 **Local JSON Storage** | All data stored privately at `~/.home-manager/` |

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- [Anthropic API key](https://console.anthropic.com/) (for AI features)

### Build

```bash
git clone https://github.com/YOUR_USERNAME/ai-home-manager.git
cd ai-home-manager
mvn clean package -DskipTests
```

### Run

```bash
export ANTHROPIC_API_KEY=sk-ant-...
java -jar target/ai-home-manager-jar-with-dependencies.jar
```

---

## 🖥️ Usage

```
╔══════════════════════════════════════════════════════╗
║          🏠  AI Home Manager  🤖                    ║
║   Track · Maintain · Save · Improve your home       ║
╚══════════════════════════════════════════════════════╝

  ── Tasks ──
  1  →  Add maintenance task
  2  →  View all tasks
  3  →  Mark task complete

  ── Bills ──
  4  →  Add bill
  5  →  View all bills
  6  →  Mark bill paid

  ── Appliances ──
  7  →  Add appliance
  8  →  View appliances

  ── AI Insights ──
  9  →  Home health report (AI)
  10 →  Seasonal checklist (AI)
  11 →  Diagnose a problem (AI)
  12 →  Analyze bill spending (AI)
  13 →  Ask a home question (AI)

  ── Settings ──
  14 →  View / edit home profile
  15 →  Dashboard summary
  0  →  Exit
```

### Dashboard

```
╔══════ HOME DASHBOARD ══════╗
  Tasks pending    : 4
  Overdue tasks    : 1
  Unpaid bills     : 3
  Bills due soon   : $342.50
  Appliances at EOL: 1
  Monthly running  : $487.00
╚═══════════════════════════╝
```

---

## 🗂️ Project Structure

```
src/
├── main/java/com/home/
│   ├── cli/
│   │   └── HomeManagerApp.java      # Entry point, 15-option menu
│   ├── model/
│   │   ├── MaintenanceTask.java     # Tasks with priority, status, recurrence
│   │   ├── Bill.java                # Bills with payment tracking
│   │   ├── Appliance.java           # Appliances with warranty/EOL logic
│   │   └── HomeProfile.java         # Home details (age, type, features)
│   ├── service/
│   │   ├── AIService.java           # 5 Claude AI analysis modes
│   │   ├── TaskService.java         # Task CRUD + priority sorting
│   │   └── BillService.java         # Bill CRUD + monthly cost calc
│   ├── storage/
│   │   └── StorageService.java      # JSON persistence to ~/.home-manager/
│   └── util/
│       └── ConsoleUI.java           # Colored ANSI terminal output
└── test/java/com/home/
    └── service/
        └── HomeManagerTest.java     # 14 JUnit 5 tests
```

---

## 🤖 AI Capabilities

| AI Feature | What Claude Does |
|---|---|
| **Home Health Report** | Scores your home, forecasts maintenance, spots risks, suggests savings |
| **Seasonal Checklist** | Tailored task list for your home type, age, and current season |
| **Problem Diagnosis** | Causes, severity, DIY fix, when to call a pro, cost estimate |
| **Bill Analysis** | Spending benchmarks, saving opportunities, energy tips |
| **Home Q&A** | Any question answered with full knowledge of your home |

---

## 📋 Task Categories

`PLUMBING` `ELECTRICAL` `HVAC` `APPLIANCE` `GARDEN` `CLEANING` `STRUCTURAL` `PEST` `SAFETY` `OTHER`

## 💳 Bill Types

`ELECTRICITY` `WATER` `GAS` `INTERNET` `INSURANCE` `MORTGAGE` `RENT` `HOA` `PHONE` `SUBSCRIPTION` `OTHER`

---

## 🔒 Privacy

All data is stored **locally** at `~/.home-manager/`. Only content you explicitly submit to AI features is sent to Anthropic's API.

---

## 🧪 Running Tests

```bash
mvn test
```

---

## 📄 License

MIT License — free to use, modify, and distribute.
