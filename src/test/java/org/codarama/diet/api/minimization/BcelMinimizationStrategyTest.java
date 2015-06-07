package org.codarama.diet.api.minimization;

import com.google.common.collect.ImmutableSet;
import org.codarama.diet.bundle.JarExploder;
import org.codarama.diet.dependency.matcher.DependencyMatcherStrategy;
import org.codarama.diet.dependency.matcher.impl.UnanimousBasedDependencyMatcherStrategy;
import org.codarama.diet.dependency.resolver.DependencyResolver;
import org.codarama.diet.dependency.resolver.impl.ManualBinaryParseClassDependencyResolver;
import org.codarama.diet.dependency.resolver.impl.ManualParseSourceDependencyResolver;
import org.codarama.diet.model.ClassFile;
import org.codarama.diet.model.ExplodedJar;
import org.codarama.diet.model.SourceFile;
import org.codarama.diet.util.Files;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.jar.JarFile;

import org.codarama.diet.model.ClassName;

import java.io.File;

/**
 * Tests {@link org.codarama.diet.api.minimization.BcelMinimizationStrategy}
 *
 * Created by ayld on 6/6/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:META-INF/test-contexts/testBcelMinimizationStrategy.xml"})
public class BcelMinimizationStrategyTest {

    private static final Set<ClassName> MOCK_RESOLVER_RESULT = ImmutableSet.of(
            new ClassName("org.codarama.Test"),
            new ClassName("org.codarama.Pest"),
            new ClassName("org.codarama.AndSomeRest"),
            new ClassName("org.codarama.iHope")
    );

    @Autowired
    private String workDir;

    private BcelMinimizationStrategy bcelMinimizationStrategy;

    @Before
    public void init() throws IOException {
        prepareTestWorkDir();

        this.bcelMinimizationStrategy = new BcelMinimizationStrategy();
        this.bcelMinimizationStrategy.setClassDependencyResolver(newMockBcelResolver());
        this.bcelMinimizationStrategy.setSourceDependencyResolver(newMockSourceResolver());
        this.bcelMinimizationStrategy.setLibJarExploder(newMockExploder());
        this.bcelMinimizationStrategy.setDependencyMatcherStrategy(newAlwaysTrueMatcher());
        this.bcelMinimizationStrategy.setFileFinder(newMockFileFinder());
    }

    @Test
    public void returnsSingleFile() throws IOException {
        final Set<ClassFile> minimizeResult = this.bcelMinimizationStrategy.minimize(Collections.emptySet(), Collections.emptySet());

        Assert.assertNotNull(minimizeResult);
        Assert.assertTrue(minimizeResult.size() == 1);
    }

    private DependencyMatcherStrategy newAlwaysTrueMatcher() {
        final DependencyMatcherStrategy matcher = mock(UnanimousBasedDependencyMatcherStrategy.class);
        when(matcher.matches(any(ClassName.class), any(ClassFile.class))).thenReturn(true);
        return matcher;
    }

    private DependencyResolver<ClassFile> newMockBcelResolver() throws IOException {
        final DependencyResolver<ClassFile> result = mock(ManualBinaryParseClassDependencyResolver.class);
        when(result.resolve(any(Set.class))).thenReturn(MOCK_RESOLVER_RESULT);
        when(result.resolve(any(ClassFile.class))).thenReturn(MOCK_RESOLVER_RESULT);

        return result;
    }

    private JarExploder newMockExploder() throws IOException {
        final ExplodedJar mockExplodedJar = mock(ExplodedJar.class);
        when(mockExplodedJar.getArchive()).thenReturn(mock(JarFile.class));
        when(mockExplodedJar.getExtractedPath()).thenReturn(workDir);

        final JarExploder result = mock(JarExploder.class);
        when(result.explode(any(Set.class))).thenReturn(ImmutableSet.of(mockExplodedJar));
        when(result.explode(any(JarFile.class))).thenReturn(mockExplodedJar);

        return result;
    }

    private DependencyResolver<SourceFile> newMockSourceResolver() throws IOException {
        final DependencyResolver<SourceFile> result = mock(ManualParseSourceDependencyResolver.class);
        when(result.resolve(any(Set.class))).thenReturn(MOCK_RESOLVER_RESULT);
        when(result.resolve(any(SourceFile.class))).thenReturn(MOCK_RESOLVER_RESULT);

        return result;
    }

    private void prepareTestWorkDir() throws IOException {
        this.workDir += "/bcelMinimizationStrategyTest";

        final File testWorkDir = new File(workDir);
        delete(testWorkDir);

        final boolean creationSuccess = testWorkDir.mkdirs();
        if (!creationSuccess) {
            Assert.fail("failed to create test work dir: " + testWorkDir.getAbsolutePath());
        }
    }

    private Files newMockFileFinder() throws IOException {
        final Files result = mock(Files.class);
        when(result.withExtension(any(String.class))).thenReturn(result);

        final File notReallyMockClass = ClassFile.fromClasspath("test-classes/primefaces-3.5.jar/org/primefaces/model/TreeTableModel.class").physicalFile();
        when(result.list()).thenReturn(ImmutableSet.of(
                notReallyMockClass
        ));

        return result;
    }

    private static void delete(File file) throws IOException {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            for (File sub : file.listFiles()) {
                delete(sub);
            }
        }
        if (!file.delete()) {
            throw new IOException("failed to deleteRecursive: " + file);
        }
    }
}
