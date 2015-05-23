package org.codarama.diet.dependency.resolver.impl;

import java.io.IOException;
import java.util.Set;

import org.codarama.diet.component.ListenableComponent;
import org.codarama.diet.dependency.resolver.DependencyResolver;
import org.codarama.diet.event.model.SourceDependencyResolutionEndEvent;
import org.codarama.diet.event.model.SourceDependencyResolutionStartEvent;
import org.codarama.diet.model.ClassName;
import org.codarama.diet.model.SourceFile;
import org.codarama.diet.util.Tokenizer;
import org.codarama.diet.util.annotation.ThreadSafe;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;

@ThreadSafe
// TODO Use JavaParser instead of parsing manually
public class ManualParseSourceDependencyResolver extends ListenableComponent implements DependencyResolver<SourceFile>{
	
	@Override
	public Set<ClassName> resolve(SourceFile source) throws IOException {
		eventBus.post(new SourceDependencyResolutionStartEvent("resolving: " + source.physicalFile().getAbsolutePath(), this.getClass()));
		
		final String sourceFileContent = Resources.toString(source.physicalFile().toURI().toURL(), Charsets.UTF_8);
		
		// we can somehow select only lines starting with import so we don't need to iterate over every single line
		final Set<ClassName> result = Sets.newHashSet();
		for (String line : Splitter.on("\n").split(sourceFileContent)) {
			
			// we reached class definition, no point to loop any further
			final String publicClassDefinition = Joiner.on(" ").join(SourceFile.PUBLIC_KEYWORD, SourceFile.CLASS_KEYWORD);
			if (line.startsWith(publicClassDefinition) || line.startsWith(SourceFile.CLASS_KEYWORD)) {
				break;
			}
			
			if (line.startsWith(SourceFile.IMPORT_KEYWORD)) {
				
				if (line.endsWith(SourceFile.WILDCARD_IMPORT_SUFFIX)) {
					throw new IllegalArgumentException("wildcard imports: " + line + ", not currently supported");
				}
				
				final String dependency = Tokenizer.delimiter(" ").tokenize(line).lastToken()
										.replaceAll(";", "") // remove semicolon at end of imports
										.replaceAll("\r", ""); // remove windows newline chars
				result.add(new ClassName(dependency));
			}
		}
		
		eventBus.post(new SourceDependencyResolutionEndEvent("resolved: " + result, this.getClass()));
		
		return result;
	}

	@Override
	public Set<ClassName> resolve(Set<SourceFile> sources) throws IOException {
		final Set<ClassName> result = Sets.newHashSet();
		
		for (SourceFile source : sources) {
			result.addAll(resolve(source));
		}
		
		return result;
	}
}
