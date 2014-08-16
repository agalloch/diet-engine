package org.codarama.diet.bundle;

import java.io.IOException;
import java.util.Set;
import java.util.jar.JarFile;

import org.codarama.diet.model.ExplodedJar;

public interface JarExploder {
	
	public ExplodedJar explode(JarFile jar) throws IOException;
	
	public Set<ExplodedJar> explode(Set<JarFile> jar) throws IOException;
}
