package org.codarama.diet.model;

import com.google.common.collect.ImmutableSet;
import org.codarama.diet.model.marker.Resolvable;
import org.codarama.diet.util.Tokenizer;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Meant to represent a fully qualified class name.
 * <p>
 * The idea behind the class is to provide a model object that makes sure that a string is actually a class name and not
 * something else, so one doesn't need to check 'by hand' every time.
 */
public final class ClassName implements Resolvable {

    /**
     * This regex only checks plausibility it doesn't check validity. For a validity check a comparison against Java's reserved words must be made.
     * Also checks whether the name contains only Unicode chars.
     * <p>
     * More info here:
     * http://docs.oracle.com/javase/specs/#3.8
     */
    private final static String CLASS_NAME_VALIDATION_REGEX = "([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*";


    /**
     * Class names that are valid classes, but still don't match the above regex.
     * <p>
     * See here:
     * http://docs.oracle.com/javase/7/docs/technotes/tools/solaris/javadoc.html#packagecomment
     */
    private final static Set<String> EXCEPTIONAL_CLASS_NAMES = ImmutableSet.of(
            "package-info"
    );

    public static final String INNER_CLASS_SEPARATOR = "$";

    private final String qualifiedClassName;

    /**
     * Creates a {@link ClassName}.
     * <p>
     * Doesn't allow nulls or empty strings.
     * Does everything it can to make sure that the argument is in fact a fully qualified class name and not something else.
     *
     * @param qualifiedClassName a class name
     */
    public ClassName(String qualifiedClassName) {
        if (!isClassName(qualifiedClassName)) {
            throw new IllegalArgumentException("invalid fully qualified class name: " + qualifiedClassName +
                    ", expected: " + CLASS_NAME_VALIDATION_REGEX + ", actual: " + qualifiedClassName);
        }
        this.qualifiedClassName = qualifiedClassName;
    }

    private static boolean isClassName(String qualifiedClassName) {
        final String shortName = Tokenizer.delimiter(".").tokenize(qualifiedClassName).lastToken();

        return Pattern.matches(CLASS_NAME_VALIDATION_REGEX, qualifiedClassName) || EXCEPTIONAL_CLASS_NAMES.contains(shortName);
    }

    /**
     * Returns the short name of the wrapped qualified class name.
     * <p>
     * <pre>
     *
     * In other words for this:
     *
     *   <code>org.codarama.diet.model.ClassName</code>
     *
     * this method will return:
     *
     *   <code>ClassName</code>
     *
     * <pre>
     *
     * @return the short name of the wrapped qualified class name
     */
    public String shortName() {
        return Tokenizer.delimiter(".").tokenize(qualifiedClassName).lastToken();
    }

    /**
     * Returns the wrapped qualifiedClassName.
     *
     * @return the wrapped qualifiedClassName
     */
    @Override
    public String toString() {
        return qualifiedClassName;
    }

    @Override
    public int hashCode() {
        return qualifiedClassName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof ClassName)) {
            return false;
        }

        final ClassName other = (ClassName) obj;

        return other.toString().equals(this.toString());
    }
}
