package org.codarama.diet.api;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarFile;

import org.codarama.diet.bundle.JarExploder;
import org.codarama.diet.bundle.JarMaker;
import org.codarama.diet.dependency.matcher.DependencyMatcherStrategy;
import org.codarama.diet.dependency.resolver.DependencyResolver;
import org.codarama.diet.model.ClassFile;
import org.codarama.diet.model.ClassName;
import org.codarama.diet.model.SourceFile;
import org.codarama.diet.util.Components;
import org.codarama.diet.util.Directories;
import org.codarama.diet.util.Files;
import org.codarama.diet.util.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * The {@link DietMinimizer} is a simple implementation of the {@link Minimizer} interface that uses the
 * {@link DependencyMatcherStrategy} to locate the dependencies that we want to include in the minimized result
 */
public final class DietMinimizer implements Minimizer {

	private static final Logger LOG = LoggerFactory.getLogger(DietMinimizer.class);
	private static final String JAVA_API_ROOT_PACKAGE = "java";

	private final DependencyMatcherStrategy dependencyMatcherStrategy = Components.DEPENDENCY_MATCHER_STRATEGY
			.getInstance();
	private final DependencyResolver<ClassFile> classDependencyResolver = Components.CLASS_DEPENDENCY_RESOLVER
			.getInstance();
	private final DependencyResolver<SourceFile> sourceDependencyResolver = Components.SOURCE_DEPENDENCY_RESOLVER
			.getInstance();

	private final JarMaker jarMaker = Components.JAR_MAKER.getInstance();
	private final JarExploder libJarExploder = Components.LIB_JAR_EXPLODER.getInstance();
	private final JarExploder explicitJarExploder = Components.EXPLICIT_JAR_EXPLODER.getInstance(); // don't
																									// use
																									// if
																									// you're
																									// under
																									// 18

	// this is usually the OS temp dir
	private String workDir = Settings.DEFAULT_OUT_DIR.getValue();
	// this is where explicitly included dependencies go
	private String explicitOutDir = Settings.EXPLICIT_OUT_DIR.getValue();

	private File outJar = new File(Joiner.on(File.separator).join(workDir, Settings.DEFAULT_RESULT_JAR_NAME.getValue()));

	private final File sourceDir;

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
	private DietMinimizer(File pathToSources) {
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

		return new DietMinimizer(sourceDirectory);
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

		final String resultJarPath = Joiner.on(File.separator).join(out.getAbsolutePath(),
				Settings.DEFAULT_RESULT_JAR_NAME.getValue());
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
	public JarFile getJar() throws IOException {

		final Set<SourceFile> sources = Sets.newHashSet();
		for (File sourceFile : Files.in(sourceDir.getAbsolutePath()).withExtension(SourceFile.EXTENSION).list()) {
			sources.add(SourceFile.fromFile(sourceFile));
		}

		final Set<ClassName> sourceDependencies = sourceDependencyResolver.resolve(sources);

		explodeJars(libraryLocations);
		final Set<File> libClasses = ImmutableSet.copyOf(Files.in(workDir).withExtension(ClassFile.EXTENSION).list());
		final Set<ClassFile> foundDependencies = findInLib(sourceDependencies, libClasses);

		addDependenciesOfDependencies(foundDependencies, libClasses);
		foundDependencies.addAll(forceIncludeDependenciesAsFiles(this.forceIncludeJars, this.forceIncludeClasses,
				libClasses));

		final Set<File> dependenciesForPackaging = Sets.newHashSetWithExpectedSize(foundDependencies.size());
		for (ClassFile dep : foundDependencies) {
			dependenciesForPackaging.add(dep.physicalFile());
		}

		final JarFile result = jarMaker.zip(dependenciesForPackaging);

		cleanWorkDir();

		return result;
	}

	private void cleanWorkDir() throws IOException {
		final Set<File> dirtyDirs = Directories.in(workDir).nameEndsWith(JarMaker.JAR_FILE_EXTENSION).list(); // dirty
																												// ho
																												// ho
																												// ho
																												// ;)
		for (File dirty : dirtyDirs) {
			Files.deleteRecursive(dirty);
		}
	}

	private Set<ClassFile> forceIncludeDependenciesAsFiles(Set<JarFile> explicitIncludeJars,
			Set<ClassName> explicitIncludeClasses, final Set<File> libClasses) throws IOException {
		final Set<ClassFile> result = Sets.newHashSet();

		for (ClassName includeClass : explicitIncludeClasses) {

			final Set<ClassFile> foundInLib = findInLib(ImmutableSet.of(includeClass), libClasses);
			if (foundInLib.size() < 1) {
				throw new IllegalStateException("can't find user defined class: " + includeClass);
			}
			result.addAll(foundInLib);
		}

		explicitJarExploder.explode(explicitIncludeJars);

		for (File extracted : Files.in(explicitOutDir).withExtension(ClassFile.EXTENSION).list()) {
			result.add(ClassFile.fromFile(extracted));
		}

		return result;
	}

	private Set<ClassFile> addDependenciesOfDependencies(Set<ClassFile> deps, final Set<File> libClasses)
			throws IOException {
		removeJavaApiDeps(deps);

		deps.addAll(findInLib(classDependencyResolver.resolve(deps), libClasses));

		final int sizeBeforeResolve = deps.size();
		if (deps.size() == sizeBeforeResolve) {
			return deps;
		}

		return addDependenciesOfDependencies(deps, libClasses);
	}

	private void removeJavaApiDeps(Set<ClassFile> deps) {
		for (Iterator<ClassFile> iterator = deps.iterator(); iterator.hasNext();) {

			final ClassFile dep = iterator.next();

			if (dep.qualifiedName().toString().startsWith(JAVA_API_ROOT_PACKAGE)) {
				iterator.remove();
			}
		}
	}

	private Set<ClassFile> findInLib(Set<ClassName> dependencyNames, Set<File> libClasses) throws IOException {

		final Set<ClassFile> result = Sets.newHashSetWithExpectedSize(dependencyNames.size());
		for (ClassName dependencyName : dependencyNames) {
			for (File libClass : libClasses) {

				final ClassFile libClassFile = ClassFile.fromFile(libClass);

				if (dependencyMatcherStrategy.matches(dependencyName, libClassFile)) {
					result.add(libClassFile);
				}
			}
		}
		return result;
	}

	private void explodeJars(Set<File> libraryLocations) throws IOException {
		final Set<JarFile> libJars = Sets.newHashSet();

		for (File jarFile : libraryLocations) {
			try {
				libJars.add(new JarFile(jarFile));
			} catch (IOException e) {
				LOG.error("Error proccesing dependency " + jarFile.getAbsolutePath(), e);
			}
		}

		libJarExploder.explode(libJars);
	}
}
