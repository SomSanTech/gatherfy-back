package com.gatherfy.gatherfyback.configuration

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test


@Disabled("Skipping DB tests in CI/CD")
class CICDTest {
    @Test
    fun testDatabaseConnection() {
        // This test will be skipped
    }
}