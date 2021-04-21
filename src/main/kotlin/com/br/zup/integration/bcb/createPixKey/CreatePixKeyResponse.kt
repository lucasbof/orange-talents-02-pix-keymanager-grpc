package com.br.zup.integration.bcb.createPixKey

import java.time.LocalDateTime

data class CreatePixKeyResponse(
        val keyType: PixKeyType,
        val key: String,
        val bankAccount: BankAccount,
        val owner: Owner,
        val createdAt: LocalDateTime
)
