package org.codarama.diet.util.system.jdk.model;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.codarama.diet.util.system.jdk.JdkToolExecutor;

import java.util.Arrays;
import java.util.Set;

/**
 * Represents a call to a JDK /bin tool.
 * Intended to be used with {@link JdkToolExecutor}.
 *
 * For example, this would be a jdk tool:
 *   /opt/jdk/bin/javac -version
 *
 * This would not:
 *   /usr/bin/ls -l
 *
 * Created by Ayld on 6/11/15.
 */
public enum JdkToolCalls {

    JDEPS_VERBOSE_CLASSPATH("jdeps", "-v", "-cp"),
    JAVA_VERSION("java", "-version");

    private final String name;
    private final Set<String> args;

    private final JdkToolExecutor executor = JdkToolExecutor.newInstance();

    /**
     * Creates a new call.
     *
     * @param name the name of the tool, e.x.: java, javac, jdeps etc.
     * @param options options to pass to the shell, e.x.: -v, -version, -cp etc.
     * */
    private JdkToolCalls(String name, String... options) {
        this.name = name;
        this.args = Sets.newLinkedHashSet();
        this.args.addAll(Arrays.asList(options));
    }

    /**
     * Adds arguments to the shell command when calling the tool.
     * For example:
     *   "javac     -o      MyClass.class   MyClass.java"
     *      ^       ^          ^               ^
     *    tool   option     argument         argument
     *
     * @param args arguments to the tool call
     * */
    public JdkToolCalls withArgs(String... args) {
        this.args.addAll(Arrays.asList(args));
        return this;
    }

    /**
     * Executes the call to the OS shell.
     *
     * @return the ouput of the shell call, on its System.out stream
     * @throws RuntimeException for anything on System.err
     * */
    public String exec() {
        return this.executor.execShellCmd(this);
    }

    /**
     * @return the name of this call
     *         e.x.:
     *           for "java -version" this method would return "java"
     * */
    public String getName() {
        return this.name;
    }

    /**
     * Would return everything except the name of this call.
     * For example:
     *   for "javac -o Class.class Class.java" this method will return ["-o", "Class.class", "Class.java"]
     *
     * @return a set containing the options and arguments of this call
     * */
    public Set<String> getOptionsAndArgs() {
        return ImmutableSet.copyOf(this.args);
    }
}
