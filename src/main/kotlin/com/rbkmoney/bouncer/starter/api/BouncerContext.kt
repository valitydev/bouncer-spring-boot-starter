package com.rbkmoney.bouncer.starter.api

abstract class BouncerContext {
    abstract val userId: String
    abstract val tokenId: String
    abstract val tokenExpiration: Long
}
