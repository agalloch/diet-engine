package net.ayld.diet.dependency.matcher.condition.impl;

import net.ayld.diet.dependency.matcher.condition.MatchingCondition;
import net.ayld.diet.model.ClassFile;
import net.ayld.diet.model.ClassName;

public class BinaryClassNameVsDependencyQualifiedName implements MatchingCondition{

	@Override
	public boolean satisfied(ClassName className, ClassFile classFile) {
		return className.equals(classFile.qualifiedName());
	}
}
