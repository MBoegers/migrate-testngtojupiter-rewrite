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

import io.github.mboegers.openrewrite.testngtojupiter.helper.AnnotationArguments;
import io.github.mboegers.openrewrite.testngtojupiter.helper.FindAnnotation;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.*;
import org.openrewrite.java.tree.J;

import java.time.Duration;
import java.util.Comparator;
import java.util.Optional;

@Value
@EqualsAndHashCode(callSuper = false)
public class MigrateEnabledArgument extends Recipe {
    @Override
    public String getDisplayName() {
        return "Replace TestNG enable Test";
    }

    @Override
    public String getDescription() {
        return "Replace @org.testng.annotations.Test's parameter with Jupiter @Disabled.";
    }

    @Override
    public Duration getEstimatedEffortPerOccurrence() {
        return Duration.ofMinutes(5);
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MigrateEnabledArgumentVisitor();
    }

    static class MigrateEnabledArgumentVisitor extends JavaIsoVisitor<ExecutionContext> {
        private final AnnotationMatcher TESTNG_TEST_MATCHER = new AnnotationMatcher("@org.testng.annotations.Test");

        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
            method = super.visitMethodDeclaration(method, ctx);

            // add @Disables if enabled=false
            Optional<Boolean> isEnabled = FindAnnotation.find(method, TESTNG_TEST_MATCHER).stream().findAny()
                    .flatMap(j -> AnnotationArguments.extractLiteral(j, "enabled", Boolean.class));

            if (isEnabled.isPresent() && !isEnabled.get()) {
                var addAnnotationCoordinate = method.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName));
                method = JavaTemplate
                        .builder("@Disabled")
                        .javaParser(JavaParser.fromJavaVersion().classpath("junit-jupiter-api"))
                        .imports("org.junit.jupiter.api.Disabled")
                        .build()
                        .apply(getCursor(), addAnnotationCoordinate);
                maybeAddImport("org.junit.jupiter.api.Disabled", false);
            }

            // remove argument assigment
            doAfterVisit(new RemoveAnnotationAttribute("org.testng.annotations.Test", "enabled").getVisitor());

            return method;
        }
    }
}
