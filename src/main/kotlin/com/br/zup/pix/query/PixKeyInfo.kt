package com.br.zup.pix.query

import com.br.zup.pix.AccountType
import com.br.zup.pix.AssociatedAccount
import com.br.zup.pix.KeyType
import com.br.zup.pix.PixKey
import java.time.LocalDateTime
import java.util.*

data class PixKeyInfo(
    val pixId: UUID? = null,
    val clientId: UUID? = null,
    val type: KeyType,
    val key: String,
    val accountType: AccountType,
    val account: AssociatedAccount,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {

    constructor(pixKey: PixKey) : this(
        pixId = pixKey.id,
        clientId = pixKey.clientId,
        type = pixKey.keyType,
        key = pixKey.key,
        accountType = pixKey.accountType,
        account = pixKey.account,
        createdAt = pixKey.createdAt
    )
}
