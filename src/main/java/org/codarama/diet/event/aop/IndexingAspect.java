package org.codarama.diet.event.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Aspect intended to contain pointcuts for indexing joinpoints.
 *
 * @see {@link org.codarama.diet.util.Components}
 */
@Aspect
public class IndexingAspect {

    // any get method in the index package
    @Pointcut(("execution(* get(..)) && within(org.codarama.diet.index.impl..*)"))
    public void get() {};

    // any index method in the index package
    @Pointcut(("execution(* index(..)) && within(org.codarama.diet.index.impl..*)"))
    public void index() {};
}
