package org.codarama.diet.model;

import com.google.common.base.Strings;

import java.io.File;
import java.util.Objects;
import java.util.jar.JarFile;

/** 
 * Represents an extracted (exploded) .jar file.
 * */
public class ExplodedJar {
	
	final String extractedPath;
	final JarFile archive;

	public ExplodedJar(String extractedPath, JarFile archive) {
		if (archive == null) {
			throw new IllegalArgumentException("null archive");
		}
		if (Strings.isNullOrEmpty(extractedPath)) {
			throw new IllegalArgumentException("extracted path is null or empty");
		}
		if (!isPath(extractedPath)) {
			throw new IllegalArgumentException("Directory at: " + extractedPath + ", does not exist or is not a directory");
		}
		this.extractedPath = extractedPath;
		this.archive = archive;
	}

	private static boolean isPath(String toCheck) {
		final File result = new File(toCheck);
		
		return result.exists() && result.isDirectory();
	}
	
	public JarFile getArchive() {
		return archive;
	}

	public String getExtractedPath() {
		return extractedPath;
	}

	@Override
	public String toString() {
		return getExtractedPath();
	}

	@Override
	public int hashCode() {
		return Objects.hash(extractedPath, archive);
    }

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		
		if (!(obj instanceof ExplodedJar)) {
			return false;
		}
		
		final ExplodedJar other = (ExplodedJar) obj;
		
		return 
				other.getArchive().equals(this.archive)
				&& 
				other.getExtractedPath().equals(this.extractedPath);
	}
}
