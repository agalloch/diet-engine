package org.codarama.diet.resolver.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import org.codarama.diet.dependency.resolver.DependencyResolver;
import org.codarama.diet.dependency.resolver.impl.JdepsClassNameDependencyResolver;
import org.codarama.diet.model.ClassFile;
import org.codarama.diet.model.ClassName;
import org.codarama.diet.util.Files;
import org.codarama.diet.util.Tokenizer;
import org.codarama.diet.util.system.Jdeps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * Created by ayld on 6/2/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:META-INF/test-contexts/testJdepsClassNameDependencyResolver.xml"})
public class TestJdepsClassNameDependencyResolver {

    @Autowired
    private DependencyResolver<ClassFile> classDependencyResolver;
    private JdepsClassNameDependencyResolver jdepsResolver;

    private ClassName classNameToResolve;
    private ClassFile classFileToResolve; // TODO
    private JarFile guavaJar;
    private JarFile jar2Jar;

    @Before
    public void init() throws IOException {
        final String pathToLibraries = toPath(Resources.getResource("test-classes/test-lib-dir"));

        this.guavaJar = new JarFile(
                Files.in(pathToLibraries).nonRecursive().named("guava-14.0.1").single()
        );
        this.jar2Jar = new JarFile(
                Files.in(pathToLibraries).nonRecursive().named("jar2").single()
        );

        this. jdepsResolver = JdepsClassNameDependencyResolver.Builder.jars(
                ImmutableSet.of(guavaJar, jar2Jar)
        ).build();

        this.classNameToResolve =  new ClassName("com.google.common.collect.Sets");
    }

    @Test
    public void jdepsVsJdepsResolver() throws IOException {
        final Set<ClassName> resolverResult = jdepsResolver.resolve(classNameToResolve);
        Assert.assertNotNull(resolverResult);

        final Set<ClassName> jdepsResult = Jdeps.Builder
                .searchInJars(ImmutableSet.of(guavaJar, jar2Jar))
                .forDependenciesOf(classNameToResolve)
                .build()
                .findDependencies();
        Assert.assertNotNull(jdepsResult);

        Assert.assertEquals(resolverResult, jdepsResult);
    }

    public void jdepsResolverVsBinaryClassResolver() throws IOException {
        // TODO
    }

    private String toPath(URL uri) {
        return Tokenizer.delimiter(":").tokenize(uri.toString()).lastToken();
    }
}
