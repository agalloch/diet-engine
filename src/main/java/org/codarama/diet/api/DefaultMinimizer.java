package org.codarama.diet.api;

import com.google.common.annotations.Beta;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.codarama.diet.api.reporting.MinimizationReport;
import org.codarama.diet.bundle.JarMaker;
import org.codarama.diet.dependency.matcher.DependencyMatcherStrategy;
import org.codarama.diet.model.ClassName;
import org.codarama.diet.util.Files;
import org.codarama.diet.util.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * The {@link DefaultMinimizer} is a simple implementation of the {@link Minimizer} interface that uses the
 * {@link DependencyMatcherStrategy} to locate the dependencies that we want to include in the minimized result
 */
@Beta
public abstract class DefaultMinimizer implements Minimizer {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultMinimizer.class);

	// this is usually the OS temp dir
	private String workDir = Settings.DEFAULT_OUT_DIR.getValue();
	// this is where explicitly included dependencies go
	protected File outJar = new File(Joiner.on(File.separator).join(workDir, Settings.DEFAULT_RESULT_JAR_NAME.getValue()));

	protected final File sourceDir;

	private Set<JarFile> forceIncludeJars = Sets.newHashSet();
	private Set<ClassName> forceIncludeClasses = Sets.newHashSet();
	protected Set<File> libraryLocations = Sets.newHashSet();

	/**
	 * <p>
	 * Constructor
	 * </p>
	 * <p>
	 * Private because we want to encourage the users to use the builder pattern when creating this object
	 * </p>
	 * 
	 * @param pathToSources
	 *            the full path to the root sources directory
	 */
	protected DefaultMinimizer(File pathToSources) {
		final File outJarDir = new File(outJar.getParent());

		if (!outJarDir.exists() && !outJarDir.mkdirs()) {
			throw new IllegalStateException("unable to create parent dir for output jar: " + outJar.getParent());
		}

		this.sourceDir = pathToSources;
	}

	@Override
	public Minimizer libs(String pathToLibs) throws IOException {
		final File lib = new File(pathToLibs);

		if (!lib.exists() || !lib.isDirectory()) {
			throw new IllegalArgumentException("Directory at: " + pathToLibs + " does not exist or is not a directory");
		}

		libraryLocations = ImmutableSet.copyOf(Files.in(pathToLibs).withExtension(JarMaker.JAR_FILE_EXTENSION).list());
		return this;
	}

	@Override
	public Minimizer libs(Set<File> artifactLocations) {
		this.libraryLocations = artifactLocations;
		return this;
	}

	@Override
	public Minimizer output(String pathToOutput) {
		final File out = new File(pathToOutput);

		if (!out.exists() && !out.mkdirs()) {
			throw new IllegalStateException("unable to create dir for output jar: " + pathToOutput);
		}

		final String resultJarPath = Joiner
				.on(File.separator)
				.join(out.getAbsolutePath(), Settings.DEFAULT_RESULT_JAR_NAME.getValue());
		this.outJar = new File(resultJarPath);

		return this;
	}

	@Override
	public Minimizer forceInclude(JarFile... jars) {
		this.forceIncludeJars.addAll(Arrays.asList(jars));

		return this;
	}

	@Override
	public Minimizer forceInclude(ClassName... classes) {
		this.forceIncludeClasses.addAll(Arrays.asList(classes));

		return this;
	}

	@Override
	public abstract MinimizationReport minimize() throws IOException;
}
