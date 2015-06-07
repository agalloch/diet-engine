package org.codarama.diet.api;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarFile;

import org.codarama.diet.api.reporting.MinimizationReport;
import org.codarama.diet.api.reporting.MinimizationStatistics;
import org.codarama.diet.model.ClassName;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link DefaultMinimizer}.
 *
 * On an inappropriate side note:
 *   these tests might be a tad useless, but still ...
 *
 * Created by siliev on 15-6-5.
 */
public class MinimizerUnitTest {

   private Minimizer mockMinimizer;

   @Before
   public void init() throws IOException {
      final MinimizationStatistics mockStatistics = mock(MinimizationStatistics.class);
      when(mockStatistics.getMinimizedDependenciesCount()).thenReturn(10);
      when(mockStatistics.getSourceFilesCount()).thenReturn(200);
      when(mockStatistics.getTotalDependenciesCount()).thenReturn(1000);
      when(mockStatistics.getTotalExecutionTime()).thenReturn(5L * 60L * 1000L);

      final JarFile minimizationResult = mock(JarFile.class);
      when(minimizationResult.getName()).thenReturn("diet.jar");

      final MinimizationReport mockReport = mock(MinimizationReport.class);
      when(mockReport.getStatistics()).thenReturn(mockStatistics);
      when(mockReport.getJar()).thenReturn(minimizationResult);

      this.mockMinimizer = mock(DefaultMinimizer.class);
      when(this.mockMinimizer.libs(any(Set.class))).thenReturn(mockMinimizer);
      when(this.mockMinimizer.libs(any(String.class))).thenReturn(mockMinimizer);
      when(this.mockMinimizer.forceInclude(any(JarFile.class))).thenReturn(mockMinimizer);
      when(this.mockMinimizer.forceInclude(any(ClassName.class))).thenReturn(mockMinimizer);
      when(this.mockMinimizer.output(any(String.class))).thenReturn(mockMinimizer);
   }

//   @Test
   public void returnsNonNullMinimizationReport() throws IOException {
      final MinimizationReport report = this.mockMinimizer.minimize();
      assertNotNull("report is null", report);
   }
}
