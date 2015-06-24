package org.codarama.diet.dependency.resolver.impl;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.bcel.classfile.*;
import org.codarama.diet.dependency.resolver.DependencyResolver;
import org.codarama.diet.model.ClassName;
import org.codarama.diet.model.ClassStream;
import org.codarama.diet.util.Java;

import java.io.IOException;
import java.util.Set;

/**
 * Resolves the dependencies of {@link org.codarama.diet.model.ClassStream} which is essentially a byte stream.
 * The model class validates whether said stream is actually a compiled Java class.
 *
 * Created by ayld on 6/21/2015.
 */
public class ClassStreamDependencyResolver implements DependencyResolver<ClassStream> {

    private static final String BINARY_ARRAY_ID_PREFIX = "[";
    private static final String BINARY_TYPE_PREFIX = "L";
    private static final String BINARY_ARRAY_ID_SUFFIX = ";";

    private static final String ARRAY_ID_PREFIX_REGEX = "\\" + BINARY_ARRAY_ID_PREFIX + "+";
    private static final String TYPE_PREFIX_REGEX = BINARY_TYPE_PREFIX;

    @Override
    public Set<ClassName> resolve(ClassStream classStream) throws IOException {

        final JavaClass javaClass = new ClassParser(classStream.content(), classStream.name().toString()).parse();

        final DependencyVisitor dependencyVisitor = new DependencyVisitor(javaClass);
        final DescendingVisitor classWalker = new DescendingVisitor(javaClass, dependencyVisitor);

        classWalker.visit();

        return dependencyVisitor.getFoundDependencies();
    }

    @Override
    public Set<ClassName> resolve(Set<ClassStream> classStreams) throws IOException {
        final Set<ClassName> result = Sets.newHashSet();

        for (ClassStream classFile : classStreams) {
            result.addAll(resolve(classFile));
        }

        return result;
    }

    private static class DependencyVisitor extends EmptyVisitor {

        private final JavaClass javaClass;
        private Set<ClassName> foundDependencies = Sets.newHashSet();

        private DependencyVisitor(JavaClass javaClass) {
            this.javaClass = javaClass;
        }

        @Override
        public void visitConstantClass(ConstantClass constantClass) {
            final ConstantPool cp = javaClass.getConstantPool();

            String dependency = constantClass.getBytes(cp);

            // handle array dependencies
            // if this is an array dependency remove identifiers
            if (dependency.startsWith(BINARY_ARRAY_ID_PREFIX)) {
                dependency = dependency.replaceAll(ARRAY_ID_PREFIX_REGEX, "");
                dependency = dependency.replaceAll(BINARY_ARRAY_ID_SUFFIX, "");
            }

            // handle binary type notations
            if (dependency.startsWith(BINARY_TYPE_PREFIX)) {
                dependency = dependency.replaceAll(TYPE_PREFIX_REGEX, "");
            }

            // because for some reason BCEL returns dependencies like this:
            //
            // com/something/Class
            //
            // which is odd
            // TODO check if there is a way to return dependencies dot delimited
            dependency = dependency.replaceAll("/", ".");

            // don't return the class we're resolving as a dependency
            final boolean isNotResolvedClass = !dependency.equals(javaClass.getClassName());

            // don't return java.io, java.lang ect. dependencies
            final boolean isNotJavaCoreDependency = !dependency.startsWith(Java.ROOT_PACKAGE);
            if (isNotResolvedClass && isNotJavaCoreDependency) {
                foundDependencies.add(new ClassName(dependency));
            }
        }

        private Set<ClassName> getFoundDependencies() {
            return ImmutableSet.copyOf(foundDependencies);
        }
    }
}
