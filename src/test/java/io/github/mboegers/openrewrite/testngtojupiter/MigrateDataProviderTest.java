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
              package de.boeg.tst.provider;
                            
              import org.testng.annotations.DataProvider;
                            
              public class BoxPrimitiveDataProvider {
                  @DataProvider(name = "anotherBoxPrimitiveDataProvider")
                  public static Object[][] boxPrimitiveDataProvider() { /*...*/ }
              }
              """;
            @Language("java") String should = """
              package de.boeg.tst.provider;
                            
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
              package de.boeg.tst.provider;
              import org.testng.annotations.DataProvider;
                            
              public class BoxPrimitiveDataProvider {
                  @DataProvider
                  public static Object[][] boxPrimitiveDataProvider() { /*...*/ }
              }
              """;
            @Language("java") String should = """
              package de.boeg.tst.provider;
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

    }

    @Nested
    class NewTests {
        @Test
        void fullMigrate() {
            rewriteRun(
              java(
                """
                  package de.boeg.tst.provider;
                  import org.testng.annotations.DataProvider;
                            
                  public class BoxPrimitiveDataProvider {                   
                      @DataProvider
                      public static Object[][] boxPrimitiveDataProvider() { /*...*/ }
                  }
                  """,
                """
                  package de.boeg.tst.provider;
                  import org.junit.jupiter.params.provider.Arguments;
                                    
                  import java.util.Arrays;
                  import java.util.stream.Stream;
                              
                  public class BoxPrimitiveDataProvider {
                      public static Object[][] boxPrimitiveDataProvider() { /*...*/ }
                              
                      public static Stream<Arguments> boxPrimitiveDataProvider() {
                          return Arrays.stream(boxPrimitiveDataProvider()).map(Arguments::of);
                      }
                  }
                  """
              ),
              java(
                """
                  package de.boeg.tst.real;
                  import org.testng.annotations.Test;
                                    
                  import de.boeg.tst.provider.BoxPrimitiveDataProvider;
                                  
                  public class HotSpotConstantReflectionProviderTest {
                      @Test(dataProvider = "boxPrimitiveDataProvider", dataProviderClass = BoxPrimitiveDataProvider.class)
                      public void testUnboxPrimitive(Object constant, Object expected) {/*...*/}
                  }
                  """, """
                  package de.boeg.tst.real;
                  import org.junit.jupiter.params.ParameterizedTest;
                  import org.junit.jupiter.params.provider.MethodSource;
                  import org.testng.annotations.Test;
                                    
                  import de.boeg.tst.provider.BoxPrimitiveDataProvider;
                                    
                  public class HotSpotConstantReflectionProviderTest {
                      @Test
                      @ParameterizedTest
                      @MethodSource("de.boeg.tst.provider.BoxPrimitiveDataProvider#boxPrimitiveDataProvider")
                      public void testUnboxPrimitive(Object constant, Object expected) {/*...*/}
                  }
                  """
              ));
        }

        @Test
        void WrapOnlyDataprovider() {
            rewriteRun(
              java(
                """
                  package de.boeg.tst.provider;
                  import org.testng.annotations.DataProvider;
                            
                  public class BoxPrimitiveDataProvider {                   
                      @DataProvider
                      public static Object[][] boxPrimitiveDataProvider() { /*...*/ }
                  }
                  """,
                """
                  package de.boeg.tst.provider;
                  import org.junit.jupiter.params.provider.Arguments;
                                    
                  import java.util.Arrays;
                  import java.util.stream.Stream;
                              
                  public class BoxPrimitiveDataProvider {
                      public static Object[][] boxPrimitiveDataProvider() { /*...*/ }
                              
                      public static Stream<Arguments> boxPrimitiveDataProvider() {
                          return Arrays.stream(boxPrimitiveDataProvider()).map(Arguments::of);
                      }
                  }
                  """
              ),
              java(
                """
                  package de.boeg.tst.real;
                  import org.testng.annotations.Test;
                                    
                  import org.junit.jupiter.params.ParameterizedTest;
                  import org.junit.jupiter.params.provider.MethodSource;
                                    
                  import de.boeg.tst.provider.BoxPrimitiveDataProvider;
                                  
                  class BoxPrimitiveDataProvider {}
                                    
                  public class HotSpotConstantReflectionProviderTest {
                      @Test
                      @MethodSource("de.boeg.tst.provider.BoxPrimitiveDataProvider#boxPrimitiveDataProvider")
                      @ParameterizedTest
                      public void testUnboxPrimitive(Object constant, Object expected) {/*...*/}
                  }
                  """
              ));
        }

        @Test
        void addsParameterizedTest() {
            rewriteRun(
              java(
                """
                  package de.boeg.tst.provider;
                  import org.junit.jupiter.params.provider.Arguments;
                                    
                  import java.util.Arrays;
                  import java.util.stream.Stream;
                              
                  public class BoxPrimitiveDataProvider {
                      public static Object[][] boxPrimitiveDataProvider() { /*...*/ }
                              
                      public static Stream<Arguments> boxPrimitiveDataProvider() {
                          return Arrays.stream(boxPrimitiveDataProvider()).map(Arguments::of);
                      }
                  }
                  """
              ),
              java(
                """
                  package de.boeg.tst.real;
                  import org.testng.annotations.Test;
                  import org.junit.jupiter.params.provider.MethodSource;
                                    
                  import de.boeg.tst.provider.BoxPrimitiveDataProvider;
                                    
                  public class HotSpotConstantReflectionProviderTest {
                      @Test(dataProvider = "boxPrimitiveDataProvider", dataProviderClass = BoxPrimitiveDataProvider.class)
                      @MethodSource("de.boeg.tst.provider.BoxPrimitiveDataProvider#boxPrimitiveDataProvider")
                      public void testUnboxPrimitive(Object constant, Object expected) {/*...*/}
                  }
                  """, """
                  package de.boeg.tst.real;
                  import org.testng.annotations.Test;
                  import org.junit.jupiter.params.ParameterizedTest;
                  import org.junit.jupiter.params.provider.MethodSource;
                                    
                  import de.boeg.tst.provider.BoxPrimitiveDataProvider;
                                    
                  public class HotSpotConstantReflectionProviderTest {
                      @Test
                      @MethodSource("de.boeg.tst.provider.BoxPrimitiveDataProvider#boxPrimitiveDataProvider")
                      @ParameterizedTest
                      public void testUnboxPrimitive(Object constant, Object expected) {/*...*/}
                  }
                  """
              ));
        }

        @Test
        void addsMethodSource() {
            rewriteRun(
              java(
                """
                  package de.boeg.tst.provider;
                  import org.junit.jupiter.params.provider.Arguments;
                                    
                  import java.util.Arrays;
                  import java.util.stream.Stream;
                              
                  public class BoxPrimitiveDataProvider {
                      public static Object[][] boxPrimitiveDataProvider() { /*...*/ }
                              
                      public static Stream<Arguments> boxPrimitiveDataProvider() {
                          return Arrays.stream(boxPrimitiveDataProvider()).map(Arguments::of);
                      }
                  }
                  """
              ),
              java(
                """
                  package de.boeg.tst.real;
                  import org.testng.annotations.Test;
                                    
                  import org.junit.jupiter.params.ParameterizedTest;
                                    
                  import de.boeg.tst.provider.BoxPrimitiveDataProvider;
                                  
                  public class HotSpotConstantReflectionProviderTest {
                      @Test(dataProvider = "boxPrimitiveDataProvider", dataProviderClass = BoxPrimitiveDataProvider.class)
                      @ParameterizedTest
                      public void testUnboxPrimitive(Object constant, Object expected) {/*...*/}
                  }
                  """, """
                  package de.boeg.tst.real;
                  import org.junit.jupiter.params.provider.MethodSource;
                  import org.testng.annotations.Test;
                                    
                  import org.junit.jupiter.params.ParameterizedTest;
                                    
                  import de.boeg.tst.provider.BoxPrimitiveDataProvider;
                                    
                  public class HotSpotConstantReflectionProviderTest {
                      @Test
                      @ParameterizedTest
                      @MethodSource("de.boeg.tst.provider.BoxPrimitiveDataProvider#boxPrimitiveDataProvider")
                      public void testUnboxPrimitive(Object constant, Object expected) {/*...*/}
                  }
                  """
              ));
        }

        @Test
        void removesTestNgAnnotationArguments() {
            rewriteRun(
              java(
                """
                  package de.boeg.tst.provider;
                  import org.junit.jupiter.params.provider.Arguments;
                                    
                  import java.util.Arrays;
                  import java.util.stream.Stream;
                              
                  public class BoxPrimitiveDataProvider {
                      public static Object[][] boxPrimitiveDataProvider() { /*...*/ }
                              
                      public static Stream<Arguments> boxPrimitiveDataProvider() {
                          return Arrays.stream(boxPrimitiveDataProvider()).map(Arguments::of);
                      }
                  }
                  """
              ),
              java(
                """
                  package de.boeg.tst.real;
                  import org.testng.annotations.Test;
                                    
                  import org.junit.jupiter.params.ParameterizedTest;
                                    
                  import de.boeg.tst.provider.BoxPrimitiveDataProvider;
                                  
                  public class HotSpotConstantReflectionProviderTest {
                      @Test(dataProvider = "boxPrimitiveDataProvider", dataProviderClass = BoxPrimitiveDataProvider.class)
                      @ParameterizedTest
                      public void testUnboxPrimitive(Object constant, Object expected) {/*...*/}
                  }
                  """, """
                  package de.boeg.tst.real;
                  import org.junit.jupiter.params.provider.MethodSource;
                  import org.testng.annotations.Test;
                                    
                  import org.junit.jupiter.params.ParameterizedTest;
                                    
                  import de.boeg.tst.provider.BoxPrimitiveDataProvider;
                                  
                  public class HotSpotConstantReflectionProviderTest {
                      @Test
                      @ParameterizedTest
                      @MethodSource("de.boeg.tst.provider.BoxPrimitiveDataProvider#boxPrimitiveDataProvider")
                      public void testUnboxPrimitive(Object constant, Object expected) {/*...*/}
                  }
                  """
              ));
        }

        @Test
        void doNothingIfNameMissing() {
            rewriteRun(
              java(
                """
                  package de.boeg.tst.provider;
                  import org.junit.jupiter.params.provider.Arguments;
                                    
                  import java.util.Arrays;
                  import java.util.stream.Stream;
                              
                  public class BoxPrimitiveDataProvider {
                      public static Object[][] boxPrimitiveDataProvider() { /*...*/ }
                              
                      public static Stream<Arguments> boxPrimitiveDataProvider() {
                          return Arrays.stream(boxPrimitiveDataProvider()).map(Arguments::of);
                      }
                  }
                  """
              ),
              java(
                """
                  package de.boeg.tst.real;
                  import org.testng.annotations.Test;
                                  
                  public class HotSpotConstantReflectionProviderTest {
                      @Test(enabled = false)
                      public void testUnboxPrimitive(Object constant, Object expected) {/*...*/}
                  }
                  """
              ));
        }
    }
}
