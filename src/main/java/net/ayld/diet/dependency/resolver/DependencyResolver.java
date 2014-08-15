package net.ayld.diet.dependency.resolver;

import java.io.IOException;
import java.util.Set;

import net.ayld.diet.model.ClassName;

public interface DependencyResolver<T> {
	
	public Set<ClassName> resolve(T from) throws IOException;
	
	public Set<ClassName> resolve(Set<T> from) throws IOException;
}
