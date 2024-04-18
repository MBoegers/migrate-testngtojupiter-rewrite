package io.github.mboegers.openrewrite.testngtojupiter.helper;

import org.openrewrite.java.AnnotationMatcher;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.marker.SearchResult;

/**
 * Checks whether a given annotation is used
 */
public class UsesAnnotation<P> extends JavaIsoVisitor<P> {
    private final AnnotationMatcher annotationMatcher;

    public UsesAnnotation(AnnotationMatcher annotationMatcher) {
        this.annotationMatcher = annotationMatcher;
    }

    @Override
    public J.Annotation visitAnnotation(J.Annotation annotation, P ctx) {
        annotation = super.visitAnnotation(annotation, ctx);
        
        if (annotationMatcher.matches(annotation)) {
            return SearchResult.found(annotation);
        } else {
            return annotation;
        }
    }
}
