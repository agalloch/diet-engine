package org.codarama.diet.minimization.impl;

import com.google.common.collect.Sets;
import org.codarama.diet.dependency.resolver.DependencyResolver;
import org.codarama.diet.index.LibraryIndex;
import org.codarama.diet.minimization.MinimizationStrategy;
import org.codarama.diet.model.ClassName;
import org.codarama.diet.model.ClassStream;
import org.codarama.diet.model.SourceFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * A {@link org.codarama.diet.minimization.MinimizationStrategy} that indexes all .class files in library jars to a tree
 * and the runs queries on that tree for (hopefully) optimal performance.
 *
 * Created by ayld on 20.06.15.
 */
public class IndexedMinimizationStrategy implements MinimizationStrategy<SourceFile, JarFile, ClassStream>{

    private static final Logger LOG = LoggerFactory.getLogger(IndexedMinimizationStrategy.class);

    private DependencyResolver<SourceFile> sourceDependencyResolver;
    private DependencyResolver<ClassStream> classDependencyResolver;
    private LibraryIndex index;

    @Override
    public Set<ClassStream> minimize(Set<SourceFile> sources, Set<JarFile> libraries) throws IOException {
        final Set<ClassName> sourceDependencies = sourceDependencyResolver.resolve(sources);
        index.index(libraries);

        final Set<ClassStream> result = Sets.newHashSet();
        final Set<ClassName> resultNames = Sets.newHashSet();
        for (ClassName sourceDep : sourceDependencies) {
            resolveAndAddAllFromIndex(sourceDep, result, resultNames);
        }

        return result;
    }

    private void resolveAndAddAllFromIndex(ClassName depName, Set<ClassStream> deps, Set<ClassName> depNames) {
        if (!depNames.contains(depName) && index.contains(depName)) {

            final ClassStream dep = index.get(depName);

            deps.add(dep);
            depNames.add(depName);

            final Set<ClassName> depsOfDep = getDepsOfDep(dep);
            for (ClassName depOfDep : depsOfDep) {
                resolveAndAddAllFromIndex(depOfDep, deps, depNames);
            }
        }
    }

    private Set<ClassName> getDepsOfDep(ClassStream dep) {
        try {
            return classDependencyResolver.resolve(dep);
        } catch (IOException e) {
            // shouldn't happen here as we've called contains
            // so this happening here means a bug in the index or some runtime error
            throw new RuntimeException(e);
        }
    }

    @Required
    public void setClassDependencyResolver(DependencyResolver<ClassStream> classDependencyResolver) {
        this.classDependencyResolver = classDependencyResolver;
    }

    @Required
    public void setSourceDependencyResolver(DependencyResolver<SourceFile> sourceDependencyResolver) {
        this.sourceDependencyResolver = sourceDependencyResolver;
    }

    @Required
    public void setIndex(LibraryIndex index) {
        this.index = index;
    }
}
