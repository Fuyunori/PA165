package cz.muni.fi.pa165.currency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final Logger logger = LoggerFactory.getLogger(CurrencyConvertorImpl.class);

    public CurrencyConvertorImpl(ExchangeRateTable exchangeRateTable) {
        this.exchangeRateTable = exchangeRateTable;
    }

    @Override
    public BigDecimal convert(Currency sourceCurrency, Currency targetCurrency, BigDecimal sourceAmount) {
        logger.trace("Convert method called with this parameter {}", exchangeRateTable);
        if(sourceCurrency == null || targetCurrency == null || sourceAmount == null){
            logger.warn("At least one of the arguments of the convert method is null.");
            throw new IllegalArgumentException();
        }

        if(sourceCurrency.equals(targetCurrency)){
            logger.trace("Convert method successfully finished with two same currency as arguments.");
            return convertToTwoDecimalPointsUsingHalfEven(sourceAmount);
        }

        try {
            BigDecimal exchangeRate = exchangeRateTable.getExchangeRate(sourceCurrency,targetCurrency);

            if(exchangeRate == null){
                logger.warn("Exchange rate returned by {} is null.", exchangeRateTable);
                throw new UnknownExchangeRateException("The currency is unknown");
            }

            BigDecimal result = sourceAmount.multiply(exchangeRate);
            logger.trace("Convert method successfully finished.");
            return convertToTwoDecimalPointsUsingHalfEven(result);
        } catch (ExternalServiceFailureException esfe){
            logger.error("Exchange rate returned by {} couldn't be retrieved.", exchangeRateTable);
            throw new UnknownExchangeRateException("The exchange rate couldn't be retrieved.");
        }
    }

    private BigDecimal convertToTwoDecimalPointsUsingHalfEven(BigDecimal result) {
        return result.setScale(2, RoundingMode.HALF_EVEN);
    }

}
