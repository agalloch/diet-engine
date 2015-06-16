package org.codarama.diet.resolver.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import org.codarama.diet.dependency.resolver.DependencyResolver;
import org.codarama.diet.dependency.resolver.impl.JdepsClassNameDependencyResolver;
import org.codarama.diet.model.ClassFile;
import org.codarama.diet.model.ClassInClasspath;
import org.codarama.diet.model.ClassName;
import org.codarama.diet.util.Files;
import org.codarama.diet.util.Tokenizer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * Tests whether results from {@link JdepsClassNameDependencyResolver}
 * and {@link org.codarama.diet.dependency.resolver.impl.ManualBinaryParseClassDependencyResolver} match.
 *
 * Created by ayld on 6/2/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:META-INF/test-contexts/testJdepsClassNameDependencyResolver.xml"})
public class TestJdepsClassNameDependencyResolver {

    private static final String JAVA_CORE_PACKAGE = "java";

    @Autowired
    private DependencyResolver<ClassFile> classDependencyResolver;

    @Autowired
    private JdepsClassNameDependencyResolver jdepsDependencyResolver;

    private ClassName classNameToResolve;
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

        this.classNameToResolve =  new ClassName("com.google.common.collect.Sets");
    }

    @Test
    public void jdepsResolverVsBinaryClassResolver() throws IOException {
        Set<ClassName> classResolverResult = classDependencyResolver.resolve(
                ClassFile.fromFilepath(
                        toPath(Resources.getResource("test-classes/guava-14.0.1/com/google/common/collect/Sets.class"))
                )
        );
        Assert.assertNotNull(classResolverResult);
        Assert.assertNotNull(classResolverResult.size() > 0);

        classResolverResult = removeJavaApiDeps(classResolverResult);
        Assert.assertNotNull(classResolverResult);
        Assert.assertNotNull(classResolverResult.size() > 0);

        final ClassInClasspath toResolve = ClassInClasspath.Builder.newInstance()
                .clazz(classNameToResolve)
                .classpath(
                        ImmutableSet.of(
                                guavaJar, jar2Jar
                        )
                ).build();

        final Set<ClassName> jdepsResolverResult = jdepsDependencyResolver.resolve(toResolve);
        Assert.assertNotNull(jdepsResolverResult);
        Assert.assertNotNull(jdepsResolverResult.size() > 0);

        final Ordering<Object> dependencyOrdering = Ordering.usingToString();
        final List<ClassName> sortedClassResolverResult = dependencyOrdering.sortedCopy(classResolverResult);
        final List<ClassName> sortedJdepsResolverResult = dependencyOrdering.sortedCopy(jdepsResolverResult);

        Assert.assertEquals(sortedClassResolverResult, sortedJdepsResolverResult);
    }

    private Set<ClassName> removeJavaApiDeps(Set<ClassName> from) {
        final Set<ClassName> result = Sets.newHashSet();
        for (ClassName dep : from) {
            // skip java lang dependencies
            if (dep.toString().startsWith(JAVA_CORE_PACKAGE)) {
                continue;
            }
            result.add(dep);
        }
        return result;
    }

    private String toPath(URL uri) {
        return Tokenizer.delimiter(":").tokenize(uri.toString()).lastToken();
    }
}
