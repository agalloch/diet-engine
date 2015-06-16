package org.codarama.diet.api;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.jar.JarFile;

import com.google.common.annotations.Beta;
import org.codarama.diet.api.reporting.MinimizationReport;
import org.codarama.diet.model.ClassName;

/**
 * <p>
 * Any implementation of the {@link Minimizer} interface should be responsible for building itself using the builder
 * pattern and providing the API frontend of Diet's jar minimizing functionality.
 * </p>
 */
@Beta
public interface Minimizer {

	/**
	 * <p>
	 * Set up a given path to serve as the {@link Minimizer}s libraries directory
	 * </p>
	 * <p>
	 * The {@link Minimizer} would assume that all the dependencies of the sources are located within this directory or
	 * it's sub directories
	 * </p>
	 * 
	 * @param pathToLibraries
	 *            the file path to the directory
	 * @return an instance of the {@link Minimizer} that is being set up
	 * @throws IOException
	 *             in case there is a problem resolving the path
	 */
	Minimizer libs(String pathToLibraries) throws IOException;

	/**
	 * <p>
	 * Use the provided {@link Set} of {@link File}s as the list of dependencies, which need to be minimized
	 * </p>
	 * 
	 * @param artifactLocations
	 *            a {@link Set} of {@link File}s that are dependencies of this project
	 * @return an instance of the {@link Minimizer} that is being set up
	 */
	Minimizer libs(Set<File> artifactLocations);

	/**
	 * <p>
	 * Set up a given path to server as the {@link Minimizer}s output directory
	 * </p>
	 * <p>
	 * All the minimized output would be dropped in that location
	 * </p>
	 * 
	 * @param pathToOutput
	 *            the path to the directory to serve as an output
	 * @return an instance of the {@link Minimizer} that is being set up
	 */
	Minimizer output(String pathToOutput);

	/**
	 * <p>
	 * Forcefully include {@link JarFile}s even if they did not want to be part of this
	 * </p>
	 * <p>
	 * Used for sources that could not be recognized by the {@link Minimizer}s dependency detecting logic, essentially a
	 * fallback mechanism
	 * </p>
	 * 
	 * @param jars
	 *            an array of {@link JarFile} to be forcefully included
	 * @return an instance of the {@link Minimizer} that is being set up
	 */
	Minimizer forceInclude(JarFile... jars);

	/**
	 * <p>
	 * Forcefully include {@link ClassName}s even if they did not want to be part of this
	 * </p>
	 * <p>
	 * Used for sources that could not be recognized by the {@link Minimizer}s dependency detecting logic, essentially a
	 * fallback mechanism
	 * </p>
	 * 
	 * @param classes
	 *            an array of {@link ClassName}s to be forcefully included
	 * @return an instance of the {@link Minimizer} that is being set up
	 */
	Minimizer forceInclude(ClassName... classes);

	/**
	 * @return the resulting {@link MinimizationReport}
	 * @throws IOException
	 *             if an error occurred while deleting the temporary files
	 */
	MinimizationReport minimize() throws IOException;

}
