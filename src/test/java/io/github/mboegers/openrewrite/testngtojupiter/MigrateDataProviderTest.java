/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package io.github.mboegers.openrewrite.testngtojupiter;

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
    class WrapDataProvider {
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
                            
                  public static Stream<Arguments> anotherBoxPrimitiveDataProvider() {
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
                            
                  public static Stream<Arguments> boxPrimitiveDataProvider() {
                      return Arrays.stream(boxPrimitiveDataProvider()).map(Arguments::of);
                  }
              }
              """;
            rewriteRun(java(is, should));
        }

        @Test
        void doNothingWithoutAnnotation() {
            rewriteRun(java("""
              import org.testng.annotations.DataProvider;
                            
              public class BoxPrimitiveDataProvider {
                  public static Object[][] boxPrimitiveDataProvider() { /*...*/ }
              }
              """));
        }

    }

    @Nested
    class MigrateParameterToAnnotation {

        @Test
        void useWrappedMethod() {
            rewriteRun(
              java(
                """
                  import org.testng.annotations.Test;
                  import de.boeg.tst.BoxPrimitiveDataProvider;
                                  
                  public class HotSpotConstantReflectionProviderTest {
                      @Test(dataProvider = "boxPrimitiveDataProvider", dataProviderClass = BoxPrimitiveDataProvider.class)
                      public void testUnboxPrimitive(Object constant, Object expected) {/*...*/}
                  }
                  """, """
                  import org.junit.jupiter.params.ParameterizedTest;
                  import org.junit.jupiter.params.provider.MethodSource;
                                  
                  class BoxPrimitiveDataProvider {}
                                    
                  public class HotSpotConstantReflectionProviderTest {
                      @ParameterizedTest
                      @MethodSource("de.boeg.tst.BoxPrimitiveDataProvider#boxPrimitiveDataProviderSource")
                      public void testUnboxPrimitive(Object constant, Object expected) {/*...*/}
                  }
                  """
              ));
        }
    }
}
