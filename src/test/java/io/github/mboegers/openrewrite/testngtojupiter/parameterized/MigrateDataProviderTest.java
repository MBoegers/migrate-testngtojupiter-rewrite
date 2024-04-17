/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package io.github.mboegers.openrewrite.testngtojupiter.parameterized;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class MigrateDataProviderTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.parser(JavaParser.fromJavaVersion()
            .logCompilationWarningsAndErrors(true)
            .classpath("junit-jupiter-api", "junit-jupiter-params", "testng"))
          .recipe(new MigrateDataProvider());
    }

    @Nested
    class Wrap {
        /*
         * annotation parameter other than name are ignored
         */
        @Test
        void withName() {
            @Language("java") String is = """
              import org.testng.annotations.DataProvider;
                            
              public class BoxPrimitiveDataProvider {
                  @DataProvider(name = "anotherBoxPrimitiveDataProvider")
                  public static Object[][] boxPrimitiveDataProvider() { /*...*/ }
              }
              """;
            @Language("java") String should = """
              import org.junit.jupiter.params.provider.Arguments;
                            
              import java.util.Arrays;
              import java.util.stream.Stream;
                            
              public class BoxPrimitiveDataProvider {
                  public static Object[][] boxPrimitiveDataProvider() { /*...*/ }
                            
                  public static Stream<Arguments> anotherBoxPrimitiveDataProviderSource() {
                      return Arrays.stream(boxPrimitiveDataProvider()).map(Arguments::of);
                  }
              }
              """;
            rewriteRun(java(is, should));
        }

        @Test
        void withDefaultName() {
            @Language("java") String is = """
              import org.testng.annotations.DataProvider;
                            
              public class BoxPrimitiveDataProvider {
                  @DataProvider
                  public static Object[][] boxPrimitiveDataProvider() { /*...*/ }
              }
              """;
            @Language("java") String should = """
              import org.junit.jupiter.params.provider.Arguments;
                            
              import java.util.Arrays;
              import java.util.stream.Stream;
                            
              public class BoxPrimitiveDataProvider {
                  public static Object[][] boxPrimitiveDataProvider() { /*...*/ }
                            
                  public static Stream<Arguments> boxPrimitiveDataProviderSource() {
                      return Arrays.stream(boxPrimitiveDataProvider()).map(Arguments::of);
                  }
              }
              """;
            rewriteRun(java(is, should));
        }
    }
}
