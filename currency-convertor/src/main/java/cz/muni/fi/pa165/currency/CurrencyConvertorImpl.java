package cz.muni.fi.pa165.currency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;


/**
 * This is base implementation of {@link CurrencyConvertor}.
 *
 * @author petr.adamek@embedit.cz
 */
public class CurrencyConvertorImpl implements CurrencyConvertor {

    private final ExchangeRateTable exchangeRateTable;
    //private final Logger logger = LoggerFactory.getLogger(CurrencyConvertorImpl.class);

    public CurrencyConvertorImpl(ExchangeRateTable exchangeRateTable) {
        this.exchangeRateTable = exchangeRateTable;
    }

    @Override
    public BigDecimal convert(Currency sourceCurrency, Currency targetCurrency, BigDecimal sourceAmount) {
        if(sourceCurrency == null || targetCurrency == null || sourceAmount == null){
            throw new IllegalArgumentException();
        }

        if(sourceCurrency.equals(targetCurrency)){
            return convertToTwoDecimalPointsUsingHalfEven(sourceAmount);
        }

        try {
            BigDecimal exchangeRate = exchangeRateTable.getExchangeRate(sourceCurrency,targetCurrency);

            if(exchangeRate == null){
                throw new UnknownExchangeRateException("The currency is unknown");
            }

            BigDecimal result = sourceAmount.multiply(exchangeRate);
            return convertToTwoDecimalPointsUsingHalfEven(result);
        } catch (ExternalServiceFailureException esfe){
            throw new UnknownExchangeRateException("The exchange rate couldn't be retrieved.");
        }
    }

    private BigDecimal convertToTwoDecimalPointsUsingHalfEven(BigDecimal result) {
        return result.setScale(2, RoundingMode.HALF_EVEN);
    }

}
