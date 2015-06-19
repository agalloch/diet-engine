/* ***************************************************************************
 * Copyright 2014 VMware, Inc. All rights reserved. -- VMware Confidential
 * **************************************************************************
 */

package org.codarama.diet.util.system.jdk.model;

import com.google.common.base.Strings;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link JdkToolCalls}.
 *
 * Created by siliev on 15-6-19.
 */
public class TestJdkToolCalls {

   private JdkToolCalls javaVersionCall = JdkToolCalls.JAVA_VERSION;

   @Test
   public void execDoesntEverReturnNull() {
      final String execResult = javaVersionCall.exec();

      Assert.assertFalse(Strings.isNullOrEmpty(execResult));
   }

   @Test
   public void returnsName() {
      final String javaVersionCallName = javaVersionCall.getName();

      Assert.assertFalse(Strings.isNullOrEmpty(javaVersionCallName));
   }

   @Test
   public void clearsArguments() {
      javaVersionCall = javaVersionCall.withArgs("1", "2", "3");

      Assert.assertTrue(javaVersionCall.getOptionsAndArgs() != null);
      Assert.assertFalse(javaVersionCall.getOptionsAndArgs().isEmpty());

      javaVersionCall.exec(); // ignore result

      Assert.assertTrue(javaVersionCall.getOptionsAndArgs() != null);
      Assert.assertTrue(javaVersionCall.getOptionsAndArgs().isEmpty());
   }
}
