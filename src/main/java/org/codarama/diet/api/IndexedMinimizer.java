package org.codarama.diet.api;

import org.codarama.diet.api.reporting.MinimizationReport;
import org.codarama.diet.model.ClassName;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * A minimizer that indexes all .class files in the jars it's trying to minimize.
 *
 * Created by ayld on 20.06.15.
 */
public class IndexedMinimizer extends DefaultMinimizer implements Minimizer{

    private IndexedMinimizer(File pathToSources) {
        super(pathToSources);
    }

    @Override
    public MinimizationReport minimize() throws IOException {
        return null;
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
