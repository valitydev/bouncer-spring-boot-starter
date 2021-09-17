package com.rbkmoney.bouncer.starter

import com.rbkmoney.bouncer.context.v1.Auth
import com.rbkmoney.bouncer.context.v1.ContextFragment
import com.rbkmoney.bouncer.context.v1.Deployment
import com.rbkmoney.bouncer.context.v1.Environment
import com.rbkmoney.bouncer.context.v1.Token
import com.rbkmoney.bouncer.ctx.ContextFragmentType
import com.rbkmoney.bouncer.decisions.Context
import com.rbkmoney.bouncer.starter.api.BouncerContext
import com.rbkmoney.bouncer.starter.api.BouncerContextFactory
import com.rbkmoney.bouncer.starter.config.properties.BouncerProperties
import mu.KotlinLogging
import org.apache.thrift.TSerializer
import org.springframework.boot.context.properties.EnableConfigurationProperties
import java.time.Instant

@EnableConfigurationProperties(BouncerProperties::class)
abstract class AbstractBouncerContextFactory(
    private val bouncerProperties: BouncerProperties,
    private val userAuthContextProvider: UserAuthContextProvider
) : BouncerContextFactory {

    private val log = KotlinLogging.logger {}

    override fun buildContext(bouncerContext: BouncerContext): Context {
        val contextFragment = buildContextFragment(bouncerContext)
        val serializer = TSerializer()
        val fragment = com.rbkmoney.bouncer.ctx.ContextFragment().apply {
            setType(ContextFragmentType.v1_thrift_binary)
            setContent(serializer.serialize(contextFragment))
        }

        return Context().apply {
            putToFragments(bouncerProperties.contextFragmentId, fragment)
            putToFragments("user", buildUser(bouncerContext.userId))
        }
    }

    protected fun buildContextFragment(bouncerContext: BouncerContext): ContextFragment {
        val contextFragment = ContextFragment()
            .setAuth(buildAuth(bouncerContext.tokenId, bouncerContext.tokenExpiration))
            .setEnv(buildEnvironment())
        customizeContext(contextFragment, bouncerContext)
        log.debug { "Context fragment to bouncer $contextFragment" }
        return contextFragment
    }

    protected fun buildUser(userId: String) = userAuthContextProvider.getUserAuthContext(userId)

    protected fun buildAuth(tokenId: String, tokenExpiration: Long) =
        Auth()
            .setToken(Token().setId(tokenId))
            .setMethod(bouncerProperties.authMethod)
            .setExpiration(Instant.ofEpochSecond(tokenExpiration).toString())

    protected fun buildEnvironment(): Environment {
        val deployment = Deployment().apply {
            id = bouncerProperties.deploymentId
        }
        return Environment().apply {
            this.deployment = deployment
            this.now = Instant.now().toString()
        }
    }

    protected abstract fun customizeContext(contextFragment: ContextFragment, bouncerContext: BouncerContext)
}
