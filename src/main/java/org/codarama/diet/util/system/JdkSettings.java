package org.codarama.diet.util.system;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.codarama.diet.util.system.jdk.model.JdkToolCalls;

import java.io.File;
import java.nio.file.Path;

/**
 * A utility generally intended for JdkSettings setting discovering.
 *
 * Created by Ayld on 6/11/15.
 */
public class JdkSettings {

    public static final String JAVA_COMPILER_EXECUTABLE_NAME = "javac";
    public static final String JAVA_EXECUTABLE_NAME = "java";
    public static final String JAVA_ROOT_PACKAGE = "java";

    private static final String JAVA_18_MAJOR_VERSION = "1.8";

    private static final String JDK_BIN_DIR_NAME = "bin";
    private static final String JDK_HOME_VAR_NAME = "JAVA_HOME";

    private JdkSettings() {
    }

    public boolean isJdkInstalled() {
        if (getJdkHomeVal() == null) {
            return false;
        }
        final String supposedJdkBin = getSupposedJdkBinDir();
        final File supposedJavac = new File(
                Joiner.on(File.separator).join(supposedJdkBin, JAVA_COMPILER_EXECUTABLE_NAME)
        );
        return supposedJavac.exists();
    }

    private String getJdkHomeVal() {
        return System.getenv(JDK_HOME_VAR_NAME);
    }

    private String getSupposedJdkBinDir() {
        return Joiner.on(File.separator).join(getJdkHomeVal(), JDK_BIN_DIR_NAME);
    }

    public File getJdkBinDir() {
        if (!isJdkInstalled()) {
            throw new IllegalStateException(JDK_HOME_VAR_NAME + " is not set, or is not pointing to a JDK assuming JDK not installed.\n" +
                    "Current " + JDK_HOME_VAR_NAME + " val is: " + getJdkHomeVal());
        }
        final String jdkBinPath = getSupposedJdkBinDir();
        return new File(jdkBinPath);
    }

    private File getJavaExecutable() {
        final File jdkBinDir = getJdkBinDir();
        final String javaExecutablePath = Joiner.on(File.separator).join(jdkBinDir, JAVA_EXECUTABLE_NAME);
        return new File(javaExecutablePath);
    }

    public boolean isJDK18 () {
        // call java -version
        final String javaVersionOutput = JdkToolCalls.JAVA_VERSION.exec();

        return isJdkInstalled() && javaVersionOutput.contains(JAVA_18_MAJOR_VERSION);
    }

    public static JdkSettings newInstance() {
        return new JdkSettings();
    }
}
