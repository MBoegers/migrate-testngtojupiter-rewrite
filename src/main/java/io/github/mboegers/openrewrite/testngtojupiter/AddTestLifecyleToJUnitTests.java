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
import org.openrewrite.java.AnnotationMatcher;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;

import java.time.Duration;
import java.util.Comparator;

@Value
@EqualsAndHashCode(callSuper = false)
public class AddTestLifecyleToJUnitTests extends Recipe {

    @Override
    public String getDisplayName() {
        return "Add @TestInstance(TestInstance.Lifecycle.PER_CLASS) to Jupiter tests";
    }

    @Override
    public String getDescription() {
        return "To align JUnit Jupiter behavior with the expected behavior coming from TestNG the annotation @TestInstance(TestInstance.Lifecycle.PER_CLASS) is needed.";
    }

    @Override
    public Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofMinutes(5);
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new AddTestLifecyleToJUnitTests.AddLifecyleAnnotationVisitor();
    }

    static class AddLifecyleAnnotationVisitor extends JavaIsoVisitor<ExecutionContext> {

        private static final AnnotationMatcher TEST_MATCHER = new AnnotationMatcher("@org.junit.jupiter.api.Test");
        private static final AnnotationMatcher TEST_INSTANCE_MATCHER = new AnnotationMatcher("@org.junit.jupiter.api.TestInstance");

        @Override
        public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext ctx) {
            classDecl = super.visitClassDeclaration(classDecl, ctx);

            boolean hasTestInstanceAnnotation = classDecl.getLeadingAnnotations().stream().anyMatch(TEST_INSTANCE_MATCHER::matches);

            Boolean usesJUnit = getCursor().pollMessage("USES_JUNIT");
            if (hasTestInstanceAnnotation || usesJUnit == null || !usesJUnit) {
                return classDecl;
            }

            // transform TestNG @Test to Jupiter
            var addAnnotationCoordinate = classDecl.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName));
            var cursor = getCursor();
            classDecl = JavaTemplate
                    .builder("@TestInstance(TestInstance.Lifecycle.PER_CLASS)")
                    .javaParser(JavaParser.fromJavaVersion().classpath("junit-jupiter-api"))
                    .imports("org.junit.jupiter.api.TestInstance")
                    .build()
                    .apply(cursor, addAnnotationCoordinate);

            // update imports
            maybeAddImport("org.junit.jupiter.api.TestInstance", false);

            return classDecl;
        }

        @Override
        public J.Annotation visitAnnotation(J.Annotation annotation, ExecutionContext ctx) {
            annotation = super.visitAnnotation(annotation, ctx);

            if (TEST_MATCHER.matches(annotation)) {
                getCursor().dropParentUntil(J.ClassDeclaration.class::isInstance).putMessage("USES_JUNIT", true);
            }

            return annotation;
        }
    }
}
