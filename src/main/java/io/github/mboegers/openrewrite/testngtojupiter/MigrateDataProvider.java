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
import io.github.mboegers.openrewrite.testngtojupiter.helper.FindAnnotatedMethods;
import io.github.mboegers.openrewrite.testngtojupiter.helper.FindAnnotation;
import org.openrewrite.*;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.*;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaCoordinates;
import org.openrewrite.java.tree.JavaType;

import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class MigrateDataProvider extends Recipe {

    private static final String DATA_PROVIDER = "org.testng.annotations.DataProvider";
    private static final AnnotationMatcher DATA_PROVIDER_MATCHER = new AnnotationMatcher("@" + DATA_PROVIDER);

    @Override
    public String getDisplayName() {
        return "Migrate @DataProvider utilities";
    }

    @Override
    public String getDescription() {
        return "Wrap `@DataProvider` methods into a Jupiter parameterized test with MethodSource.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new TreeVisitor<>() {
            @Override
            public @Nullable Tree visit(@Nullable Tree tree, ExecutionContext executionContext, Cursor parent) {
                tree = super.visit(tree, executionContext, parent);
                // wrap methods
                tree = new WrapDataProviderMethod().visit(tree, executionContext, parent);
                // remove @DataProvider
                tree = new RemoveAnnotationVisitor(DATA_PROVIDER_MATCHER).visit(tree, executionContext, parent);
                // use @MethodeSource and @ParameterizedTest
                tree = new UseParameterizedTest().visit(tree, executionContext, parent);
                tree = new UseMethodSource().visit(tree, executionContext, parent);
                // remove dataProviderName and dataProviderClass arguments
                tree = new RemoveAnnotationAttribute("org.testng.annotations.Test", "dataProvider")
                        .getVisitor().visit(tree, executionContext);
                tree = new RemoveAnnotationAttribute("org.testng.annotations.Test", "dataProviderClass")
                        .getVisitor().visit(tree, executionContext);
                return tree;
            }
        };
    }

    private class WrapDataProviderMethod extends JavaIsoVisitor<ExecutionContext> {

        private static final JavaTemplate methodeSourceTemplate = JavaTemplate.builder("""
                        public static Stream<Arguments> #{}() {
                            return Arrays.stream(#{}()).map(Arguments::of);
                        }
                        """)
                .imports("org.junit.jupiter.params.provider.Arguments", "java.util.Arrays", "java.util.stream.Stream")
                .contextSensitive()
                .javaParser(JavaParser.fromJavaVersion().classpath("junit-jupiter-params"))
                .build();

        @Override
        public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, org.openrewrite.ExecutionContext ctx) {
            classDecl = super.visitClassDeclaration(classDecl, ctx);

            Set<J.MethodDeclaration> dataProviders = FindAnnotatedMethods.find(classDecl, DATA_PROVIDER_MATCHER);

            // for each add a Wrapper that translates to Jupiter method source
            for (J.MethodDeclaration provider : dataProviders) {
                String providerMethodName = provider.getSimpleName();
                String providerName = FindAnnotation.find(provider, DATA_PROVIDER_MATCHER).stream().findAny()
                        .flatMap(j -> AnnotationArguments.extractLiteral(j, "name", String.class))
                        .orElse(providerMethodName);

                classDecl = classDecl.withBody(methodeSourceTemplate.apply(
                        new Cursor(getCursor(), classDecl.getBody()), classDecl.getBody().getCoordinates().lastStatement(),
                        providerName, providerMethodName));
            }

            // add new imports
            maybeAddImport("org.junit.jupiter.params.provider.Arguments");
            maybeAddImport("java.util.Arrays");
            maybeAddImport("java.util.stream.Stream");

            return classDecl;
        }
    }

    private class UseParameterizedTest extends JavaIsoVisitor<ExecutionContext> {
        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
            method = super.visitMethodDeclaration(method, ctx);

            // if @ParameterizedTest is used, skip
            Optional<J.Annotation> paraeterizedTestAnnotation = FindAnnotation.findFirst(method, new AnnotationMatcher("@org.junit.jupiter.params.ParameterizedTest"));
            if (paraeterizedTestAnnotation.isPresent()) {
                return method;
            }

            // if no TestNG @Test present, skip
            Optional<J.Annotation> testNgAnnotation = FindAnnotation.findFirst(method, new AnnotationMatcher("@org.testng.annotations.Test"));
            if (testNgAnnotation.isEmpty()) {
                return method;
            }

            // determine if a parameterized test is applicable
            Optional<String> dataProviderMethodName = AnnotationArguments.extractLiteral(testNgAnnotation.get(), "dataProvider", String.class);
            if (dataProviderMethodName.isEmpty()) {
                return method;
            }

            JavaCoordinates addAnnotationCoordinate = method.getCoordinates().addAnnotation((a, b) -> 1);

            method = JavaTemplate
                    .builder("@ParameterizedTest")
                    .javaParser(JavaParser.fromJavaVersion().classpath("junit-jupiter-params"))
                    .imports("org.junit.jupiter.params.ParameterizedTest")
                    .build()
                    .apply(getCursor(), addAnnotationCoordinate);

            maybeAddImport("org.junit.jupiter.params.ParameterizedTest");

            return method;
        }
    }

    class UseMethodSource extends JavaIsoVisitor<ExecutionContext> {
        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
            method = super.visitMethodDeclaration(method, ctx);

            // if @MethodSource is used, skip
            Optional<J.Annotation> methodSourceAnnotation = FindAnnotation.findFirst(method, new AnnotationMatcher("@org.junit.jupiter.params.provider.MethodSource"));
            if (methodSourceAnnotation.isPresent()) {
                return method;
            }

            // if no testng annotation is present, skip
            Optional<J.Annotation> testNgAnnotation = FindAnnotation.findFirst(method, new AnnotationMatcher("@org.testng.annotations.Test"));
            if (testNgAnnotation.isEmpty()) {
                return method;
            }

            // determine Provider name, if not present skip!
            Optional<String> dataProviderMethodName = AnnotationArguments.extractLiteral(testNgAnnotation.get(), "dataProvider", String.class);
            if (dataProviderMethodName.isEmpty()) {
                return method;
            }

            // determin provider class or use current class as default
            String dataProviderClass = AnnotationArguments.extractAssignments(testNgAnnotation.get(), "dataProviderClass").stream()
                    .findAny()
                    .map(J.FieldAccess.class::cast)
                    .map(J.FieldAccess::getTarget)
                    .map(e -> e.unwrap().getType())
                    .filter(JavaType.Class.class::isInstance)
                    .map(JavaType.Class.class::cast)
                    .map(JavaType.Class::getFullyQualifiedName)
                    .orElse(requireNonNull(getCursor().firstEnclosingOrThrow(J.ClassDeclaration.class).getType()).getFullyQualifiedName());

            // add MethodSource annotation
            JavaCoordinates addAnnotationCoordinate = method.getCoordinates().addAnnotation((a, b) -> 1);
            method = JavaTemplate
                    .builder("@MethodSource(\"#{}##{}\")")
                    .javaParser(JavaParser.fromJavaVersion().classpath("junit-jupiter-params"))
                    .imports("org.junit.jupiter.params.provider.MethodSource")
                    .build()
                    .apply(getCursor(), addAnnotationCoordinate, dataProviderClass, dataProviderMethodName.get());

            maybeAddImport("org.junit.jupiter.params.provider.MethodSource");

            return method;
        }
    }
}
