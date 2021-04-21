package com.br.zup.pix

import io.micronaut.validation.validator.constraints.EmailValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

enum class KeyType {
    CPF {
        override fun validate(key: String?): Boolean {
            if (key.isNullOrBlank()) {
                return false
            }

            if (!key.matches("^[0-9]{11}\$".toRegex())) {
                return false
            }

            return CPFValidator().run {
                initialize(null)
                isValid(key, null)
            }
        }
    },
    CELL_PHONE {
        override fun validate(key: String?): Boolean {
            if(key.isNullOrBlank()) {
                return false
            }
            return key.matches("^\\\\+[1-9][0-9]\\\\d{1,14}\\\$".toRegex())
        }
    },
    EMAIL {
        override fun validate(key: String?): Boolean {
            if(key.isNullOrBlank()) {
                return false
            }
            return EmailValidator().run {
                initialize(null)
                isValid(key, null)
            }
        }
    },
    RANDOM_KEY {
        override fun validate(key: String?): Boolean = key.isNullOrBlank()
    };

    abstract fun validate(key: String?): Boolean
}