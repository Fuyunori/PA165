package cz.muni.fi.pa165;

import cz.muni.fi.pa165.aspects.MethodDurationMeasurement;
import cz.muni.fi.pa165.currency.ExchangeRateTable;
import cz.muni.fi.pa165.currency.ExchangeRateTableImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@ComponentScan(basePackageClasses = {ExchangeRateTable.class, MethodDurationMeasurement.class})
@EnableAspectJAutoProxy
public class JavaConfig {
    
    @Bean
    public ExchangeRateTable exchangeRateTable(){
        return new ExchangeRateTableImpl();
    }
}
