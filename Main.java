// Ahana Sil, Aanya Shridhar, 12/4/2025

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Main {

    public static void main(String[] argss) {
        String[] args = {"Shakespeare.txt", "LesMiserables.txt"};

        if (args.length == 0) {
            System.out.println("Usage: java Main <file1> <file2> ...");
            return;
        }

        long startTime = System.nanoTime();  // Start timing all files

        for (String filePath : args) {
            processFile(filePath);
        }

        long endTime = System.nanoTime();    // End timing all files
        long totalTime = endTime - startTime;

        System.out.println("Total time for all files: " + (totalTime / 1_000_000.0) + " ms");
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
        String ext = (dot == -1) ? "" : name.substring(dot);

        File outputFile = new File(
                inputFile.getParent(),
                base + "_hashed" + ext
        );

        try (
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                String hashed = heavyHash(line);
                writer.write(hashed);
                writer.newLine();
            }

        } catch (IOException e) {
            System.out.println("Error processing " + filePath + ": " + e.getMessage());
            return;
        }

        System.out.println("Created: " + outputFile.getAbsolutePath());
    }

    // CPU-INTENSIVE HASHING TASK
    private static String heavyHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = input.getBytes();

            // Do 1,000 rounds — you can increase this if needed
            for (int i = 0; i < 1000; i++) {
                hash = digest.digest(hash);
            }

            // Convert hash bytes to hex string
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
