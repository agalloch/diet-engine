package org.codarama.diet.resolver.impl;

import java.io.IOException;
import java.util.Set;

import org.junit.Assert;
import org.codarama.diet.dependency.resolver.DependencyResolver;
import org.codarama.diet.model.ClassFile;
import org.codarama.diet.model.ClassName;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:META-INF/test-contexts/testConcurrentClassDependencyResolver.xml"})
public class TestConcurrentClassDependencyResolver {

	@Autowired
	private DependencyResolver<ClassFile> classDependencyResolver;
	
	@Test
	public void concurrentResolve() throws IOException {
		final Set<ClassName> resolved = classDependencyResolver.resolve(ClassFile.fromClasspath("test-classes/primefaces-3.5.jar/org/primefaces/model/TreeTableModel.class"));
		
		Assert.assertTrue(resolved != null);
		Assert.assertTrue(!resolved.isEmpty());
		Assert.assertEquals(1, resolved.size());
		Assert.assertEquals(new ClassName("org.primefaces.model.TreeNode"), resolved.iterator().next());
	}
}
