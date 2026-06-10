package com.home.util;

import org.fusesource.jansi.AnsiConsole;
import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;

public class ConsoleUI {

    public static void init()     { AnsiConsole.systemInstall(); }
    public static void shutdown() { AnsiConsole.systemUninstall(); }

    public static void printBanner() {
        System.out.println(ansi().fg(CYAN).bold().a("""
                ╔══════════════════════════════════════════════════════╗
                ║          🏠  AI Home Manager  🤖                    ║
                ║   Track · Maintain · Save · Improve your home       ║
                ╚══════════════════════════════════════════════════════╝
                """).reset());
    }

    public static void printMenu() {
        System.out.println(ansi().fg(YELLOW).bold().a("\n──────── MAIN MENU ────────").reset());

        System.out.println(ansi().fg(CYAN).bold().a("  ── Tasks ──").reset());
        System.out.println("  " + g("1") + "  →  Add maintenance task");
        System.out.println("  " + g("2") + "  →  View all tasks");
        System.out.println("  " + g("3") + "  →  Mark task complete");

        System.out.println(ansi().fg(CYAN).bold().a("\n  ── Bills ──").reset());
        System.out.println("  " + g("4") + "  →  Add bill");
        System.out.println("  " + g("5") + "  →  View all bills");
        System.out.println("  " + g("6") + "  →  Mark bill paid");

        System.out.println(ansi().fg(CYAN).bold().a("\n  ── Appliances ──").reset());
        System.out.println("  " + g("7") + "  →  Add appliance");
        System.out.println("  " + g("8") + "  →  View appliances");

        System.out.println(ansi().fg(CYAN).bold().a("\n  ── AI Insights ──").reset());
        System.out.println("  " + g("9") + "  →  Home health report (AI)");
        System.out.println("  " + g("10") + " →  Seasonal checklist (AI)");
        System.out.println("  " + g("11") + " →  Diagnose a problem (AI)");
        System.out.println("  " + g("12") + " →  Analyze bill spending (AI)");
        System.out.println("  " + g("13") + " →  Ask a home question (AI)");

        System.out.println(ansi().fg(CYAN).bold().a("\n  ── Settings ──").reset());
        System.out.println("  " + g("14") + " →  View / edit home profile");
        System.out.println("  " + g("15") + " →  Dashboard summary");
        System.out.println("  " + r("0")  + "  →  Exit");
        System.out.print(ansi().fg(YELLOW).a("\nYour choice: ").reset());
    }

    public static void printDashboard(int pendingTasks, int overdueTasks,
                                      int unpaidBills, double upcomingBillsTotal,
                                      int appliancesNearEOL, double monthlyRunningCost) {
        System.out.println(ansi().fg(MAGENTA).bold().a("\n╔══════ HOME DASHBOARD ══════╗").reset());
        System.out.printf("  Tasks pending    : %s%d%s%n",
                overdueTasks > 0 ? ansi().fg(RED).toString() : ansi().fg(GREEN).toString(),
                pendingTasks, ansi().reset().toString());
        System.out.printf("  Overdue tasks    : %s%d%s%n",
                overdueTasks > 0 ? ansi().fg(RED).toString() : ansi().fg(GREEN).toString(),
                overdueTasks, ansi().reset().toString());
        System.out.printf("  Unpaid bills     : %s%d%s%n",
                unpaidBills > 0 ? ansi().fg(YELLOW).toString() : ansi().fg(GREEN).toString(),
                unpaidBills, ansi().reset().toString());
        System.out.printf("  Bills due soon   : $%.2f%n", upcomingBillsTotal);
        System.out.printf("  Appliances at EOL: %s%d%s%n",
                appliancesNearEOL > 0 ? ansi().fg(YELLOW).toString() : ansi().fg(GREEN).toString(),
                appliancesNearEOL, ansi().reset().toString());
        System.out.printf("  Monthly running  : $%.2f%n", monthlyRunningCost);
        System.out.println(ansi().fg(MAGENTA).bold().a("╚═══════════════════════════╝").reset());
    }

    public static void printHeader(String title) {
        System.out.println(ansi().fg(MAGENTA).bold().a("\n══ " + title + " ══").reset());
    }

    public static void printSuccess(String msg) { System.out.println(ansi().fg(GREEN).a("✔ " + msg).reset()); }
    public static void printError(String msg)   { System.out.println(ansi().fg(RED).a("✘ " + msg).reset()); }
    public static void printInfo(String msg)    { System.out.println(ansi().fg(CYAN).a("ℹ " + msg).reset()); }
    public static void printWarning(String msg) { System.out.println(ansi().fg(YELLOW).a("⚠ " + msg).reset()); }

    public static void printAIResponse(String response) {
        System.out.println(ansi().fg(CYAN).bold().a("\n🤖 AI Analysis:\n").reset());
        System.out.println(ansi().fg(WHITE).a(response).reset());
        System.out.println(ansi().fg(CYAN).a("─".repeat(60)).reset());
    }

    public static void printTaskRow(String id, String title, String priority,
                                    String status, String dueDate, double cost) {
        String color = switch (priority) {
            case "URGENT" -> ansi().fg(RED).toString();
            case "HIGH"   -> ansi().fg(YELLOW).toString();
            default       -> ansi().fg(WHITE).toString();
        };
        System.out.printf("  %s%-8s%s %-22s %-8s %-10s $%.0f%n",
                ansi().fg(YELLOW).toString(), id, ansi().reset().toString(),
                truncate(title, 22), color + priority + ansi().reset(), dueDate, cost);
    }

    public static void printBillRow(String id, String type, String provider,
                                    double amount, String status, String dueDate) {
        String color = status.contains("OVERDUE") ? ansi().fg(RED).toString()
                     : status.contains("PAID")    ? ansi().fg(GREEN).toString()
                     : ansi().fg(WHITE).toString();
        System.out.printf("  %s%-8s%s %-14s %-16s $%-8.2f %s%s  %s%n",
                ansi().fg(YELLOW).toString(), id, ansi().reset().toString(),
                type, truncate(provider, 16), amount,
                color, status, ansi().reset() + dueDate);
    }

    private static String g(String s) { return ansi().fg(GREEN).a(s).reset().toString(); }
    private static String r(String s) { return ansi().fg(RED).a(s).reset().toString(); }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
