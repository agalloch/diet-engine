package org.codarama.diet.api;

import com.google.common.io.Resources;
import org.codarama.diet.api.reporting.listener.EventListener;
import org.codarama.diet.event.model.*;
import org.codarama.diet.test.util.suite.IntegrationTest;
import org.codarama.diet.util.Tokenizer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:META-INF/test-contexts/testListenerRegistrar.xml"})
public class RegistrarTest implements IntegrationTest {

    @Test
    public void jarExtractionUpdateCallCount() throws IOException {
        final Listener callCountListener = new Listener();

        ListenerRegistrar.register(callCountListener);

        IndexedMinimizer
                .sources(toPath(Resources.getResource("test-classes/test-src-dir")))
                .libs(toPath(Resources.getResource("test-classes/test-lib-dir")))
                .minimize();

        Assert.assertTrue(callCountListener.getCallCount() >= 3); // this is not too correct as the event bus is in
        // another thread and call count may vary
    }

    @Test
    public void parentSubscriptionShouldAlsoFireOnChildren() throws IOException {
        final SupertypeListener supertypeListener = new SupertypeListener();
        ListenerRegistrar.register(supertypeListener);

        IndexedMinimizer
                .sources(toPath(Resources.getResource("test-classes/test-src-dir")))
                .libs(toPath(Resources.getResource("test-classes/test-lib-dir")))
                .minimize();

        assertTrue("Listener not called for supertype", supertypeListener.gotSupertype);
        assertTrue("Listener not called for start subtype", supertypeListener.gotStartSubtype);
        assertTrue("Listener not called for end subtype", supertypeListener.gotEndSubtype);
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

    private static class Listener implements EventListener<OperationStartEvent> {

        private int callCount = 0;

        public int getCallCount() {
            return callCount;
        }

        @Override
        public void on(OperationStartEvent u) {
            callCount++;
        }
    }
}
