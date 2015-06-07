package org.codarama.diet.api.minimization;

import org.codarama.diet.model.ClassFile;
import org.codarama.diet.model.Resolvable;

import java.io.IOException;
import java.util.Set;

/**
 * Intended to be used in {@link org.codarama.diet.api.Minimizer}s to allow for different minimization approaches.
 * Also to allow decoupling configuration from the Minimizer API.
 *
 * Created by ayld on 6/6/2015.
 */
public interface MinimizationStrategy<ST extends Resolvable, LT> {

    /**
     * Minimizes a set of sources given a set of libraries.
     * Sources and libraries can be anything {@link org.codarama.diet.model.Resolvable}.
     *
     * @param sources the sources to derive dependencies from
     * @param libraries the libraries to search for dependencies in
     *
     * @return a set of {@link org.codarama.diet.model.ClassFile}s on which the sources depend
     * */
    public Set<ClassFile> minimize(Set<ST> sources, Set<LT> libraries) throws IOException;
}
