package org.codarama.diet.test.util;

import org.springframework.beans.factory.FactoryBean;

/**
 * A factory bean creating a string bean containing the path to the OS temp dir.
 * Needed because the tests need a dir that we are sure we have write access to.
 */
public class WorkDirPathLocatorBeanFactory implements FactoryBean<String> {

    /**
     * Now the temp dir is located in a different spot in each OS.
     * The JVM however "knows" where it is, but abstracts the knowledge in the File API.
     * So we "ask" it to create a temp "probe" file for us and return the path to its parent
     * which should be the temp folder we have write access to.
     *
     * @return the path to the user relative OS temp folder
     */
    @Override
    public String getObject() throws Exception {
        return String.valueOf(java.io.File.createTempFile("probe", "tmp").getParent());
    }

    @Override
    public Class<?> getObjectType() {
        return String.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
