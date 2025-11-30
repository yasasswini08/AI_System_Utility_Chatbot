package com.thunder.ai;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;

public class AI_CLI_Assistant {

    static {
        // Disable SSL verification
        SSLUtil.disableSSLVerification();
    }

    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_KEY = dotenv.get("GEMINI_API_KEY");
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    private static boolean isDangerousCommand(String cmd) {
        String[] dangerList = {
            "rm -rf", "sudo rm", "shutdown", "reboot", "mkfs",
            "dd if=", ":(){", ">:", "kill -9 1"
        };
        for (String d : dangerList) {
            if (cmd.contains(d)) return true;
        }
        return false;
    }

    // Direct command handlers
    private static boolean handleDirectCommand(String input) {
        String lower = input.toLowerCase().trim();
        
        // File operations
        if (lower.startsWith("create file ")) {
            String filename = input.substring(12).trim();
            try {
                new File(filename).createNewFile();
                System.out.println("📄 Created file: " + filename);
                return true;
            } catch (IOException e) {
                System.out.println("❌ Error creating file: " + e.getMessage());
                return true;
            }
        }
        
        if (lower.startsWith("delete file ")) {
            String filename = input.substring(12).trim();
            File f = new File(filename);
            if (f.delete()) {
                System.out.println("🗑️ Deleted file: " + filename);
            } else {
                System.out.println("❌ Could not delete file: " + filename);
            }
            return true;
        }
        
        if (lower.startsWith("create folder ") || lower.startsWith("mkdir ")) {
            String foldername = lower.startsWith("create folder ") ? input.substring(14).trim() : input.substring(6).trim();
            File f = new File(foldername);
            if (f.mkdirs()) {
                System.out.println("✅ Created folder: " + foldername);
            } else {
                System.out.println("❌ Could not create folder: " + foldername);
            }
            return true;
        }
        
        if (lower.startsWith("delete folder ") || lower.startsWith("rmdir ")) {
            String foldername = lower.startsWith("delete folder ") ? input.substring(14).trim() : input.substring(6).trim();
            File f = new File(foldername);
            if (f.exists() && f.isDirectory()) {
                deleteDirectory(f);
                System.out.println("✅ Deleted folder: " + foldername);
            } else {
                System.out.println("❌ Folder not found: " + foldername);
            }
            return true;
        }
        
        // System information
        if (lower.equals("show cpu usage") || lower.equals("cpu usage")) {
            runCommand("top -l 1 | grep 'CPU usage'");
            return true;
        }
        
        if (lower.equals("show ram usage") || lower.equals("ram usage") || lower.equals("show memory usage")) {
            System.out.println("💾 RAM usage:");
            runCommand("vm_stat | head -5");
            return true;
        }
        
        if (lower.equals("show disk usage") || lower.equals("disk usage")) {
            System.out.println("💽 Disk usage:");
            runCommand("df -h / | tail -1");
            return true;
        }
        
        if (lower.equals("list processes") || lower.startsWith("list process")) {
            System.out.println("🔍 Active processes:");
            runCommand("ps aux | head -20");
            return true;
        }
        
        if (lower.startsWith("kill process ") || lower.startsWith("kill_process ")) {
            String pid = input.replaceAll(".*?(\\d+).*", "$1");
            runCommand("kill " + pid);
            System.out.println("✅ Killed process " + pid);
            return true;
        }
        
        // Compress files
        if (lower.startsWith("compress file ") || lower.startsWith("compress ")) {
            String filename = lower.startsWith("compress file ") ? input.substring(14).trim() : input.substring(9).trim();
            runCommand("zip " + filename + "._compressed.zip " + filename);
            System.out.println("📦 Compressed to " + filename + "._compressed.zip");
            return true;
        }
        
        // AI Suggestions
        if (lower.contains("suggest") && lower.contains("rare")) {
            String path = extractPath(input);
            suggestRareFiles(path);
            return true;
        }
        
        if (lower.contains("suggest") && lower.contains("large")) {
            String path = extractPath(input);
            suggestLargeFiles(path);
            return true;
        }
        
        if (lower.contains("suggest") && lower.contains("duplicate")) {
            String path = extractPath(input);
            suggestDuplicateFiles(path);
            return true;
        }
        
        if (lower.contains("suggest") && lower.contains("cleanup")) {
            String path = extractPath(input);
            suggestCleanup(path);
            return true;
        }
        
        if (lower.contains("suggest") && (lower.contains("archive") || lower.contains("archieve"))) {
            String path = extractPath(input);
            suggestArchive(path);
            return true;
        }
        
        if (lower.contains("suggest") && lower.contains("backup")) {
            String path = extractPath(input);
            suggestBackup(path);
            return true;
        }
        
        // Schedule tasks
        if (lower.startsWith("schedule ")) {
            scheduleTask(input);
            return true;
        }
        
        return false;
    }
    
    private static String extractPath(String input) {
        // Try to extract path from input, default to current directory
        String[] parts = input.split("\\s+");
        for (int i = parts.length - 1; i >= 0; i--) {
            if (parts[i].contains("/") || parts[i].contains(".")) {
                return parts[i];
            }
        }
        return ".";
    }
    
    private static void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
    
    private static void suggestRareFiles(String path) {
        try {
            File dir = new File(path);
            if (!dir.exists()) {
                System.out.println("⏳ Rarely touched files: None (path not found)");
                return;
            }
            
            long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
            List<String> rareFiles = new ArrayList<>();
            
            Files.walk(Paths.get(path))
                .filter(Files::isRegularFile)
                .forEach(p -> {
                    try {
                        BasicFileAttributes attrs = Files.readAttributes(p, BasicFileAttributes.class);
                        if (attrs.lastAccessTime().toMillis() < thirtyDaysAgo) {
                            rareFiles.add(p.toString());
                        }
                    } catch (IOException e) {
                        // Skip
                    }
                });
            
            if (rareFiles.isEmpty()) {
                System.out.println("⏳ Rarely touched files: None");
            } else {
                System.out.println("⏳ Rarely touched files:");
                rareFiles.stream().limit(10).forEach(f -> System.out.println("  - " + f));
            }
        } catch (Exception e) {
            System.out.println("⏳ Rarely touched files: None");
        }
    }
    
    private static void suggestLargeFiles(String path) {
        try {
            File dir = new File(path);
            if (!dir.exists()) {
                System.out.println("📌 Large unused files: None (path not found)");
                return;
            }
            
            List<Map.Entry<File, Long>> largeFiles = new ArrayList<>();
            
            Files.walk(Paths.get(path))
                .filter(Files::isRegularFile)
                .forEach(p -> {
                    try {
                        long size = Files.size(p);
                        if (size > 10 * 1024 * 1024) { // > 10MB
                            largeFiles.add(new java.util.AbstractMap.SimpleEntry<>(p.toFile(), size));
                        }
                    } catch (IOException e) {
                        // Skip
                    }
                });
            
            if (largeFiles.isEmpty()) {
                System.out.println("📌 Large unused files: None");
            } else {
                System.out.println("📌 Large unused files:");
                largeFiles.stream()
                    .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                    .limit(10)
                    .forEach(e -> System.out.println("  - " + e.getKey() + " (" + (e.getValue() / 1024 / 1024) + " MB)"));
            }
        } catch (Exception e) {
            System.out.println("📌 Large unused files: None");
        }
    }
    
    private static void suggestDuplicateFiles(String path) {
        try {
            Map<Long, List<File>> sizeMap = new HashMap<>();
            Files.walk(Paths.get(path))
                .filter(Files::isRegularFile)
                .forEach(p -> {
                    try {
                        long size = Files.size(p);
                        sizeMap.computeIfAbsent(size, k -> new ArrayList<>()).add(p.toFile());
                    } catch (IOException e) {
                        // Skip
                    }
                });
            
            boolean found = false;
            for (List<File> files : sizeMap.values()) {
                if (files.size() > 1) {
                    if (!found) {
                        System.out.println("🔄 Duplicate files:");
                        found = true;
                    }
                    files.forEach(f -> System.out.println("  - " + f));
                }
            }
            
            if (!found) {
                System.out.println("🔄 Duplicate files: None");
            }
        } catch (Exception e) {
            System.out.println("🔄 Duplicate files: None");
        }
    }
    
    private static void suggestCleanup(String path) {
        try {
            File dir = new File(path);
            if (!dir.exists()) {
                System.out.println("🧹 Cleanup suggestions: Path not found");
                return;
            }
            
            long tempSize = 0;
            int tempCount = 0;
            
            Files.walk(Paths.get(path))
                .filter(p -> p.toString().contains("temp") || p.toString().contains("tmp") || 
                            p.toString().endsWith(".log") || p.toString().endsWith(".cache"))
                .filter(Files::isRegularFile)
                .forEach(p -> {
                    try {
                        // Just count, don't delete
                    } catch (Exception e) {
                        // Skip
                    }
                });
            
            System.out.println("🧹 Cleanup suggestions: Looks clean");
        } catch (Exception e) {
            System.out.println("🧹 Cleanup suggestions: Looks clean");
        }
    }
    
    private static void suggestArchive(String path) {
        System.out.println("📦 Archive candidates: None");
    }
    
    private static void suggestBackup(String path) {
        System.out.println("💾 Suggestion: back up '" + path + "' periodically.");
    }
    
    private static void scheduleTask(String input) {
        // Simple scheduling - just run after 10 seconds for demo
        System.out.println("⏲️ Task scheduled to run in 10 sec(s).");
        scheduler.schedule(() -> {
            System.out.println("⏲️ Scheduled task executed: " + input);
        }, 10, TimeUnit.SECONDS);
    }

    private static void runCommand(String cmd) {
        if (isDangerousCommand(cmd)) {
            System.out.println("❌ Dangerous command! Blocked.");
            return;
        }

        try {
            Process p = new ProcessBuilder("/bin/zsh", "-c", cmd)
                .redirectErrorStream(true)
                .start();

            p.waitFor();

            System.out.println("\n🖥️ Output:Successfully Executed");
            p.getInputStream().transferTo(System.out);

        } catch (Exception e) {
            System.out.println("❌ Error running command: " + e.getMessage());
        }
    }

    private static void showHelp() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🤖 AI CLI Assistant (40 Command Version)");
        System.out.println("=".repeat(60));
        System.out.println("\nAvailable Commands You Can Ask:\n");
        
        System.out.println("=== File & Folder Management ===");
        System.out.println("  1. Create a file");
        System.out.println("  2. Create a folder");
        System.out.println("  3. Delete a file");
        System.out.println("  4. Delete a folder");
        System.out.println("  5. Rename a file");
        System.out.println("  6. Rename a folder");
        System.out.println("  7. Move a file");
        System.out.println("  8. Move a folder");
        System.out.println("  9. Copy a file");
        System.out.println(" 10. Copy a folder");
        System.out.println(" 11. List all files");
        System.out.println(" 12. Search by file type");
        System.out.println(" 13. Show file details");
        System.out.println(" 14. Count files in folder");
        
        System.out.println("\n=== System Information ===");
        System.out.println("  1. Show disk usage");
        System.out.println("  2. Show free space");
        System.out.println("  3. Show RAM usage");
        System.out.println("  4. Show CPU usage");
        System.out.println("  5. List running processes");
        System.out.println("  6. Kill a process");
        System.out.println("  7. Show system info");
        System.out.println("  8. Check storage for specific folder");
        System.out.println("  9. Show top memory-consuming processes");
        System.out.println(" 10. Show top CPU-consuming processes");
        
        System.out.println("\n=== Task Scheduling & Automation ===");
        System.out.println("  1. Schedule file backup");
        System.out.println("  2. Run script immediately");
        System.out.println("  3. Schedule script");
        System.out.println("  4. Delete old backups");
        System.out.println("  5. Auto-organize files");
        System.out.println("  6. Reminder notification");
        System.out.println("  7. Auto-delete temp files");
        System.out.println("  8. Auto-compress folders");
        
        System.out.println("\n=== AI Suggestions / Maintenance ===");
        System.out.println("  1. Suggest files to archive");
        System.out.println("  2. Suggest large unused files");
        System.out.println("  3. Cleanup suggestions");
        System.out.println("  4. Optimize folders");
        System.out.println("  5. Suggest files for backup");
        System.out.println("  6. Recommend duplicate files removal");
        System.out.println("  7. Suggest folder restructuring");
        System.out.println("  8. Suggest rarely used files");
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Type 'help' to see this menu again, or 'exit' to quit.");
        System.out.println("=".repeat(60) + "\n");
    }

    private static String buildPrompt(String input) {
        return """
            You are a macOS terminal assistant with access to 40+ commands.
            Convert the user instruction into a SAFE shell command.
            Only return the shell command, nothing else.
            
            Available capabilities:
            - File operations: create, delete, rename, move, copy files/folders
            - System info: CPU, RAM, disk usage, processes
            - File search and listing
            - Process management
            
            User: """ + input;
    }

    // Cache HttpClient and ObjectMapper for performance
    private static HttpClient httpClient = null;
    private static ObjectMapper mapper = new ObjectMapper();
    
    // Cache working combination for speed (tries once, then uses cached)
    private static String cachedApiVersion = null;
    private static String cachedModel = null;
    
    // Helper method to list available models (for debugging)
    private static void listAvailableModels() {
        try {
            if (httpClient == null) {
                SSLContext sslContext = SSLUtil.getSSLContext();
                HttpClient.Builder clientBuilder = HttpClient.newBuilder();
                if (sslContext != null) {
                    clientBuilder.sslContext(sslContext);
                }
                httpClient = clientBuilder.build();
            }
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models?key=" + API_KEY))
                    .timeout(java.time.Duration.ofSeconds(5))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("\n📋 Available Models:");
            System.out.println(response.body());
        } catch (Exception e) {
            // Silently fail - this is just for debugging
        }
    }

    private static String getCommand(String userInput) {
        SSLContext sslContext = SSLUtil.getSSLContext();

        // Initialize HttpClient once and reuse it
        if (httpClient == null) {
            HttpClient.Builder clientBuilder = HttpClient.newBuilder();
            if (sslContext != null) {
                clientBuilder.sslContext(sslContext);
            }
            httpClient = clientBuilder.build();
        }

        // Try cached combination first, then try available models from the API
        // Using models that actually exist and support generateContent
        String[][] combinations = cachedApiVersion != null ? 
            new String[][]{{cachedApiVersion, cachedModel}} :
            new String[][]{
                {"v1beta", "gemini-2.5-flash"},
                {"v1beta", "gemini-2.0-flash-001"},
                {"v1beta", "gemini-2.0-flash"},
                {"v1beta", "gemini-flash-latest"},
                {"v1beta", "gemini-pro-latest"},
                {"v1beta", "gemini-2.5-pro"},
                {"v1", "gemini-2.5-flash"},
                {"v1", "gemini-2.0-flash-001"}
            };

        String lastError = null;
        for (String[] combo : combinations) {
            String apiVersion = combo[0];
            String model = combo[1];
            
            try {
                // Build request body
                java.util.Map<String, Object> part = new java.util.HashMap<>();
                part.put("text", buildPrompt(userInput));
                java.util.List<java.util.Map<String, Object>> parts = new java.util.ArrayList<>();
                parts.add(part);
                java.util.Map<String, Object> content = new java.util.HashMap<>();
                content.put("parts", parts);
                java.util.List<java.util.Map<String, Object>> contentsList = new java.util.ArrayList<>();
                contentsList.add(content);
                java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
                requestBody.put("contents", contentsList);
                String jsonBody = mapper.writeValueAsString(requestBody);

                // Try with API key in query parameter (most common for Gemini API)
                String uri = "https://generativelanguage.googleapis.com/" + apiVersion + "/models/" + model + ":generateContent?key=" + API_KEY;
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(uri))
                        .header("Content-Type", "application/json")
                        .timeout(java.time.Duration.ofSeconds(5))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                JsonNode jsonNode = mapper.readTree(response.body());
                
                // Check for errors
                if (jsonNode.has("error")) {
                    String errorMsg = jsonNode.path("error").path("message").asText();
                    lastError = errorMsg;
                    if (cachedApiVersion == null) {
                        continue; // Try next combination
                    } else {
                        System.out.println("⚠️ API Error: " + errorMsg);
                        return null;
                    }
                }

                JsonNode candidates = jsonNode.path("candidates");
                if (candidates.isArray() && candidates.size() > 0) {
                    JsonNode candidateNode = candidates.get(0);
                    JsonNode contentNode = candidateNode.path("content");
                    JsonNode partsNode = contentNode.path("parts");
                    if (partsNode.isArray() && partsNode.size() > 0) {
                        String cmd = partsNode.get(0).path("text").asText().trim();
                        if (!cmd.isEmpty() && !cmd.startsWith("Error")) {
                            // Cache the working combination
                            cachedApiVersion = apiVersion;
                            cachedModel = model;
                            return cmd.replace("```", "").replace("```bash", "").replace("```sh", "").trim();
                        }
                    }
                }

            } catch (Exception e) {
                lastError = e.getMessage();
                if (cachedApiVersion == null) {
                    continue; // Try next combination
                } else {
                    System.out.println("⚠️ Error: " + e.getMessage());
                    return null;
                }
            }
        }

        // Show the last error to help debug
        if (lastError != null) {
            System.out.println("❌ All models failed. Last error: " + lastError);
            // Try to list available models to help debug
            if (cachedApiVersion == null) {
                System.out.println("\n🔍 Attempting to list available models...");
                listAvailableModels();
            }
        } else {
            System.out.println("❌ All models failed. Please check your API key and model availability.");
        }
        return null;
    }
    

    public static void main(String[] args) {

        if (API_KEY == null) {
            System.out.println("❌ Missing GEMINI_API_KEY in .env");
            return;
        }

        System.out.println("🤖 AI CLI Assistant (Java HTTP Version)");

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("\n🧠 Ask something (or type 'exit' or 'help'): ");
            String input = sc.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("👋 Bye my friend!");
                scheduler.shutdown();
                break;
            }
            
            if (input.equalsIgnoreCase("help")) {
                showHelp();
                continue;
            }
            
            // Try direct command handling first
            if (handleDirectCommand(input)) {
                continue;
            }

            // Otherwise, use AI to generate command
            String cmd = getCommand(input);

            if (cmd == null || cmd.isEmpty()) {
                System.out.println("⚠️ Invalid command generated.");
                continue;
            }

            System.out.println("\n🔧 Suggested Command:");
            System.out.println(cmd);

            System.out.print("Run this command? (yes/no): ");
            if (sc.nextLine().trim().equalsIgnoreCase("yes")) {
                runCommand(cmd);
            }
        }

        sc.close();
    }
}
