package com.rbkmoney.bouncer.starter

import com.rbkmoney.bouncer.context.v1.ContextFragment
import com.rbkmoney.bouncer.context.v1.Token
import com.rbkmoney.bouncer.starter.api.BouncerContext
import com.rbkmoney.bouncer.starter.config.properties.BouncerProperties
import io.github.benas.randombeans.api.EnhancedRandom
import org.apache.thrift.TDeserializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant

internal class AbstractBouncerContextFactoryTest {

    private val userAuthContextProvider: UserAuthContextProvider = mock {}

    private val bouncerProperties: BouncerProperties

    private val bouncerContextFactory: AbstractBouncerContextFactory

    private val tDeserializer = TDeserializer()

    init {
        bouncerProperties = BouncerProperties(
            contextFragmentId = EnhancedRandom.random(String::class.java),
            deploymentId = EnhancedRandom.random(String::class.java),
            authMethod = EnhancedRandom.random(String::class.java),
            realm = EnhancedRandom.random(String::class.java),
            ruleSetId = EnhancedRandom.random(String::class.java)
        )
        bouncerContextFactory = object : AbstractBouncerContextFactory(bouncerProperties, userAuthContextProvider) {
            override fun customizeContext(contextFragment: ContextFragment, bouncerContext: BouncerContext) {}
        }
    }

    @BeforeEach
    fun setUp() {
        reset(userAuthContextProvider)
    }

    @Test
    fun buildContext() {
        val bouncerContext = buildBouncerContext()
        val userContext = com.rbkmoney.bouncer.ctx.ContextFragment()
        whenever(userAuthContextProvider.getUserAuthContext(bouncerContext.userId)).thenReturn(userContext)
        val resultContext = bouncerContextFactory.buildContext(bouncerContext)

        val userFragment = resultContext.fragments["user"]
        val generalFragment = ContextFragment()
        tDeserializer.deserialize(
            generalFragment,
            resultContext.fragments[bouncerProperties.contextFragmentId]!!.getContent()
        )

        assertEquals(userContext, userFragment)
        assertEquals(generalFragment.env.deployment, bouncerProperties.deploymentId)

        assertEquals(generalFragment.auth.token, Token().setId(bouncerContext.tokenId))
        assertEquals(generalFragment.auth.method, bouncerProperties.authMethod)
        assertEquals(generalFragment.auth.expiration, Instant.ofEpochSecond(bouncerContext.tokenExpiration).toString())
        verify(userAuthContextProvider).getUserAuthContext(bouncerContext.userId)
    }

    private fun buildBouncerContext() = object : BouncerContext() {
        override val userId = EnhancedRandom.random(String::class.java)
        override val tokenId = EnhancedRandom.random(String::class.java)
        override val tokenExpiration = EnhancedRandom.random(Long::class.java)
    }
}
