package cz.muni.fi.pa165.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
public class MethodDurationMeasurement {

    @Around("execution(public * *(..))")
    public Object measure(ProceedingJoinPoint pjp) throws Throwable {
        StopWatch stopWatch = new StopWatch();

        stopWatch.start();
        Object result = pjp.proceed();
        stopWatch.stop();

        System.out.println(stopWatch.shortSummary());

        return result;
    }
}
