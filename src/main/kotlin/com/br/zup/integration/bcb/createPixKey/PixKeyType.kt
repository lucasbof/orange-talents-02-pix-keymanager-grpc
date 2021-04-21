package com.br.zup.integration.bcb.createPixKey

import com.br.zup.pix.KeyType

enum class PixKeyType(val domainType: KeyType?) {

    CPF(KeyType.CPF),
    CNPJ(null),
    PHONE(KeyType.CELL_PHONE),
    EMAIL(KeyType.EMAIL),
    RANDOM(KeyType.RANDOM_KEY);

    companion object {

        private val mapping = values().associateBy(PixKeyType::domainType)

        fun by(domainType: KeyType): PixKeyType {
            return mapping[domainType]
                    ?: throw IllegalArgumentException("PixKeyType invalid or not found for $domainType")
        }
    }
}