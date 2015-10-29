package org.codarama.diet.api;

import com.google.common.collect.Sets;
import org.codarama.diet.api.reporting.MinimizationReport;
import org.codarama.diet.api.reporting.ReportBuilder;
import org.codarama.diet.bundle.JarMaker;
import org.codarama.diet.minimization.MinimizationStrategy;
import org.codarama.diet.model.ClassFile;
import org.codarama.diet.model.ClassName;
import org.codarama.diet.model.ClassStream;
import org.codarama.diet.model.SourceFile;
import org.codarama.diet.util.Components;
import org.codarama.diet.util.Files;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * A minimizer that indexes all .class files in the jars it's trying to minimize.
 *
 * Created by ayld on 20.06.15.
 */
public class IndexedMinimizer extends DefaultMinimizer implements Minimizer{

    private final MinimizationStrategy<SourceFile, JarFile, ClassStream> minimizationStrategy = Components.INDEXED_MINIMIZATION_STRATEGY.getInstance();
    private final JarMaker<ClassStream> jarMaker = Components.STREAM_JAR_MAKER.getInstance();

    private IndexedMinimizer(File pathToSources) {
        super(pathToSources);
    }

    public static Minimizer sources(String pathToSources) {
        final File sourceDirectory = new File(pathToSources);

        if (!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
            throw new IllegalArgumentException("directory at: " + pathToSources
                    + " does not exist or is not a directory");
        }

        return new IndexedMinimizer(sourceDirectory);
    }

    @Override
    public MinimizationReport minimize() throws IOException {
        // TODO itching to use aspects here, as this is a cross cutting concern, but lets keep it simple for now
        ReportBuilder reportBuilder = ReportBuilder.startClock();


        final Set<File> sourceFiles = Files
                .in(sourceDir.getAbsolutePath())
                .withExtension(SourceFile.EXTENSION)
                .list();

        final Set<SourceFile> sources = Sets.newHashSet(
                sourceFiles
                    .stream()
                    .map(SourceFile::fromFile)
                    .collect(Collectors.toList())
        );

        final Set<JarFile> libs = toJarFiles(libraryLocations);
        reportBuilder.sources(sources).allLibsCount(countClasses(libs));

        final Set<ClassStream> minimized = minimizationStrategy.minimize(sources, libs);

        reportBuilder.minimizedLibs(minimized);

        final JarFile minimizedJar = jarMaker.zip(minimized);

        reportBuilder.setJarFile(minimizedJar).stopClock();

        return reportBuilder.getReport();
    }

    private int countClasses(Set<JarFile> jars) {
        int result = 0;
        for (JarFile jar : jars) {
            result += jar.size();
        }
        return result;
    }

    private Set<JarFile> toJarFiles(Collection<File> jars) throws IOException{
        final Set<JarFile> result = Sets.newHashSetWithExpectedSize(jars.size());
        for (File jar : jars) {
            result.add(new JarFile(jar));
        }
        return result;
    }


    @Override
    public Minimizer libs(String pathToLibraries) throws IOException {
        return super.libs(pathToLibraries);
    }

    @Override
    public Minimizer libs(Set<File> artifactLocations) {
        return super.libs(artifactLocations);
    }

    @Override
    public Minimizer output(String pathToOutput) {
        return super.output(pathToOutput);
    }

    @Override
    public Minimizer forceInclude(JarFile... jars) {
        return super.forceInclude(jars);
    }

    @Override
    public Minimizer forceInclude(ClassName... classes) {
        return super.forceInclude(classes);
    }
}
