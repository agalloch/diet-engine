package org.codarama.diet.dependency.matcher.impl;

import java.util.Set;

import org.codarama.diet.dependency.matcher.DependencyMatcherStrategy;
import org.codarama.diet.dependency.matcher.condition.MatchingCondition;
import org.codarama.diet.model.ClassFile;
import org.codarama.diet.model.ClassName;

import org.springframework.beans.factory.annotation.Required;

/**
 * A matcher strategy that requires all its conditions to be satisfied in order for its match method to return true.
 * */
public class UnanimousBasedDependencyMatcherStrategy implements DependencyMatcherStrategy{

	private Set<MatchingCondition> conditions;
	
	@Override
	public boolean matches(ClassName className, ClassFile classFile) {
		for (MatchingCondition condition : conditions) {
			
			if (!condition.satisfied(className, classFile)) {
				return false;
			}
			
		}
		return true;
	}
	
	@Required
	public void setConditions(Set<MatchingCondition> conditions) {
		this.conditions = conditions;
	}
}
