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
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.*;
import org.openrewrite.java.tree.J;

import java.time.Duration;
import java.util.Comparator;

@Value
@EqualsAndHashCode(callSuper = false)
public class MigrateTestAnnotation extends Recipe {
    @Override
    public String getDisplayName() {
        return "Replace @Test Annotation";
    }

    @Override
    public String getDescription() {
        return "Replace @org.testng.annotations.Test with Jupiter equivalents.";
    }

    @Override
    public Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofMinutes(5);
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new ReplaceTestAnnotationVisitor();
    }

    class ReplaceTestAnnotationVisitor extends JavaIsoVisitor<ExecutionContext> {

        private static final String TESTNG_TEST_FQN = "org.testng.annotations.Test";
        private final AnnotationMatcher TESTNG_TEST_MATCHER = new AnnotationMatcher("@" + TESTNG_TEST_FQN);

        private static final String JUPITER_TEST = "org.junit.jupiter.api.Test";
        private static final String TEST_ANNOTATION = "@Test";

        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
            method = super.visitMethodDeclaration(method, ctx);
            var methodAnnotations = method.getLeadingAnnotations();

            //return early if no TestNG used or still has arguments
            var testNgAnnotation = methodAnnotations.stream()
                    .filter(TESTNG_TEST_MATCHER::matches)
                    .findAny();
            if (testNgAnnotation.isEmpty()) {
                return method;
            }

            boolean hasArguments = testNgAnnotation
                    .map(J.Annotation::getArguments)
                    .map(as -> !as.isEmpty() && as.stream().noneMatch(J.Empty.class::isInstance))
                    .orElse(false);
            if (hasArguments) {
                return method;
            }

            // transform TestNG @Test to Jupiter
            var addAnnotationCoordinate = method.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName));
            var cursor = getCursor();
            method = JavaTemplate
                    .builder(TEST_ANNOTATION)
                    .javaParser(JavaParser.fromJavaVersion().classpath("junit-jupiter-api"))
                    .imports(JUPITER_TEST)
                    .build()
                    .apply(cursor, addAnnotationCoordinate);

            // update imports
            maybeAddImport(JUPITER_TEST, false);
            maybeRemoveImport(TESTNG_TEST_FQN);

            //remove old annotation
            doAfterVisit(new RemoveAnnotationVisitor(TESTNG_TEST_MATCHER));

            return method;
        }
    }
}
