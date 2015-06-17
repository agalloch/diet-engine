package org.codarama.diet.api.minimization.impl;

import com.google.common.collect.Sets;
import org.codarama.diet.api.minimization.MinimizationStrategy;
import org.codarama.diet.bundle.JarExploder;
import org.codarama.diet.dependency.matcher.DependencyMatcherStrategy;
import org.codarama.diet.dependency.resolver.DependencyResolver;
import org.codarama.diet.model.ClassFile;
import org.codarama.diet.model.ClassName;
import org.codarama.diet.model.SourceFile;
import org.codarama.diet.util.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarFile;

import static org.codarama.diet.util.system.JdkSettings.JAVA_ROOT_PACKAGE;

/**
 * A {@link org.codarama.diet.api.minimization.MinimizationStrategy} utilizing the Apache BCEL library.
 *
 * Created by ayld on 6/6/2015.
 */
public class BcelMinimizationStrategy implements MinimizationStrategy<SourceFile, File> {
    private static final Logger LOG = LoggerFactory.getLogger(BcelMinimizationStrategy.class);

    private DependencyMatcherStrategy dependencyMatcherStrategy;
    private DependencyResolver<ClassFile> classDependencyResolver;
    private DependencyResolver<SourceFile> sourceDependencyResolver;

    private JarExploder libJarExploder;
    private Files fileFinder;

    public Set<ClassFile> minimize(Set<SourceFile> sources, Set<File> libs) throws IOException {
        final Set<ClassName> sourceDependencies = sourceDependencyResolver.resolve(sources);

        explodeJars(libs);

        final Set<File> libClasses = Sets.newHashSet(fileFinder.withExtension(ClassFile.EXTENSION).list());

        // find sources dependencies
        final Set<ClassFile> result = findInLib(sourceDependencies, libClasses);

        // recursively find class file dependencies until we find them all
        addDependenciesOfDependencies(result, libClasses);

        return result;
    }

    private Set<ClassFile> addDependenciesOfDependencies(Set<ClassFile> deps, final Set<File> libClasses) throws IOException {
        removeJavaApiDeps(deps);

        final int sizeBeforeResolve = deps.size();

        deps.addAll(findInLib(classDependencyResolver.resolve(deps), libClasses));
        LOG.debug("Found " + (deps.size() - sizeBeforeResolve) + " new lib classes for inclusion");
        if (deps.size() == sizeBeforeResolve) {
            return deps;
        }

        return addDependenciesOfDependencies(deps, libClasses);
    }

    private Set<ClassFile> findInLib(Set<ClassName> dependencyNames, Set<File> libClasses) throws IOException {
        final int dependenciesCount = dependencyNames.size();
        LOG.debug("Looking for " + dependenciesCount + " dependencies in " + libClasses.size() + " lib classes");

        final Set<ClassFile> result = Sets.newHashSetWithExpectedSize(dependenciesCount);
        for (ClassName dependencyName : dependencyNames) {
            if (isJavaApiDep(dependencyName)) {
                continue;
            }

            final Iterator<File> libClassesIter = libClasses.iterator();
            while (libClassesIter.hasNext()) {

                final File libClass = libClassesIter.next();
                final ClassFile libClassFile = ClassFile.fromFile(libClass);

                if (dependencyMatcherStrategy.matches(dependencyName, libClassFile)) {
                    result.add(libClassFile);
                    libClassesIter.remove();
                }
            }
        }
        LOG.debug("Need to include " + result.size() + " lib classes for these " + dependenciesCount + " dependencies");
        return result;
    }

    private void removeJavaApiDeps(Set<ClassFile> deps) {
        for (Iterator<ClassFile> iterator = deps.iterator(); iterator.hasNext();) {

            final ClassFile dep = iterator.next();

            if (isJavaApiDep(dep)) {
                iterator.remove();
            }
        }
    }

    private boolean isJavaApiDep(ClassFile dep) {
        return dep.qualifiedName().toString().startsWith(JAVA_ROOT_PACKAGE);
    }

    private boolean isJavaApiDep(ClassName dep) {
        return dep.toString().startsWith(JAVA_ROOT_PACKAGE);
    }

    private void explodeJars(Set<File> libraryLocations) throws IOException {
        final Set<JarFile> libJars = Sets.newHashSet();

        for (File jarFile : libraryLocations) {
            try {
                libJars.add(new JarFile(jarFile));
            } catch (IOException e) {
                LOG.error("Error processing dependency " + jarFile.getAbsolutePath(), e);
            }
        }

        libJarExploder.explode(libJars);
    }

    @Required
    public void setFileFinder(Files fileFinder) {
        this.fileFinder = fileFinder;
    }

    @Required
    public void setDependencyMatcherStrategy(DependencyMatcherStrategy dependencyMatcherStrategy) {
        this.dependencyMatcherStrategy = dependencyMatcherStrategy;
    }

    @Required
    public void setClassDependencyResolver(DependencyResolver<ClassFile> classDependencyResolver) {
        this.classDependencyResolver = classDependencyResolver;
    }

    @Required
    public void setSourceDependencyResolver(DependencyResolver<SourceFile> sourceDependencyResolver) {
        this.sourceDependencyResolver = sourceDependencyResolver;
    }

    @Required
    public void setLibJarExploder(JarExploder libJarExploder) {
        this.libJarExploder = libJarExploder;
    }
}
