package com.br.zup.pix

import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Embeddable
class AssociatedAccount(

        @field:NotBlank
        @Column(name = "account_agency", nullable = false)
        val agency: String,

        @field:NotBlank
        @Column(name = "account_number", nullable = false)
        val number: String,

        @field:NotBlank
        @Column(name = "account_institution", nullable = false)
        val institution: String,

        @field:NotBlank
        @Column(name = "account_cpf_holder", nullable = false)
        val cpfHolder: String,

        @field:NotBlank
        @Column(name = "account_name_holder", nullable = false)
        val nameHolder: String

) {
        companion object {
                val ITAU_UNIBANCO_ISPB: String = "60701190"
        }
}