package org.codarama.diet.minimization;

import org.codarama.diet.model.marker.Packagable;
import org.codarama.diet.model.marker.Resolvable;

import java.io.IOException;
import java.util.Set;

/**
 * Intended to be used in {@link org.codarama.diet.api.Minimizer}s to allow for different minimization approaches.
 * Also to allow decoupling configuration from the Minimizer API.
 *
 * Generic arguments:
 *   - ST(source type): the type of the sources to resolve from, e.x. SourceFile, ClassName etc.
 *   - LT(libraries type): the type of the libraries to resolve from, e.x. JarFile, ClassFile etc.
 *   - RT(return type): type of the returned set, should be {@link org.codarama.diet.model.marker.Packagable} so that
 *                      a jar can be made from it afterword
 *
 *   Generally ST can be anything {@link org.codarama.diet.model.marker.Resolvable}, LT can be absolutely anything,
 *   RT can be anything {@link org.codarama.diet.model.marker.Packagable}
 *
 * @see org.codarama.diet.model.marker.Resolvable
 * @see org.codarama.diet.model.marker.Packagable
 *
 * Created by ayld on 6/6/2015.
 */
public interface MinimizationStrategy<ST extends Resolvable, LT, RT extends Packagable> {

    /**
     * Minimizes a set of sources given a set of libraries.
     * Sources and libraries can be anything {@link org.codarama.diet.model.marker.Resolvable}.
     *
     * @param sources the sources to derive dependencies from
     * @param libraries the libraries to search for dependencies in
     *
     * @return a set of {@link org.codarama.diet.model.ClassFile}s on which the sources depend
     * */
    public Set<RT> minimize(Set<ST> sources, Set<LT> libraries) throws IOException;
}
