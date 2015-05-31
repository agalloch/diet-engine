package org.codarama.diet.util.system;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import org.codarama.diet.model.ClassFile;
import org.codarama.diet.model.ClassName;
import org.codarama.diet.test.util.suite.IntegrationTest;
import org.codarama.diet.util.Files;
import org.codarama.diet.util.Tokenizer;
import org.codarama.diet.util.annotation.Immutable;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * Tests the {@link org.codarama.diet.util.system.Jdeps} invoker util.
 *
 * Created by ayld on 5/29/2015.
 */
@Category(IntegrationTest.class)
public class TestJdeps implements IntegrationTest{

    @Test
    public void findDependencies() throws IOException {
        final String pathToLibraries = toPath(Resources.getResource("test-classes/test-lib-dir"));
        final JarFile guavaJar = new JarFile(
                Files.in(pathToLibraries).nonRecursive().named("guava-14.0.1").single()
        );

        final Set<JarFile> jars = ImmutableSet.of(guavaJar);
        final Set<ClassName> classes = ImmutableSet.of(new ClassName("com.google.common.collect.Sets"));

        final Jdeps jdeps = Jdeps.Builder.searchInJars(jars).forDependenciesOf(classes).build();

        final Set<ClassName> foundDependencies = jdeps.findDependencies();

        Assert.assertNotNull(foundDependencies);
        Assert.assertTrue(foundDependencies.size() == 27);
    }

    private String toPath(URL uri) {
        return Tokenizer.delimiter(":").tokenize(uri.toString()).lastToken();
    }
}
