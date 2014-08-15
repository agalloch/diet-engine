package net.ayld.diet.dependency.matcher.condition.impl;

import com.google.common.io.Files;

import net.ayld.diet.dependency.matcher.condition.MatchingCondition;
import net.ayld.diet.model.ClassFile;
import net.ayld.diet.model.ClassName;

public class ClassShortNameVsClassFilename implements MatchingCondition{

	@Override
	public boolean satisfied(ClassName className, ClassFile classFile) {
		final String shortName = className.shortName();
		final String classFilenameNoExtension = Files.getNameWithoutExtension(classFile.physicalFile().getName());
		
		return shortName.toLowerCase().equals(classFilenameNoExtension.toLowerCase());
	}
}
