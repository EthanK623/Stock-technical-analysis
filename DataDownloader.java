// src/main/java/org/example/DataDownloader.java
package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader; // Added for error stream reading
import java.io.BufferedReader;    // Added for error stream reading
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class DataDownloader {

    private static final String API_KEY = "J2F8G3Z9Z3ZHWCL9";
    private static final String BASE_URL = "https://www.alphavantage.co/query?";

    /**
     * Downloads stock data from Alpha Vantage and saves it to a specified file.
     *
     * @param function The Alpha Vantage API function (e.g., "TIME_SERIES_DAILY").
     * @param symbol The stock symbol (e.g., "MSFT").
     * @param outputSize The output size ("compact" or "full").
     * @param outputPath The path to the file where the data will be saved.
     * @return true if the download was successful, false otherwise.
     * @throws IOException If an I/O error occurs during the download.
     */
    public boolean downloadStockData(String function, String symbol, String outputSize, Path outputPath) throws IOException {
        HttpURLConnection connection = null; // Initialize to null
        boolean success = false; // Flag to track success

        try {
            String urlString = String.format(
                    "%sfunction=%s&symbol=%s&outputsize=%s&apikey=%s",
                    BASE_URL, function, symbol, outputSize, API_KEY
            );

            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection(); // Assign to connection variable
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0 Safari/537.36");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = connection.getInputStream()) {
                    Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
                }
                success = true; // Set success flag
            } else {
                System.err.println("Request failed: HTTP code ");
                // Read and print error stream if available
                try (InputStream errorStream = connection.getErrorStream()) {
                    if (errorStream != null) {
                        BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
                        String line;
                        StringBuilder errorResponse = new StringBuilder();
                        while ((line = errorReader.readLine()) != null) {
                            errorResponse.append(line).append("\n");
                        }
                        //System.err.println("Error Response from Server:\n" + errorResponse.toString());
                    }
                } catch (IOException e) {
                    //System.err.println("Could not read error stream: " + e.getMessage());
                }
                success = false; // Set success flag
            }
        } finally {
            // Ensure the connection is disconnected even if an exception occurs
            if (connection != null) {
                connection.disconnect();
            }
        }
        return success; // Return the success flag
    }
}