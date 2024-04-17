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

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class AddTestLifecycleToJUnitTestsTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.parser(JavaParser.fromJavaVersion()
            .logCompilationWarningsAndErrors(true)
            .classpath("junit-jupiter-api", "testng"))
          .recipe(new AddTestLifecyleToJUnitTests());
    }

    @DocumentExample
    @Test
    void addToJUnitTest() {
        //language=java
        rewriteRun(java(
          """
            import org.junit.jupiter.api.Test;
                           
            class MyTest {
                @Test
                void test() {}
            }
            """, """
            import org.junit.jupiter.api.Test;
            import org.junit.jupiter.api.TestInstance;
             
            @TestInstance(TestInstance.Lifecycle.PER_CLASS)
            class MyTest {
                @Test
                void test() {}
            }
            """));
    }

    @Test
    void addToNestedJUnitTest() {
        //language=java
        rewriteRun(java(
          """
            import org.junit.jupiter.api.Test;
                           
            class MyTest {
                class InnerTest {
                    @Test
                    void test() {}
                }
                
                class OtherClass {}
            }
            """, """
            import org.junit.jupiter.api.Test;
            import org.junit.jupiter.api.TestInstance;
             
            class MyTest {
                @TestInstance(TestInstance.Lifecycle.PER_CLASS)
                class InnerTest {
                    @Test
                    void test() {}
                }
             
                class OtherClass {}
            }
            """));
    }

    @Test
    void doNetReaddToJUnitTest() {
        //language=java
        rewriteRun(java(
          """
            import org.junit.jupiter.api.Test;
            import org.junit.jupiter.api.TestInstance;
             
            @TestInstance(TestInstance.Lifecycle.PER_CLASS)
            class MyTest {
                @Test
                void test() {}
            }
            """));
    }

    @Test
    void doNetReaddToTestNGTest() {
        //language=java
        rewriteRun(java(
          """
            import org.testng.annotations.Test;
                       
            class MyTest {
                @Test
                void test() {}
            }
            """));
    }
}
