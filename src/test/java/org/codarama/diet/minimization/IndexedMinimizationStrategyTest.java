package org.codarama.diet.minimization;

import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import org.codarama.diet.bundle.JarMaker;
import org.codarama.diet.index.LibraryIndex;
import org.codarama.diet.minimization.impl.IndexedMinimizationStrategy;
import org.codarama.diet.model.ClassName;
import org.codarama.diet.model.ClassStream;
import org.codarama.diet.model.SourceFile;
import org.codarama.diet.util.Files;
import org.codarama.diet.util.Tokenizer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link org.codarama.diet.minimization.impl.IndexedMinimizationStrategy}.
 *
 * Created by siliev on 15-6-24.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:META-INF/test-contexts/testIndexedMinimizationStrategy.xml"})
public class IndexedMinimizationStrategyTest {

    private static final Logger LOG = LoggerFactory.getLogger(IndexedMinimizationStrategyTest.class);

    private String pathToSources;

    private Set<File> sources;
    private Set<SourceFile> sourceFiles;

    private Set<File> libraries;
    private Set<JarFile> jarLibraries;

    @Autowired
    private IndexedMinimizationStrategy indexedStrategy;

    @Autowired
    private LibraryIndex modeShapeIndex;

    @Before
    public void init() throws IOException {
        final String pathToLibraries = toPath(Resources.getResource("test-classes/test-lib-dir"));

        this.pathToSources = toPath(Resources.getResource("test-classes/test-src-dir"));

        this.sources = Files.in(pathToSources).withExtension(SourceFile.EXTENSION).all();
        this.sourceFiles = Sets.newHashSetWithExpectedSize(sources.size());
        this.sourceFiles.addAll(
                sources.stream()
                        .map(SourceFile::fromFile)
                        .collect(Collectors.toList())
        );
        this.libraries = Files.in(pathToLibraries).withExtension(JarMaker.JAR_FILE_EXTENSION).all();
        this.jarLibraries = Sets.newHashSetWithExpectedSize(libraries.size());
        for (File lib : libraries) {
            this.jarLibraries.add(new JarFile(lib));
        }
    }

    @Test
    public void minimize() throws IOException {

        long startTime = System.currentTimeMillis();
        final Set<ClassStream> indexMinimized = indexedStrategy.minimize(sourceFiles, jarLibraries);
        long endTime = System.currentTimeMillis();

        LOG.info("minimization took: " + (endTime - startTime) / 1000 + " seconds");

        assertNotNull(indexMinimized);
        assertTrue(indexMinimized.size() > 0);

        final Set<ClassName> minimizedNames = Sets.newHashSet();
        minimizedNames.addAll(
                indexMinimized
                        .stream()
                        .map(ClassStream::name)
                        .collect(Collectors.toList())
        );

        assertTrue(minimizedNames.contains(new ClassName("com.google.common.collect.Sets$CartesianSet")));
        assertTrue(minimizedNames.contains(new ClassName("com.google.common.math.LongMath$1")));
        assertTrue(minimizedNames.contains(new ClassName("com.google.common.math.LongMath")));
        assertTrue(minimizedNames.contains(new ClassName("com.google.common.collect.ImmutableMultimap$Keys$KeysEntrySet")));
        assertTrue(minimizedNames.contains(new ClassName("com.google.common.collect.MapMakerInternalMap$ValueReference")));
        assertTrue(minimizedNames.contains(new ClassName("com.google.common.collect.RegularImmutableAsList")));

        LOG.info("libraries contained: " + modeShapeIndex.size() + " classes before minimization");
        LOG.info("libraries contain: " + indexMinimized.size() + " classes after minimization");
    }

    private String toPath(URL uri) {
        return Tokenizer.delimiter(":").tokenize(uri.toString()).lastToken();
    }
}
