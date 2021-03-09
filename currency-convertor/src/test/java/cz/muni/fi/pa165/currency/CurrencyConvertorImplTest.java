package cz.muni.fi.pa165.currency;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CurrencyConvertorImplTest {
    @Mock
    CurrencyConvertor currencyConvertor;

    Currency eur = Currency.getInstance("EUR");
    Currency czk = Currency.getInstance("CZK");
    final BigDecimal EUR_TO_CZK_RATE = new BigDecimal("26.25");

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testConvert() {
        final BigDecimal ZERO = new BigDecimal("0");
        final BigDecimal ONE = new BigDecimal("1");
        final BigDecimal SIXTY_SEVEN_POINT_SOMETHING = new BigDecimal("67.85625");
        final BigDecimal NINETY_POINT_SOMETHING = new BigDecimal("90.69375");

        // BASIC TESTS
        // convert 1 EUR to CZK -> 26.25 CZK
        when(currencyConvertor.convert(eur, czk, ONE)).thenReturn(EUR_TO_CZK_RATE.multiply(ONE));
        assertEquals(EUR_TO_CZK_RATE, currencyConvertor.convert(eur,czk,ONE));

        // convert 1 CZK to EUR -> 0.04 EUR
        when(currencyConvertor.convert(czk, eur, ONE)).thenReturn(ONE.divide(EUR_TO_CZK_RATE, 2, RoundingMode.HALF_EVEN));
        assertEquals(new BigDecimal("0.04"), currencyConvertor.convert(czk,eur,ONE));

        // convert 26.25 CZK to EUR -> 1 EUR
        when(currencyConvertor.convert(czk, eur, EUR_TO_CZK_RATE)).thenReturn(EUR_TO_CZK_RATE.divide(EUR_TO_CZK_RATE, RoundingMode.HALF_EVEN));
        assertEquals(new BigDecimal("1.00"), currencyConvertor.convert(czk,eur,EUR_TO_CZK_RATE));

        // Don't forget to test border values and proper rounding.
        // CHECKING CORRECT ROUNDING ACCORDING TO HALF_EVEN
        // convert 67.85625 CZK to EUR -> 2.585 EUR - the rounding is half_even, thus it's rounded down, because the discarded number is even
        when(currencyConvertor.convert(czk, eur, SIXTY_SEVEN_POINT_SOMETHING)).thenReturn(SIXTY_SEVEN_POINT_SOMETHING.divide(EUR_TO_CZK_RATE, 2, RoundingMode.HALF_EVEN));
        assertEquals(new BigDecimal("2.58"), currencyConvertor.convert(czk,eur,SIXTY_SEVEN_POINT_SOMETHING));

        // convert 90.708 CZK to EUR -> 3,4555... EUR - the rounding is half_even, thus it's rounded up, because the discarded number is odd
        when(currencyConvertor.convert(czk, eur, NINETY_POINT_SOMETHING)).thenReturn(NINETY_POINT_SOMETHING.divide(EUR_TO_CZK_RATE, 2, RoundingMode.HALF_EVEN));
        assertEquals(new BigDecimal("3.46"), currencyConvertor.convert(czk,eur,NINETY_POINT_SOMETHING));

        // CHECKING MULTIPLYING BY ZERO
        // convert 0 EUR to CZK -> 0 CZK
        when(currencyConvertor.convert(eur, czk, ZERO)).thenReturn(EUR_TO_CZK_RATE.multiply(ZERO));
        assertEquals(new BigDecimal("0.00"), currencyConvertor.convert(eur,czk,ZERO));

        // convert 0 EUR to CZK -> 0 CZK
        when(currencyConvertor.convert(czk, eur, ZERO)).thenReturn(EUR_TO_CZK_RATE.multiply(ZERO));
        assertEquals(new BigDecimal("0.00"), currencyConvertor.convert(czk,eur,ZERO));

        // SAME CURRENCY CONVERSION - NO CHANGE
        // convert 0 CZK to CZK -> 0 CZK
        Currency currency = any(Currency.class);
        when(currencyConvertor.convert(currency, currency, ZERO)).thenReturn(new BigDecimal("0.00"));
        assertEquals(new BigDecimal("0.00"), currencyConvertor.convert(eq(czk),eq(czk),ZERO));

        // convert 0 EUR to EUR -> 0 EUR
        when(currencyConvertor.convert(eur, eur, ZERO)).thenReturn(new BigDecimal("0.00"));
        assertEquals(new BigDecimal("0.00"), currencyConvertor.convert(eur,eur,ZERO));
    }

    @Test
    public void testConvertWithNullSourceCurrency() {
        Currency anyTargetCurrency = any(Currency.class);
        BigDecimal anySourceAmount = any(BigDecimal.class);

        when(currencyConvertor.convert(eq(null), anyTargetCurrency, anySourceAmount)).thenThrow(new IllegalArgumentException());
        assertThrows(IllegalArgumentException.class, () -> currencyConvertor.convert(eq(null), eq(czk), eq(EUR_TO_CZK_RATE)));
        assertThrows(IllegalArgumentException.class, () -> currencyConvertor.convert(eq(null), eq(eur), eq(EUR_TO_CZK_RATE)));
    }

    @Test
    public void testConvertWithNullTargetCurrency() {
        Currency anySourceCurrency = any(Currency.class);
        BigDecimal anySourceAmount = any(BigDecimal.class);

        when(currencyConvertor.convert(anySourceCurrency, eq(null), anySourceAmount)).thenThrow(new IllegalArgumentException());
        assertThrows(IllegalArgumentException.class, () -> currencyConvertor.convert(eq(czk), eq(null), eq(EUR_TO_CZK_RATE)));
        assertThrows(IllegalArgumentException.class, () -> currencyConvertor.convert(eq(eur), eq(null), eq(EUR_TO_CZK_RATE)));
    }

    @Test
    public void testConvertWithNullSourceAmount() {
        when(currencyConvertor.convert(czk, eur, null)).thenThrow(new IllegalArgumentException());
        assertThrows(IllegalArgumentException.class, () -> currencyConvertor.convert(czk,eur,null));
    }

    @Test
    public void testConvertWithUnknownCurrency() {
        Currency unknownCurrency = Currency.getInstance("XYZ");
        when(currencyConvertor.convert(unknownCurrency, eur, EUR_TO_CZK_RATE)).thenThrow(new IllegalArgumentException());
        assertThrows(IllegalArgumentException.class, () -> currencyConvertor.convert(unknownCurrency, eur, EUR_TO_CZK_RATE));

        when(currencyConvertor.convert(czk, unknownCurrency, EUR_TO_CZK_RATE)).thenThrow(new IllegalArgumentException());
        assertThrows(IllegalArgumentException.class, () -> currencyConvertor.convert(czk, unknownCurrency, EUR_TO_CZK_RATE));
    }

    @Test
    public void testConvertWithExternalServiceFailure() {
        fail("Test is not implemented yet.");
    }

}
