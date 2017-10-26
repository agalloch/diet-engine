package org.codarama.diet.event.aop;

import com.google.common.base.Stopwatch;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.codarama.diet.component.ListenableComponent;
import org.codarama.diet.event.model.ClassDependencyResolutionEndEvent;
import org.codarama.diet.event.model.ClassDependencyResolutionStartEvent;
import org.codarama.diet.event.model.MinimizationEndEvent;
import org.codarama.diet.event.model.MinimizationEvent;
import org.codarama.diet.event.model.MinimizationStartEvent;
import org.codarama.diet.event.model.SourceDependencyResolutionEndEvent;
import org.codarama.diet.event.model.SourceDependencyResolutionStartEvent;
import org.codarama.diet.model.ClassName;
import org.codarama.diet.model.SourceFile;

import java.io.IOException;
import java.util.Set;

/**
 * Intended to contain advices for components that need to be profiled.
 * Usually stuff here is added on a currently needed basis.
 */
@Aspect
public class ProfilingAdvice extends ListenableComponent {

    @Around("org.codarama.diet.event.aop.IndexingAspect.get()")
    public Object profileGet(ProceedingJoinPoint pjp) throws Throwable {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        eventBus.post(
                new MinimizationEvent("Index.get called for: " + getDepNameFromIndexGetArgs(pjp.getArgs()),
                        this.getClass())
        );

        // execute joinpoint
        final Object joinpointResult = pjp.proceed();

        stopwatch.stop();
        eventBus.post(
                new MinimizationEvent("Index.get done in: " + stopwatch,
                        this.getClass())
        );
        return joinpointResult;
    }

    @Around("org.codarama.diet.event.aop.IndexingAspect.index()")
    public Object profileIndex(ProceedingJoinPoint pjp) throws Throwable {

        final Stopwatch stopwatch = Stopwatch.createStarted();
        eventBus.post(
                new MinimizationEvent("Starting indexing",
                        this.getClass())
        );

        // execute joinpoint
        final Object joinpointResult = pjp.proceed();

        stopwatch.stop();
        eventBus.post(
                new MinimizationEvent("Indexing done in: " + stopwatch,
                        this.getClass())
        );

        return joinpointResult;
    }

    @Around("org.codarama.diet.event.aop.MinimizationAspect.minimize()")
    public Object profileMinimize(ProceedingJoinPoint pjp) throws Throwable {

        eventBus.post(
                new MinimizationStartEvent(
                        "Minimization starting on: " + getSourcesSizeFromMinimizeArgs(pjp.getArgs()) +
                                " sources, " + getLibsSizeFromMinimizeArgs(pjp.getArgs()) +
                                " libs", this.getClass())
        );
        final Stopwatch stopwatch = Stopwatch.createStarted();

        // execute joinpoint
        final Object joinpointResult = pjp.proceed();

        stopwatch.stop();
        eventBus.post(
                new MinimizationEndEvent("Minimization done in: " + stopwatch,
                        this.getClass())
        );

        return joinpointResult;
    }

    @Around("org.codarama.diet.event.aop.ResolutionAspect.classStreamResolve()")
    public Object profileStreamResolve(ProceedingJoinPoint pjp) throws Throwable {

        // TODO AOP for the stopwatch and the events
        final Stopwatch stopwatch = Stopwatch.createStarted();
        eventBus.post(
                new ClassDependencyResolutionStartEvent("Resolving: "
                        + getClassNameFromStreamResolveArgs(pjp.getArgs()), this.getClass())
        );

        // execute joinpoint
        final Object joinpointResult = pjp.proceed();

        stopwatch.stop();
        eventBus.post(
                new ClassDependencyResolutionEndEvent("Class dependency resolution took: "
                        + stopwatch.toString(), this.getClass())
        );

        return joinpointResult;
    }

    @Around("org.codarama.diet.event.aop.ResolutionAspect.sourceResolve()")
    public Object profileSourceResolve(ProceedingJoinPoint pjp) throws Throwable {

        eventBus.post(new SourceDependencyResolutionStartEvent("resolving: "
                + getSourcePathFromSourceResolveArgs(pjp.getArgs()), this.getClass()));

        // execute joinpoint
        final Object joinpointResult = pjp.proceed();

        eventBus.post(new SourceDependencyResolutionEndEvent("resolved: " + joinpointResult, this.getClass()));

        return joinpointResult;
    }

    private String getSourcePathFromSourceResolveArgs(Object[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException(this.getClass().getSimpleName()
                    + " expects single argument to sourceResolve()");
        }
        return ((SourceFile) args[0]).physicalFile().getAbsolutePath();
    }

    private String getClassNameFromStreamResolveArgs(Object[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() +
                    " expects single argument to classResolve()");
        }
        return args[0].toString();
    }

    private int getSourcesSizeFromMinimizeArgs(Object[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " expects 2 arguments to minimize()");
        }
        return ((Set) args[0]).size();
    }

    private int getLibsSizeFromMinimizeArgs(Object[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " expects 2 arguments to minimize()");
        }
        return ((Set) args[1]).size();
    }

    private String getDepNameFromIndexGetArgs(Object[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " expects a single argument to get()");
        }
        if (!ClassName.class.isAssignableFrom(args[0].getClass())) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " expects ClassName as argument to minimize()");
        }
        return args[0].toString();
    }
}
