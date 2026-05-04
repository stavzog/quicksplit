package util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import mjson.Json;

/**
 * Service for handling currency conversion using the Frankfurter API.
 * Implements an in-memory cache to minimize network overhead.
 */
public class CurrencyService {

    // Frankfurter API endpoint for fetching currency rates
    // docs at https://frankfurter.dev
    private static final String API_URL = "https://api.frankfurter.app/latest";
    // store the results of the api lookup into RAM cache so that we
    // don't need to make repeated calls to the api for the same currencies
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
     * Fetches the exchange rate between two currencies.
     * Uses an in-memory cache to avoid redundant API calls.
     */
    public double getRate(String from, String to) {
        String cacheKey = from + "_" + to;
        if (ratesCache.containsKey(cacheKey)) {
            return ratesCache.get(cacheKey);
        }

        try {
            // build the request like https://api.frankfurter.app/latest?from=EUR&to=USD
            String url = String.format("%s?from=%s&to=%s", API_URL, from, to);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                    "Failed to fetch exchange rate: HTTP " +
                        response.statusCode()
                );
            }

            // parse response
            // format: {"amount":1.0,"base":"EUR","date":"2024-05-03","rates":{"USD":1.0765}}
            Json data = Json.read(response.body());
            double rate = data.at("rates").at(to).asDouble();

            ratesCache.put(cacheKey, rate);
            // also cache the inverse rate to be efficient
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
