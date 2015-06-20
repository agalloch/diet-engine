package org.codarama.diet.index;

import org.codarama.diet.model.ClassName;

import java.util.Set;
import java.util.jar.JarFile;

/**
 * A tree index for a library of jar files.
 * Nodes contain folders or class names.
 *
 * An example index for the following classes should look like this:
 *
 * com.google.guava.collections.Sets
 * com.google.guava.collections.Lists
 * com.google.guava.util.Strings
 *
 *                   com
 *                    |
 *                  google
 *                    |
 *                  guava
 *                  /    \
 *         collections   util
 *            /     \      |
 *          Sets   Lists   Strings
 *
 * Created by ayld on 20.06.15.
 */
public interface LibraryIndex {

    /**
     * Adds a set of jar files to the current index.
     *
     * @param libs the jars to add
     * @return the modified index
     * */
    LibraryIndex index(Set<JarFile> libs);

    /**
     * Adds a jar file to the current index.
     *
     * @param lib the jar to add
     * @return the modified index
     * */
    LibraryIndex index(JarFile lib);

    /**
     * Checks if a class name is contained in the current index.
     *
     * @param className the class name to check for
     * @return true if found, false if not
     * */
    boolean contains(ClassName className);
}
