package org.codarama.diet.bundle.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import org.codarama.diet.bundle.JarMaker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:META-INF/test-contexts/testManualJarMakerContext.xml"})
public class TestManualJarMaker {
	
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
			Joiner.on(File.separator).join("org", "apache")  + File.separator,
			Joiner.on(File.separator).join("org", "apache", "commons")  + File.separator,
			Joiner.on(File.separator).join("org", "apache", "commons", "lang3")  + File.separator,
			Joiner.on(File.separator).join("org", "apache", "commons", "lang3", "CharRange$1.class")
	);
	
	@Autowired
	private String workDir;
	
	@Autowired
	private JarMaker<File> jarMaker;
	
	@Test
	public void zip() throws URISyntaxException, IOException {
		final Set<File> toZip = ImmutableSet.of(
				new File(Resources.getResource("test-classes/ClassName.class").toURI()),
				new File(Resources.getResource("test-classes/PrimePartialViewContext.class").toURI()),
				new File(Resources.getResource("test-classes/CharRange$1.class").toURI())
		);
		
		final JarFile jar = jarMaker.zip(toZip);
		
		assertTrue("zipped jar is null", jar != null);
		
		final Enumeration<JarEntry> entries = jar.entries();
		while (entries.hasMoreElements()) {
			
			final JarEntry entry = entries.nextElement();
			
			assertTrue("invalid entry: " + entry, CORRECT_ZIPPED_ENTRY_NAMES.contains(entry.getName()));
		}
		
		final File zipDir = new File(workDir);
		final Set<File> zipDirFiles = Sets.newHashSet(zipDir.listFiles());
		assertTrue(!zipDirFiles.isEmpty());

		assertTrue(jar.getName() + ", missing", zipDirFiles.contains(new File(jar.getName())));
	}
}
