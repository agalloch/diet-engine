package org.codarama.diet.api.reporting;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.jar.JarFile;

import org.codarama.diet.model.ClassFile;
import org.codarama.diet.model.SourceFile;

/**
 * A simple builder class that builds up a {@link MinimizationReport}
 */
public class ReportBuilder {

	private long startTime;

	private MinimizationStatisticsImplementation statistics = new MinimizationStatisticsImplementation();
	private MinimizationReportImplementation report = new MinimizationReportImplementation(statistics);

	private ReportBuilder() {
		// Disable class instantiation from other classes
	}

	/**
	 * Construct a new instance of the {@link ReportBuilder} and start the clock that measures when the minimization
	 * process has started
	 * 
	 * @return a new instance of the {@link ReportBuilder}
	 */
	public static ReportBuilder startClock() {
		ReportBuilder builder = new ReportBuilder();
		builder.startTime = System.currentTimeMillis();
		return builder;
	}

	/**
	 * Collects the required statistics from the sources that are being minimized
	 * 
	 * @param sources
	 *            a {@link Set} of {@link SourceFile}s whose dependencies we are minimizing
	 * @return the instance of the {@link ReportBuilder} for chaining purposes
	 */
	public ReportBuilder sources(Set<SourceFile> sources) {
		this.report.statistics.sourcesCount = sources.size();
		return this;
	}

	/**
	 * Sets the resulting minimized {@link JarFile} containing all the direct and transitive dependencies
	 * 
	 * @param jarFile
	 *            the {@link JarFile}containing all the direct and transitive dependencies
	 * @return the instance of the {@link ReportBuilder} for chaining purposes
	 */
	public ReportBuilder setJarFile(JarFile jarFile) {
		this.report.jarFile = jarFile;
		return this;

	}

	/**
	 * Collects the required statistics from the dependencies that are being minimized
	 * 
	 * @param libClasses
	 *            a {@link Set} of {@link File}s that include all the dependencies before they were minimized
	 * @return the instance of the {@link ReportBuilder} for chaining purposes
	 */
	public ReportBuilder allLibs(Set<File> libClasses) {
		this.report.statistics.totalDependenciesCount = libClasses.size();
		return this;
	}

	/**
	 * Collects the required total number of classes to be minimized.
	 *
	 * @param count
	 *            the total number of classes (usually jar entries)
	 * @return the instance of the {@link ReportBuilder} for chaining purposes
	 */
	public ReportBuilder allLibsCount(int count) {
		this.report.statistics.totalDependenciesCount = count;
		return this;
	}

	/**
	 * Collects the required statistics from the dependencies after they were minimized
	 * 
	 * @param foundDependencies
	 *            a {@link Set} of {@link ClassFile}s that include the dependencies after they were minimized
	 * @return the instance of the {@link ReportBuilder} for chaining purposes
	 */
	public ReportBuilder minimizedLibs(Set<?> foundDependencies) {
		this.report.statistics.minimizedDependenciesCount = foundDependencies.size();
		return this;
	}

	/**
	 * Stops the clock, measuring how much time it took for the minimization process to complete
	 * 
	 * @return the instance of the {@link ReportBuilder} for chaining purposes
	 */
	public ReportBuilder stopClock() {
		long endTime = System.currentTimeMillis();
		this.statistics.totalTime = endTime - startTime;

		return this;
	}

	/**
	 * @return the {@link MinimizationReport} that we were building
	 */
	public MinimizationReport getReport() {
		return this.report;
	}

	private class MinimizationReportImplementation implements MinimizationReport {

		private JarFile jarFile;

		private MinimizationStatisticsImplementation statistics;

		private MinimizationReportImplementation(MinimizationStatisticsImplementation statistics) {
			this.statistics = statistics;
		}

		@Override
		public JarFile getJar() throws IOException {
			return jarFile;
		}

		@Override
		public MinimizationStatistics getStatistics() {
			return statistics;
		}
	}

	private class MinimizationStatisticsImplementation implements MinimizationStatistics {
		private int minimizedDependenciesCount;
		private int totalDependenciesCount;
		private int sourcesCount;
		private long totalTime;

		@Override
		public long getTotalExecutionTime() {
			return this.totalTime;
		}

		@Override
		public int getSourceFilesCount() {
			return this.sourcesCount;
		}

		@Override
		public int getTotalDependenciesCount() {
			return this.totalDependenciesCount;
		}

		@Override
		public int getMinimizedDependenciesCount() {
			return this.minimizedDependenciesCount;
		}
	}
}
