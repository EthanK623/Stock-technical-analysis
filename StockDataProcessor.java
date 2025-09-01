// src/main/java/org/example/AlphaVantageDownloader.java (or StockDataProcessor.java)
package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.nio.file.Files;

public class StockDataProcessor{
    String symbol;
    ArrayList<Double> closePrices = new ArrayList<>();
    ArrayList<Double> highPrices = new ArrayList<>();
    ArrayList<Double> lowPrices = new ArrayList<>();
    ArrayList<Long> dailyVolumes = new ArrayList<>();

        public StockDataProcessor(String ticker) {
            this.symbol = ticker;

        String outputSize = "compact";
        String function = "TIME_SERIES_DAILY";
        String fileName = symbol + "_alpha.json";
        Path filePath = Paths.get(fileName);
        DataDownloader downloader = new DataDownloader();

        try {
            // Use the DataDownloader to download the data
            boolean downloadSuccess = downloader.downloadStockData(function, symbol, outputSize, filePath);

            if (downloadSuccess) {
                // --- Start JSON Parsing with Jackson ---
                ObjectMapper objectMapper = new ObjectMapper();
                try (Reader reader = new FileReader(filePath.toFile())) { // Convert Path to File
                    JsonNode rootNode = objectMapper.readTree(reader);

                    // Check for API errors first
                    if (rootNode.has("Error Message")) {
                        System.out.println("Invalid ticker! Please try again.");
                        return; // Exit if there's an API error
                    }
                    if (rootNode.has("Note")) {
                        System.out.println("The daily rate limit has been hit. Please try again tomorrow");
                        return;
                    }

                    JsonNode timeSeriesNode = rootNode.get("Time Series (Daily)");

                    if (timeSeriesNode != null && timeSeriesNode.isObject()) {
                        Iterator<Map.Entry<String, JsonNode>> fields = timeSeriesNode.fields();
                        while (fields.hasNext()) {
                            Map.Entry<String, JsonNode> entry = fields.next();
                            String date = entry.getKey();
                            JsonNode dailyData = entry.getValue();
                            JsonNode highPriceNode = dailyData.get("2. high");
                            if (highPriceNode != null) {
                                double highPrice = highPriceNode.asDouble();
                                highPrices.add(highPrice);
                            }

                            JsonNode lowPriceNode = dailyData.get("3. low");
                            if (lowPriceNode != null) {
                                double lowPrice = lowPriceNode.asDouble();
                                lowPrices.add(lowPrice);
                            }

                            JsonNode closePriceNode = dailyData.get("4. close");
                            if (closePriceNode != null) {
                                double closePrice = closePriceNode.asDouble();
                                closePrices.add(closePrice);
                            }

                            JsonNode volumePriceNode = dailyData.get("5. volume");
                            if (volumePriceNode != null) {
                                long volume = highPriceNode.asLong();
                                dailyVolumes.add(volume);
                            }
                        }
                    } else {
                        System.out.println("Could not find 'Time Series (Daily)' or it's not an object in the JSON.");
                    }

                } catch (IOException e) {
                    System.err.println("Error reading or parsing JSON file: " + e.getMessage());
                    e.printStackTrace();
                }
                // --- End JSON Parsing with Jackson ---
            } else {
                System.out.println("Data download failed, skipping JSON parsing.");
            }

        } catch (IOException e) {
            System.err.println("An unexpected I/O error occurred during download or file operations: " + e.getMessage());
            e.printStackTrace();
        }

            try {
                Files.delete(filePath);
            } catch (IOException e) {
                System.out.println("Could not delete the file: " + filePath);
                e.printStackTrace();
            }
    }
    public ArrayList<Double> getClosePrices() {
        return closePrices;
    }
    public ArrayList<Double> getHighPrices() {
            return highPrices;
    }
    public ArrayList<Double> getLowPrices() {
            return lowPrices;
    }
    public ArrayList<Long> getDailyVolumes() {
            return dailyVolumes;
    }
}