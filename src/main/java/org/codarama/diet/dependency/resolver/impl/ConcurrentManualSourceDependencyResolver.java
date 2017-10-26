package org.codarama.diet.dependency.resolver.impl;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.codarama.diet.dependency.resolver.DependencyResolver;
import org.codarama.diet.model.ClassName;
import org.codarama.diet.model.SourceFile;

import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class ConcurrentManualSourceDependencyResolver extends ManualParseSourceDependencyResolver implements DependencyResolver<SourceFile> {

    private ExecutorService threadPool;

    @Override
    public Set<ClassName> resolve(Set<SourceFile> sources) throws IOException {
        final Set<ClassName> result = Sets.newHashSet();

        final Set<Callable<Set<ClassName>>> resolutionTasks = Sets.newHashSetWithExpectedSize(sources.size());
        for (final SourceFile source : sources) {

            final Callable<Set<ClassName>> resolutionTask = new Callable<Set<ClassName>>() {

                @Override
                public Set<ClassName> call() throws Exception {
                    return resolve(source);
                }
            };
            resolutionTasks.add(resolutionTask);
        }
        try {
            final List<Future<Set<ClassName>>> futures = threadPool.invokeAll(resolutionTasks);
            for (Future<Set<ClassName>> f : futures) {

                result.addAll(f.get());

            }
            threadPool.shutdown();
            threadPool.awaitTermination(1, TimeUnit.DAYS); // FOREVER !!!

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return ImmutableSet.copyOf(result);
    }

    @Required
    public void setThreadPool(ExecutorService threadPool) {
        this.threadPool = threadPool;
    }
}
