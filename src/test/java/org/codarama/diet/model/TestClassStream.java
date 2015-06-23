package org.codarama.diet.model;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Set;

/**
 * Tests {@link org.codarama.diet.model.ClassStream}.
 *
 * Created by ayld on 6/21/2015.
 */
public class TestClassStream {

    @Test
    public void creation() throws FileNotFoundException {
        final ClassFile classFile = ClassFile.fromClasspath("test-classes/guava-14.0.1/com/google/common/io/AppendableWriter.class");
        Set<ClassName> dependencies = classFile.dependencies();

        Assert.assertNotNull(dependencies);
        Assert.assertTrue(dependencies.size() > 0);

        final BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(classFile.physicalFile()));

        final ClassStream testClassStream = ClassStream.fromStream(fileInputStream);

        Assert.assertNotNull(testClassStream);

        dependencies = testClassStream.dependencies();

        Assert.assertNotNull(dependencies);
        Assert.assertTrue(dependencies.size() > 0);
    }

}
