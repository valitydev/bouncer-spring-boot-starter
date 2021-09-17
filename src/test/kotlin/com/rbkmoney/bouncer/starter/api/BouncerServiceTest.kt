package com.rbkmoney.bouncer.starter.api

import com.rbkmoney.bouncer.decisions.ArbiterSrv
import com.rbkmoney.bouncer.decisions.Context
import com.rbkmoney.bouncer.decisions.Judgement
import com.rbkmoney.bouncer.decisions.Resolution
import com.rbkmoney.bouncer.decisions.ResolutionAllowed
import com.rbkmoney.bouncer.decisions.ResolutionForbidden
import com.rbkmoney.bouncer.decisions.ResolutionRestricted
import com.rbkmoney.bouncer.decisions.RulesetNotFound
import com.rbkmoney.bouncer.starter.config.properties.BouncerProperties
import io.github.benas.randombeans.api.EnhancedRandom.random
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class BouncerServiceTest {

    private val bouncerClient: ArbiterSrv.Iface = mock { }

    private val bouncerContextFactory: BouncerContextFactory = mock { }

    private val bouncerProperties: BouncerProperties

    private val bouncerService: BouncerService

    init {
        bouncerProperties = BouncerProperties(
            contextFragmentId = random(String::class.java),
            deploymentId = random(String::class.java),
            authMethod = random(String::class.java),
            realm = random(String::class.java),
            ruleSetId = random(String::class.java)
        )
        bouncerService = BouncerService(bouncerContextFactory, bouncerClient, bouncerProperties)
    }

    @BeforeEach
    fun setUp() {
        reset(bouncerClient, bouncerContextFactory)
    }

    @Test
    fun `havePrivileges should fail because can't get user AuthContext`() {
        val bouncerContext = buildBouncerContext()
        whenever(bouncerContextFactory.buildContext(bouncerContext))
            .thenThrow(RuntimeException("Can't get user auth context"))
        val exception = assertThrows(RuntimeException::class.java) {
            bouncerService.havePrivileges(bouncerContext)
        }
        assertThat(exception.message, Matchers.containsString("Can't get user auth context"))
        verify(bouncerContextFactory).buildContext(bouncerContext)
    }

    @Test
    fun `havePrivileges should fail because of incorrect bouncer call`() {
        val bouncerContext = buildBouncerContext()
        whenever(bouncerContextFactory.buildContext(bouncerContext)).thenReturn(Context())
        whenever(bouncerClient.judge(any(), any())).thenThrow(RulesetNotFound())
        val exception = assertThrows(BouncerException::class.java) {
            bouncerService.havePrivileges(bouncerContext)
        }
        assertThat(exception.message, Matchers.containsString("Error while call bouncer"))
        verify(bouncerContextFactory).buildContext(bouncerContext)
        verify(bouncerClient).judge(any(), any())
    }

    @Test
    fun `havePrivileges should return restricted resolution`() {
        val bouncerContext = buildBouncerContext()
        whenever(bouncerContextFactory.buildContext(bouncerContext)).thenReturn(Context())
        val resolution = Resolution().apply { restricted = ResolutionRestricted() }
        val judgement = Judgement().apply { setResolution(resolution) }
        whenever(bouncerClient.judge(any(), any())).thenReturn(judgement)
        val result = bouncerService.havePrivileges(bouncerContext)
        assertFalse(result)
        verify(bouncerContextFactory).buildContext(bouncerContext)
        verify(bouncerClient).judge(any(), any())
    }

    @Test
    fun `havePrivileges should return allowed resolution`() {
        val bouncerContext = buildBouncerContext()
        whenever(bouncerContextFactory.buildContext(bouncerContext)).thenReturn(Context())
        val resolution = Resolution().apply { allowed = ResolutionAllowed() }
        val judgement = Judgement().apply { setResolution(resolution) }
        whenever(bouncerClient.judge(any(), any())).thenReturn(judgement)
        val result = bouncerService.havePrivileges(bouncerContext)
        assertTrue(result)
        verify(bouncerContextFactory).buildContext(bouncerContext)
        verify(bouncerClient).judge(any(), any())
    }

    @Test
    fun `havePrivileges should return forbidden resolution`() {
        val bouncerContext = buildBouncerContext()
        whenever(bouncerContextFactory.buildContext(bouncerContext)).thenReturn(Context())
        val resolution = Resolution().apply { forbidden = ResolutionForbidden() }
        val judgement = Judgement().apply { setResolution(resolution) }
        whenever(bouncerClient.judge(any(), any())).thenReturn(judgement)
        val result = bouncerService.havePrivileges(bouncerContext)
        assertFalse(result)
        verify(bouncerContextFactory).buildContext(bouncerContext)
        verify(bouncerClient).judge(any(), any())
    }

    private fun buildBouncerContext() = object : BouncerContext() {
        override val userId = random(String::class.java)
        override val tokenId = random(String::class.java)
        override val tokenExpiration = random(Long::class.java)
    }
}
