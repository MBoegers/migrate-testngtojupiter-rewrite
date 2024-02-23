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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.testng.Assert;

import static org.openrewrite.java.Assertions.java;

class MigrateAssertionsTests implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.parser(JavaParser.fromJavaVersion()
            .logCompilationWarningsAndErrors(true)
            .classpath("junit-jupiter-api", "testng"))
          .recipe(new MigrateAssertionsRecipes());
    }

    @Nested
    class MigrateAssertEquals {
        @ParameterizedTest
        @ValueSource(strings = {
          "boolean", "boolean[]",
          "byte", "byte[]",
          "char", "char[]",
          "double", "double[]",
          "float", "float[]",
          "int", "int[]",
          "long", "long[]",
          "short", "short[]",
          "java.lang.Boolean", "java.lang.Boolean[]",
          "java.lang.Character", "java.lang.Character[]",
          "java.lang.Double", "java.lang.Double[]",
          "java.lang.Float", "java.lang.Float[]",
          "java.lang.Integer", "java.lang.Integer[]",
          "java.lang.String", "java.lang.String[]",
          "java.util.Map<?,?>", "java.util.Set<?>"
        })
        void withErrorMessage(String type) {
            //language=java
            rewriteRun(java("""
                    import org.testng.Assert;
                         
                    class MyTest {
                        void testMethod() {
                            %s actual;
                            %s expected;
                          
                            Assert.assertEquals(actual, expected, "Test failed badly");
                        }
                    }
                    """.formatted(type, type), """
                    import org.junit.jupiter.api.Assertions;
                 
                    class MyTest {
                        void testMethod() {
                            %s actual;
                            %s expected;
                                         
                            Assertions.assertEquals(expected, actual, "Test failed badly");
                        }
                    }
                    """.formatted(type, type)));
        }

        @ParameterizedTest
        @ValueSource(strings = {
          "boolean", "boolean[]",
          "byte", "byte[]",
          "char", "char[]",
          "double", "double[]",
          "float", "float[]",
          "int", "int[]",
          "long", "long[]",
          "short", "short[]",
          "java.lang.Boolean", "java.lang.Boolean[]",
          "java.lang.Character", "java.lang.Character[]",
          "java.lang.Double", "java.lang.Double[]",
          "java.lang.Float", "java.lang.Float[]",
          "java.lang.Integer", "java.lang.Integer[]",
          "java.lang.String", "java.lang.String[]",
          "java.util.Map<?,?>", "java.util.Set<?>"
        })
        void withoutErrorMessage(String type) {
            //language=java
            rewriteRun(java("""
                    import org.testng.Assert;
                         
                    class MyTest {
                        void testMethod() {
                            %s actual;
                            %s expected;
                          
                            Assert.assertEquals(actual, expected);
                        }
                    }
                    """.formatted(type, type), """
                    import org.junit.jupiter.api.Assertions;
                 
                    class MyTest {
                        void testMethod() {
                            %s actual;
                            %s expected;
                                         
                            Assertions.assertEquals(expected, actual);
                        }
                    }
                    """.formatted(type, type)));
        }
    }


}
