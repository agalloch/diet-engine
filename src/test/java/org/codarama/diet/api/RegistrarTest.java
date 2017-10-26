package org.codarama.diet.api;

import com.google.common.eventbus.EventBus;
import com.google.common.io.Resources;
import org.codarama.diet.api.reporting.listener.EventListener;
import org.codarama.diet.component.ListenableComponent;
import org.codarama.diet.event.model.*;
import org.codarama.diet.test.util.suite.IntegrationTest;
import org.codarama.diet.util.Components;
import org.codarama.diet.util.Tokenizer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class RegistrarTest implements IntegrationTest {

    private final EventBus statusUpdateEventBus = Components.EVENT_BUS.getInstance();

    @Test
    public void parentSubscriptionShouldAlsoFireOnChildren() throws IOException, InterruptedException {
        final SupertypeListener supertypeListener = new SupertypeListener();
        ListenerRegistrar.register(supertypeListener);

        statusUpdateEventBus.post(new MinimizationStartEvent("subStartTest", null));
        statusUpdateEventBus.post(new MinimizationEndEvent("subEndTest", null));
        statusUpdateEventBus.post(new MinimizationEvent("superTest", null));

        // XXX not ideal, but when testing API classes we can't inject a sync event bus
        // currently the async event bus doesn't post everything when expected
        Thread.sleep(2000);

        assertTrue("Listener not called for start subtype", supertypeListener.gotStartSubtype);
        assertTrue("Listener not called for end subtype", supertypeListener.gotEndSubtype);
        assertTrue("Listener not called for supertype", supertypeListener.gotSupertype);
        assertTrue("3 calls expected, but got: " + supertypeListener.callCount, supertypeListener.callCount == 3);
    }

    private String toPath(URL uri) {
        return Tokenizer.delimiter(":").tokenize(uri.toString()).lastToken();
    }

    private static class SupertypeListener implements EventListener<MinimizationEvent> {

        private int callCount = 0;

        private boolean gotSupertype = false;
        private boolean gotStartSubtype = false;
        private boolean gotEndSubtype = false;

        @Override
        public void on(MinimizationEvent event) {
            callCount++;
            if (!MinimizationEvent.class.isAssignableFrom(event.getClass())) {
                fail("Received event of type: " + event.getClass() + ", when not subscribed for this type");
            }
            if (event instanceof MinimizationStartEvent) {
                gotStartSubtype = true;
            } else if (event instanceof MinimizationEndEvent) {
                gotEndSubtype = true;
            } else {
                gotSupertype = true;
            }
        }
    }
}
