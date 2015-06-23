package org.codarama.diet.dependency.resolver.impl;

import java.io.IOException;
import java.util.Set;

import org.codarama.diet.component.ListenableComponent;
import org.codarama.diet.dependency.resolver.DependencyResolver;
import org.codarama.diet.event.model.ClassDependencyResolutionEndEvent;
import org.codarama.diet.event.model.ClassDependencyResolutionStartEvent;
import org.codarama.diet.model.ClassFile;
import org.codarama.diet.model.ClassName;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.DescendingVisitor;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.JavaClass;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.codarama.diet.model.ClassStream;
import org.springframework.beans.factory.annotation.Required;

public class ClassFileDependencyResolver extends ListenableComponent implements DependencyResolver<ClassFile>{

    private DependencyResolver<ClassStream> classStreamResolver;

	@Override
	public Set<ClassName> resolve(ClassFile classFile) throws IOException {
        return classStreamResolver.resolve(classFile.stream());
	}
	
	@Override
	public Set<ClassName> resolve(Set<ClassFile> classFiles) throws IOException {
		final Set<ClassName> result = Sets.newHashSet();

		for (ClassFile classFile : classFiles) {
			result.addAll(resolve(classFile));
		}

		return result;
	}

    @Required
    public void setClassStreamResolver(DependencyResolver<ClassStream> classStreamResolver) {
        this.classStreamResolver = classStreamResolver;
    }
}
