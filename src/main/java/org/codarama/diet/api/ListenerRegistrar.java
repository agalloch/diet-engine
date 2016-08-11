package org.codarama.diet.api;

import com.google.common.annotations.Beta;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.codarama.diet.api.reporting.listener.EventListener;
import org.codarama.diet.event.model.ComponentEvent;
import org.codarama.diet.util.Components;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Beta
public final class ListenerRegistrar {

    private ListenerRegistrar() {
    }

    public static <E extends ComponentEvent> void register(EventListener<E> listener) {
        Components.EVENT_BUS.<EventBus>getInstance().register(new GuavaListener<>(listener));
    }

    private static class GuavaListener<E extends ComponentEvent> {
        private final EventListener<E> listener;

        private GuavaListener(EventListener<E> listener) {
            this.listener = listener;
        }

        @Subscribe
        private void on(E event) {
            final Class listenerGenericType = getListenerGenericType();
            final Class eventGenericType = event.getClass();

            final boolean listenerTypeIsSuperclassOfEventType = listenerGenericType.isAssignableFrom(eventGenericType);
            if (listenerTypeIsSuperclassOfEventType) {
                listener.on(event);
            }
        }

        private Class getListenerGenericType() {
            final ParameterizedType listenerGenericType = (ParameterizedType) listener.getClass().getGenericInterfaces()[0];
            final String listenerGenericTypeName = listenerGenericType.getActualTypeArguments()[0].getTypeName();
            try {
                return Class.forName(listenerGenericTypeName);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Unknown generic type for listener: " + listener.getClass());
            }
        }
    }
}
