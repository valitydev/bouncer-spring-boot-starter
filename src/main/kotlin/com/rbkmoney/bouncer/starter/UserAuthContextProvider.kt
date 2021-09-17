package com.rbkmoney.bouncer.starter

import com.rbkmoney.bouncer.ctx.ContextFragment
import com.rbkmoney.orgmanagement.AuthContextProviderSrv

open class UserAuthContextProvider(
    private val authContextProvider: AuthContextProviderSrv.Iface
) {

    fun getUserAuthContext(userId: String): ContextFragment =
        try {
            authContextProvider.getUserContext(userId) // ToDo: cache it
        } catch (ex: Exception) {
            throw RuntimeException("Can't get user auth context: userId = $userId", ex)
        }
}
