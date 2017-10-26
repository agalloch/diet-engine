package org.codarama.diet.api;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import org.codarama.diet.model.ClassFile;
import org.codarama.diet.model.ClassName;
import org.codarama.diet.test.util.suite.IntegrationTest;
import org.codarama.diet.util.Files;
import org.codarama.diet.util.Settings;
import org.codarama.diet.util.Tokenizer;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Tests {@link IndexedMinimizer}
 *
 * Created by Ayld on 7/10/15.
 */
public class IndexedMinimizerTest implements IntegrationTest{
    private static final String MINIMIZATION_RESULT_FILE_NAME = "minimizationTestExpectedResult";

    @Test
    public void minimize() throws IOException {
        final String pathToSources = toPath(Resources.getResource("test-classes/test-src-dir"));
        final String pathToLibraries = toPath(Resources.getResource("test-classes/test-lib-dir"));
        final JarFile resultJar = IndexedMinimizer
                .sources(pathToSources)
                .libs(pathToLibraries)
                .minimize()
                .getJar();
        Assert.assertTrue(resultJar != null);

        final String resultJarName = Tokenizer.delimiter(File.separator).tokenize(resultJar.getName()).lastToken();
        Assert.assertTrue(resultJarName.equals(Settings.DEFAULT_RESULT_JAR_NAME.getValue()));

        final String resultJarNameNoExtension = Tokenizer.delimiter(".").tokenize(resultJarName).firstToken();
        final File dietJar = Files.in(Settings.DEFAULT_OUT_DIR.getValue()).named(resultJarNameNoExtension).single();
        Assert.assertNotNull(dietJar);
        Assert.assertTrue(dietJar.exists());

        final Enumeration<JarEntry> jarEntries = resultJar.entries();
        final Set<String> actualResultEntries = Sets.newHashSet();
        while (jarEntries.hasMoreElements()) {

            final JarEntry entry = jarEntries.nextElement();
            final String resultingEntry = entry.getName();

            // replace all slashes with dots, for OS independence
            final String osAgnosticEntry = resultingEntry.replaceAll("/", ".").replaceAll("\\\\", ".");
            actualResultEntries.add(osAgnosticEntry);
        }

        final List<String> expectedResultEntries = getExpectedMinimizationResult();
        for (String expectedEntry : expectedResultEntries) {
            final boolean isNonClassEntry = !expectedEntry.endsWith(ClassFile.EXTENSION);
            if (isNonClassEntry) {
                continue; // skip non-class entries
            }
            Assert.assertTrue("results do not contain: " + expectedEntry, actualResultEntries.contains(expectedEntry));
        }
    }

    private List<String> getExpectedMinimizationResult() throws IOException {
        final File resultsFile = new File(toPath(Resources.getResource(MINIMIZATION_RESULT_FILE_NAME)));
        return com.google.common.io.Files.readLines(resultsFile, Charsets.UTF_8);
    }

    @Test
    public void mandatoryInclude() throws IOException {
        final JarFile mandatoryJar = new JarFile(new File(
                toPath(Resources.getResource("test-classes/test-lib-dir/commons-lang3-3.1.jar"))));

        final JarFile outJar = IndexedMinimizer.sources(toPath(Resources.getResource("test-classes/test-src-dir")))
                .libs(toPath(Resources.getResource("test-classes/test-lib-dir")))
                .forceInclude(new ClassName("org.primefaces.json.JSONArray")).forceInclude(mandatoryJar).minimize()
                .getJar();

        Assert.assertTrue("out jar is null", outJar != null);

        final String outJarName = Tokenizer.delimiter(File.separator).tokenize(outJar.getName()).lastToken();
        Assert.assertTrue(outJarName.equals(Settings.DEFAULT_RESULT_JAR_NAME.getValue()));

        Assert.assertTrue("org.primefaces.json.JSONArray, mandatory include not found",
                outJar.getEntry(Joiner.on(File.separator).join("org", "primefaces", "json", "JSONArray.class")) != null);

        final Enumeration<JarEntry> mandatoryEntries = mandatoryJar.entries();

        while (mandatoryEntries.hasMoreElements()) {
            final JarEntry mandatoryEntry = mandatoryEntries.nextElement();

            // skip non-class entries
            if (mandatoryEntry.getName().contains("META-INF") || mandatoryEntry.getName().contains("templates")) {
                continue;
            }

            final Enumeration<JarEntry> actualEntries = outJar.entries();
            boolean found = false;
            while (actualEntries.hasMoreElements()) {
                final JarEntry actualEntry = actualEntries.nextElement();

                final String mandatoryEntryName = normalize(mandatoryEntry.getName());
                final String actualEntryName = normalize(actualEntry.getName());
                if (mandatoryEntryName.equals(actualEntryName)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                Assert.fail("entry: " + mandatoryEntry.getName() + ", not found");
            }
        }
    }

    // Here be dragons.
    // A.K.A. The-Magic-Method-Of-Many-Slashes
    private String normalize(String jarEntryName) {
        if (jarEntryName.contains("/")) {
            return jarEntryName.replace("/", File.separator);
        }
        if (jarEntryName.contains("\\")) {
            return jarEntryName.replace("\\", File.separator); // because Windows is awesome !
        }
        return jarEntryName;
    }

    private String toPath(URL uri) {
        return Tokenizer.delimiter(":").tokenize(uri.toString()).lastToken();
    }
}
