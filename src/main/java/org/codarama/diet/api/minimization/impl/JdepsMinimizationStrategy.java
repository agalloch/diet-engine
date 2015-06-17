/* ***************************************************************************
 * Copyright 2014 VMware, Inc. All rights reserved. -- VMware Confidential
 * **************************************************************************
 */

package org.codarama.diet.api.minimization.impl;

import com.google.common.collect.Sets;
import org.codarama.diet.api.minimization.MinimizationStrategy;
import org.codarama.diet.dependency.matcher.DependencyMatcherStrategy;
import org.codarama.diet.dependency.resolver.DependencyResolver;
import org.codarama.diet.model.ClassFile;
import org.codarama.diet.model.ClassInClasspath;
import org.codarama.diet.model.ClassName;
import org.codarama.diet.model.SourceFile;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * A {@link org.codarama.diet.api.minimization.MinimizationStrategy} utilizing the
 * {@link org.codarama.diet.dependency.resolver.impl.JdepsClassNameDependencyResolver}.
 *
 * @see {@link org.codarama.diet.dependency.resolver.impl.JdepsClassNameDependencyResolver}
 *
 * Created by siliev on 15-6-17.
 */
public class JdepsMinimizationStrategy implements MinimizationStrategy<SourceFile, JarFile>{

   private DependencyMatcherStrategy dependencyMatcherStrategy;
   private DependencyResolver<SourceFile> sourceDependencyResolver;
   private DependencyResolver<ClassInClasspath> jdepsDependencyResolver;

   @Override
   public Set<ClassFile> minimize(Set<SourceFile> sources, Set<JarFile> libraries) throws IOException {
      final Set<ClassName> sourceDependencies = sourceDependencyResolver.resolve(sources);

      final Set<ClassName> allResolvedDependencies = Sets.newHashSet();
      for (ClassName currentSourceDependency : sourceDependencies) {

         final Set<ClassName> initialDep = Sets.newHashSetWithExpectedSize(1);
         initialDep.add(currentSourceDependency);

         final Set<ClassName> resolvedCurrentSourceDependencies = getDependenciesOfDependencies(initialDep, libraries);
         allResolvedDependencies.addAll(resolvedCurrentSourceDependencies);
      }

      // TODO make a Set<ClassFile> of allResolvedDependencies

      return null;
   }

   private Set<ClassName> getDependenciesOfDependencies(Set<ClassName> deps, Set<JarFile> libraries) throws IOException {
      for (ClassName dep : deps) {

         final ClassInClasspath depAndLibs = ClassInClasspath.Builder
                 .newInstance()
                 .clazz(dep)
                 .classpath(libraries)
                 .build();

         final Set<ClassName> newlyResolved = jdepsDependencyResolver.resolve(depAndLibs);

         final boolean depsChanged = deps.addAll(newlyResolved);

         if (!depsChanged) {
            return deps;
         }

         getDependenciesOfDependencies(deps, libraries);
      }
      return deps;
   }

   @Required
   public void setSourceDependencyResolver(DependencyResolver<SourceFile> sourceDependencyResolver) {
      this.sourceDependencyResolver = sourceDependencyResolver;
   }

   @Required
   public void setDependencyMatcherStrategy(DependencyMatcherStrategy dependencyMatcherStrategy) {
      this.dependencyMatcherStrategy = dependencyMatcherStrategy;
   }

   @Required
   public void setJdepsDependencyResolver(DependencyResolver<ClassInClasspath> jdepsDependencyResolver) {
      this.jdepsDependencyResolver = jdepsDependencyResolver;
   }
}
