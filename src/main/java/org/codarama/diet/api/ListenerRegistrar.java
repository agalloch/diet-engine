package org.codarama.diet.api;

import com.google.common.annotations.Beta;
import org.codarama.diet.util.Components;

import com.google.common.eventbus.EventBus;

@Beta
public final class ListenerRegistrar {

	private final Object[] listeners;
	
	private ListenerRegistrar(Object[] listeners) {
		this.listeners = listeners;
	}

	public static ListenerRegistrar listeners(Object... listeners) {
		return new ListenerRegistrar(listeners);
	}
	
	public void register() {
		for (Object l : listeners) {
			Components.EVENT_BUS.<EventBus>getInstance().register(l);
		}
	}
}
