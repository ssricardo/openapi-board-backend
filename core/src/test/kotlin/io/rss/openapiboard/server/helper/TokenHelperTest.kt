package io.rss.openapiboard.server.helper

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

// TODO: Implement test
class TokenHelperTest {

    @Test
    fun convertToString() {
    }

    @Test
    fun validateConvertToUser() {
    }

    @Test
    fun generateMailToken() {
    }

    @Test
    fun validateRetrieveMailInfo() {
    }

    @Test
    fun testHash() {
        val res = DigestUtils.sha1Hex("name" + "GET" + "nspace" + 1)
        println(res)
    }
}