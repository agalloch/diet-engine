package org.codarama.diet.bundle.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.jar.JarFile;

import junit.framework.Assert;
import org.codarama.diet.bundle.JarExploder;
import org.codarama.diet.model.ExplodedJar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:META-INF/test-contexts/testConcurrentJarExploder.xml"})
public class TestCuncurrentJarExploder {

	@Autowired
	private JarExploder jarExploder;
	
	@Test
	public void explode() throws IOException, URISyntaxException {
		final Set<JarFile> toExtract = ImmutableSet.of(
				new JarFile(Resources.getResource("test-classes/lib/aspectjweaver-1.6.12.jar").toURI().getPath()),
				new JarFile(Resources.getResource("test-classes/lib/commons-lang3-3.1.jar").toURI().getPath()),
				new JarFile(Resources.getResource("test-classes/lib/primefaces-3.5.jar").toURI().getPath())
		);
		
		final Set<ExplodedJar> exploded = jarExploder.explode(toExtract);
		
		Assert.assertTrue(exploded != null);
		Assert.assertTrue(exploded.size() == 3);
	}
}
