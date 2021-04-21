package com.br.zup.integration.itau

import com.br.zup.pix.AssociatedAccount

data class AccountDataResponse(
        val tipo: String,
        val instituicao: InstitutionResponse,
        val agencia: String,
        val numero: String,
        val titular: HolderResponse
) {

    fun toModel(): AssociatedAccount {
        return AssociatedAccount(
                institution = this.instituicao.nome,
                nameHolder = this.titular.nome,
                cpfHolder = this.titular.cpf,
                agency = this.agencia,
                number = this.numero
        )
    }
}
data class HolderResponse(val nome: String, val cpf: String)
data class InstitutionResponse(val nome: String, val ispb: String)