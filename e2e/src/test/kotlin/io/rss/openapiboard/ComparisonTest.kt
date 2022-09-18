package io.rss.openapiboard

import io.rss.openapiboard.DataProvider.givenApi
import io.rss.openapiboard.TestRequestHelpers.`GET json`
import io.rss.openapiboard.TestRequestHelpers.`PUT multipart-form`
import io.rss.openapiboard.TestRequestHelpers.`from last response equals`
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.util.LinkedMultiValueMap

@DisplayName("Manager app can create comparisons between APIs")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ComparisonTest {

    @BeforeAll
    fun beforeAll() {
        println("Loading sample data")
        givenApi("pets-store", "left", "1.0")
        givenApi("pets-store", "right", "1.7")
    }

    @Test
    fun `it should get the comparison of 2 apis`() {
        val api1 = "&srcName=pets-store&srcNs=left&srcVersion=1.0"
        val api2 = "&compareName=pets-store&compareNs=right&compareVersion=1.7"

        `GET json`("apis/comparison?q=1$api1$api2")

        "$.source.name" `from last response equals` "pets-store"
        "$.source.namespace" `from last response equals` "left"
        "$.compared.namespace" `from last response equals` "right"
    }

}