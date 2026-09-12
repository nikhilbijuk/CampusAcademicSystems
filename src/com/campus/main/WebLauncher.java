package com.campus.main;

import com.campus.web.CampusWebServer;
import java.awt.Desktop;
import java.net.URI;
import java.util.Scanner;

public class WebLauncher {

    private static int getPort(String[] args) {
        if (args != null && args.length > 0 && args[0] != null && !args[0].trim().isEmpty()) {
            try {
                return Integer.parseInt(args[0].trim());
            } catch (NumberFormatException ignored) {}
        }
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.trim().isEmpty()) {
            try {
                return Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {}
        }
        return 8080;
    }

    public static void main(String[] args) {
        int port = getPort(args);
        System.out.println("==========================================================");
        System.out.println("=== Campus Management System - Web Server Launcher ===");
        System.out.println("==========================================================");

        try {
            CampusWebServer webServer = new CampusWebServer(port);
            webServer.start();

            String url = "http://localhost:" + port;
            System.out.println("\n [READY] Web application is live!");
            System.out.println(" Open your browser and navigate to: " + url);
            System.out.println(" Press 'q' followed by Enter (or Ctrl+C) to stop the server.\n");

            // Attempt to open default browser automatically
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(url));
                }
            } catch (Exception e) {
                // If headless or browser cannot be launched, proceed silently
            }

            // Add JVM shutdown hook for clean termination
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                webServer.stop();
                System.out.println("Server stopped.");
            }));

            // Wait for user input to stop if stdin available, otherwise wait indefinitely
            try {
                if (System.console() != null) {
                    try (Scanner scanner = new Scanner(System.in)) {
                        while (scanner.hasNextLine()) {
                            String line = scanner.nextLine().trim();
                            if ("q".equalsIgnoreCase(line) || "exit".equalsIgnoreCase(line)) {
                                break;
                            }
                        }
                    }
                    webServer.stop();
                    System.out.println("Server shutdown cleanly. Goodbye!");
                } else {
                    // Non-interactive or daemon mode
                    Thread.currentThread().join();
                }
            } catch (Exception e) {
                // Keep running until killed
                Thread.currentThread().join();
            }
        } catch (Exception e) {
            System.err.println("Fatal Error starting web server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
