package org.codarama.diet.event.aop;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.codarama.diet.bundle.JarMaker;
import org.codarama.diet.dependency.resolver.DependencyResolver;
import org.codarama.diet.event.model.ComponentEvent;
import org.codarama.diet.index.LibraryIndex;
import org.codarama.diet.minimization.MinimizationStrategy;
import org.codarama.diet.model.ClassFile;
import org.codarama.diet.model.ClassName;
import org.codarama.diet.model.ClassStream;
import org.codarama.diet.model.SourceFile;
import org.codarama.diet.util.Files;
import org.codarama.diet.util.Tokenizer;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.io.Resources;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:META-INF/test-contexts/testProfilingAdvice.xml"})
public class TestProfilingAdvice {

    @Autowired
    private MinimizationStrategy<SourceFile, JarFile, ClassStream> indexedMinimizationStrategy;

    @Autowired
    private LibraryIndex modeShapeIndex;

    @Autowired
    private EventBus statusUpdateEventBus;

    @Autowired
    private DependencyResolver<SourceFile> sourceDependencyResolver;

    @Autowired
    private DependencyResolver<ClassStream> classStreamResolver;

    private static final Set<JarFile> TO_INDEX = new HashSet<>();

    @BeforeClass
    public static void init() throws URISyntaxException, IOException {
        TO_INDEX.add(new JarFile(Resources.getResource("test-classes/lib/aspectjweaver-1.6.12.jar").toURI().getPath()));
        TO_INDEX.add(new JarFile(Resources.getResource("test-classes/lib/commons-lang3-3.1.jar").toURI().getPath()));
    }

    @Test
    public void sourceResolvePostsUpdates() throws URISyntaxException, IOException {
        final File source = new File(Resources.getResource("test-classes/ValidCoffee.java").toURI());

        final Set<String> statusUpdateMsgs = subscribeForUpdates();
        sourceDependencyResolver.resolve(SourceFile.fromFile(source));

        assertTrue("No status updates received", statusUpdateMsgs.size() > 0);
    }

    @Test
    public void classResolvePostsUpdates() throws IOException {
        final ClassFile classFile = ClassFile.fromClasspath("test-classes/primefaces-3.5.jar/org/primefaces/model/TreeTableModel.class");
        final ClassStream stream = classFile.stream();

        final Set<String> statusUpdateMsgs = subscribeForUpdates();
        classStreamResolver.resolve(stream);

        assertTrue("No status updates received", statusUpdateMsgs.size() > 0);
    }

    @Test
    public void indexGetPostsStatusUpdates() {

        modeShapeIndex.index(TO_INDEX);
        final Set<String> statusUpdateMsgs = subscribeForUpdates();

        modeShapeIndex.get(new ClassName("org.aspectj.apache.bcel.Constants"));

        assertTrue("No status updates received", statusUpdateMsgs.size() > 0);
    }

    @Test
    public void indexIndexPostsStatusUpdates() {

        final Set<String> statusUpdateMsgs = subscribeForUpdates();
        modeShapeIndex.index(TO_INDEX);

        assertTrue("No status updates received", statusUpdateMsgs.size() > 0);
    }

    @Test
    public void minimizePostsStatusUpdates() throws IOException {
        final String pathToLibraries = toPath(Resources.getResource("test-classes/test-lib-dir"));
        final String pathToSources = toPath(Resources.getResource("test-classes/test-src-dir"));

        final Set<File> sources = Files.in(pathToSources).withExtension(SourceFile.EXTENSION).all();
        final Set<SourceFile> sourceFiles = Sets.newHashSetWithExpectedSize(sources.size());
        sourceFiles.addAll(
                sources.stream()
                        .map(SourceFile::fromFile)
                        .collect(Collectors.toList())
        );
        final Set<File> libraries = Files.in(pathToLibraries).withExtension(JarMaker.JAR_FILE_EXTENSION).all();
        final Set<JarFile> jarLibraries = Sets.newHashSetWithExpectedSize(libraries.size());
        for (File lib : libraries) {
            jarLibraries.add(new JarFile(lib));
        }

        final Set<String> statusUpdateMsgs = subscribeForUpdates();
        indexedMinimizationStrategy.minimize(sourceFiles, jarLibraries);

        assertTrue("No status updates received", statusUpdateMsgs.size() > 0);
    }

    private String toPath(URL uri) {
        return Tokenizer.delimiter(":").tokenize(uri.toString()).lastToken();
    }


    private Set<String> subscribeForUpdates() {
        final Set<String> statusUpdateMsgs = new HashSet<>();
        final Object listener = new Object() {
            @Subscribe
            public void on(ComponentEvent e) {
                statusUpdateMsgs.add(e.getMessage());
            }
        };
        statusUpdateEventBus.register(listener);
        return statusUpdateMsgs;
    }
}
