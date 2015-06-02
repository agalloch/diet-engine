package org.codarama.diet.dependency.resolver.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.codarama.diet.component.ListenableComponent;
import org.codarama.diet.dependency.resolver.DependencyResolver;
import org.codarama.diet.model.ClassName;
import org.codarama.diet.util.system.Jdeps;

import java.io.IOException;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * Uses the Jdeps JDK 8 util to try and resolve dependencies of a binary {@link org.codarama.diet.model.ClassName} file.
 *
 * This is to be used in cases where you need to know
 *
 * @see {@link org.codarama.diet.util.system.Jdeps}
 *
 * Created by ayld on 5/31/2015.
 */
public class JdepsClassNameDependencyResolver extends ListenableComponent implements DependencyResolver<ClassName> {

    private final Set<JarFile> jars;

    private JdepsClassNameDependencyResolver(Set<JarFile> jars) {
        this.jars = jars;
    }

    @Override
    public Set<ClassName> resolve(ClassName resolvable) throws IOException {
        return Jdeps.Builder
                .searchInJars(jars)
                .forDependenciesOf(
                        resolvable
                )
                .build()
                .findDependencies();
    }

    @Override
    public Set<ClassName> resolve(Set<ClassName> resolvables) throws IOException {
        final Set<ClassName> result = Sets.newHashSet();
        for (ClassName resolvable : resolvables) {
            result.addAll(resolve(resolvable));
        }
        return result;
    }

    public static class Builder {

        private final Set<JarFile> jars;

        private Builder(Set<JarFile> jars) {
            this.jars = jars;
        }

        public static Builder jars(Set<JarFile> jars) {
            return new Builder(jars);
        }

        public JdepsClassNameDependencyResolver build() {
            return new JdepsClassNameDependencyResolver(jars);
        }
    }
}
