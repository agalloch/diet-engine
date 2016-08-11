package org.codarama.diet.event.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Exposes resolution related joinpoints.
 *
 * @see {@link org.codarama.diet.dependency.resolver.DependencyResolver}
 */
@Aspect
public class ResolutionAspect {

    // the resolve method of the class stream resolver
    @Pointcut(("execution(* resolve(..)) && args(org.codarama.diet.model.ClassStream) && within(org.codarama.diet.dependency.resolver.impl.ClassStreamDependencyResolver)"))
    public void classStreamResolve() {};

    // the resolve method of the source resolver
    @Pointcut(("execution(* resolve(..)) && args(org.codarama.diet.model.SourceFile) && within(org.codarama.diet.dependency.resolver.impl.ManualParseSourceDependencyResolver)"))
    public void sourceResolve() {};
}
