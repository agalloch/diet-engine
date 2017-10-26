package org.codarama.diet.event.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Aspect intended to contain pointcuts for minimization joinpoints.
 *
 * @see {@link org.codarama.diet.util.Components}
 */
@Aspect
public class MinimizationAspect {

    // any minimize method in the minimization.impl package
    @Pointcut("execution(* minimize(..)) && within(org.codarama.diet.minimization.impl..*)")
    public void minimize() {};
}
