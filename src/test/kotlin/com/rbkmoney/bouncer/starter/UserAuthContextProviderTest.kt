package com.rbkmoney.bouncer.starter

import com.rbkmoney.orgmanagement.AuthContextProviderSrv
import com.rbkmoney.orgmanagement.UserNotFound
import io.github.benas.randombeans.api.EnhancedRandom.random
import org.apache.thrift.TException
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class UserAuthContextProviderTest {

    private val authContextProvider: AuthContextProviderSrv.Iface = mock {}

    private val userAuthContextProvider: UserAuthContextProvider

    init {
        userAuthContextProvider = UserAuthContextProvider(authContextProvider)
    }

    @BeforeEach
    fun setUp() {
        reset(authContextProvider)
    }

    @Test
    fun `getUserAuthContext should fail because of UserNotFound exception`() {
        val userId = random(String::class.java)
        whenever(authContextProvider.getUserContext(userId)).thenThrow(UserNotFound())
        val exception = assertThrows(RuntimeException::class.java) {
            userAuthContextProvider.getUserAuthContext(userId)
        }
        assertThat(exception.message, containsString("Can't get user auth context: userId = $userId"))
        verify(authContextProvider).getUserContext(userId)
    }

    @Test
    fun `getUserAuthContext should fail because of TException`() {
        val userId = random(String::class.java)
        whenever(authContextProvider.getUserContext(userId)).thenThrow(TException())
        val exception = assertThrows(RuntimeException::class.java) {
            userAuthContextProvider.getUserAuthContext(userId)
        }
        assertThat(exception.message, containsString("Can't get user auth context: userId = $userId"))
        verify(authContextProvider).getUserContext(userId)
    }

    @Test
    fun `getUserAuthContext should return result`() {
        val userId = random(String::class.java)
        whenever(authContextProvider.getUserContext(userId))
            .thenReturn(com.rbkmoney.bouncer.ctx.ContextFragment())
        assertNotNull(userAuthContextProvider.getUserAuthContext(userId))
        verify(authContextProvider).getUserContext(userId)
    }
}
