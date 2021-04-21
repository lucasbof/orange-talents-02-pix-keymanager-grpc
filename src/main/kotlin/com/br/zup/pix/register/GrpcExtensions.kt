package com.br.zup.pix.register

import com.br.zup.ProtoAccountType
import com.br.zup.ProtoKeyType
import com.br.zup.ProtoRegisterKeyRequest
import com.br.zup.pix.AccountType
import com.br.zup.pix.KeyType

fun ProtoRegisterKeyRequest.toModel() : NewPixKey {
    return NewPixKey(
            clientId = clientId,
            typeKey = when (keyType) {
                ProtoKeyType.UNKNOWN_KEY_TYPE -> null
                else -> KeyType.valueOf(keyType.name)
            },
            key = keyValue,
            accountType = when (accountType) {
                ProtoAccountType.UNKNOWN_ACCOUNT_TYPE -> null
                else -> AccountType.valueOf(accountType.name)
            }
    )
}