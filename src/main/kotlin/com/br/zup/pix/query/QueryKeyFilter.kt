package com.br.zup.pix.query

import com.br.zup.integration.bcb.CentralBankClient
import com.br.zup.pix.NotFoundPixKeyException
import com.br.zup.pix.PixKeyRepository
import com.br.zup.shared.validation.ValidUUID
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import org.hibernate.validator.constraints.Length
import java.util.*
import javax.validation.constraints.NotBlank

@Introspected
sealed class QueryKeyFilter {

    abstract fun filter(repository: PixKeyRepository, bcbClient: CentralBankClient): PixKeyInfo

    @Introspected
    data class SearchByPixId(
        @field:NotBlank @field:ValidUUID val clientId: String?,
        @field:NotBlank @field:ValidUUID val pixId: String?
    ) : QueryKeyFilter() {

        override fun filter(repository: PixKeyRepository, bcbClient: CentralBankClient): PixKeyInfo {
            val pixIdUuid = UUID.fromString(pixId)
            val clienteIdUuid = UUID.fromString(clientId)

            return repository.findByIdAndClientId(id = pixIdUuid, clientId = clienteIdUuid)
                .map {PixKeyInfo(it)}
                .orElseThrow {
                    NotFoundPixKeyException("pixId nao encontrado ou nao pertence ao clientId")
                }

        }
    }

    @Introspected
    data class SearchByKey(
        @field:NotBlank @field:Length(max = 77) val key: String?
    ) : QueryKeyFilter() {
        override fun filter(repository: PixKeyRepository, bcbClient: CentralBankClient): PixKeyInfo {
            return repository.findByKey(key!!)
                .map { PixKeyInfo(it) }
                .orElseGet {
                    val response = bcbClient.findByKey(key)
                    when (response.status) {
                        HttpStatus.OK -> response.body()?.toModel()
                        else -> throw NotFoundPixKeyException("Chave Pix não encontrada")
                    }
                }
        }
    }

    @Introspected
    class Invalid() : QueryKeyFilter() {
        override fun filter(repository: PixKeyRepository, bcbClient: CentralBankClient): PixKeyInfo {
            throw IllegalArgumentException("nem uma chave e nem uma combinacao de pixId e clientId foram informados")
        }

        override fun equals(other: Any?): Boolean {
            return this === other
        }

        override fun hashCode(): Int {
            return System.identityHashCode(this)
        }
    }
}
