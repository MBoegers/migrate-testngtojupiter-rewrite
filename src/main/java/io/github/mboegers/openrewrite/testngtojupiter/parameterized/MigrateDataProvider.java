package io.github.mboegers.openrewrite.testngtojupiter.parameterized;

import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.*;
import org.openrewrite.java.tree.J;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class MigrateDataProvider extends Recipe {
    @Override
    public String getDisplayName() {
        return "Migrate @DataProvider utilities";
    }

    @Override
    public String getDescription() {
        return "Wrap `@DataProvider` methods into a Jupiter parameterized test MethodSource.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MigrateDataProviderVisitor();
    }

    private class MigrateDataProviderVisitor extends JavaIsoVisitor<ExecutionContext> {
        private static final AnnotationMatcher DATAPROVIDER_MATCHER = new AnnotationMatcher("@org.testng.annotations.DataProvider");

        private static final JavaTemplate methodeSourceTemplate = JavaTemplate.builder("""
                        public static Stream<Arguments> #{}Source() {
                          return Arrays.stream(#{}()).map(Arguments::of);
                        }
                        """)
                .imports("org.junit.jupiter.params.provider.Arguments", "java.util.Arrays", "java.util.stream.Stream")
                .contextSensitive()
                .javaParser(JavaParser.fromJavaVersion()
                        .logCompilationWarningsAndErrors(true)
                        .classpath("junit-jupiter-api", "junit-jupiter-params", "testng"))
                .build();
        private static final RemoveAnnotationVisitor removeAnnotationVisitor = new RemoveAnnotationVisitor(DATAPROVIDER_MATCHER);

        @Override
        public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, org.openrewrite.ExecutionContext executionContext) {
            classDecl = super.visitClassDeclaration(classDecl, executionContext);

            Set<J.MethodDeclaration> dataProviders = classDecl.getBody().getStatements().stream()
                    .filter(J.MethodDeclaration.class::isInstance)
                    .map(J.MethodDeclaration.class::cast)
                    .filter(m -> m.getLeadingAnnotations().stream().anyMatch(DATAPROVIDER_MATCHER::matches))
                    .collect(Collectors.toSet());

            for (J.MethodDeclaration provider : dataProviders) {
                String providerMethodName = provider.getSimpleName();
                String providerName = provider.getLeadingAnnotations().stream()
                        .filter(DATAPROVIDER_MATCHER::matches)
                        .map(J.Annotation::getArguments)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .filter(J.Assignment.class::isInstance)
                        .map(J.Assignment.class::cast)
                        .filter(a -> "name".equals(((J.Identifier) a.getVariable()).getSimpleName()))
                        .map(J.Assignment::getAssignment)
                        .filter(J.Literal.class::isInstance)
                        .map(J.Literal.class::cast)
                        .map(J.Literal::getValue)
                        .map(Objects::toString)
                        .findAny()
                        .orElse(providerMethodName);

                classDecl = classDecl.withBody(methodeSourceTemplate.apply(
                        new Cursor(getCursor(), classDecl.getBody()), classDecl.getBody().getCoordinates().lastStatement(),
                        providerName, providerMethodName));
            }

            doAfterVisit(new RemoveAnnotationVisitor(DATAPROVIDER_MATCHER));
            maybeRemoveImport("org.testng.annotations.DataProvider");
            maybeAddImport("org.junit.jupiter.params.provider.Arguments");
            maybeAddImport("java.util.Arrays");
            maybeAddImport("java.util.stream.Stream");

            return classDecl;
        }
    }
}
