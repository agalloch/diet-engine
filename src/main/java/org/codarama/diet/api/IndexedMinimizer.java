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
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * A minimizer that indexes all .class files in the jars it's trying to minimize.
 *
 * Created by ayld on 20.06.15.
 */
public class IndexedMinimizer extends DefaultMinimizer implements Minimizer{

    private final MinimizationStrategy<SourceFile, File, ClassStream> minimizationStrategy = Components.INDEXED_MINIMIZATION_STRATEGY.getInstance();
    private final JarMaker<ClassStream> jarMaker = Components.STREAM_JAR_MAKER.getInstance();

    private IndexedMinimizer(File pathToSources) {
        super(pathToSources);
    }

    @Override
    public MinimizationReport minimize() throws IOException {
        // TODO itching to use aspects here, as this is a cross cutting concern, but lets keep it simple for now
        ReportBuilder reportBuilder = ReportBuilder.startClock();

        final Set<SourceFile> sources = Sets.newHashSet();
        sources.addAll(
                Files.in(sourceDir.getAbsolutePath())
                        .withExtension(SourceFile.EXTENSION)
                        .list()
                        .stream()
                        .map(SourceFile::fromFile)
                        .collect(Collectors.toList())
        );

        final Set<ClassStream> minimized = minimizationStrategy.minimize(sources, libraryLocations);

        final JarFile minimizedJar = jarMaker.zip(minimized);

        reportBuilder.setJarFile(minimizedJar).stopClock();

        return reportBuilder.getReport();
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
