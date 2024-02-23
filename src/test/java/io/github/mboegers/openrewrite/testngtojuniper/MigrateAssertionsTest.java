package io.github.mboegers.openrewrite.testngtojuniper;

import io.guthub.mboegers.openrewrite.testngtojuniper.MigrateAssertionsRecipes;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class MigrateAssertionsTest  implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.parser(JavaParser.fromJavaVersion()
            .logCompilationWarningsAndErrors(true)
            .classpath("junit-jupiter-api", "testng"))
          .recipe(new MigrateAssertionsRecipes());
    }

    @Test
    void replaceAssertEquals() {
        //language=java
        rewriteRun(java("""
                import org.testng.Assert;
                     
                class MyTest {
                    void testInt() {
                        int actual = 1;
                        int expected = 2;
                        
                        Assert.assertEquals(actual, expected);
                    }
                         
                    void testObject() {
                        Object actual = new Object();
                        Object expected = new Object();
                      
                        Assert.assertEquals(actual, expected);
                    }
                }
                ""","""
                import org.junit.jupiter.api.Assertions;
                
                class MyTest {
                    void testInt() {
                        int actual = 1;
                        int expected = 2;
                        
                        Assertions.assertEquals(expected, actual);
                    }
                         
                    void testObject() {
                        Object actual = new Object();
                        Object expected = new Object();
                     
                        Assertions.assertEquals(expected, actual);
                    }
                }
                """));
    }
}
