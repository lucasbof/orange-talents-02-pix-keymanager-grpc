package com.br.zup.pix.register

import com.br.zup.ProtoAccountType
import com.br.zup.ProtoKeyType
import com.br.zup.ProtoRegisterKeyRequest
import com.br.zup.pix.AccountType
import com.br.zup.pix.KeyType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class GrpcExtensionsKtTest {

    @Test
    internal fun `typeKey deve ser null quando for UNKNOWN_KEY_TYPE`() {
        val request = ProtoRegisterKeyRequest.newBuilder()
            .setClientId("445455454")
            .setAccountType(ProtoAccountType.CHECKING_ACCOUNT)
            .setKeyType(ProtoKeyType.UNKNOWN_KEY_TYPE)
            .setKeyValue("4545")
            .build()

        val pixKey = request.toModel()

        assertEquals(null, pixKey.typeKey)
    }

    @Test
    internal fun `typeKey deve ser um dos tipos de KeyType quando nao for UNKNOWN_KEY_TYPE`() {
        val request = ProtoRegisterKeyRequest.newBuilder()
            .setClientId("445455454")
            .setAccountType(ProtoAccountType.CHECKING_ACCOUNT)
            .setKeyType(ProtoKeyType.CPF)
            .setKeyValue("4545")
            .build()

        val pixKey = request.toModel()

        assertEquals(KeyType.CPF, pixKey.typeKey)
    }

    @Test
    internal fun `accountType deve ser null quando for UNKNOWN_ACCOUNT_TYPE`() {
        val request = ProtoRegisterKeyRequest.newBuilder()
            .setClientId("445455454")
            .setAccountType(ProtoAccountType.UNKNOWN_ACCOUNT_TYPE)
            .setKeyType(ProtoKeyType.CPF)
            .setKeyValue("4545")
            .build()

        val pixKey = request.toModel()

        assertEquals(null, pixKey.accountType)
    }

    @Test
    internal fun `accountType deve deve ser um dos tipos de AccountType quando nao for UNKNOWN_ACCOUNT_TYPE`() {
        val request = ProtoRegisterKeyRequest.newBuilder()
            .setClientId("445455454")
            .setAccountType(ProtoAccountType.SAVINGS_ACCOUNT)
            .setKeyType(ProtoKeyType.CPF)
            .setKeyValue("4545")
            .build()

        val pixKey = request.toModel()

        assertEquals(AccountType.SAVINGS_ACCOUNT, pixKey.accountType)
    }

}