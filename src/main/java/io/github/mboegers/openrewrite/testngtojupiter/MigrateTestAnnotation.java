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
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaCoordinates;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            var javaCoordinates = method.getCoordinates().addAnnotation((o1, o2) -> -1);
            var testAnnotation = testNGAnnotation.get();
            var annotations = method.getLeadingAnnotations();
            var arguments = extractArgumentsFrom(testAnnotation);

            annotations.add(new AddTestAnnotation(javaCoordinates, getCursor()).visitAnnotation(testAnnotation, ctx));
            annotations.add(new TransformEnabled(javaCoordinates, getCursor(), arguments).visitAnnotation(testAnnotation, ctx));

            method = method.withLeadingAnnotations(annotations);

            // remove org.testng.annotations.Test annotation
            maybeRemoveImport(TESTNG_TEST_FQN);
            return new RemoveAnnotationVisitor(TESTNG_TEST_MATCHER).visitMethodDeclaration(method, ctx);
        }

        class AddTestAnnotation extends JavaIsoVisitor<ExecutionContext> {
            private static final String JUPITER_TEST = "org.junit.jupiter.api.Test";
            private static final String TEST_ANNOTATION = "@Test";

            private final JavaCoordinates javaCoordinates;
            private final Cursor cursor;

            public AddTestAnnotation(JavaCoordinates javaCoordinates, Cursor cursor) {
                this.javaCoordinates = javaCoordinates;
                this.cursor = cursor;
            }

            @Override
            public J.Annotation visitAnnotation(J.Annotation annotation, ExecutionContext executionContext) {
                J.Annotation testAnnotation = JavaTemplate
                        .builder(TEST_ANNOTATION)
                        .javaParser(JavaParser.fromJavaVersion().classpath( "junit-jupiter-api"))
                        .imports(JUPITER_TEST)
                        .build()
                        .apply(cursor, javaCoordinates);
                maybeAddImport(JUPITER_TEST, false);

                return testAnnotation;
            }
        }

        class TransformEnabled extends JavaIsoVisitor<ExecutionContext> {

            private static final String AT_DISABLED = "@Disabled";
            private static final String DISABLED_FQN = "org.junit.jupiter.api.Disabled";

            private static final String TEST_NG_SKIP_KEY = "enabled";

            private final JavaCoordinates javaCoordinates;
            private final Map<String, Expression> arguments;

            private final Cursor cursor;

            public TransformEnabled(JavaCoordinates javaCoordinates, Cursor cursor, Map<String, Expression> arguments) {
                this.javaCoordinates = javaCoordinates;
                this.arguments = arguments;
                this.cursor = cursor;
            }

            @Override
            public J.Annotation visitAnnotation(J.Annotation annotation, ExecutionContext executionContext) {
                Boolean isEnabled = (Boolean) ((J.Literal) arguments.get(TEST_NG_SKIP_KEY)).getValue();

                if (isEnabled == null || !isEnabled) {
                    return annotation;
                }

                J.Annotation testAnnotation = JavaTemplate
                        .builder(AT_DISABLED)
                        .javaParser(JavaParser.fromJavaVersion().classpath( "junit-jupiter-api"))
                        .imports(DISABLED_FQN)
                        .build()
                        .apply(cursor, javaCoordinates);
                maybeAddImport(DISABLED_FQN, false);

                return testAnnotation;
            }
        }
    }

    private static Map<String, Expression> extractArgumentsFrom(J.Annotation annotation) {
        List<Expression> arguments = annotation.getArguments();
        if (arguments == null) {
            return Map.of();
        }

        Map<String, Expression> values = new HashMap<>();
        for (var expr : arguments) {
            J.Assignment assigment = (J.Assignment) expr;
            J.Identifier variable = (J.Identifier) assigment.getVariable();
            Expression initializer = assigment.getAssignment();

            values.put(variable.getSimpleName(), initializer);
        }

        return Map.copyOf(values);
    }
}
