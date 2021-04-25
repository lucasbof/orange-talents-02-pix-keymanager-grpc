package com.br.zup.pix.register

import com.br.zup.integration.bcb.CentralBankClient
import com.br.zup.integration.bcb.createPixKey.CreatePixKeyRequest
import com.br.zup.integration.itau.ItauCustomerAccountsClient
import com.br.zup.pix.ExistingPixKeyException
import com.br.zup.pix.NotFoundPixKeyException
import com.br.zup.pix.PixKey
import com.br.zup.pix.PixKeyRepository
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NewKeyPixService(
        @Inject val repository: PixKeyRepository,
        @Inject val itauClient: ItauCustomerAccountsClient,
        @Inject val bcbClient: CentralBankClient,
) {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun register(@Valid newKey: NewPixKey): PixKey {
        if (repository.existsByKey(newKey.key!!))
            throw ExistingPixKeyException("Chave Pix '${newKey.key}' existente")

        val response = itauClient.searchAccountByType(newKey.clientId!!, newKey.accountType!!.portugueseName)
        val associatedAccount = response.body()?.toModel()
                ?: throw IllegalStateException("Cliente não encontrado no Itau")

        val pixKey = newKey.toModel(associatedAccount)
        repository.save(pixKey)

        val bcbRequest = CreatePixKeyRequest.of(pixKey).also {
            LOGGER.info("Registrando chave Pix no Banco Central do Brasil (BCB): $it")
        }

        val bcbResponse = bcbClient.createPixKey(bcbRequest)
        if (bcbResponse.status != HttpStatus.CREATED)
            throw IllegalStateException("Erro ao registrar chave Pix no Banco Central do Brasil (BCB)")

        pixKey.update(bcbResponse.body()!!.key)

        return pixKey
    }

}
