package org.codarama.diet.event.model;

import org.codarama.diet.component.ListenableComponent;

public class SourceDependencyResolutionStartEvent extends OperationStartEvent {

	public SourceDependencyResolutionStartEvent(String message, Class<? extends ListenableComponent> by) {
		super(message, by);
	}
}
