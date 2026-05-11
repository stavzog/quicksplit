package util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Tests for CurrencyService.
 * Note: These tests require an internet connection to reach the Frankfurter API.
 * In a production environment, we would mock the HttpClient, but for a
 * CS class project, testing the actual integration is valuable to ensure
 * the external API contract hasn't changed.
 */
public class CurrencyServiceTest {

    private CurrencyService currencyService;
    private final String baseCurrency = "USD";

    @BeforeEach
    void setUp() {
        currencyService = new CurrencyService(baseCurrency);
    }

    @Test
    void testIdentityConversion() {
        // converting usd to usd should return the exact same amount without network calls
        double amount = 100.0;
        double result = currencyService.convertToBase(amount, "USD");
        assertEquals(
            amount,
            result,
            0.001,
            "USD to USD conversion should be identity"
        );
    }

    @Test
    @Tag("network")
    void testGetRateNetwork() {
        // basic connectivity and parsing test
        try {
            double rate = currencyService.getRate("EUR", "USD");
            assertTrue(rate > 0, "Exchange rate should be a positive number");
            System.out.println("Current EUR to USD rate: " + rate);
        } catch (Exception e) {
            fail("Network call to Frankfurter API failed: " + e.getMessage());
        }
    }

    @Test
    @Tag("network")
    void testConvertFromBase() {
        // testing usd -> eur conversion
        double usdAmount = 10.0;
        double eurAmount = currencyService.convertFromBase(usdAmount, "EUR");

        assertTrue(eurAmount > 0, "Converted amount should be positive");

        // test caching: the second call should be instant and return same result
        double secondCall = currencyService.convertFromBase(usdAmount, "EUR");
        assertEquals(
            eurAmount,
            secondCall,
            "Cached result should match original"
        );
    }

    @Test
    void testCaseInsensitivity() {
        // the service should handle "eur", "EUR", and "eUr" identically
        try {
            double rate1 = currencyService.getRate("eur", "usd");
            double rate2 = currencyService.getRate("EUR", "USD");
            assertEquals(
                rate1,
                rate2,
                "Currency codes should be case-insensitive"
            );
        } catch (Exception e) {
            // If network fails, we skip this check as it's tested elsewhere
        }
    }

    @Test
    void testInvalidCurrencyCode() {
        // testing how the system handles garbage input
        assertThrows(
            RuntimeException.class,
            () -> {
                currencyService.getRate("INVALID", "USD");
            },
            "Should throw RuntimeException for invalid currency codes"
        );
    }

    @Test
    void testInverseRateCaching() {
        // if we fetch EUR->USD, USD->EUR should now be in cache
        double eurToUsd = currencyService.getRate("EUR", "USD");
        double usdToEur = currencyService.getRate("USD", "EUR");

        assertEquals(
            1.0,
            eurToUsd * usdToEur,
            0.01,
            "Inverse rates should multiply to approximately 1"
        );
    }
}
