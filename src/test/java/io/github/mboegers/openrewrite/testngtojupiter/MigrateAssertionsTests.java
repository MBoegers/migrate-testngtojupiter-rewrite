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
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

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
        @Nested
        class WithFailMessage {
            @Test
            void migrateEqualsInt() {
                //language=java
                rewriteRun(java("""
                  import org.testng.Assert;
                         
                    class MyTest {
                        void testInt() {
                            int actual = 1;
                            int expected = 2;
                            
                            Assert.assertEquals(actual, expected, "Test failed badly");
                        }
                    }
                     """,
                  """
                    import org.junit.jupiter.api.Assertions;
                 
                     class MyTest {
                        void testInt() {
                            int actual = 1;
                            int expected = 2;
                                         
                            Assertions.assertEquals(expected, actual, "Test failed badly");
                        }
                    }
                    """));
            }
        }

        @Nested
        class WithoutFailMessage {
            @Test
            void migrateEqualsInt() {
                //language=java
                rewriteRun(java("""
                  import org.testng.Assert;
                         
                    class MyTest {
                        void testInt() {
                            int actual = 1;
                            int expected = 2;
                            
                            Assert.assertEquals(actual, expected);
                        }
                    }
                     """,
                  """
                    import org.junit.jupiter.api.Assertions;
                 
                     class MyTest {
                        void testInt() {
                            int actual = 1;
                            int expected = 2;
                                         
                            Assertions.assertEquals(expected, actual);
                        }
                    }
                    """));
            }
        }
    }
}
