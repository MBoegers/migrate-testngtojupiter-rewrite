/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.mboegers.openrewrite.testngtojuniper;

import io.guthub.mboegers.openrewrite.testngtojuniper.MigrateTestAnnotation;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class MigrateAnnotationsTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.parser(JavaParser.fromJavaVersion()
          .logCompilationWarningsAndErrors(true)
          .classpath("junit-jupiter-api", "testng"))
          .recipe(new MigrateTestAnnotation());
    }

    @Test
    void replaceEmptyTest() {
        //language=java
        rewriteRun(java("""
               import org.testng.annotations.Test;
               
               class MyTest {
                   @Test
                   void test() {}
               }
                ""","""
                import org.junit.jupiter.api.Test;
                
                class MyTest {
                    @Test
                    void test() {}
                }
                """));
    }
}
