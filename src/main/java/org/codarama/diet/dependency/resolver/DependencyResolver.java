package org.codarama.diet.dependency.resolver;

import java.io.IOException;
import java.util.Set;

import org.codarama.diet.model.ClassName;
import org.codarama.diet.model.marker.Resolvable;

/**
 * Resolves a {@link Resolvable}'s dependencies.
 * */
public interface DependencyResolver<T extends Resolvable> {

    /**
     * Returns the dependencies of given resolvable as {@link ClassName}s.
     *
     * @param resolvable resolvable to find dependencies of
     * @return dependencies of given resolvable as {@link ClassName}s
     * */
	Set<ClassName> resolve(T resolvable) throws IOException;
	
	Set<ClassName> resolve(Set<T> resolvables) throws IOException;
}
