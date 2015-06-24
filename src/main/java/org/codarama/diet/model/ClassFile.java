package org.codarama.diet.model;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.apache.bcel.classfile.ClassParser;
import org.codarama.diet.dependency.resolver.DependencyResolver;
import org.codarama.diet.model.marker.Packagable;
import org.codarama.diet.model.marker.Resolvable;
import org.codarama.diet.util.Components;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Set;

/** 
 * Meant to represent a compiled binary .class file on the file system.
 * 
 * This class tries to do everything it can to make sure it wraps a file that is actually a Java .class file.
 * It does this by parsing the binary .class file and checking whether they match the JVM .class file specifications.
 * 
 * More info here:
 *   http://en.wikipedia.org/wiki/Java_class_file
 * */
public class ClassFile implements Resolvable, Packagable { // XXX magic numbers
	
	public static final String EXTENSION = "class";
	
	private Set<ClassName> dependencies;
	
	private final File classFile;
	private final ClassName qualifiedName;

	private ClassFile(File classfile) { // XXX copy code
		try {
			this.classFile = classfile;
			this.qualifiedName = new ClassName(new ClassParser(classFile.getAbsolutePath()).parse().getClassName());
			
		} catch (IOException e) {
			// assuming bcel threw IOException because validation failed
			throw new IllegalArgumentException("file: " + classfile.getAbsolutePath() + ", not valid or is not a class file", e);
		}
	}
	
	
	/** 
	 * Returns the qualified class name of this ClassFile.
	 * 
	 * @see {@link ClassName}
	 * */
	public ClassName qualifiedName() {
		return qualifiedName;
	}
	
	/** 
	 * Creates a {@link ClassFile} from a file on the classpath rather than the file system.
	 * Checks whether the given file is actually a class file.
	 * 
	 * @param path path to the .class resource
	 * 
	 * @return a new {@link ClassFile}
	 * 
	 * @throws IllegalArgumentException if the file is not found or the file is not a class file
	 * */
	public static ClassFile fromClasspath(String path) {
		try {
			
			return new ClassFile(new File(Resources.getResource(path).toURI()));
			
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("can not find a file at: " + path);
		}
	}
	
	/** 
	 * Creates a {@link ClassFile} from a path, checking whether the given file is actually a class file.
	 * 
	 * @param path path to a .class file on the file system
	 *             preceding slash will be interpreted as an absolute path
	 *             no preceding slash will be interpreted as a relative path
	 * 
	 * @return a new {@link ClassFile}
	 * 
	 * @throws IllegalArgumentException if the file is not found or the file is not a class file
	 * */
	public static ClassFile fromFilepath(String path) {
		return new ClassFile(new File(path));
	}

	/**
	 * Creates a {@link ClassFile} from a {@link File}, checking whether the given file is actually a class file.
	 *
	 * @param classFile a compiled Java class file on the file system
	 * @return a new {@link ClassFile}
	 * @throws IllegalArgumentException if the file is not found or the file is not a class file
	 * */
	public static ClassFile fromFile(File classFile) {
		if (classFile == null) {
			throw new IllegalArgumentException("null argument not allowed");
		}
		return new ClassFile(classFile);
	}
	
	/**
	 * Returns the dependencies of this {@link ClassFile}
	 * */
	public Set<ClassName> dependencies() {
		final DependencyResolver<ClassFile> classDependencyResolver = Components.CLASS_DEPENDENCY_RESOLVER.getInstance();
		if (dependencies == null) {
			try {
				
				dependencies = classDependencyResolver.resolve(this);
				
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return ImmutableSet.copyOf(dependencies);
	}
	
	/** 
	 * Returns the wrapped class file as a {@link File}.
	 * 
	 * @return the wrapped class file as a {@link File}.
	 * */
	public File physicalFile() {
		return new File(classFile.getAbsolutePath());
	}

    public ClassStream stream() {
        final BufferedInputStream fileInputStream;
        try {
            fileInputStream = new BufferedInputStream(new FileInputStream(classFile));
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("could not find file: " + classFile.getAbsolutePath());
        }
        return ClassStream.fromStream(fileInputStream);
    }

	/** 
	 * Returns the path to the wrapped {@link File}.
	 * */
	@Override
	public String toString() {
		return classFile.getAbsolutePath();
	}

	@Override
	public int hashCode() {
		return classFile.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		
		if (!(obj instanceof ClassFile)) {
			return false;
		}
		
		final ClassFile other = (ClassFile) obj;
		
		return other.physicalFile().equals(this.physicalFile());
	}

    public static boolean isClassfile(File child) {
        return Files.getFileExtension(child.getName()).equals(EXTENSION);
    }
}
