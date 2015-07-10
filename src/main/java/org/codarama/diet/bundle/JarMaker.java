package org.codarama.diet.bundle;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * A interface for creating jar files.
 * */
public interface JarMaker<F> {
	
	String JAR_FILE_EXTENSION = "jar";

	/**
	 * Creates a {@link JarFile} from a set of {@link File}s and returns it.
	 * Implementers must check wheter files in the set actually exist and are not directories.
	 * If validations fail an {@link IOException} must be thrown.
	 *
	 * @throws IOException if files contained in the set don't exist or are directories
	 * @param files a set of files to be jarred (zipped)
	 * @return a zipped Jar file containing given files
	 * */
	JarFile zip(Set<F> files) throws IOException;
}
