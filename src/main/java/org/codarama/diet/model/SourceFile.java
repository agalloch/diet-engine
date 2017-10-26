package org.codarama.diet.model;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import org.codarama.diet.dependency.resolver.DependencyResolver;
import org.codarama.diet.model.marker.Resolvable;
import org.codarama.diet.util.Components;
import org.codarama.diet.util.Tokenizer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;

/**
 * Represents a Java source file.
 * <p>
 * Upon creation this class parses the Java source it is being created from and validates it's correctness. Throws
 * {@link IllegalArgumentException} if the source file fails validation.
 */
public class SourceFile implements Resolvable {

    public static final String EXTENSION = "java";
    public static final String PACKAGE_KEYWORD = "package";
    public static final String IMPORT_KEYWORD = "import";
    public static final String WILDCARD_IMPORT_SUFFIX = "*";
    public static final String CLASS_KEYWORD = "class";
    public static final String PUBLIC_KEYWORD = "public";
    public static final String COMMENT_START = "//";
    public static final String BLOCK_COMMENT_START = "/*";

    private Set<ClassName> dependencies;
    private final File source;

    private SourceFile(File sourceFile) {
        try {
            if (!isSourceFile(sourceFile)) {
                throw new IllegalArgumentException(sourceFile + " does not look like Java source");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        this.source = sourceFile;
    }

    /**
     * Creates a {@link SourceFile} from the given absolute file path. The path should point to a valid .java source
     * file. The given file will be validated on creation and a {@link IllegalArgumentException} will be thrown if the
     * validation fails.
     *
     * @param absPath absolute path to a .java source file
     * @return a {@link SourceFile}
     */
    public static SourceFile fromFilepath(String absPath) {
        return new SourceFile(new File(absPath));
    }

    /**
     * Creates a {@link SourceFile} from the given relative path. The given path
     * should be part of your classpath and point to a valid .java source file.
     * The given file will be validated on creation and a
     * {@link IllegalArgumentException} will be thrown if the validation fails.
     *
     * @param path relative path to a .java source file on the classpath
     * @return a {@link SourceFile}
     */
    public static SourceFile fromClasspath(String path) {
        try {

            return new SourceFile(new File(Resources.getResource(path).toURI()));

        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("can not find a file at: " + path);
        }
    }

    /**
     * Creates a {@link SourceFile} from the given {@link File}. The given file
     * should be a valid .java source file, otherwise an
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param file a .java source file
     * @return a {@link SourceFile}
     */
    public static SourceFile fromFile(File file) {
        if (file == null) {
            throw new IllegalArgumentException("can't create a SourceFile from a null file");
        }
        return fromFilepath(file.getAbsolutePath());
    }

    /**
     * Returns the dependencies of this {@link SourceFile}
     */
    public Set<ClassName> dependencies() {
        final DependencyResolver<SourceFile> sourceDependencyResolver = Components.SOURCE_DEPENDENCY_RESOLVER
                .getInstance();
        if (dependencies == null) {
            try {

                dependencies = sourceDependencyResolver.resolve(this);

            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return ImmutableSet.copyOf(dependencies);
    }

    public File physicalFile() {
        return new File(source.getAbsolutePath());
    }

    private static boolean isSourceFile(File sourceFile) throws IOException {
        if (sourceFile == null) {
            return false;
        }

        final String name = sourceFile.getName();
        final String extension = Tokenizer.delimiter(".").tokenize(name).lastToken();

        if (!extension.equals(EXTENSION)) {
            return false;
        }

        try {
            // [tmateev] I trusted them for saying this is fast, but we might want to measure how fast it really is
            JavaParser.parse(sourceFile);
        } catch (ParseException e) {
            // assuming parse failed because file is not a valid Java source file
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return source.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof SourceFile)) {
            return false;
        }

        final SourceFile other = (SourceFile) obj;

        return other.physicalFile().equals(this.source);
    }
}
