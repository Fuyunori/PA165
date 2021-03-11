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
    CurrencyConvertor currencyConvertor;

    @Mock
    ExchangeRateTable exchangeRateTable;

    Currency eur = Currency.getInstance("EUR");
    Currency czk = Currency.getInstance("CZK");
    final BigDecimal ZERO = new BigDecimal("0");
    final BigDecimal ONE = new BigDecimal("1");
    final BigDecimal EUR_TO_CZK_RATE = new BigDecimal("26.25");
    final BigDecimal CZK_TO_EUR_RATE = ONE.divide(EUR_TO_CZK_RATE, 12, RoundingMode.HALF_EVEN);

    @Before
    public void setUp() throws ExternalServiceFailureException {
        MockitoAnnotations.initMocks(this);

        when(exchangeRateTable.getExchangeRate(eur, czk)).thenReturn(EUR_TO_CZK_RATE);
        when(exchangeRateTable.getExchangeRate(czk, eur)).thenReturn(CZK_TO_EUR_RATE);

        currencyConvertor = new CurrencyConvertorImpl(exchangeRateTable);
    }

    @Test
    public void testConvert() throws ExternalServiceFailureException {
        // BASIC TESTS
        // convert 1 EUR to CZK -> 26.25 CZK
        assertEquals(new BigDecimal("26.25"), currencyConvertor.convert(eur,czk,ONE));

        // convert 1 CZK to EUR -> 0.04 EUR
        assertEquals(new BigDecimal("0.04"), currencyConvertor.convert(czk,eur,ONE));

        // convert 0.03809523809523809523809523809524 EUR to CZK -> 1 CZK
        final BigDecimal ZERO_POINT_THREE = new BigDecimal("0.03809523809523809523809523809524");
        assertEquals(retainTwoDecimalPoints(ONE), currencyConvertor.convert(eur,czk,ZERO_POINT_THREE));

        // convert 26.25 CZK to EUR -> 1 EUR
        assertEquals(retainTwoDecimalPoints(ONE), currencyConvertor.convert(czk,eur,EUR_TO_CZK_RATE));

        // Don't forget to test border values and proper rounding.
    }

    private BigDecimal retainTwoDecimalPoints(BigDecimal value){
        return value.setScale(2, RoundingMode.HALF_EVEN);
    }

    /**
     * CONVERSION FROM THE SAME CURRENCY
     */
    @Test
    public void testConvertWithSameCurrencies()  {
        // convert 0 CZK to CZK -> 0 CZK
        assertEquals(retainTwoDecimalPoints(ZERO), currencyConvertor.convert(czk,czk,ZERO));

        // convert 0 EUR to EUR -> 0 EUR
        assertEquals(retainTwoDecimalPoints(ZERO), currencyConvertor.convert(eur,eur,ZERO));
    }

    /**
     * CHECKING CORRECT ROUNDING UP ACCORDING TO HALF_EVEN
     */
    @Test
    public void testConvertWithRoundingUp() {
        // convert 90.708 CZK to EUR -> 3,4555... EUR - the rounding is half_even, thus it's rounded up, because the discarded number is odd
        final BigDecimal NINETY_POINT = new BigDecimal("90.69375");
        assertEquals(new BigDecimal("3.45"), currencyConvertor.convert(czk,eur, NINETY_POINT));
    }

    /**
     * CHECKING CORRECT ROUNDING DOWN ACCORDING TO HALF_EVEN
     */
    @Test
    public void testConvertWithRoundingDown() {
        // convert 67.85625 CZK to EUR -> 2.585 EUR - the rounding is half_even, thus it's rounded down, because the discarded number is even
        final BigDecimal SIXTY_SEVEN = new BigDecimal("67.85625");
        assertEquals(new BigDecimal("2.58"), currencyConvertor.convert(czk,eur, SIXTY_SEVEN));
    }

    /**
     * CHECKING MULTIPLYING BY ZERO
     */
    @Test
    public void testConvertWithZeroAmount() {
        // convert 0 EUR to CZK -> 0 CZK
        assertEquals(retainTwoDecimalPoints(ZERO), currencyConvertor.convert(eur,czk,ZERO));

        // convert 0 CZK to EUR -> 0 EUR
        assertEquals(retainTwoDecimalPoints(ZERO), currencyConvertor.convert(czk,eur,ZERO));
    }

    @Test
    public void testConvertWithNullSourceCurrency() {
        assertThrows(IllegalArgumentException.class, () -> currencyConvertor.convert(null, czk, EUR_TO_CZK_RATE));
        assertThrows(IllegalArgumentException.class, () -> currencyConvertor.convert(null, eur, EUR_TO_CZK_RATE));
    }

    @Test
    public void testConvertWithNullTargetCurrency() {
        assertThrows(IllegalArgumentException.class, () -> currencyConvertor.convert(czk, null, EUR_TO_CZK_RATE));
        assertThrows(IllegalArgumentException.class, () -> currencyConvertor.convert(eur, null, EUR_TO_CZK_RATE));
    }

    @Test
    public void testConvertWithNullSourceAmount() {
        assertThrows(IllegalArgumentException.class, () -> currencyConvertor.convert(czk,eur,null));
    }

    @Test
    public void testConvertWithUnknownCurrency() throws ExternalServiceFailureException {
        when(exchangeRateTable.getExchangeRate(any(Currency.class), any(Currency.class))).thenReturn(null);
        assertThrows(UnknownExchangeRateException.class,() -> currencyConvertor.convert(czk, eur, ZERO));
    }

    @Test
    public void testConvertWithExternalServiceFailure() throws ExternalServiceFailureException {
        when(exchangeRateTable.getExchangeRate(any(Currency.class), any(Currency.class))).thenThrow(new ExternalServiceFailureException("The service is unavailable"));
        assertThrows(UnknownExchangeRateException.class, () -> currencyConvertor.convert(czk, eur, ZERO));
        assertThrows(UnknownExchangeRateException.class, () -> currencyConvertor.convert(eur, czk, ZERO));
    }

}
