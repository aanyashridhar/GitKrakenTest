// Ahana Sil, Aanya Shridhar, 12/4/2025

import java.io.*;

public class Main {

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
        String ext = (dot == -1) ? "" : name.substring(dot);

        File outputFile = new File(
                inputFile.getParent(),
                base + "_capitalized" + ext
        );

        long startTime = System.nanoTime();  // Start timing

        try (
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line.toUpperCase()); //Capitalizing all words
                writer.newLine();
            }

        } catch (IOException e) {
            System.out.println("Error processing " + filePath + ": " + e.getMessage());
            return;
        }

        long endTime = System.nanoTime();   // End timing
        long timeTaken = endTime - startTime;

        System.out.println("Created: " + outputFile.getAbsolutePath());
        System.out.println("Time taken: " + (timeTaken / 1_000_000.0) + " ms\n");
    }
}
