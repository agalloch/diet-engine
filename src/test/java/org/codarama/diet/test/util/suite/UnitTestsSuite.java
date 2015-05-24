package org.codarama.diet.test.util.suite;

import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.runner.RunWith;

/**
 * A suite for unit tests.
 * They don't need to be explicitly included as this is the default suite.
 * Test groups that are <i>not</i> unit tests should be explicitly excluded.
 *
 * Created by ayld on 5/24/2015.
 */
@RunWith(ClasspathSuite.class)
@ClasspathSuite.ExcludeBaseTypeFilter(IntegrationTest.class)
public class UnitTestsSuite {
}
