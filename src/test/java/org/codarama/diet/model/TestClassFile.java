package org.codarama.diet.model;

import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestClassFile {

    private static final Logger LOG = LoggerFactory.getLogger(TestClassFile.class);

	@Test
	public void valid() {
		ClassFile.fromClasspath("test-classes/ClassName.class");
	}
	
	@Test
	public void dependencies() {
		final Set<ClassName> dependencies = ClassFile.fromClasspath("test-classes/ClassName.class").dependencies();
		
		Assert.assertTrue(dependencies != null);

        final int expectedCount = 9;
        boolean isDepCountExpected = dependencies.size() == expectedCount;

        if (!isDepCountExpected) {
            LOG.info("Dumping out dependencies:");

            for (ClassName dep : dependencies) {
                LOG.info(dep.toString());
            }
        }
        Assert.assertTrue("expected " + expectedCount + "  but found " + dependencies.size() + " dependencies", isDepCountExpected);
		
		// I should not be able to change the state
		try {
			dependencies.add(new ClassName("a.name.that.should.Fail"));
		} catch (UnsupportedOperationException e) {
			// yay !
			return;
		}
		Assert.fail(); // awww
	}
}
