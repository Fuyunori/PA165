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

    @Mock
    ExchangeRateTable exchangeRateTable;

    Currency eur = Currency.getInstance("EUR");
    Currency czk = Currency.getInstance("CZK");
    final BigDecimal EUR_TO_CZK_RATE = new BigDecimal("26.25");
    final BigDecimal ZERO = new BigDecimal("0");
    final BigDecimal ONE = new BigDecimal("1");

    @Before
    public void setUp() throws ExternalServiceFailureException {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testConvert() throws ExternalServiceFailureException {
        when(exchangeRateTable.getExchangeRate(eur, czk)).thenReturn(EUR_TO_CZK_RATE);
        BigDecimal exchangeRateFromEURtoCZK = exchangeRateTable.getExchangeRate(eur, czk);

        // BASIC TESTS
        // convert 1 EUR to CZK -> 26.25 CZK
        when(currencyConvertor.convert(eur, czk, ONE)).thenReturn(exchangeRateFromEURtoCZK.multiply(ONE));
        assertEquals(EUR_TO_CZK_RATE, currencyConvertor.convert(eur,czk,ONE));

        // convert 1 CZK to EUR -> 0.04 EUR
        when(currencyConvertor.convert(czk, eur, ONE)).thenReturn(ONE.divide(exchangeRateFromEURtoCZK, 2, RoundingMode.HALF_EVEN));
        assertEquals(new BigDecimal("0.04"), currencyConvertor.convert(czk,eur,ONE));

        // convert 26.25 CZK to EUR -> 1 EUR
        when(currencyConvertor.convert(czk, eur, EUR_TO_CZK_RATE)).thenReturn(exchangeRateFromEURtoCZK.divide(EUR_TO_CZK_RATE, RoundingMode.HALF_EVEN));
        assertEquals(new BigDecimal("1.00"), currencyConvertor.convert(czk,eur,EUR_TO_CZK_RATE));

        // convert 0.03809523809523809523809523809524 EUR to CZK -> 1 CZK
        when(currencyConvertor.convert(eur, czk, new BigDecimal("0.03809523809523809523809523809524"))).thenReturn(exchangeRateFromEURtoCZK.divide(EUR_TO_CZK_RATE, RoundingMode.HALF_EVEN));
        assertEquals(new BigDecimal("1.00"), currencyConvertor.convert(eur,czk,new BigDecimal("0.03809523809523809523809523809524")));

        // Don't forget to test border values and proper rounding.
    }

    /**
     * CONVERSION FROM THE SAME CURRENCY
     */
    @Test
    public void testConvertWithSameCurrencies() {
        // convert 0 CZK to CZK -> 0 CZK
        when(currencyConvertor.convert(czk, czk, ZERO)).thenReturn(new BigDecimal("0.00"));
        assertEquals(new BigDecimal("0.00"), currencyConvertor.convert(czk,czk,ZERO));

        // convert 0 EUR to EUR -> 0 EUR
        when(currencyConvertor.convert(eur, eur, ZERO)).thenReturn(new BigDecimal("0.00"));
        assertEquals(new BigDecimal("0.00"), currencyConvertor.convert(eur,eur,ZERO));
    }

    /**
     * CHECKING CORRECT ROUNDING UP ACCORDING TO HALF_EVEN
     * @throws ExternalServiceFailureException
     */
    @Test
    public void testConvertWithRoundingUp() throws ExternalServiceFailureException {
        when(exchangeRateTable.getExchangeRate(eur, czk)).thenReturn(EUR_TO_CZK_RATE);
        BigDecimal exchangeRateFromEURtoCZK = exchangeRateTable.getExchangeRate(eur, czk);

        // convert 90.708 CZK to EUR -> 3,4555... EUR - the rounding is half_even, thus it's rounded up, because the discarded number is odd
        final BigDecimal NINETY_POINT = new BigDecimal("90.69375");
        when(currencyConvertor.convert(czk, eur, NINETY_POINT)).thenReturn(NINETY_POINT.divide(exchangeRateFromEURtoCZK, 2, RoundingMode.HALF_EVEN));
        assertEquals(new BigDecimal("3.46"), currencyConvertor.convert(czk,eur, NINETY_POINT));
    }

    /**
     * CHECKING CORRECT ROUNDING DOWN ACCORDING TO HALF_EVEN
     * @throws ExternalServiceFailureException
     */
    @Test
    public void testConvertWithRoundingDown() throws ExternalServiceFailureException {
        when(exchangeRateTable.getExchangeRate(eur, czk)).thenReturn(EUR_TO_CZK_RATE);
        BigDecimal exchangeRateFromEURtoCZK = exchangeRateTable.getExchangeRate(eur, czk);

        // convert 67.85625 CZK to EUR -> 2.585 EUR - the rounding is half_even, thus it's rounded down, because the discarded number is even
        final BigDecimal SIXTY_SEVEN = new BigDecimal("67.85625");
        when(currencyConvertor.convert(czk, eur, SIXTY_SEVEN)).thenReturn(SIXTY_SEVEN.divide(exchangeRateFromEURtoCZK, 2, RoundingMode.HALF_EVEN));
        assertEquals(new BigDecimal("2.58"), currencyConvertor.convert(czk,eur, SIXTY_SEVEN));
    }

    /**
     * CHECKING MULTIPLYING BY ZERO
     */
    @Test
    public void testConvertWithZeroAmount(){
        // convert 0 EUR to CZK -> 0 CZK
        when(currencyConvertor.convert(eur, czk, ZERO)).thenReturn(ZERO);
        assertEquals(new BigDecimal("0.00"), currencyConvertor.convert(eur,czk,ZERO));

        // convert 0 EUR to CZK -> 0 CZK
        when(currencyConvertor.convert(czk, eur, ZERO)).thenReturn(ZERO);
        assertEquals(new BigDecimal("0.00"), currencyConvertor.convert(czk,eur,ZERO));
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
        Currency anySourceCurrency = any(Currency.class);
        Currency anyTargetCurrency = any(Currency.class);

        when(currencyConvertor.convert(anySourceCurrency, anyTargetCurrency, eq(null))).thenThrow(new IllegalArgumentException());
        assertThrows(IllegalArgumentException.class, () -> currencyConvertor.convert(eq(czk),eq(eur),eq(null)));
    }

    @Test
    public void testConvertWithUnknownCurrency() throws ExternalServiceFailureException {
        Currency anySourceCurrency = any(Currency.class);
        Currency anyTargetCurrency = any(Currency.class);
        BigDecimal anySourceAmount = any(BigDecimal.class);

        when(exchangeRateTable.getExchangeRate(anySourceCurrency, anyTargetCurrency)).thenReturn(null);
        BigDecimal exchangeRateFromEURtoCZK = exchangeRateTable.getExchangeRate(eur, czk);

        when(currencyConvertor.convert(anySourceCurrency, anyTargetCurrency, anySourceAmount)).thenThrow(
                new UnknownExchangeRateException("The exchange rate is not known,\n" +
                        "     * because the lookup failed or information about given currencies pair is\n" +
                        "     * not available"));

    }

    @Test
    public void testConvertWithExternalServiceFailure() {
        fail("Test is not implemented yet.");
    }

}
