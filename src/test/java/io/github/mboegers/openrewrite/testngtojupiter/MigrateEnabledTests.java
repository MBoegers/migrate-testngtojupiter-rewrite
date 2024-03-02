package io.github.mboegers.openrewrite.testngtojupiter;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class MigrateEnabledTests implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.parser(JavaParser.fromJavaVersion()
            .logCompilationWarningsAndErrors(true)
            .classpath("junit-jupiter-api", "testng"))
          .recipe(new MigrateEnabledArgument());
    }

    @Test
    @DocumentExample
    void enabledFalse() {
        //language=java
        rewriteRun(java("""
          import org.testng.annotations.Test;
                         
          class MyTest {
              @Test(enabled = false)
              void test() {}
          }
          """, """
          import org.junit.jupiter.api.Disabled;
          import org.testng.annotations.Test;
           
          class MyTest {
              @Disabled
              @Test
              void test() {}
          }
          """));
    }

    @Test
    void enabledTrue() {
        //language=java
        rewriteRun(java("""
          import org.testng.annotations.Test;
                         
          class MyTest {
              @Test(enabled = true)
              void test() {}
          }
          """, """
          import org.testng.annotations.Test;
           
          class MyTest {
              @Test
              void test() {}
          }
          """));
    }

    @Test
    void enabledDefault() {
        //language=java
        rewriteRun(java("""
          import org.testng.annotations.Test;
                         
          class MyTest {
              @Test()
              void test() {}
          }
          """));
    }

    @Test
    void enabledDefaultNoBrace() {
        //language=java
        rewriteRun(java("""
          import org.testng.annotations.Test;
                         
          class MyTest {
              @Test
              void test() {}
          }
          """));
    }
}
