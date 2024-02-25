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

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.*;
import org.openrewrite.java.tree.J;

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

        private static final String TESTNG_TEST_FQN = "org.testng.annotations.Test";
        private final AnnotationMatcher TESTNG_TEST_MATCHER = new AnnotationMatcher("@" + TESTNG_TEST_FQN);

        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
            method = super.visitMethodDeclaration(method, ctx);

            //return early if no TestNG used
            Optional<J.Annotation> testNGAnnotation = method.getLeadingAnnotations().stream().filter(TESTNG_TEST_MATCHER::matches).findAny();
            if (testNGAnnotation.isEmpty()) {
                return method;
            }

            // transform TestNG @Test to Jupiter
            var testAnnotation = testNGAnnotation.get();
            var annotations = method.getLeadingAnnotations();

            annotations.add(new AddTestAnnotation().visitAnnotation(testAnnotation, ctx));

            method = method.withLeadingAnnotations(annotations);

            // remove org.testng.annotations.Test annotation
            maybeRemoveImport(TESTNG_TEST_FQN);
            return new RemoveAnnotationVisitor(TESTNG_TEST_MATCHER).visitMethodDeclaration(method, ctx);
        }

        class AddTestAnnotation extends JavaIsoVisitor<ExecutionContext> {
            private static final String JUPITER_TEST = "org.junit.jupiter.api.Test";
            private static final String TEST_ANNOTATION = "@Test";

            @Override
            public J.Annotation visitAnnotation(J.Annotation annotation, ExecutionContext executionContext) {
                J.Annotation testAnnotation = JavaTemplate
                        .builder(TEST_ANNOTATION)
                        .javaParser(JavaParser.fromJavaVersion().classpath( "junit-jupiter-api"))
                        .imports(JUPITER_TEST)
                        .build()
                        .apply(getCursor(), annotation.getCoordinates().replace());
                maybeAddImport(JUPITER_TEST, false);

                return testAnnotation;
            }
        }
    }
}
