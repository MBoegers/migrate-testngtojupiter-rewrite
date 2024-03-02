package io.github.mboegers.openrewrite.testngtojupiter;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.*;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

import java.util.Comparator;
import java.util.List;

@Value
@EqualsAndHashCode(callSuper = false)
public class MigrateEnabledArgument extends Recipe {
    @Override
    public String getDisplayName() {
        return "Replace TestNG enable Test";
    }

    @Override
    public String getDescription() {
        return "Replace @org.testng.annotations.Test's parameter with Juniper @Disabled.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MigrateEnabledArgumentVisitor();
    }

    static class MigrateEnabledArgumentVisitor extends JavaIsoVisitor<ExecutionContext> {
        private final AnnotationMatcher TESTNG_TEST_MATCHER = new AnnotationMatcher("@org.testng.annotations.Test");

        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext executionContext) {
            method = super.visitMethodDeclaration(method, executionContext);

            // return early if not @Test annotation with argument absent present
            var testNgAnnotation = method.getLeadingAnnotations().stream()
                    .filter(TESTNG_TEST_MATCHER::matches)
                    .findAny();
            if (testNgAnnotation.isEmpty()) {
                return method;
            }

            var enabledArgument = testNgAnnotation
                    .map(J.Annotation::getArguments).orElse(List.of())
                    .stream()
                    .filter(this::isEnabledExpression)
                    .map(J.Assignment.class::cast)
                    .findAny();
            if (enabledArgument.isEmpty()) {
                return method;
            }

            // add @Disables if enabled=false
            Boolean isEnabled = (Boolean) ((J.Literal) enabledArgument.get().getAssignment().unwrap()).getValue();
            if (Boolean.FALSE.equals(isEnabled)) {
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

        private boolean isEnabledExpression(Expression expr) {
            return expr instanceof J.Assignment &&
                   "enabled".equals(((J.Identifier) ((J.Assignment) expr).getVariable()).getSimpleName());
        }
    }
}
