package com.br.zup.integration.bcb.createPixKey

data class Owner(
        val type: OwnerType,
        val name: String,
        val taxIdNumber: String
) {

    enum class OwnerType {
        NATURAL_PERSON,
        LEGAL_PERSON
    }
}