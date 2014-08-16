package org.codarama.diet.dependency.matcher.condition.impl;

import org.codarama.diet.dependency.matcher.condition.MatchingCondition;
import org.codarama.diet.model.ClassFile;
import org.codarama.diet.model.ClassName;

public class BinaryClassNameVsDependencyQualifiedName implements MatchingCondition{

	@Override
	public boolean satisfied(ClassName className, ClassFile classFile) {
		return className.equals(classFile.qualifiedName());
	}
}
