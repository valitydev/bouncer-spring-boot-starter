package com.rbkmoney.bouncer.starter.api

import com.rbkmoney.bouncer.decisions.ArbiterSrv
import com.rbkmoney.bouncer.starter.config.properties.BouncerProperties
import org.apache.thrift.TException

open class BouncerService(
    private val bouncerContextFactory: BouncerContextFactory,
    private val bouncerClient: ArbiterSrv.Iface,
    private val bouncerProperties: BouncerProperties
) {

    fun havePrivileges(bouncerContext: BouncerContext): Boolean =
        try {
            val context = bouncerContextFactory.buildContext(bouncerContext)
            val judge = bouncerClient.judge(bouncerProperties.ruleSetId, context)
            val resolution = judge.getResolution()
            resolution.isSetAllowed
        } catch (e: TException) {
            throw BouncerException("Error while call bouncer", e)
        }
}
