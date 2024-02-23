/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
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