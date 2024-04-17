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
