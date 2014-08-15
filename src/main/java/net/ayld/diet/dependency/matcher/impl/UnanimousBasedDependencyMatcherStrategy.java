package net.ayld.diet.dependency.matcher.impl;

import java.util.Set;

import net.ayld.diet.dependency.matcher.DependencyMatcherStrategy;
import net.ayld.diet.dependency.matcher.condition.MatchingCondition;
import net.ayld.diet.model.ClassFile;
import net.ayld.diet.model.ClassName;

import org.springframework.beans.factory.annotation.Required;

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
