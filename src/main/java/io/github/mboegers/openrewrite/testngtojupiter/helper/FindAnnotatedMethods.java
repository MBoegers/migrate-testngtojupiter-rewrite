package io.github.mboegers.openrewrite.testngtojupiter.helper;

import org.openrewrite.ExecutionContext;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.AnnotationMatcher;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.marker.SearchResult;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Finds a {@linkplain J.MethodDeclaration} that is annotated with an {@linkplain J.Annotation} matching the given Annotation Matcher
 *
 * @see AnnotationMatcher
 * @see org.openrewrite.java.tree.J.MethodDeclaration
 */
public class FindAnnotatedMethods extends JavaIsoVisitor<ExecutionContext> {

    private final AnnotationMatcher annotationMatcher;

    public FindAnnotatedMethods(AnnotationMatcher annotationMatcher) {
        this.annotationMatcher = annotationMatcher;
    }

    public static Set<J.MethodDeclaration> find(J subtree, AnnotationMatcher annotationMatcher) {
        return TreeVisitor.collect(new FindAnnotatedMethods(annotationMatcher), subtree, new HashSet<>())
                .stream()
                .filter(J.MethodDeclaration.class::isInstance)
                .map(J.MethodDeclaration.class::cast)
                .collect(Collectors.toSet());
    }

    @Override
    public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
        J.MethodDeclaration m = super.visitMethodDeclaration(method, ctx);

        boolean isAnnotatedWithTargetAnnotation = m.getLeadingAnnotations().stream().anyMatch(annotationMatcher::matches);
        if (isAnnotatedWithTargetAnnotation) {
            m = SearchResult.found(m);
        }

        return m;
    }
}
