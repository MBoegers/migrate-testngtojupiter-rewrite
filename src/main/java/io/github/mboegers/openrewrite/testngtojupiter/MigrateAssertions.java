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

import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import org.junit.jupiter.api.Assertions;
import org.openrewrite.java.template.RecipeDescriptor;
import org.testng.Assert;

@RecipeDescriptor(
        name = "Migrate TestNG Asserts to Jupiter",
        description = "Migrate all TestNG Assertions to JUnit Jupiter Assertions."
)
public class MigrateAssertions {
    @RecipeDescriptor(
            name = "Replace `Assert#assertEquals(?, ?)`",
            description = "Replace `org.testng.Assert#assertEquals(?, ?)` with `org.junit.jupiter.api.Assertions#assertEquals(?, ?)`."
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

    @RecipeDescriptor(
            name = "Replace `Assert#assertEquals(?, ?, String)`",
            description = "Replace `org.testng.Assert#assertEquals(?, ?, String)` with `org.junit.jupiter.api.Assertions#assertEquals(?, ?, String)`."
    )
    public static class MigrateObjectAssertWithMsg {

        @BeforeTemplate
        void before(Object actual, Object expected, String msg) {
            Assert.assertEquals(actual, expected, msg);
        }

        @AfterTemplate
        void after(Object actual, Object expected, String msg) {
            Assertions.assertEquals(expected, actual, msg);
        }
    }


    @RecipeDescriptor(
            name = "Replace `Assert#assertEquals(?, ?)`",
            description = "Replace `org.testng.Assert#assertEquals(?, ?)` with `org.junit.jupiter.api.Assertions#assertEquals(?, ?)`."
    )
    public static class MigrateAssertNotEqual {

        @BeforeTemplate
        void before(Object actual, Object expected) {
            Assert.assertNotEquals(actual, expected);
        }

        @AfterTemplate
        void after(Object actual, Object expected) {
            Assertions.assertNotEquals(expected, actual);
        }
    }

    @RecipeDescriptor(
            name = "Replace `Assert#assertEquals(?, ?, String)`",
            description = "Replace `org.testng.Assert#assertEquals(?, ?, String)` with `org.junit.jupiter.api.Assertions#assertEquals(?, ?, String)`."
    )
    public static class MigrateAssertNotEqualWithMsg {

        @BeforeTemplate
        void before(Object actual, Object expected, String msg) {
            Assert.assertNotEquals(actual, expected, msg);
        }

        @AfterTemplate
        void after(Object actual, Object expected, String msg) {
            Assertions.assertNotEquals(expected, actual, msg);
        }
    }

    @RecipeDescriptor(
            name = "Replace `Assert#assertFalse(boolean)`",
            description = "Replace `org.testng.Assert#assertFalse(boolean)` with `org.junit.jupiter.api.Assertions#assertFalse(boolean)`."
    )
    public static class MigrateAssertFalse {

        @BeforeTemplate
        void before(boolean expr) {
            Assert.assertFalse(expr);
        }

        @AfterTemplate
        void after(boolean expr) {
            Assertions.assertFalse(expr);
        }
    }

    @RecipeDescriptor(
            name = "Replace `Assert#assertFalse(boolean, String)`",
            description = "Replace `org.testng.Assert#assertFalse(boolean, String)` with `org.junit.jupiter.api.Assertions#assertFalse(boolean, String)`."
    )
    public static class MigrateAssertFalseWithMsg {

        @BeforeTemplate
        void before(boolean expr, String msg) {
            Assert.assertFalse(expr, msg);
        }

        @AfterTemplate
        void after(boolean expr, String msg) {
            Assertions.assertFalse(expr, msg);
        }
    }

    @RecipeDescriptor(
            name = "Replace `Assert#assertTrue(boolean)`",
            description = "Replace `org.testng.Assert#assertTrue(boolean)` with `org.junit.jupiter.api.Assertions#assertTrue(boolean)`."
    )
    public static class MigrateAssertTrue {

        @BeforeTemplate
        void before(boolean expr) {
            Assert.assertTrue(expr);
        }

        @AfterTemplate
        void after(boolean expr) {
            Assertions.assertTrue(expr);
        }
    }

    @RecipeDescriptor(
            name = "Replace `Assert#assertTrue(boolean, String)`",
            description = "Replace `org.testng.Assert#assertTrue(boolean, String)` with `org.junit.jupiter.api.Assertions#assertTrue(boolean, String)`."
    )
    public static class MigrateAssertTrueWithMsg {

        @BeforeTemplate
        void before(boolean expr, String msg) {
            Assert.assertTrue(expr, msg);
        }

        @AfterTemplate
        void after(boolean expr, String msg) {
            Assertions.assertTrue(expr, msg);
        }
    }
}
