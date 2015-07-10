package org.codarama.diet.api;

import com.google.common.io.Resources;
import org.codarama.diet.test.util.suite.IntegrationTest;
import org.codarama.diet.util.Tokenizer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Brutally pits the minimizers against each other in a gory bloodbath battle to the death.
 *
 * @see {@link DefaultMinimizer}
 * @see {@link IndexedMinimizer}
 *
 * Created by Ayld on 7/10/15.
 */
public class ManualVsIndexedMinimizedTest implements IntegrationTest{

    private static final Logger LOG = LoggerFactory.getLogger(ManualVsIndexedMinimizedTest.class);

    @Test
    public void battle() throws IOException {
        final String pathToSources = toPath(Resources.getResource("test-classes/test-src-dir"));
        final String pathToLibraries = toPath(Resources.getResource("test-classes/test-lib-dir"));

        long startTime = System.currentTimeMillis();

        DefaultMinimizer
                .sources(pathToSources)
                .libs(pathToLibraries)
                .minimize()
                .getJar();

        long endTime = System.currentTimeMillis();
        LOG.info("manual minimizer finished in: " + ((endTime - startTime) / 1000 / 60) + " minutes (" + (endTime - startTime) / 1000 + " seconds)");

        startTime = System.currentTimeMillis();

        IndexedMinimizer
                .sources(pathToSources)
                .libs(pathToLibraries)
                .minimize()
                .getJar();

        endTime = System.currentTimeMillis();
        LOG.info("indexed minimizer finished in: " + ((endTime - startTime) / 1000 / 60) + " minutes (" + (endTime - startTime) / 1000 + " seconds)");
    }

    private String toPath(URL uri) {
        return Tokenizer.delimiter(":").tokenize(uri.toString()).lastToken();
    }
}
