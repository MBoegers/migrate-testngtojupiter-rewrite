package io.github.mboegers.openrewrite.testngtojupiter.helper;

import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

import java.util.List;
import java.util.Optional;

public final class AnnotationParameterValue {

    public static boolean hasAny(J.Annotation annotation) {
        List<Expression> arguments = annotation.getArguments();

        if (arguments == null || arguments.isEmpty()) {
            return false;
        }

        boolean containsNoEmpty = arguments.stream().noneMatch(J.Empty.class::isInstance);
        return containsNoEmpty;
    }

    public static <T> Optional<T> extract(J.Annotation annotation, String parameterName, Class<T> valueClass) {
        List<Expression> arguments = annotation.getArguments();

        if (arguments == null) {
            return Optional.empty();
        }

        return arguments.stream()
                .filter(J.Assignment.class::isInstance)
                .map(J.Assignment.class::cast)
                .filter(a -> parameterName.equals(((J.Identifier) a.getVariable()).getSimpleName()))
                .map(J.Assignment::getAssignment)
                .filter(J.Literal.class::isInstance)
                .map(J.Literal.class::cast)
                .findAny()
                .map(J.Literal::getValue)
                .map(valueClass::cast);
    }
}
