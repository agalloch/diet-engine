package org.codarama.diet.event.model;

import org.codarama.diet.component.ListenableComponent;

public class SourceDependencyResolutionEndEvent extends OperationEndEvent {

	public SourceDependencyResolutionEndEvent(String message, Class<? extends ListenableComponent> by) {
		super(message, by);
	}
}
