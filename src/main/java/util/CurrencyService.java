package util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import mjson.Json;

/**
 * Service for handling currency conversion using the Frankfurter v2 API.
 * Implements an in-memory cache to minimize network overhead.
 */
public class CurrencyService {

    // Frankfurter v2 API endpoint for fetching single pair rates
    private static final String API_BASE_URL =
        "https://api.frankfurter.dev/v2/rate";

    // in memory cache for exchange rates
    private final Map<String, Double> ratesCache = new HashMap<>();
    private final String baseCurrency;
    private final HttpClient httpClient;

    public CurrencyService(String baseCurrency) {
        this.baseCurrency = baseCurrency.toUpperCase();
        this.httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();
    }

    /**
     * Converts an amount from a given currency to the base currency.
     * @param amount the amount to convert
     * @param fromCurrency the source currency code (e.g., "EUR")
     * @return the amount in base currency (USD)
     */
    public double convertToBase(double amount, String fromCurrency) {
        fromCurrency = fromCurrency.toUpperCase();
        if (fromCurrency.equals(baseCurrency)) {
            return amount;
        }
        double rate = getRate(fromCurrency, baseCurrency);
        return amount * rate;
    }

    /**
     * Converts an amount from the base currency to a target currency.
     * @param amount the amount in base currency (USD)
     * @param toCurrency the target currency code (e.g., "GBP")
     * @return the converted amount
     */
    public double convertFromBase(double amount, String toCurrency) {
        toCurrency = toCurrency.toUpperCase();
        if (toCurrency.equals(baseCurrency)) {
            return amount;
        }
        double rate = getRate(baseCurrency, toCurrency);
        return amount * rate;
    }

    /**
     * Fetches the exchange rate between two currencies using the v2 /rate/BASE/QUOTE endpoint.
     * Uses an in-memory cache to avoid redundant API calls.
     */
    public double getRate(String from, String to) {
        if (from.equals(to)) {
            return 1.0;
        }

        from = from.toUpperCase();
        to = to.toUpperCase();

        String cacheKey = from + "_" + to;
        if (ratesCache.containsKey(cacheKey)) {
            return ratesCache.get(cacheKey);
        }

        try {
            // format: https://api.frankfurter.dev/v2/rate/EUR/USD
            String url = String.format("%s/%s/%s", API_BASE_URL, from, to);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                // parse error message
                String errorMsg;
                try {
                    errorMsg = Json.read(response.body())
                        .at("message")
                        .asString();
                } catch (Exception e) {
                    errorMsg = "HTTP " + response.statusCode();
                }
                throw new RuntimeException("API Error: " + errorMsg);
            }

            // format: {"amount":1.0,"base":"EUR","date":"2024-05-22","quote":"USD","rate":1.0825}
            Json data = Json.read(response.body());
            double rate = data.at("rate").asDouble();

            ratesCache.put(cacheKey, rate);

            // cache the inverse rate to be efficient
            ratesCache.put(to + "_" + from, 1.0 / rate);

            return rate;
        } catch (Exception e) {
            throw new RuntimeException(
                "Error during currency conversion: " + e.getMessage(),
                e
            );
        }
    }
}
