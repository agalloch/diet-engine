package org.codarama.diet.dependency.matcher.condition.impl;

import java.io.File;
import java.util.List;

import com.google.common.base.Joiner;

import org.codarama.diet.dependency.matcher.condition.MatchingCondition;
import org.codarama.diet.model.ClassFile;
import org.codarama.diet.model.ClassName;
import org.codarama.diet.util.Tokenizer;

public class ClassnamePackagesVsClassfilePath implements MatchingCondition{

	@Override
	public boolean satisfied(ClassName className, ClassFile classFile) {
		final List<String> classPackagesAndName = Tokenizer.delimiter(".").tokenize(className.toString()).tokens();
		final List<String> classPackagesNoName = classPackagesAndName.subList(0, classPackagesAndName.size() - 1);
		
		final String classPackages = Joiner.on("").join(classPackagesNoName);
		final String classfileFullPath = classFile.toString();
		
		// FIXME this is incorrect as it would match
		//
		// com.something.Bad
		// to
		// /home/user/extracted.jar/org/wrong/com/something/Bad.class
		//
		return classfileFullPath.replaceAll("\\" + File.separator, "").contains(classPackages);
	}
}
