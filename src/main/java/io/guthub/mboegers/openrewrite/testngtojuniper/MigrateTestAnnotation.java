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
package io.guthub.mboegers.openrewrite.testngtojuniper;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.*;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaCoordinates;

import java.util.Optional;

@Value
@EqualsAndHashCode(callSuper = false)
public class MigrateTestAnnotation extends Recipe {

    @Override
    public String getDisplayName() {
        return "Replace @Test Annotation";
    }

    @Override
    public String getDescription() {
        return "Replace @org.testng.annotations.Test with Juniper equivalents.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new ReplaceTestAnnotationVisitor();
    }

    class ReplaceTestAnnotationVisitor extends JavaIsoVisitor<ExecutionContext> {

        private static final String JUNIPER_TEST = "org.junit.jupiter.api.Test";
        private static final String TESTNG_TEST_FQN = "org.testng.annotations.Test";
        private final AnnotationMatcher TESTNG_TEST_MATCHER = new AnnotationMatcher("@" + TESTNG_TEST_FQN);
        private final AnnotationMatcher JUNIPER_TEST_MATCHER = new AnnotationMatcher("@" + JUNIPER_TEST);

        class TestNgInfos {
            final String[] groups;

            final Boolean enabled;

            final String[] dependsOnGroups;

            final String[] dependsOnMethods;

            final Long timeOut;

            final Long invocationTimeOut;

            final Integer invocationCoun;

            final Integer threadPoolSiz;

            final Integer successPercentage;

            final String dataProvider;

            final Class<?> dataProviderClass;

            final String dataProviderDynamicClass;

            final Boolean alwaysRun;

            final String description;

            final Class[] expectedExceptions;

            final String expectedExceptionsMessageRegExp;

            final String suiteName;

            final String testName;

            final Boolean singleThreaded;

            final Boolean skipFailedInvocations;

            final Boolean ignoreMissingDependencies;

            final Integer priorit;

            TestNgInfos(String[] groups, Boolean enabled, String[] dependsOnGroups, String[] dependsOnMethods, Long timeOut, Long invocationTimeOut, Integer invocationCoun, Integer threadPoolSiz, Integer successPercentage, String dataProvider, Class<?> dataProviderClass, String dataProviderDynamicClass, Boolean alwaysRun, String description, Class[] expectedExceptions, String expectedExceptionsMessageRegExp, String suiteName, String testName, Boolean singleThreaded, Boolean skipFailedInvocations, Boolean ignoreMissingDependencies, Integer priorit) {
                this.groups = groups;
                this.enabled = enabled;
                this.dependsOnGroups = dependsOnGroups;
                this.dependsOnMethods = dependsOnMethods;
                this.timeOut = timeOut;
                this.invocationTimeOut = invocationTimeOut;
                this.invocationCoun = invocationCoun;
                this.threadPoolSiz = threadPoolSiz;
                this.successPercentage = successPercentage;
                this.dataProvider = dataProvider;
                this.dataProviderClass = dataProviderClass;
                this.dataProviderDynamicClass = dataProviderDynamicClass;
                this.alwaysRun = alwaysRun;
                this.description = description;
                this.expectedExceptions = expectedExceptions;
                this.expectedExceptionsMessageRegExp = expectedExceptionsMessageRegExp;
                this.suiteName = suiteName;
                this.testName = testName;
                this.singleThreaded = singleThreaded;
                this.skipFailedInvocations = skipFailedInvocations;
                this.ignoreMissingDependencies = ignoreMissingDependencies;
                this.priorit = priorit;
            }
        }

        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
            method = super.visitMethodDeclaration(method, ctx);

            //return early if no TestNG used
            Optional<J.Annotation> testNGAnnotation = method.getLeadingAnnotations().stream().filter(TESTNG_TEST_MATCHER::matches).findAny();
            if (!testNGAnnotation.isPresent()) {
                return method;
            }

            // transform TestNG @Test to Juniper
            JavaCoordinates javaCoordinates = method.getCoordinates().addAnnotation((o1, o2) -> -1);
            method = transformAnnotationToJuniper(method, ctx, testNGAnnotation.get(), getCursor(), javaCoordinates);

            // remove org.testng.annotations.Test annotation
            maybeRemoveImport(TESTNG_TEST_FQN);
            return new RemoveAnnotationVisitor(TESTNG_TEST_MATCHER).visitMethodDeclaration(method, ctx);
        }

        private J.MethodDeclaration transformAnnotationToJuniper(J.MethodDeclaration method, ExecutionContext ctx, J.Annotation annotation, Cursor cursor, JavaCoordinates coordinates) {
            J.MethodDeclaration result = JavaTemplate.builder("@Test")
                    .javaParser(JavaParser.fromJavaVersion().classpath( "junit-jupiter-api"))
                    .imports(JUNIPER_TEST)
                    .build()
                    .apply(getCursor(), coordinates);
            maybeAddImport(JUNIPER_TEST, false);
            return result;
        }
    }
}
