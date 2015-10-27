package org.codarama.diet.minimization;

import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import org.codarama.diet.bundle.JarMaker;
import org.codarama.diet.model.ClassFile;
import org.codarama.diet.model.ClassStream;
import org.codarama.diet.model.SourceFile;
import org.codarama.diet.test.util.suite.IntegrationTest;
import org.codarama.diet.util.Files;
import org.codarama.diet.util.Tokenizer;
import org.junit.Assert;
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

/**
 * Tests
 * {@link org.codarama.diet.minimization.impl.IndexedMinimizationStrategy}
 * and
 * {@link org.codarama.diet.minimization.impl.BcelMinimizationStrategy}.
 *
 * Also, BATTLE OF THA RESOLVERS!
 * PREPARE MORTAL! INCOMING MICRO BENCHMARKS!
 *
 * Created by ayld on 6/21/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:META-INF/test-contexts/testIndexedVsManualMinimizationStrategy.xml"})
public class IndexedVsManualMinimizationStrategyTest implements IntegrationTest{

    private static final Logger LOG = LoggerFactory.getLogger(IndexedVsManualMinimizationStrategyTest.class);

    @Autowired
    private MinimizationStrategy<SourceFile, JarFile, ClassStream> indexedStrategy;

    @Autowired
    private MinimizationStrategy<SourceFile, File, ClassFile> bcelMinimizationStrategy;

    private Set<SourceFile> sourceFiles;

    private Set<File> libraries;
    private Set<JarFile> jarLibraries;

    @Before
    public void init() throws IOException {
        final String pathToLibraries = toPath(Resources.getResource("test-classes/test-lib-dir"));
        final String pathToSources = toPath(Resources.getResource("test-classes/test-src-dir"));

        final Set<File> sources = Files.in(pathToSources).withExtension(SourceFile.EXTENSION).all();

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

    // LET'S GET READY TO RUMBLE
    @Test
    public void battle() throws IOException {
        // Ready !
        long startTime = System.currentTimeMillis(); // Set !
        final Set<ClassStream> indexMinimized = indexedStrategy.minimize(sourceFiles, jarLibraries); // Go !
        long endTime = System.currentTimeMillis(); // We have a contender !!!

        long runtime = endTime - startTime;
        LOG.info("indexed strategy finished in: " + runtime / 1000 + " seconds");

        Assert.assertNotNull(indexMinimized);
        Assert.assertTrue(indexMinimized.size() > 0);

        // Ready !
        startTime = System.currentTimeMillis(); // Set !
        final Set<ClassFile> bcelMinimized = bcelMinimizationStrategy.minimize(sourceFiles, libraries); // Go ! (again)
        endTime = System.currentTimeMillis(); // We have another contender !!!

        Assert.assertNotNull(bcelMinimized);
        Assert.assertTrue(bcelMinimized.size() > 0);

        runtime = endTime - startTime;
        LOG.info("manual strategy finished in: " + runtime / 1000 + " seconds");
    }

    private String toPath(URL uri) {
        return Tokenizer.delimiter(":").tokenize(uri.toString()).lastToken();
    }
}
