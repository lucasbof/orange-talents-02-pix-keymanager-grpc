package com.br.zup.pix.remove

import com.br.zup.ProtoRemoveKeyRequest
import com.br.zup.ProtoRemoveKeyServiceGrpc
import com.br.zup.integration.bcb.CentralBankClient
import com.br.zup.integration.bcb.removePixKey.DeletePixKeyRequest
import com.br.zup.integration.bcb.removePixKey.DeletePixKeyResponse
import com.br.zup.pix.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RemoveKeyEndpointTest(
    val repository: PixKeyRepository,
    val grpcClient: ProtoRemoveKeyServiceGrpc.ProtoRemoveKeyServiceBlockingStub
) {

    @Inject
    lateinit var bcbClient: CentralBankClient

    @MockBean(CentralBankClient::class)
    fun bcbClient(): CentralBankClient? {
        return Mockito.mock(CentralBankClient::class.java)
    }

    companion object {
        val CLIENT_ID: UUID = UUID.randomUUID()
    }

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

    @Test
    internal fun `deve remover chave quando clientId eh dono da chave e pixId existe`() {
        val pixKey = repository.save(pixKey())

        Mockito.`when`(bcbClient.deletePixKey(
            key = pixKey.key,
            request = DeletePixKeyRequest(pixKey.key)
        ))
            .thenReturn(HttpResponse.ok(
                DeletePixKeyResponse(
                    pixKey.key,
                    AssociatedAccount.ITAU_UNIBANCO_ISPB,
                    LocalDateTime.now()
                )
            ))

        val response = grpcClient.removeKey(ProtoRemoveKeyRequest.newBuilder()
            .setClientId(CLIENT_ID.toString())
            .setPixId(pixKey.id.toString())
            .build())

        assertEquals(pixKey.clientId.toString(), response.clientId)
        assertEquals(pixKey.id.toString(), response.pixId)
        assertFalse(repository.existsById(pixKey.id!!))
    }

    @Test
    internal fun `deve lacar NOT_FOUND quando o pixId nao existir`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.removeKey(ProtoRemoveKeyRequest.newBuilder()
                .setClientId(CLIENT_ID.toString())
                .setPixId(UUID.randomUUID().toString())
                .build())
        }


        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("pixId nao encontrado ou nao pertence ao clientId", status.description)
        }
    }

    @Test
    internal fun `deve lacar NOT_FOUND quando o clientId nao dor dono do pixId`() {
        val pixKey = repository.save(pixKey())

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.removeKey(ProtoRemoveKeyRequest.newBuilder()
                .setClientId(UUID.randomUUID().toString())
                .setPixId(pixKey.id.toString())
                .build())
        }

        assertTrue(repository.existsById(pixKey.id!!))
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("pixId nao encontrado ou nao pertence ao clientId", status.description)
        }
    }

    @Test
    internal fun `nao deve deletar a chave e deve retornar FAILED_PRECONDITION quando a API do bcb falhar`() {
        val pixKey = repository.save(pixKey())

        Mockito.`when`(bcbClient.deletePixKey(
            key = pixKey.key,
            request = DeletePixKeyRequest(pixKey.key)
        ))
            .thenReturn(HttpResponse.notFound())

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.removeKey(ProtoRemoveKeyRequest.newBuilder()
                .setClientId(CLIENT_ID.toString())
                .setPixId(pixKey.id.toString())
                .build())
        }

        assertTrue(repository.existsById(pixKey.id!!))
        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro ao fazer a delecao da chave no bcb", status.description)
        }
    }

    private fun pixKey(): PixKey {
        return PixKey(
            clientId = CLIENT_ID,
            keyType = KeyType.EMAIL,
            accountType = AccountType.CHECKING_ACCOUNT,
            key = "lucas@gmail.com",
            account = AssociatedAccount(
                agency = "1250",
                number = "542097",
                institution = "UNIBANCO ITAU SA",
                cpfHolder = "63657520325",
                nameHolder = "lucas@gmail.com"
            )
        )
    }

    @Factory
    class ClientRemove {
        @Bean
        fun blockingStubRemove(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ProtoRemoveKeyServiceGrpc.ProtoRemoveKeyServiceBlockingStub? {
            return ProtoRemoveKeyServiceGrpc.newBlockingStub(channel)
        }
    }
}