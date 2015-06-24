package org.codarama.diet.model;

import org.apache.bcel.classfile.ClassParser;
import org.codarama.diet.dependency.resolver.impl.ClassStreamDependencyResolver;
import org.codarama.diet.model.marker.Packagable;
import org.codarama.diet.model.marker.Resolvable;
import org.codarama.diet.util.Components;
import org.codarama.diet.util.annotation.NotThreadSafe;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a binary, compiled class file as a stream of bytes.
 * This class ensures that given stream is actually a compiled Java class (ot at least it looks like one :D).
 *
 * Created by ayld on 6/21/2015.
 */
@NotThreadSafe
public class ClassStream implements Resolvable, Packagable {

    private final InputStream streamContent;
    private final ClassName name;

    private ClassStream(InputStream streamContent) {
        this.streamContent = new BufferedInputStream(streamContent);
        try {
            this.streamContent.mark(streamContent.available());
            this.name = new ClassName(new ClassParser(this.streamContent, "").parse().getClassName());
            this.streamContent.reset();
        } catch (IOException e) {
            // assuming bcel threw IOException because validation failed
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Creates a new class stream from an input stream.
     * This method validates whether the give input stream actually contains a compiled class file.
     *
     * @throws IllegalArgumentException if the stream does not contain a compiled class
     * @param content the stream to create a ClassStream from
     * @return a new class stream
     * */
    public static ClassStream fromStream(InputStream content) {
        return new ClassStream(content);
    }

    /**
     * Returns the content of this class stream.
     * A compiled class file as a stream of bytes.
     *
     * @return a compiled class file as a stream of bytes.
     * */
    public InputStream content() {
        return this.streamContent;
    }

    /**
     * Returns the fully qualified name of the class represented by this object.
     *
     * @see {@link ClassName}
     * @return the fully qualified name of the class represented by this object
     * */
    public ClassName name() {
        return this.name;
    }

    /**
     * Returns the first level dependencies (a.k.a direct dependencies) of the class represented by this object.
     *
     * @return a set containing only the direct dependencies of the class represented by this object
     * */
    public Set<ClassName> dependencies() {
        try {
            return Components.STREAM_DEPENDENCY_RESOLVER.<ClassStreamDependencyResolver>getInstance().resolve(this);
        } catch (IOException e) {
            throw new RuntimeException("could not get dependencies of: " + this, e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClassStream)) {
            return false;
        }

        final ClassStream other = (ClassStream) o;

        // we don't compare streams here on purpose as it will be extremely slow since a Set
        // will do an equals for every add which will read both streams being checked for equality
        // we might experiment with this in the future and see how slow it actually is

        return other.name.equals(this.name);
    }

    @Override
    public int hashCode() {
        // again as the #equals method, we don't pass the streams in here as
        // the hash method would have to read from them
        return Objects.hash(name);
    }
}
