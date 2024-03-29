package io.rss.apicenter.spek

import io.rss.apicenter.RestRequestHelper
import org.spekframework.spek2.style.gherkin.ScenarioBody
import org.springframework.http.HttpStatus

fun ScenarioBody.ThenResponseIsOK() {
    Then("HTTP response is OK") {
        RestRequestHelper.assertResponseIsOK()
    }
}

fun ScenarioBody.ThenResponseIs(expectedStatus: HttpStatus) {
    Then("HTTP response is ${expectedStatus.name}") {
        RestRequestHelper.assertResponseIs(expectedStatus)
    }
}
