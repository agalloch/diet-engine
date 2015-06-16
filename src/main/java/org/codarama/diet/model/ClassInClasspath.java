package org.codarama.diet.model;

import com.google.common.collect.ImmutableSet;

import java.util.Objects;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * Represents a class in a list of jars.
 * The class is a valid {@link ClassName}.
 * The "classpath" is just a list of jars that are supposed to contain the class file.
 *
 * {@link ClassInClasspath}'s intended use is in the {@link org.codarama.diet.dependency.resolver.impl.JdepsClassNameDependencyResolver}.
 * It is used by the jdeps JDK tool to find the dependencies of a class in a list of jars.
 *
 * Keep in mind that this object does not guarantee that the class name is actually contained in the jars.
 *
 * @see {@link org.codarama.diet.dependency.resolver.impl.JdepsClassNameDependencyResolver}
 *
 * Created by Ayld on 6/16/15.
 */
public class ClassInClasspath implements Resolvable{

    private final ClassName clazz;
    private final Set<JarFile> classpath;

    private ClassInClasspath(ClassName clazz, Set<JarFile> classpath) {
        this.clazz = clazz;
        this.classpath = classpath;
    }

    public ClassName clazz() {
        return clazz;
    }

    public Set<JarFile> classpath() {
        return ImmutableSet.copyOf(classpath);
    }

    @Override
    public String toString() {
        return "(" + clazz.toString() + " in " + classpath + ")";
    }

    // XXX IDE generated
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        ClassInClasspath that = (ClassInClasspath) other;

        return clazz.equals(that.clazz) && classpath.equals(that.classpath);

    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, classpath);
    }

    public static class Builder {

        private ClassName clazz;
        private Set<JarFile> classpath;

        private Builder() {
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder clazz(ClassName clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder classpath(Set<JarFile> classpath) {
            this.classpath = classpath;
            return this;
        }

        public ClassInClasspath build() {
            if (this.clazz == null) {
                throw new IllegalStateException("Class not set");
            }
            final boolean isClasspathEmpty = this.classpath == null || this.classpath.isEmpty();
            if (isClasspathEmpty) {
                throw new IllegalStateException("Classpath not set");
            }
            return new ClassInClasspath(this.clazz, this.classpath);
        }
    }
}
