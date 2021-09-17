package com.rbkmoney.bouncer.starter.api

import com.rbkmoney.bouncer.decisions.Context

interface BouncerContextFactory {
    fun buildContext(bouncerContext: BouncerContext): Context
}
