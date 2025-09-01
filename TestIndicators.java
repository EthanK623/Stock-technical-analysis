package org.example;

import com.sun.jdi.DoubleValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestIndicators {
    public static int buy = 0;
    public static int sell = 0;

    public static double standDev(ArrayList<Double> list) {
        double sd = 0.0;
        double avg = average(list, list.size());
        for (int i = 0; i < list.size(); i++) {
            sd += Math.pow(list.get(i) - avg, 2);
        }
        sd /= (list.size() - 1);
        sd = Math.sqrt(sd);
        return sd;
    }

    public static double average(ArrayList<Double> list, double n) {
        double avg = 0;
        for (int i = 0; i < n; i++) {
            avg += list.get(i);
        }
        avg /= n;
        return avg;
    }

    public static Double calculateEMA(ArrayList<Double> prices, int period, int endIndex) {
        if (endIndex < period - 1) return null;

        double multiplier = 2.0 / (period + 1);
        double sum = 0.0;

        // Simple Moving Average seed
        for (int i = endIndex - period + 1; i <= endIndex; i++) {
            sum += prices.get(i);
        }
        double ema = sum / period;

        // Apply EMA formula iteratively
        for (int i = endIndex - period + 1; i <= endIndex; i++) {
            ema = (prices.get(i) - ema) * multiplier + ema;
        }

        return ema;
    }

    public static void analyzeSMA(ArrayList<Double> prices) {
        double five = average(prices, 5);
        double fourteen = average(prices, 14);
        double firstPercentDif = ((five - fourteen) / fourteen) * 100;
        double firstRounded = Math.round(firstPercentDif * 100.0) / 100.0;
        double twenty = average(prices, 20);
        double fifty = average(prices, 50);
        double secondPercentDif = ((twenty - fifty) / fifty) * 100;
        double secondRounded = Math.round(secondPercentDif * 100.0) / 100.0;
        if (five > fourteen * 1.025) {
            System.out.println("The MA5 is " + firstRounded + "% above the MA14, indicating short-term upward momentum");
            buy++;
        } else if (five * 1.025 < fourteen) {
            System.out.println("The MA5 is " + Math.abs(firstRounded) + "% below the MA14, indicating short-term downward momentum.");
            sell++;
        } if (twenty > fifty * 1.05) {
            System.out.println("The MA20 is " + secondRounded + "% above the MA50, indicating overall upward momentum");
            buy++;
        } else if (twenty * 1.05 < fifty) {
            System.out.println("The M205 is " + Math.abs(secondRounded) + "% below the MA50, indicating overall downward momentum.");
            sell++;
        }
    }

    public static void analyzeEMA(ArrayList<Double> prices) {
        int lastIndex = prices.size() - 1;
        ArrayList<Double> reversedList = new ArrayList<>(prices);
        Collections.reverse(reversedList);
        Double ema9 = calculateEMA(reversedList, 9, lastIndex);
        Double ema20 = calculateEMA(reversedList, 20, lastIndex);
        if (ema9 > ema20) {
            System.out.println("The EMA9 has crossed above the EMA20, forming a golden cross, indicating upward momentum.");
            buy++;
        } else if (ema9 < ema20) {
            System.out.println("The EMA9 has crossed below the EMA20, forming a death cross, indicating downward momentum.");
            sell++;
        }
    }

    public static void calculateRSI(ArrayList<Double> prices) {
        List<Double> orderedPrices = new ArrayList<>(prices);
        Collections.reverse(orderedPrices);

        ArrayList<Double> rsiList = new ArrayList<>();

        double totalGain = 0.0;
        double totalLoss = 0.0;

        // Step 1: Calculate initial average gain/loss
        for (int i = 1; i <= 14; i++) {
            double change = orderedPrices.get(i) - orderedPrices.get(i - 1);
            if (change > 0) {
                totalGain += change;
            } else {
                totalLoss += -change;
            }
        }

        double averageGain = totalGain / 14;
        double averageLoss = totalLoss / 14;

        // Step 2: Calculate first RSI value
        double rs = averageLoss == 0 ? 100 : averageGain / averageLoss;
        double rsi = 100 - (100 / (1 + rs));
        rsiList.add(rsi);

        // Step 3: Calculate subsequent RSI values with smoothing
        for (int i = 15; i < orderedPrices.size(); i++) {
            double change = orderedPrices.get(i) - orderedPrices.get(i - 1);
            double gain = change > 0 ? change : 0;
            double loss = change < 0 ? -change : 0;

            averageGain = ((averageGain * (13)) + gain) / 14;
            averageLoss = ((averageLoss * (13)) + loss) / 14;

            rs = averageLoss == 0 ? 100 : averageGain / averageLoss;
            rsi = 100 - (100 / (1 + rs));
            rsiList.add(rsi);
        }
        Collections.reverse(rsiList);
        if (rsiList.get(0) >= 70) {
            System.out.println("The RSI has reached overbought territory, indicating a possible downward trend soon.");
            sell++;
        } else if (rsiList.get(0) <= 30) {
            System.out.println("The RSI has reached oversold territory, indicating a possible upward trend soon.");
            buy++;
        }

    }

    public static void analyzeBollingerBands(ArrayList<Double> prices) {
        double sd = standDev(prices);
        double avg = average(prices, 100);
        double upper = avg + (2 * sd);
        double lower = avg - (2 * sd);

        if (prices.get(0) >= upper) {
            System.out.println("The share price is trading above the upper bollinger bands, indicating a possible trend reversal soon.");
            sell++;
        }
        if (prices.get(0) <= lower) {
            System.out.println("The share price is trading above the lower bollinger bands, indicating a possible trend reversal soon.");
            buy++;
        }
    }

    public static long[] calcOnBalanceVolume(ArrayList<Double> prices, ArrayList<Long> volumes, int cutoff) {
        long obv = 0;
        long[] values = {0, 0};
        List<Double> orderedPrices = new ArrayList<>(prices);
        List<Long> orderedVolumes = new ArrayList<>(volumes);
        Collections.reverse(orderedPrices);
        Collections.reverse(orderedVolumes);
        for (int i = 1; i < orderedPrices.size(); i++) {
            if (orderedPrices.get(i) > orderedPrices.get(i - 1)) {
                obv += orderedVolumes.get(i);
            } else if (orderedPrices.get(i) < orderedPrices.get(i - 1)) {
                obv -= orderedVolumes.get(i);
            }
            if (i == orderedPrices.size() - 1) {
                values[0] = obv;
            } else if (i == orderedPrices.size() - cutoff + 1) {
                values[1] = obv;
            }
        }
        return values;
    }

    public static void analyzeOnBalanceVolume(ArrayList<Double> prices, ArrayList<Long> volumes, int cutoff, double percent) {
        long[] comparedValues = calcOnBalanceVolume(prices, volumes, cutoff);
        double currentPrice = prices.get(0);
        double pastPrice = prices.get(prices.size() - cutoff + -1);
        if (currentPrice > pastPrice * percent && comparedValues[0] > comparedValues[1]) {
            System.out.println("The simultaneous increase of price and on-balance-volume suggests a continued upward trend");
            buy++;
        } else if (currentPrice * percent < pastPrice && comparedValues[0] > comparedValues[1]) {
            System.out.println("The decreasing price and increasing on-balance-volume suggests a possible upward trend soon.");
            buy++;
        } else if (currentPrice > pastPrice * percent && comparedValues[0] < comparedValues[1]) {
            System.out.println("The increasing price and decreasing on-balance-volume suggests a possible downward trend soon.");
            sell++;
        } else if (currentPrice * percent < pastPrice && comparedValues[0] < comparedValues[1]) {
            System.out.println("The simultaneous decrease of price and on-balance-volume suggests a continued downward trend");
            sell++;
        }
    }

    public static void analyzeMACD(ArrayList<Double> prices) {
        int lastIndex = prices.size() - 1;
        ArrayList<Double> reversedList = new ArrayList<>(prices);
        Collections.reverse(reversedList);
        Double ema12 = calculateEMA(reversedList, 12, lastIndex);
        Double ema26 = calculateEMA(reversedList, 26, lastIndex);
        double macd = ema12 - ema26;

        // Step 2: Build a short MACD list for last 9 values (needed for signal line)
        ArrayList<Double> macdList = new ArrayList<>();
        for (int i = lastIndex - 8; i <= lastIndex; i++) {
            Double shortEma12 = calculateEMA(prices, 12, i);
            Double shortEma26 = calculateEMA(prices, 26, i);
            if (shortEma12 == null || shortEma26 == null) {
                System.out.println("Not enough data for signal line");
                return;
            }
            macdList.add(shortEma12 - shortEma26);
        }

        // Step 3: Calculate 9-period EMA of MACD list (Signal Line)
        double multiplier = 2.0 / (9 + 1);
        double sum = 0.0;
        for (double val : macdList) sum += val;
        double signal = sum / 9.0;
        for (double val : macdList) {
            signal = (val - signal) * multiplier + signal;
        }
        if (macd - signal > 0) {
            System.out.println("The MACD indicator is above its signal line, suggesting upward momentum in the near future.");
            buy++;
        } else if (macd - signal < 0) {
            System.out.println("The MACD indicator is below its signal line, suggesting downward momentum in the near future.");
            sell++;
        }
    }

    public static void stochastic(ArrayList<Double> prices) {
        ArrayList<Double> kList = new ArrayList<Double>();
        for (int j = 0; j < 3; j++) {
            double percentK = 0;
            double min = prices.get(j);
            double max = prices.get(j);
            for (int i = 1 + j; i < 14 + j; i++) {
                if (prices.get(i) > max) {
                    max = prices.get(i);
                }
                if (prices.get(i) < min) {
                    min = prices.get(i);
                }
            }
            percentK = (prices.get(j) - min) / (max - min) * 100;
            kList.add(percentK);
        }
        double averageK = 0;
        for (int i = 0; i < 3; i++) {
            averageK += kList.get(i);
        }
        averageK = averageK / 3;
        if (averageK > 80) {
            System.out.println("The stochastic indicator is in overbought territory, suggesting a potential reversal downwards in the near future.");
            sell++;
        } else if (averageK < 20) {
            System.out.println("The stochastic indicator is in oversold territory, suggesting a potential reversal upwards in the near future.");
            buy++;
        }
    }

    public static void CMF(ArrayList<Double> highs, ArrayList<Double> lows, ArrayList<Double> closes, ArrayList<Long> volumes) {
        ArrayList<Double> mfmList = new ArrayList<Double>();
        ArrayList<Double> mfvList = new ArrayList<Double>();
        for (int i = 0; i < 21; i++) {
            double high = highs.get(i);
            double low = lows.get(i);
            double close = closes.get(i);
            double mfm = ((close - low) - (high - close)) / (high - low);
            mfmList.add(mfm);
        }
        for (int i = 0; i < mfmList.size(); i++) {
            mfvList.add(mfmList.get(i) * volumes.get(i));
        }
        double numerator = 0;
        double denominator = 0;
        for (int i = 0; i < mfvList.size(); i++) {
            numerator += mfvList.get(i);
            denominator += volumes.get(i);
        }
        double cmf = numerator / denominator;
        if (cmf >= 0.25) {
            System.out.println("The Chaikin money flow indicates high buying pressure, indicating upward momentum");
            buy++;
        } else if (cmf <= -0.25) {
            System.out.println("The Chaikin money flow indicates high selling pressure, indicating downward momentum");
            sell++;
        }
    }
}