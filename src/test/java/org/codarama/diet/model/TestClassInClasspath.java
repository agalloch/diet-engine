package org.codarama.diet.model;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import org.codarama.diet.util.Files;
import org.codarama.diet.util.Tokenizer;
import org.codarama.diet.util.annotation.Immutable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.jar.JarFile;

/**
 * Tests {@link ClassInClasspath}.
 *
 * Created by Ayld on 6/17/15.
 */
public class TestClassInClasspath {

    private JarFile guavaJar;

    @Before
    public void init() throws IOException {
        final String pathToLibraries = toPath(Resources.getResource("test-classes/test-lib-dir"));
        this.guavaJar = new JarFile(
                Files.in(pathToLibraries).nonRecursive().named("guava-14.0.1").single()
        );
    }

    @Test
    public void creation() throws IOException {

        final JarFile expectedJar = guavaJar;
        final ClassName expectedName = new ClassName("com.test.Class");

        final ClassInClasspath toTest = ClassInClasspath.Builder.newInstance()
                .classpath(
                        ImmutableSet.of(
                                expectedJar
                        )
                )
                .clazz(expectedName)
                .build();

        Assert.assertNotNull(toTest);
        Assert.assertEquals(ImmutableSet.of(expectedJar), toTest.classpath());
        Assert.assertEquals(expectedName, toTest.clazz());
    }

    @Test
    public void enforcesProperCreationState() {
        try {
            ClassInClasspath.Builder.newInstance().build();
        } catch (IllegalStateException e) {
            return; // ok
        }
        Assert.fail("ClassInClasspath builder must enforce class and classpath to be set pre-creation");
    }

    @Test
    public void enforcesClazzSetAtCreation() {
        try {
            ClassInClasspath.Builder.newInstance().clazz(new ClassName("com.test.Class")).build();
        } catch (IllegalStateException e) {
            return; // ok
        }
        Assert.fail("ClassInClasspath builder must enforce classpath to be set pre-creation");
    }

    @Test
    public void enforcesClasspathSetAtCreation() throws IOException {
        try {
            ClassInClasspath.Builder.newInstance().classpath(ImmutableSet.of(guavaJar)).build();
        } catch (IllegalStateException e) {
            return; // ok
        }
        Assert.fail("ClassInClasspath builder must enforce class to be set pre-creation");
    }

    private String toPath(URL uri) {
        return Tokenizer.delimiter(":").tokenize(uri.toString()).lastToken();
    }
}
