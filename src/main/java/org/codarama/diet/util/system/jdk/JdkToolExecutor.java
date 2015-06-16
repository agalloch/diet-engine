package org.codarama.diet.util.system.jdk;

import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;
import org.codarama.diet.util.system.JdkSettings;
import org.codarama.diet.util.system.jdk.model.JdkToolCalls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

/**
 * Executes {@link JdkToolCalls} to the system shell.
 *
 * @see JdkToolCalls
 *
 * Created by Ayld on 6/11/15.
 */
public class JdkToolExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(JdkToolExecutor.class);

    private static final String OS_NAME_VAR_NAME = "os.name";
    private static final String WINDOWS_OS_NAME = "Windows";
    private static final String LINUX_OS_NAME = "Linux";
    // TODO Mac

    private static final String WINDOWS_EXECUTABLE_EXTENSION = "exe";

    private JdkToolExecutor() {
    }

    public static JdkToolExecutor newInstance() {
        return new JdkToolExecutor();
    }

    /**
     * Executes a {@link JdkToolCalls} to the system shell.
     *
     * @throws RuntimeException if anything goes wrong with the system call
     *         the actual error is passed to the exception
     *
     * @param call the call to execute
     * @return the output of the shell command, on its System.out stream
     *
     * @see JdkToolCalls
     * */
    public String execShellCmd(JdkToolCalls call) {
        String cmd = Joiner.on("/").join(JdkSettings.newInstance().getJdkBinDir(), call.getName());
        cmd = makeOsCompatible(cmd);

        final Set<String> optionsAndArgs = call.getOptionsAndArgs();

        if (LOG.isDebugEnabled()) {
            LOG.debug("executing: " + cmd + " " + optionsAndArgs);
        }

        final String[] args = optionsAndArgs.toArray(new String[optionsAndArgs.size()]);

        final String[] cmdAndArgs = new String[args.length + 1];
        cmdAndArgs[0] = cmd;
        System.arraycopy(args, 0, cmdAndArgs, 1, cmdAndArgs.length - 1);

        try {

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
            // since we don't currently know what exception might occur here we just show them,
            // in the future when we have more knowledge about shell executions we might revise this,
            // if we get interrupted this is probably due to cmd execution taking too long
            // there is not much we can do about it here except log it
            throw new RuntimeException("failed to execute command: " + cmd, e);
        }
    }

    /**
     * This method makes the system call to a Java /bin tool OS compatible.
     * Currently it:
     *   - adds .exe to binary calls for Windows
     *   - quotes whole command to workaround spaces in paths for Windows
     *   - removes preceding slash given by env vars on Linux
     * */
    private String makeOsCompatible(String cmd) {
        // XXX might consider polymorphism instead of forks here if this gets any bigger
        final String currentOsName = System.getProperty(OS_NAME_VAR_NAME);


        // Windows
        if (currentOsName.contains(WINDOWS_OS_NAME)) {
            return "\"" + Joiner.on(".").join(cmd, WINDOWS_EXECUTABLE_EXTENSION) + "\"";
        }

        // Linux
        else if (currentOsName.contains(LINUX_OS_NAME)) {

            if (cmd.startsWith("/")) {
                return cmd.replaceFirst("/", "");
            }
        }

        // no modification needed
        return cmd;
    }
}
