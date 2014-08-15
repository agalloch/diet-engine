package net.ayld.diet.event.model;

import net.ayld.diet.component.ListenableComponent;

public class ClassDependencyResolutionStartEvent extends OperationStartEvent {

	public ClassDependencyResolutionStartEvent(String message, Class<? extends ListenableComponent> by) {
		super(message, by);
	}
}
