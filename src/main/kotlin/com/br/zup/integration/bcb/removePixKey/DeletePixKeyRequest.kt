package com.br.zup.integration.bcb.removePixKey

import com.br.zup.pix.AssociatedAccount

data class DeletePixKeyRequest(
    val key: String,
    val participant: String = AssociatedAccount.ITAU_UNIBANCO_ISPB
)
