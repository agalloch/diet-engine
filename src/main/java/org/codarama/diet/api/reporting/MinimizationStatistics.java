package org.codarama.diet.api.reporting;

/**
 * Contains statistics about the minimization process, such as how much time it took, how many files were minimized,
 * etc.
 */
public interface MinimizationStatistics {

	/**
	 * @return the total time it took to finish the minimization process (in milliseconds)
	 */
	long getTotalExecutionTime();

	/**
	 * @return the number of sources that were considered by the minimization engine
	 */
	int getSourceFilesCount();

	/**
	 * @return the number of dependencies before the minimization process was executed
	 */
	int getTotalDependenciesCount();

	/**
	 * @return the number of dependencies after the minimization process was executed
	 */
	int getMinimizedDependenciesCount();
}
