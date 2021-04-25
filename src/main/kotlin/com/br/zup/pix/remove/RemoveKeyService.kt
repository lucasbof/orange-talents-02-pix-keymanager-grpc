package com.br.zup.pix.remove

import com.br.zup.integration.bcb.CentralBankClient
import com.br.zup.integration.bcb.removePixKey.DeletePixKeyRequest
import com.br.zup.pix.NotFoundPixKeyException
import com.br.zup.pix.PixKeyRepository
import com.br.zup.shared.validation.ValidUUID
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import java.lang.IllegalStateException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class RemoveKeyService(
    @Inject val repository: PixKeyRepository,
    @Inject val bcbClient: CentralBankClient
) {

    @Transactional
    fun remove(
        @ValidUUID @NotBlank clientId: String?,
        @ValidUUID @NotBlank pixId: String?
    ) {
        val uuidClientId = UUID.fromString(clientId)
        val uuidPixId = UUID.fromString(pixId)

        val pixKey = repository.findByIdAndClientId(id = uuidPixId, clientId = uuidClientId)
            .orElseThrow {
                NotFoundPixKeyException("pixId nao encontrado ou nao pertence ao clientId")
            }

        repository.delete(pixKey)

        val response = bcbClient.deletePixKey(
            request = DeletePixKeyRequest(pixKey.key),
            key = pixKey.key
        )
        if (response.status != HttpStatus.OK) {
            throw IllegalStateException("Erro ao fazer a delecao da chave no bcb")
        }
    }
}