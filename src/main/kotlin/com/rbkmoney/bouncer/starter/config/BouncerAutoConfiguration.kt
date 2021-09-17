package com.rbkmoney.bouncer.starter.config

import com.rbkmoney.bouncer.decisions.ArbiterSrv
import com.rbkmoney.bouncer.starter.UserAuthContextProvider
import com.rbkmoney.bouncer.starter.api.BouncerContextFactory
import com.rbkmoney.bouncer.starter.api.BouncerService
import com.rbkmoney.bouncer.starter.config.properties.BouncerProperties
import com.rbkmoney.orgmanagement.AuthContextProviderSrv
import com.rbkmoney.woody.api.trace.context.metadata.user.UserIdentityEmailExtensionKit
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource

@Configuration
class BouncerAutoConfiguration {

    @Bean
    fun orgManagerClient(
        @Value("\${orgManagement.url}") resource: Resource,
        @Value("\${orgManagement.networkTimeout}") networkTimeout: Int
    ): AuthContextProviderSrv.Iface =
        THSpawnClientBuilder()
            .withMetaExtensions(
                listOf(
                    UserIdentityEmailExtensionKit.INSTANCE
                )
            )
            .withNetworkTimeout(networkTimeout)
            .withAddress(resource.uri)
            .build(AuthContextProviderSrv.Iface::class.java)

    @Bean
    fun bouncerClient(
        @Value("\${bouncer.url}") resource: Resource,
        @Value("\${bouncer.networkTimeout}") networkTimeout: Int
    ): ArbiterSrv.Iface =
        THSpawnClientBuilder()
            .withNetworkTimeout(networkTimeout)
            .withAddress(resource.uri)
            .build(ArbiterSrv.Iface::class.java)

    @Bean
    fun userAuthContextProvider(
        authContextProvider: AuthContextProviderSrv.Iface
    ) = UserAuthContextProvider(authContextProvider)

    @Bean
    fun bouncerService(
        bouncerContextFactory: BouncerContextFactory,
        bouncerClient: ArbiterSrv.Iface,
        bouncerProperties: BouncerProperties
    ) = BouncerService(bouncerContextFactory, bouncerClient, bouncerProperties)
}
