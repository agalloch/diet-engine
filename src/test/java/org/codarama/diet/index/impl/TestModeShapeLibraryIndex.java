package org.codarama.diet.index.impl;

import com.google.common.base.Strings;
import com.google.common.io.Resources;
import org.codarama.diet.dependency.resolver.DependencyResolver;
import org.codarama.diet.index.LibraryIndex;
import org.codarama.diet.model.ClassName;
import org.codarama.diet.model.ClassStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;

import static org.junit.Assert.*;

/**
 * Tests {@link org.codarama.diet.index.impl.ModeShapeLibraryIndex}.
 *
 * Created by ayld on 20.06.15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:META-INF/test-contexts/testModeShapeLibraryIndexContext.xml"})
public class TestModeShapeLibraryIndex {

    @Autowired
    private String indexWorkDir;

    @Autowired
    private LibraryIndex modeShapeIndex;

    @Autowired
    private DependencyResolver<ClassStream> classStreamResolver;

    private JarFile primefacesJar;

    @Before
    public void init() throws URISyntaxException, IOException {
        final Set<JarFile> toIndex = new HashSet<>();
        toIndex.add(new JarFile(Resources.getResource("test-classes/lib/aspectjweaver-1.6.12.jar").toURI().getPath()));
        toIndex.add(new JarFile(Resources.getResource("test-classes/lib/commons-lang3-3.1.jar").toURI().getPath()));

        this.primefacesJar = new JarFile(Resources.getResource("test-classes/lib/primefaces-3.5.jar").toURI().getPath());
        toIndex.add(primefacesJar);

        modeShapeIndex.index(toIndex);
    }

    @Test
    public void index() throws URISyntaxException, IOException {

        final String indexAsString = modeShapeIndex.toString();

        // just some basic checks for now
        assertFalse(Strings.isNullOrEmpty(indexAsString));
        Assert.assertFalse("empty".equals(indexAsString));
        assertTrue(indexAsString.length() > 0);
    }

    @Test
    public void contains() {
        assertTrue(modeShapeIndex.contains(new ClassName("org.primefaces.model.DefaultScheduleModel")));
        assertTrue(modeShapeIndex.contains(new ClassName("org.primefaces.model.DynamicChainedPropertyComparator")));
        assertTrue(modeShapeIndex.contains(new ClassName("org.primefaces.facelets.MethodRule$MethodBindingMetadata")));
        assertTrue(modeShapeIndex.contains(new ClassName("org.primefaces.push.PushContextImpl$1")));
        assertTrue(modeShapeIndex.contains(new ClassName("org.aspectj.bridge.MessageUtil$11")));
        assertTrue(modeShapeIndex.contains(new ClassName("org.aspectj.bridge.MessageUtil$IMessageRenderer")));
        assertTrue(modeShapeIndex.contains(new ClassName("org.apache.commons.lang3.event.EventListenerSupport$ProxyInvocationHandler")));
    }

    @Test
    public void singleJarIndex() {
        try {
            modeShapeIndex.index(primefacesJar);
        } catch (UnsupportedOperationException e) {
            return; //yay
        }
        Assert.fail(); // awww
    }

    @Test
    public void find() throws IOException {
        ClassName testName = new ClassName("org.primefaces.model.DefaultScheduleModel");
        ClassStream found = modeShapeIndex.find(testName);

        assertNotNull(found);
        assertEquals(found.name(), testName);
        assertTrue(found.content().available() > 0);


        testName = new ClassName("org.primefaces.facelets.MethodRule$MethodBindingMetadata");
        found = modeShapeIndex.find(testName);

        assertNotNull(found);
        assertEquals(found.name(), testName);
        assertTrue(found.content().available() > 0);

        testName = new ClassName("org.aspectj.bridge.MessageUtil$11");
        found = modeShapeIndex.find(testName);

        assertNotNull(found);
        assertEquals(found.name(), testName);
        assertTrue(found.content().available() > 0);

        Assert.assertNull(modeShapeIndex.find(new ClassName("non.existent.clazz.Name")));
    }

    @Test
    public void get() throws IOException {
        ClassName testName = new ClassName("org.primefaces.model.DefaultScheduleModel");
        ClassStream found = modeShapeIndex.get(testName);

        assertNotNull(found);
        assertEquals(found.name(), testName);
        assertTrue(found.content().available() > 0);


        testName = new ClassName("org.primefaces.facelets.MethodRule$MethodBindingMetadata");
        found = modeShapeIndex.get(testName);

        assertNotNull(found);
        assertEquals(found.name(), testName);
        assertTrue(found.content().available() > 0);

        testName = new ClassName("org.aspectj.bridge.MessageUtil$11");
        found = modeShapeIndex.get(testName);

        assertNotNull(found);
        assertEquals(found.name(), testName);
        assertTrue(found.content().available() > 0);

        try {
            modeShapeIndex.get(new ClassName("non.existent.clazz.Name"));
        } catch (IllegalStateException e) {
            return; //yay
        }
        Assert.fail(); // awww
    }

    @Test
    public void size() {
        final long size = modeShapeIndex.size();

        assertTrue(size > 0);
    }
}
