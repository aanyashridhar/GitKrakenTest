// Multi-threaded SHA-256 hashing

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;

public class Main {

    private static final int THREADS = Runtime.getRuntime().availableProcessors();

    public static void main(String[] argss) {
        String[] args = {"Shakespeare.txt", "LesMiserables.txt"};

        if (args.length == 0) {
            System.out.println("Usage: java Main <file1> <file2> ...");
            return;
        }

        for (String filePath : args) {
            processFile(filePath);
        }
    }

    private static void processFile(String filePath) {
        File inputFile = new File(filePath);

        if (!inputFile.exists()) {
            System.out.println("File not found: " + filePath);
            return;
        }

        // Build output file name
        String name = inputFile.getName();
        int dot = name.lastIndexOf(".");
        String base = (dot == -1) ? name : name.substring(0, dot);
        String ext  = (dot == -1) ? "" : name.substring(dot);

        File outputFile = new File(inputFile.getParent(), base + "_hashed" + ext);

        long startTime = System.currentTimeMillis(); // Start timing

        List<String> lines;

        // 1. Read all lines
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            lines = reader.lines().toList(); // Java 16+
        } catch (IOException e) {
            System.out.println("Error reading " + filePath + ": " + e.getMessage());
            return;
        }

        // 2. Process lines in parallel using ExecutorService
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        List<Future<String>> futures = new ArrayList<>(lines.size());

        for (String line : lines) {
            futures.add(executor.submit(() -> heavyHash(line)));
        }

        executor.shutdown();

        // 3. Write output in order
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for (Future<String> f : futures) {
                writer.write(f.get());
                writer.newLine();
            }
        } catch (Exception e) {
            System.out.println("Error writing output: " + e.getMessage());
            return;
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Created: " + outputFile.getAbsolutePath());
        System.out.println("Time taken: " + (endTime - startTime) + " ms\n");
    }

    // CPU-INTENSIVE HASHING TASK
    private static String heavyHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = input.getBytes();

            // 1,000 rounds
            for (int i = 0; i < 1000; i++) {
                hash = digest.digest(hash);
            }

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}