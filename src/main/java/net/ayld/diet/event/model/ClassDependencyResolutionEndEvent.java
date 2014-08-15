package net.ayld.diet.event.model;

import net.ayld.diet.component.ListenableComponent;

public class ClassDependencyResolutionEndEvent extends OperationEndEvent {

	public ClassDependencyResolutionEndEvent(String message, Class<? extends ListenableComponent> by) {
		super(message, by);
	}
}
