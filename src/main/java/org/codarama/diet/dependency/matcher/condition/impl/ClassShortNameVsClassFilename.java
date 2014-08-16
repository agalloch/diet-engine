package org.codarama.diet.dependency.matcher.condition.impl;

import com.google.common.io.Files;

import org.codarama.diet.dependency.matcher.condition.MatchingCondition;
import org.codarama.diet.model.ClassFile;
import org.codarama.diet.model.ClassName;

public class ClassShortNameVsClassFilename implements MatchingCondition{

	@Override
	public boolean satisfied(ClassName className, ClassFile classFile) {
		final String shortName = className.shortName();
		final String classFilenameNoExtension = Files.getNameWithoutExtension(classFile.physicalFile().getName());
		
		return shortName.toLowerCase().equals(classFilenameNoExtension.toLowerCase());
	}
}
