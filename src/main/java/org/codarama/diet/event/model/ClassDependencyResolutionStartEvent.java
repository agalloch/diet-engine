package org.codarama.diet.event.model;

import org.codarama.diet.component.ListenableComponent;

public class ClassDependencyResolutionStartEvent extends OperationStartEvent {

	public ClassDependencyResolutionStartEvent(String message, Class<? extends ListenableComponent> by) {
		super(message, by);
	}
}
