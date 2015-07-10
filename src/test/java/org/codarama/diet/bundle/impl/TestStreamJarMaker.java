package org.codarama.diet.bundle.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import org.codarama.diet.bundle.JarMaker;
import org.codarama.diet.model.ClassStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.Assert.assertTrue;

/**
 * Tests {@link StreamJarMaker}.
 *
 * Created by Ayld on 6/28/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:META-INF/test-contexts/testStreamJarMakerContext.xml"})
public class TestStreamJarMaker {

    private static final Set<String> CORRECT_ZIPPED_ENTRY_NAMES = ImmutableSet.of(
            "net" + File.separator,
            Joiner.on(File.separator).join("org", "codarama") + File.separator,
            Joiner.on(File.separator).join("org", "codarama", "diet") + File.separator,
            Joiner.on(File.separator).join("org", "codarama", "diet", "model") + File.separator,
            Joiner.on(File.separator).join("org", "codarama", "diet", "model", "ClassName.class"),
            "org" + File.separator,
            Joiner.on(File.separator).join("org", "primefaces") + File.separator,
            Joiner.on(File.separator).join("org", "primefaces", "context") + File.separator,
            Joiner.on(File.separator).join("org", "primefaces", "context", "PrimePartialViewContext.class"),
            Joiner.on(File.separator).join("org", "apache") + File.separator,
            Joiner.on(File.separator).join("org", "apache", "commons") + File.separator,
            Joiner.on(File.separator).join("org", "apache", "commons", "lang3") + File.separator,
            Joiner.on(File.separator).join("org", "apache", "commons", "lang3", "CharRange$1.class")
    );

    @Autowired
    private String workDir;

    @Autowired
    private JarMaker<ClassStream> jarMaker;

    @Before
    public void prepare() throws IOException {
        cleanDir(new File(workDir));
    }

    @Test
    public void makeJar() throws URISyntaxException, IOException {
        final Set<ClassStream> filesToZip = ImmutableSet.of(
                ClassStream.fromStream(
                        new FileInputStream(new File(Resources.getResource("test-classes/ClassName.class").toURI()))
                ),
                ClassStream.fromStream(
                        new FileInputStream(new File(Resources.getResource("test-classes/PrimePartialViewContext.class").toURI()))
                ),
                ClassStream.fromStream(
                        new FileInputStream(new File(Resources.getResource("test-classes/CharRange$1.class").toURI()))
                )
        );

        final JarFile jar = jarMaker.zip(filesToZip);

        assertTrue("zipped jar is null", jar != null);

        final Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {

            final JarEntry entry = entries.nextElement();

            assertTrue("invalid entry: " + entry, CORRECT_ZIPPED_ENTRY_NAMES.contains(entry.getName()));
        }

        final File zipDir = new File(workDir);
        final Set<File> zipDirFiles = Sets.newHashSet(zipDir.listFiles());

        assertTrue(jar.getName() + ", missing", zipDirFiles.contains(new File(jar.getName())));
    }

    private static void cleanDir(File file) throws IOException {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            final File[] files = file.listFiles();
            if (files == null) {
                return;
            }
            for (File sub : files) {
                cleanDir(sub);
            }
        }
        if (!file.delete()) {
            throw new IOException("failed to deleteRecursive: " + file);
        }
    }
}
