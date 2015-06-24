package org.codarama.diet.dependency.resolver;

import java.io.IOException;
import java.util.Set;
import java.util.jar.JarFile;

import org.codarama.diet.model.ClassName;

/** 
 * Contains method for finding classes/sources in bundles.
 * Bundles can be jar files or a set of files (sources/classes).
 * */
public interface DependencyBundleResolver { // this name really sux ...

    /**
     * Attempts to find the Jar a given class name belongs to.
     * Returns a set of Jar files that contain the class name.
     * Returns an empty set if none of the given bundles contains the jar file.
     *
     * @param className the class name to look for
     * @param bundles a set of jar files to look in
     *
     * @return a set of jar files containing given class name or an empty set if class name is not
     *         contained in given jars
     * */
	Set<JarFile> resolve(ClassName className, Set<JarFile> bundles) throws IOException;
}
