package io.rss.apicenter.server.services.accesscontrol

import com.nhaarman.mockitokotlin2.*
import io.rss.apicenter.server.persistence.entities.ApiRecord
import org.aspectj.lang.ProceedingJoinPoint
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.access.AccessDeniedException
import java.util.*
import kotlin.reflect.KClass

@ExtendWith(MockitoExtension::class)
internal class RequiredAuthoritiesValidatorTest {

    lateinit var underTest: RequiredAuthoritiesValidator

    @Mock
    lateinit var mockStrategy: TypeVerifierStrategy<Any>

    @BeforeEach
    internal fun setUp() {
        val strategyClass: KClass<out Any> = ApiRecord::class
        whenever(mockStrategy.getType()).thenReturn(strategyClass as KClass<Any>)
        underTest = RequiredAuthoritiesValidator(listOf(mockStrategy))
    }

    @Test
    fun `when no restricted entity`() {
        val invocation = mock<ProceedingJoinPoint> {
            on { args } doReturn arrayOf("Test 1", 1.5f, null, "Param")
            on { proceed() } doReturn "Executed"
        }

        val result = underTest.invoke(invocation)
        assertEquals("Executed", result)
    }

    @Test
    fun `when parameter should not be accessible`() {
        val data = ApiRecord(id = UUID.randomUUID(), name = "Test", namespace = "Bla", version = "1")
        whenever(mockStrategy.hasUserAccess(listOf(data))).thenReturn(false)

        val invocation = mock<ProceedingJoinPoint> {
            on { args } doReturn arrayOf(data)
        }

        assertThrows(AccessDeniedException::class.java){ underTest.invoke(invocation) }
    }

    @Test
    fun `when parameter is verified but is accessible`() {
        val data = ApiRecord(id = UUID.randomUUID(), name = "Test", namespace = "Bla", version = "1")
        whenever(mockStrategy.hasUserAccess(listOf(data))).thenReturn(true)

        val invocation = mock<ProceedingJoinPoint> {
            on { args } doReturn arrayOf(data)
            on { proceed() } doReturn "Executed"
        }

        val result = underTest.invoke(invocation)

        verify(mockStrategy, atLeast(1)).hasUserAccess(any())
        assertEquals("Executed", result)
    }

    @Test
    fun `when response list contains restricted items`() {
        val api1 = ApiRecord(id = UUID.randomUUID(), name = "Test", namespace = "Bla", version = "1")
        val api2 = ApiRecord(id = UUID.randomUUID(), name = "Num2", namespace = "Bla", version = "1")
        val callResult = listOf(api1, api2)
        whenever(mockStrategy.filterResultList(callResult)).thenReturn(listOf(api2))

        val invocation = mock<ProceedingJoinPoint> {
            on { args } doReturn arrayOf()
            on { proceed() } doReturn callResult
        }

        val result = underTest.invoke(invocation) as List<ApiRecord>

        assertEquals(1, result.size)
        assertFalse(api1 in result)
    }
}
