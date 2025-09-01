package org.example;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        boolean success = false;
        Scanner input = new Scanner(System.in);
        String ticker = "";

        while (success == false) {
            System.out.println("Enter in a stock ticker: ");
            ticker = input.nextLine();
            ticker = ticker.toUpperCase();
            StockDataProcessor stock = new StockDataProcessor(ticker);
            ArrayList<Double> closes = stock.getClosePrices();
            ArrayList<Long> volumes = stock.getDailyVolumes();
            ArrayList<Double> highs = stock.getHighPrices();
            ArrayList<Double> lows = stock.getLowPrices();
            if (closes.size() == 0) {
                continue;
            } else {
                success = true;
                TestIndicators.analyzeSMA(closes);
                TestIndicators.analyzeEMA(closes);
                TestIndicators.calculateRSI(closes);
                TestIndicators.analyzeBollingerBands(closes);
                TestIndicators.analyzeOnBalanceVolume(closes, volumes, 14, 1.04);
                TestIndicators.analyzeMACD(closes);
                TestIndicators.stochastic(closes);
                TestIndicators.CMF(highs, lows, closes, volumes);
                if (TestIndicators.buy - TestIndicators.sell >= 4) {
                    System.out.println("\nWith " + TestIndicators.buy + " bullish indicators and " + TestIndicators.sell + " bearish indicators, we give " + stock.symbol + " a strong buy rating.");
                } else if (TestIndicators.buy - TestIndicators.sell >= 2) {
                    System.out.println("\nWith " + TestIndicators.buy + " bullish indicators and " + TestIndicators.sell + " bearish indicators, we give " + stock.symbol + " a buy rating.");
                } else if (TestIndicators.buy - TestIndicators.sell >= -1) {
                    System.out.println("\nWith " + TestIndicators.buy + " bullish indicators and " + TestIndicators.sell + " bearish indicators, we give " + stock.symbol + " a neutral rating.");
                } else if (TestIndicators.buy - TestIndicators.sell >= -2) {
                    System.out.println("\nWith " + TestIndicators.buy + " bullish indicators and " + TestIndicators.sell + " bearish indicators, we give " + stock.symbol + " a sell rating.");
                } else if (TestIndicators.buy - TestIndicators.sell >= -4) {
                    System.out.println("\nWith " + TestIndicators.buy + " bullish indicators and " + TestIndicators.sell + " bearish indicators, we give " + stock.symbol + " a strong sell rating.");
                }
            }
        }
    }
}