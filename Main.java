import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;

public class Main {

    private static final int THREADS = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) {
        String[] files = {"Shakespeare.txt", "LesMiserables.txt"};

        if (files.length == 0) {
            System.out.println("Usage: java Main <file1> <file2> ...");
            return;
        }

        long totalStartTime = System.currentTimeMillis();

        // Create an ExecutorService to process books concurrently
        ExecutorService fileExecutor = Executors.newFixedThreadPool(files.length);
        List<Future<Long>> fileFutures = new ArrayList<>();

        for (String filePath : files) {
            fileFutures.add(fileExecutor.submit(() -> processFile(filePath)));
        }

        fileExecutor.shutdown();

        // Wait for all books to finish
        for (Future<Long> f : fileFutures) {
            try {
                f.get(); // we just wait, we could also sum times if desired
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        long totalEndTime = System.currentTimeMillis();
        System.out.println("Total time for all books: " + (totalEndTime - totalStartTime) + " ms");
    }

    private static long processFile(String filePath) {
        File inputFile = new File(filePath);

        if (!inputFile.exists()) {
            System.out.println("File not found: " + filePath);
            return 0;
        }

        String name = inputFile.getName();
        int dot = name.lastIndexOf(".");
        String base = (dot == -1) ? name : name.substring(0, dot);
        String ext  = (dot == -1) ? "" : name.substring(dot);

        File outputFile = new File(inputFile.getParent(), base + "_hashed" + ext);

        long startTime = System.currentTimeMillis(); // Start timing

        List<String> lines;
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            lines = reader.lines().toList();
        } catch (IOException e) {
            System.out.println("Error reading " + filePath + ": " + e.getMessage());
            return 0;
        }

        // Use a thread pool to hash lines concurrently
        ExecutorService lineExecutor = Executors.newFixedThreadPool(THREADS);
        List<Future<String>> futures = new ArrayList<>();

        for (String line : lines) {
            futures.add(lineExecutor.submit(() -> heavyHash(line)));
        }

        lineExecutor.shutdown();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for (Future<String> f : futures) {
                writer.write(f.get());
                writer.newLine();
            }
        } catch (Exception e) {
            System.out.println("Error writing output: " + e.getMessage());
            return 0;
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Created: " + outputFile.getAbsolutePath());

        return endTime - startTime;
    }

    private static String heavyHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = input.getBytes();

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