package io.rss.apicenter.server.services

import com.nhaarman.mockitokotlin2.whenever
import io.rss.apicenter.server.persistence.dao.NamespaceCachedRepository
import io.rss.apicenter.server.persistence.entities.ApiRecord
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.function.Supplier

@ExtendWith(MockitoExtension::class)
internal class NamespaceHandlerTest {

    @Mock
    lateinit var namespaceRepository: NamespaceCachedRepository

    @Mock
    var grantedAuthority: GrantedAuthority = SimpleGrantedAuthority("none")

    var authoritiesProvider: Supplier<Collection<GrantedAuthority>> = Supplier { listOf(grantedAuthority) }

    lateinit var underTest: NamespaceHandler

    @BeforeEach
    internal fun setUp() {
        underTest = NamespaceHandler(namespaceRepository, authoritiesProvider)
    }

    @Test
    fun `when user has access to required namespace`() {
        whenever(namespaceRepository.getAuthorities("comeIn")).thenReturn(listOf("READ_NEWS"))
        whenever(namespaceRepository.exists("comeIn")).thenReturn(true)
        grantedAuthority = SimpleGrantedAuthority("READ_NEWS")

        assertTrue(underTest.hasUserAccessToNamespace("comeIn"))
        underTest.assertUserHasAccess("comeIn")
    }

    @Test
    fun `when can not access namespace`() {
        whenever(namespaceRepository.getAuthorities("comeIn")).thenReturn(listOf("READ_NEWS"))
        whenever(namespaceRepository.exists("comeIn")).thenReturn(true)

        assertFalse(underTest.hasUserAccessToNamespace("comeIn"))
        assertThrows (AccessDeniedException::class.java) { underTest.assertUserHasAccess("comeIn") }
    }

    @Test
    fun `filter APIs allowed for user`() {
        val first = ApiRecord("first", "simple", "1.0")
        val second = ApiRecord("sec", "restricted", "1.0")
        val third = ApiRecord("third", "default", "1.0")

        whenever(namespaceRepository.getAuthorities("simple")).thenReturn(listOf())
        whenever(namespaceRepository.getAuthorities("default")).thenReturn(listOf("READ"))
        whenever(namespaceRepository.getAuthorities("restricted")).thenReturn(listOf("RES_ROLE"))
        grantedAuthority = SimpleGrantedAuthority("READ")

        val result = underTest.filterAllowedApi(listOf(first, second, third))

        assertEquals(2, result.size)
        assertTrue(first in result)
        assertFalse(second in result)
    }

}