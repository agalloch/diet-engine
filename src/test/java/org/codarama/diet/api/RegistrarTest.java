package org.codarama.diet.api;

import com.google.common.eventbus.Subscribe;
import com.google.common.io.Resources;
import org.codarama.diet.event.model.OperationStartEvent;
import org.codarama.diet.test.util.suite.IntegrationTest;
import org.codarama.diet.util.Tokenizer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.net.URL;

@Category(IntegrationTest.class)
public class RegistrarTest implements IntegrationTest{

	@Test
	public void jarExtractionUpdateCallCount() throws IOException {
		final Listener callCountListener = new Listener();

		ListenerRegistrar.listeners(callCountListener).register();

		IndexedMinimizer.sources(toPath(Resources.getResource("test-classes/test-src-dir")))
				.libs(toPath(Resources.getResource("test-classes/test-lib-dir"))).minimize();

		Assert.assertTrue(callCountListener.getCallCount() >= 3); // this is not too correct as the event bus is in
																	// another thread and call count may vary
	}

	private String toPath(URL uri) {
		return Tokenizer.delimiter(":").tokenize(uri.toString()).lastToken();
	}

	public static class Listener {

		private int callCount = 0;

		@Subscribe
		public void listen(OperationStartEvent u) {
			callCount++;
		}

		public int getCallCount() {
			return callCount;
		}
	}
}
