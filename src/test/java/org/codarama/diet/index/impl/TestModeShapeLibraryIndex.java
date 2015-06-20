package org.codarama.diet.index.impl;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import org.codarama.diet.index.LibraryIndex;
import org.codarama.diet.model.ClassName;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * Tests {@link org.codarama.diet.index.impl.ModeShapeLibraryIndex}.
 *
 * Created by ayld on 20.06.15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:META-INF/test-contexts/testModeShapeLibraryIndexContext.xml"})
public class TestModeShapeLibraryIndex {

    @Autowired
    private LibraryIndex modeShapeIndex;

    @Before
    public void init() throws URISyntaxException, IOException {
        final Set<JarFile> toIndex = new HashSet<>();
        toIndex.add(new JarFile(Resources.getResource("test-classes/lib/aspectjweaver-1.6.12.jar").toURI().getPath()));
        toIndex.add(new JarFile(Resources.getResource("test-classes/lib/commons-lang3-3.1.jar").toURI().getPath()));
        toIndex.add(new JarFile(Resources.getResource("test-classes/lib/primefaces-3.5.jar").toURI().getPath()));

        modeShapeIndex.index(toIndex);
    }

    @Test
    public void testIndex() throws URISyntaxException, IOException {

        final String indexAsString = modeShapeIndex.toString();

        // just some basic checks for now
        Assert.assertFalse(Strings.isNullOrEmpty(indexAsString));
        Assert.assertTrue(indexAsString.length() > 0);
    }

    @Test
    public void testContains() {
        Assert.assertTrue(modeShapeIndex.contains(new ClassName("org.primefaces.model.DefaultScheduleModel")));
        Assert.assertTrue(modeShapeIndex.contains(new ClassName("org.primefaces.model.DynamicChainedPropertyComparator")));
        Assert.assertTrue(modeShapeIndex.contains(new ClassName("org.primefaces.facelets.MethodRule$MethodBindingMetadata")));
        Assert.assertTrue(modeShapeIndex.contains(new ClassName("org.primefaces.push.PushContextImpl$1")));
        Assert.assertTrue(modeShapeIndex.contains(new ClassName("org.aspectj.bridge.MessageUtil$11")));
        Assert.assertTrue(modeShapeIndex.contains(new ClassName("org.aspectj.bridge.MessageUtil$IMessageRenderer")));
        Assert.assertTrue(modeShapeIndex.contains(new ClassName("org.apache.commons.lang3.event.EventListenerSupport$ProxyInvocationHandler")));
    }
}
