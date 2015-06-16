package org.codarama.diet.dependency.resolver.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.codarama.diet.component.ListenableComponent;
import org.codarama.diet.dependency.resolver.DependencyResolver;
import org.codarama.diet.model.ClassInClasspath;
import org.codarama.diet.model.ClassName;
import org.codarama.diet.util.Tokenizer;
import org.codarama.diet.util.system.JdkSettings;
import org.codarama.diet.util.system.jdk.model.JdkToolCalls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * Uses the Jdeps JdkSettings 8 util to try and resolve dependencies of a binary {@link org.codarama.diet.model.ClassName} file.
 *
 * This is to be used in cases where you need to know
 *
 * @see {@link org.codarama.diet.util.system.Jdeps}
 *
 * Created by ayld on 5/31/2015.
 */
public class JdepsClassNameDependencyResolver extends ListenableComponent implements DependencyResolver<ClassInClasspath> {

    private static final Logger LOG = LoggerFactory.getLogger(JdepsClassNameDependencyResolver.class);

    private static final String JDEPS_DEPENDENCY_DELIMITER = "->";

    @Override
    public Set<ClassName> resolve(ClassInClasspath resolvable) throws IOException {

        final Set<JarFile> jars = resolvable.classpath();

        // + 1 for the class name
        // we want to call "jdeps -v -cp <jars> <classname>"
        final String[] jdepsArgs = new String[jars.size() + 1];
        int count = 0;
        for (JarFile jar : jars) {
            jdepsArgs[count] = jar.getName();
            count++;
        }
        jdepsArgs[jdepsArgs.length - 1] = resolvable.clazz().toString();

        final String rawJdepsOutput = JdkToolCalls.JDEPS_VERBOSE_CLASSPATH.withArgs(jdepsArgs).exec();

        return parseOutput(rawJdepsOutput);
    }

    @Override
    public Set<ClassName> resolve(Set<ClassInClasspath> resolvables) throws IOException {
        final Set<ClassName> result = Sets.newHashSet();
        for (ClassInClasspath resolvable : resolvables) {
            result.addAll(resolve(resolvable));
        }
        return result;
    }

    private Set<ClassName> parseOutput(String out) {
        final List<String> outputLines = Tokenizer.delimiter("\n").tokenize(out).tokens();

        final HashSet<ClassName> result = Sets.newHashSet();
        for (String line : outputLines) {

            // skip non-dependency lines
            final boolean isDependencyLine = line.contains(JDEPS_DEPENDENCY_DELIMITER);
            if (!isDependencyLine) {
                continue;
            }

            // skip jar info lines that look like this:
            // guava-14.0.1.jar -> C:\Users\ayld\Documents\GitHub\Diet\target\test-classes\test-classes\test-lib-dir\guava-14.0.1.jar
            if (Tokenizer.delimiter(JDEPS_DEPENDENCY_DELIMITER).tokenize(line).firstToken().contains(".jar")) {
                continue;
            }

            // a dependency line looks like this:
            //   com.google.common.collect.Sets  -> com.google.common.collect.Sets$CartesianSet guava-14.0.1.jar
            // so split by "->" first and trim so we can split by " " later
            final String dependencyPart = Tokenizer.delimiter(JDEPS_DEPENDENCY_DELIMITER).tokenize(line).lastToken().trim();

            // we're left with this:
            // com.google.common.collect.Sets$CartesianSet guava-14.0.1.jar
            // now split by " " and get the first part which should be a class name
            final String dependency = Tokenizer.delimiter(" ").tokenize(dependencyPart).firstToken();

            // skip java core package dependencies
            if (dependency.startsWith(JdkSettings.JAVA_ROOT_PACKAGE)) {
                continue;
            }

            // skip some odd lines I'm getting for some yet unknown reason
            if (dependency.equals(" ") || Strings.isNullOrEmpty(dependency)) {
                // XXX TODO we should find out if this is a bug
                continue;
            }

            // add what we have to the result
            // the domain obj should take care of validation
            result.add(new ClassName(dependency));
        }
        return result;
    }
}
