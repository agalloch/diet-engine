package org.codarama.diet.util.system;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import org.codarama.diet.model.ClassName;
import org.codarama.diet.util.Tokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * A util for invoking the system jdk8 tool.
 *
 * More info:
 *   https://docs.oracle.com/javase/8/docs/technotes/tools/unix/system.html
 *
 * Created by ayld on 5/29/2015.
 */
public class Jdeps {
    private static final Logger LOG = LoggerFactory.getLogger(Jdeps.class);

    private static final String REQUIRED_JAVA_MAJOR_VERSION = "1.8";

    private static final String LIB_PATH_VAR_NAME = "java.library.path";
    private static final String OS_NAME_VAR_NAME = "os.name";
    private static final String WINDOWS_OS_NAME = "Windows";
    private static final String LINUX_OS_NAME = "Linux";
    // TODO Mac

    private static final String WINDOWS_EXECUTABLE_EXTENSION = "exe";

    private static final String JAVA_ROOT_PACKAGE = "java";
    private static final String JAVA_EXECUTABLE_NAME = "java";
    private static final String JAVA_VERBOSE_OPTION = "-version";

    private static final String JDEPS_EXECUTABLE_NAME = "jdeps";
    private static final String JDEPS_DEPENDENCY_DELIMITER = "->";
    private static final String JDEPS_CLASSPATH_OPTION = "-cp";
    private static final String JDEPS_VERBOSE_OPTION = "-v";

    private final String jdepsExecutablePath;

    // a list of jar files, space delimited
    private String classpath = "";

    // a list of fully qualified class names, space delimited
    private String classes = "";

    private Jdeps() {
        final String jdkLibPath = System.getProperty(LIB_PATH_VAR_NAME);
        final boolean isJdkInstalled = !Strings.isNullOrEmpty(jdkLibPath);
        if (!isJdkInstalled) {
            throw new IllegalStateException("can not use jdeps without JDK 1.8 installed");
        }

        final String javaBinPath = Tokenizer.delimiter(";").tokenize(jdkLibPath).firstToken();
        final String javaExecutablePath = Joiner.on(File.separator).join(javaBinPath, JAVA_EXECUTABLE_NAME);
        if (!is18jre(javaExecutablePath)) {
            throw new IllegalStateException("jdk is not 1.8 version");
        }
        this.jdepsExecutablePath = Joiner.on(File.separator).join(javaBinPath, JDEPS_EXECUTABLE_NAME);
    }

    private boolean is18jre(String javaExecutable) {
        final String javaVersionOutput = execShellCmd(javaExecutable, JAVA_VERBOSE_OPTION);

        // TODO XXX better check needed as this is just not enough
        return javaVersionOutput.contains(REQUIRED_JAVA_MAJOR_VERSION);
    }

    private String execShellCmd(String cmd, String... args) {
        try {
            cmd = makeOsCompatible(cmd);

            if (LOG.isDebugEnabled()) {
                LOG.debug("executing: " + cmd + " " + Arrays.asList(args));
            }

            final String[] cmdAndArgs = new String[args.length + 1];
            cmdAndArgs[0] = cmd;

            System.arraycopy(args, 0, cmdAndArgs, 1, cmdAndArgs.length - 1);

            final Process cmdProcess = new ProcessBuilder(cmdAndArgs).redirectErrorStream(true).start();

            final InputStream processInputStream = cmdProcess.getInputStream();
            final List<String> cmdOutput = CharStreams.readLines(new InputStreamReader(processInputStream));

            if (LOG.isDebugEnabled()) {
                for (String line: cmdOutput) {
                    LOG.debug(line);
                }
            }

            return Joiner.on("\n").join(cmdOutput);

        } catch (IOException e) {
            // wrapping to general
            // since we don't currently know what exception might occur here we just show them
            // in the future when we have more knowledge about shell executions we might revise this
            // if we get interrupted this is probably due to cmd execution taking too long
            // there is not much we can do about it here except log it
            throw new RuntimeException("failed to execute command: " + cmd, e);
        }
    }

    /**
     * This method makes the system call to a binary OS compatible.
     * Currently it:
     *   - adds .exe to binary calls for Windows
     *   - quotes whole command to workaround spaces in paths for Windows
     *   - removes preceding slash given by env vars on Linux
     * */
    private String makeOsCompatible(String cmd) {
        final String currentOsName = System.getProperty(OS_NAME_VAR_NAME);
        if (currentOsName.contains(WINDOWS_OS_NAME)) {
            // add .exe at the end of the binary
            return "\"" + Joiner.on(".").join(cmd, WINDOWS_EXECUTABLE_EXTENSION) + "\"";
        }
        else if (currentOsName.contains(LINUX_OS_NAME)) {

            if (cmd.startsWith("/")) {
                return cmd.replaceFirst("/", "");
            }
        }
        return cmd;
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

            // skip java code dependencies
            if (dependency.startsWith(JAVA_ROOT_PACKAGE)) {
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

    public Set<ClassName> findDependencies() {
        final String cmdOutput = execShellCmd(jdepsExecutablePath, JDEPS_VERBOSE_OPTION, JDEPS_CLASSPATH_OPTION, classpath, classes);
        return parseOutput(cmdOutput);
    }

    public static class Builder {
        private Set<JarFile> classpath;
        private Set<ClassName> classes;

        public static Builder searchInJars(Set<JarFile> searchInJars) {
            final Builder newInstance = new Builder();
            newInstance.classpath = searchInJars;

            return newInstance;
        }

        public Builder forDependenciesOf(Set<ClassName> forDependencies) {
            this.classes = forDependencies;
            return this;
        }

        public Jdeps build() {

            final StringBuilder spaceDelimitedClasspath = new StringBuilder();
            for (JarFile jar : classpath) {
                spaceDelimitedClasspath.append(jar.getName()).append(" ");
            }

            final Jdeps newInstance = new Jdeps();
            newInstance.classpath = spaceDelimitedClasspath.toString().trim();

            final StringBuilder spaceDelimitedClasses = new StringBuilder();
            for (ClassName className : classes) {
                spaceDelimitedClasses.append(className.toString()).append(" ");
            }
            newInstance.classes = spaceDelimitedClasses.toString().trim();

            return newInstance;
        }
    }
}
