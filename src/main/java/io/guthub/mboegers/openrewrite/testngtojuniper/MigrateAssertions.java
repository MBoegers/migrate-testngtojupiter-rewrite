package io.guthub.mboegers.openrewrite.testngtojuniper;

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.junit.jupiter.api.Assertions;
import org.openrewrite.java.template.RecipeDescriptor;
import org.testng.Assert;

@RecipeDescriptor(
        name = "Migrate TestNG Asserts to Juniper",
        description = "Migrate all TestNG Assertions to JUnit Juniper Assertions."
)
public class MigrateAssertions {
    @RecipeDescriptor(
            name = "Replace `Assert#assertEquals(Object, Object)`",
            description = "Replace `org.testng.Assert#assertEquals(Object, Object)` with `org.junit.jupiter.api.Assertions#assertEquals(Object, Object)`."
    )
    public static class MigrateObjectAssert {

        @BeforeTemplate
        void before(Object actual, Object expected) {
            Assert.assertEquals(actual, expected);
        }

        @AfterTemplate
        void after(Object actual, Object expected) {
            Assertions.assertEquals(expected, actual);
        }
    }
}
