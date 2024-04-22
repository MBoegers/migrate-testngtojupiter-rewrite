package io.github.mboegers.openrewrite.testngtojupiter.helper;

import org.openrewrite.ExecutionContext;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.AnnotationMatcher;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.marker.SearchResult;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Finds {@linkplain J.Annotation}s in the source set
 *
 * @see AnnotationMatcher
 * @see J.Annotation
 */
public class FindAnnotation extends JavaIsoVisitor<ExecutionContext> {

    private final AnnotationMatcher annotationMatcher;

    public FindAnnotation(AnnotationMatcher annotationMatcher) {
        this.annotationMatcher = annotationMatcher;
    }

    public static Optional<J.Annotation> findFirst(J tree, AnnotationMatcher annotationMatcher) {
        return TreeVisitor.collect(new FindAnnotation(annotationMatcher), tree, new HashSet<>(), J.Annotation.class, Function.identity()).stream().findFirst();
    }

    public static Set<J.Annotation> find(J tree, AnnotationMatcher annotationMatcher) {
        return TreeVisitor.collect(new FindAnnotation(annotationMatcher), tree, new HashSet<>(), J.Annotation.class, Function.identity());
    }

    @Override
    public J.Annotation visitAnnotation(J.Annotation annotation, ExecutionContext executionContext) {
        annotation = super.visitAnnotation(annotation, executionContext);

        if (annotationMatcher.matches(annotation)) {
            annotation = SearchResult.found(annotation);
        }

        return annotation;
    }
}
