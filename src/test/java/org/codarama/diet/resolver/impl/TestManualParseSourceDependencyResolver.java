package org.codarama.diet.resolver.impl;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.codarama.diet.dependency.resolver.DependencyResolver;
import org.codarama.diet.model.ClassName;
import org.codarama.diet.model.SourceFile;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:META-INF/test-contexts/testManualParseSourceDependencyResolverContext.xml" })
public class TestManualParseSourceDependencyResolver {

	private static final String JAVA_IMPORT_KEYWOD = "import";

	@Autowired
	private DependencyResolver<SourceFile> sourceDependencyResolver;

	@Test
	public void testResolve() throws IOException, URISyntaxException {
		final URL validSourceUrl = Resources.getResource("test-classes/ValidCoffee.java");

		// get dependencies in a way different than the resolver
		final String content = Resources.toString(validSourceUrl, Charsets.UTF_8);
		final String[] lines = content.split("\n");

		final Set<String> dependencies = new HashSet<>();
		for (String line : lines) {
			if (line.startsWith(JAVA_IMPORT_KEYWOD)) {

				final String dependency = line.split(" ")[1].replaceAll(";", "").replaceAll("\r", ""); // not very
																										// pretty ...
				dependencies.add(dependency);
			}
		}

		// get dependencies through the resolver
		final Set<ClassName> resolvedDependencies = sourceDependencyResolver.resolve(SourceFile.fromFile(new File(
				validSourceUrl.toURI())));

		// result sets should match
		Assert.assertEquals(dependencies, toStringSet(resolvedDependencies));
	}

	private Set<String> toStringSet(Set<ClassName> toConvert) {
		final Set<String> result = Sets.newHashSet();

		for (ClassName name : toConvert) {
			result.add(name.toString());
		}

		return result;
	}
}
