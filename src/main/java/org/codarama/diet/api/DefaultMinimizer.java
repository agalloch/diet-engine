package org.codarama.diet.api;

import com.google.common.annotations.Beta;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.codarama.diet.minimization.MinimizationStrategy;
import org.codarama.diet.api.reporting.MinimizationReport;
import org.codarama.diet.api.reporting.ReportBuilder;
import org.codarama.diet.bundle.JarExploder;
import org.codarama.diet.bundle.JarMaker;
import org.codarama.diet.dependency.matcher.DependencyMatcherStrategy;
import org.codarama.diet.model.ClassFile;
import org.codarama.diet.model.ClassName;
import org.codarama.diet.model.SourceFile;
import org.codarama.diet.util.Components;
import org.codarama.diet.util.Directories;
import org.codarama.diet.util.Files;
import org.codarama.diet.util.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * The {@link DefaultMinimizer} is a simple implementation of the {@link Minimizer} interface that uses the
 * {@link DependencyMatcherStrategy} to locate the dependencies that we want to include in the minimized result
 */
@Beta
public class DefaultMinimizer implements Minimizer {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultMinimizer.class);

    private final MinimizationStrategy<SourceFile, File> minimizationStrategy = Components.BCEL_MINIMIZATION_STRATEGY.getInstance();

	protected final JarMaker jarMaker = Components.JAR_MAKER.getInstance();

	private final JarExploder explicitJarExploder = Components.EXPLICIT_JAR_EXPLODER.getInstance();

	// this is usually the OS temp dir
	private String workDir = Settings.DEFAULT_OUT_DIR.getValue();
	// this is where explicitly included dependencies go
	private String explicitOutDir = Settings.EXPLICIT_OUT_DIR.getValue();

	protected File outJar = new File(Joiner.on(File.separator).join(workDir, Settings.DEFAULT_RESULT_JAR_NAME.getValue()));

	protected final File sourceDir;

	private Set<JarFile> forceIncludeJars = Sets.newHashSet();
	private Set<ClassName> forceIncludeClasses = Sets.newHashSet();
	private Set<File> libraryLocations = Sets.newHashSet();

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

	/**
	 * <p>
	 * Used to initiate the creation of a the {@link Minimizer} using a builder pattern
	 * </p>
	 * <p>
	 * To completely set up the {@link Minimizer} the users of this method should also make sure to set up :
	 * <ul>
	 * <li>the dependencies that the source files use, see {@link Minimizer#libs(Set)} and
	 * {@link Minimizer#libs(String)}</li>
	 * <li>the output directory, see {@link Minimizer#output(String)}</li>
	 * <li>(optional) any runtime dependencies that need to be forecully included, see
	 * {@link Minimizer#forceInclude(ClassName[])} and {@link Minimizer#forceInclude(JarFile[])}</li>
	 * </ul>
	 * </p>
	 * 
	 * @param pathToSources
	 * @return
	 */
	public static Minimizer sources(String pathToSources) {
		final File sourceDirectory = new File(pathToSources);

		if (!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
			throw new IllegalArgumentException("directory at: " + pathToSources
					+ " does not exist or is not a directory");
		}

		return new DefaultMinimizer(sourceDirectory);
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
	public MinimizationReport minimize() throws IOException {

		// TODO itching to use aspects here, as this is a cross cutting concern, but lets keep it simple for now
		ReportBuilder reportBuilder = ReportBuilder.startClock();

		// start by analysing all the source files
		final Set<SourceFile> sources = Sets.newHashSet();
        sources.addAll(
                Files.in(sourceDir.getAbsolutePath())
                        .withExtension(SourceFile.EXTENSION)
                        .list()
                        .stream()
                        .map(SourceFile::fromFile)
                        .collect(Collectors.toList())
        );
        final Set<ClassFile> foundDependencies = this.minimizationStrategy.minimize(sources, libraryLocations);

        final Set<File> libClasses = Sets.newHashSet(Files.in(workDir).withExtension(ClassFile.EXTENSION).list());
        reportBuilder.sources(sources).allLibs(libClasses).minimizedLibs(foundDependencies);

        // add user defined mandatory dependencies
        final Set<ClassFile> mandatoryDependencies =
				  mandatoryDependenciesAsFiles(this.forceIncludeJars, this.forceIncludeClasses);
		foundDependencies.addAll(mandatoryDependencies);

		final Set<File> dependenciesForPackaging = Sets.newHashSetWithExpectedSize(foundDependencies.size());
		for (ClassFile dep : foundDependencies) {
			dependenciesForPackaging.add(dep.physicalFile());
		}

		reportBuilder.setJarFile(jarMaker.zip(dependenciesForPackaging)).stopClock();

		cleanWorkDir();

		return reportBuilder.getReport();
	}

	private void cleanWorkDir() throws IOException {
		final Set<File> dirtyDirs = Directories.in(workDir).nameEndsWith(JarMaker.JAR_FILE_EXTENSION).list();
		for (File dirty : dirtyDirs) {
			Files.deleteRecursive(dirty);
		}
	}

	private Set<ClassFile> mandatoryDependenciesAsFiles(
			  Set<JarFile> explicitIncludeJars, Set<ClassName> explicitIncludeClasses) throws IOException {

        explicitJarExploder.explode(explicitIncludeJars);

        final Set<ClassFile> result = Sets.newHashSet();
        result.addAll(
                Files.in(explicitOutDir)
                        .withExtension(ClassFile.EXTENSION)
                        .list()
                        .stream()
                        .map(ClassFile::fromFile)
                        .collect(Collectors.toList())
        );

        for (ClassName includeClassName : explicitIncludeClasses) {
            final File includeClass;
            try {
                includeClass = Files.in(explicitOutDir).named(includeClassName.shortName()).single();
            } catch (IllegalStateException e) {
                throw new IllegalStateException("could not add mandatory class: " + includeClassName, e);
            }
            result.add(ClassFile.fromFile(includeClass));
        }
		return result;
	}
}
