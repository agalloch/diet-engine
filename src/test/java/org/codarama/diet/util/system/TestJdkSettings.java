/* ***************************************************************************
 * Copyright 2014 VMware, Inc. All rights reserved. -- VMware Confidential
 * **************************************************************************
 */

package org.codarama.diet.util.system;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Tests {@link JdkSettings}.
 *
 * Created by siliev on 15-6-19.
 */
public class TestJdkSettings {

   private final JdkSettings jdkSettings = JdkSettings.newInstance();

   @Test
   public void returnsCorrectJdkBinDir() {

      final File expected = getJdkBinDir();
      final File actual = jdkSettings.getJdkBinDir();

      Assert.assertEquals(expected, actual);
   }

   @Test
   public void correctlyChecksForJdk18() {

      final String javaVersion = System.getProperty("java.version");
      if (Strings.isNullOrEmpty(javaVersion)) {
         Assert.fail();
      }

      final boolean expected = javaVersion.contains("1.8");
      final boolean actual = jdkSettings.isJDK18();

      Assert.assertEquals(expected, actual);
   }

   @Test
   public void correctlyChecksIfJdkInstalled() {
      final File jdkBinDir = getJdkBinDir();
      final String javac = Joiner.on(File.separator).join(jdkBinDir.getAbsolutePath(), "javac");

      final boolean expected = !Strings.isNullOrEmpty(javac);
      final boolean actual = jdkSettings.isJdkInstalled();

      Assert.assertEquals(expected, actual);
   }

   private File getJdkBinDir() {
      final String javaHomeDir = System.getenv("JAVA_HOME");

      // JAVA_HOME should point to a jdk
      // there is no formal way to decide whether JAVA_HOME points to JDK or a JRE
      // the general idea currently is for the check is:
      //   - make sure /jdkPath/bin contains javac
      // if above conditions are true we should be in a JDK
      if (Strings.isNullOrEmpty(javaHomeDir)) {
         Assert.fail("JAVA_HOME not set");
      }

      return new File(Joiner.on(File.separator).join(javaHomeDir, "bin"));
   }
}
