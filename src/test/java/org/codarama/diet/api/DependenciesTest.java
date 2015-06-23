package org.codarama.diet.api;

import java.util.Set;

import org.junit.Assert;
import org.codarama.diet.model.ClassFile;
import org.codarama.diet.model.ClassName;
import org.codarama.diet.model.SourceFile;

import org.junit.Test;

public class DependenciesTest {

	@Test
	public void fromClass() {
		final Set<ClassName> dependencies = Dependencies
				.ofClass(ClassFile.fromClasspath("test-classes/primefaces-3.5.jar/org/primefaces/model/TreeTableModel.class"))
				.set();

		Assert.assertTrue(dependencies != null);
		Assert.assertTrue(!dependencies.isEmpty());
		Assert.assertEquals(1, dependencies.size());
		Assert.assertEquals(new ClassName("org.primefaces.model.TreeNode"), dependencies.iterator().next());
	}
	
	@Test
	public void fromSource() {
		final Set<ClassName> dependencies = Dependencies
				.ofSource(SourceFile.fromClasspath("test-classes/ValidCoffee.java"))
				.set();
		
		Assert.assertTrue(dependencies != null);
		Assert.assertTrue(!dependencies.isEmpty());
		Assert.assertTrue(dependencies.size() == 4);
	}
}
