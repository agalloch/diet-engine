package org.codarama.diet.minimization.impl;

import org.codarama.diet.minimization.MinimizationStrategy;
import org.codarama.diet.model.ClassFile;
import org.codarama.diet.model.SourceFile;

import java.io.IOException;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * A {@link org.codarama.diet.minimization.MinimizationStrategy} that indexes all .class files in library jars to a tree
 * and the runs queries on that tree for (hopefully) optimal performance.
 *
 * Created by ayld on 20.06.15.
 */
public class IndexedMinimizationStrategy implements MinimizationStrategy<SourceFile, JarFile>{



    @Override
    public Set<ClassFile> minimize(Set<SourceFile> sources, Set<JarFile> libraries) throws IOException {
        return null;
    }
}
