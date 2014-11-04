package org.codarama.diet.api.reporting;

import java.io.IOException;
import java.util.jar.JarFile;

/**
 * A report, containing the results from the minimization operations
 */
public interface MinimizationReport {

	/**
	 * The {@link JarFile} that contains the minimized set of dependencies
	 * 
	 * @return the {@link JarFile} containing only the dependencies that we need
	 * @throws IOException
	 *             if an error occurred while deleting the temporary files
	 */
	JarFile getJar() throws IOException;

	/**
	 * @return the {@link MinimizationStatistics} for the executed minimization job
	 */
	MinimizationStatistics getStatistics();

}
