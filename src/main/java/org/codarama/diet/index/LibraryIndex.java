package org.codarama.diet.index;

import org.codarama.diet.model.ClassName;
import org.codarama.diet.model.ClassStream;

import java.io.InputStream;
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
 * The primary goal of the index is to index classes, whether an implementation decides to
 * keep or discard directory entries is left to the implementer.
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

    /**
     * Returns the class corresponding to the given {@link org.codarama.diet.model.ClassName} as a stream of bytes.
     * If a file with this name is not found this method throws an exception.
     *
     * @throws java.lang.IllegalStateException if no file with given name is indexed yet
     * @param name the name of the file to look for
     * @return the required file as a stream of bites
     * */
    ClassStream get(ClassName name);

    /**
     * Searches for a class file based on given name and returns it as a stream of bytes.
     * If a file with this name is not found this method returns null.
     *
     * @param name the name of the file to look for
     * @return the required file as a stream of bites
     * */
    ClassStream find(ClassName name);

    /**
     * Returns the number of currently indexed classes.
     * Implementers should omit directory entries count, even if they index them.
     *
     * @return the number of currently indexed classes.
     * */
    long size();
}
