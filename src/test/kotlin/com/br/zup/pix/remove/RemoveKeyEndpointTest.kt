package com.br.zup.pix.remove

import com.br.zup.*
import com.br.zup.pix.*
import com.br.zup.pix.register.RegisterKeyEndpointTest
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)
internal class RemoveKeyEndpointTest(
    val repository: PixKeyRepository,
    val grpcClient: ProtoRemoveKeyServiceGrpc.ProtoRemoveKeyServiceBlockingStub
) {

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