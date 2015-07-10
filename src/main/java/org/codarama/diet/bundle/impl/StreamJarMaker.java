package org.codarama.diet.bundle.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.codarama.diet.bundle.JarMaker;
import org.codarama.diet.model.ClassFile;
import org.codarama.diet.model.ClassStream;

import java.io.*;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.regex.Pattern;

/**
 * A {@link JarMaker} that makes a {@link JarFile} from {@link ClassStream}s.
 *
 * Created by Ayld on 6/28/15.
 */
public class StreamJarMaker implements JarMaker<ClassStream> {

    private String facadeJarName = "diet.jar";

    @Override
    public JarFile zip(Set<ClassStream> files) throws IOException {

        makeOutputDir();

        final JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(new File(facadeJarName)));

        final Set<String> dirEntries = Sets.newHashSet();
        final Set<String> classEntries = Sets.newHashSet();
        for (ClassStream clazz : files) {
            final String simpleName = clazz.name().shortName();

            final String packages = clazz.name().toString().replaceAll(escapeRegexChars(simpleName), "");
            final StringBuilder packageDirs = new StringBuilder();

            for (String packageName : Splitter.on(".").split(packages)) {

                if (Strings.isNullOrEmpty(packageName)) {
                    continue;
                }
                packageDirs.append(packageName).append(File.separator);

                if (!dirEntries.contains(packageDirs.toString())) {
                    jarOut.putNextEntry(new JarEntry(packageDirs.toString()));
                }
                dirEntries.add(packageDirs.toString());
            }

            final String clazzFilename = Joiner.on(".").join(simpleName, ClassFile.EXTENSION);
            final String classEntry = packageDirs.append(clazzFilename).toString();

            if (!classEntries.contains(classEntry)) {
                jarOut.putNextEntry(new JarEntry(classEntry));

                final InputStream in = clazz.content();

                int len;
                final byte[] buf = new byte[1024];
                while ((len = in.read(buf)) > 0) {
                    jarOut.write(buf, 0, len);
                }

                jarOut.closeEntry();
                in.close();
            }
            classEntries.add(classEntry);
        }
        jarOut.close();

        return new JarFile(facadeJarName);
    }

    private String escapeRegexChars(String str) {
        return Pattern.quote(str);
    }

    private void makeOutputDir() {
        // ignoring result because it is ok for the parent to exist
        new File(new File(facadeJarName).getParent()).mkdirs();
    }

    public void setZippedJarName(String zippedJarName) {
        this.facadeJarName = zippedJarName;
    }
}
