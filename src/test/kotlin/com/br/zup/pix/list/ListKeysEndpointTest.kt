package com.br.zup.pix.list

import com.br.zup.ProtoListKeyRequest
import com.br.zup.ProtoListKeyResponse
import com.br.zup.ProtoListKeyServiceGrpc
import com.br.zup.pix.*
import com.br.zup.util.violations
import com.google.protobuf.Timestamp
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@MicronautTest(transactional = false)
internal class ListKeysEndpointTest(
    val repository: PixKeyRepository,
    val grpcClient: ProtoListKeyServiceGrpc.ProtoListKeyServiceBlockingStub
) {

    companion object {
        val CLIENT_ID: UUID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    internal fun `deve trazer lista de chaves de um clientId valido`() {
        val pixKey1 = repository.save(pixKey(CLIENT_ID))
        val pixKey2 = repository.save(pixKey(CLIENT_ID))
        repository.save(pixKey(UUID.randomUUID()))

        val response = grpcClient.listKeys(
            ProtoListKeyRequest.newBuilder()
                .setClientId(CLIENT_ID.toString())
                .build()
        )

        with(response) {
            assertEquals(2, keysList.size)
            assertPixKeyObj(keysList = keysList, pixKeyLocal = pixKey1)
            assertPixKeyObj(keysList = keysList, pixKeyLocal = pixKey2)
        }
    }

    @Test
    internal fun `deve retornar lista vazia quando nao houver chaves para um determinado clientId`() {
        repository.save(pixKey(UUID.randomUUID()))

        val response = grpcClient.listKeys(
            ProtoListKeyRequest.newBuilder()
                .setClientId(CLIENT_ID.toString())
                .build()
        )

        with(response) {
            assertEquals(0, keysList.size)
        }
    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT com as VIOLATIONS no metadado quando o clientId eh invalido`() {
        val thrown = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.listKeys(
                ProtoListKeyRequest.newBuilder()
                    .setClientId("")
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            MatcherAssert.assertThat(
                violations(), Matchers.containsInAnyOrder(
                    Pair("clientId", "não deve estar em branco"),
                    Pair("clientId", "não é um formato válido de UUID"),
                )
            )
        }
    }

    fun assertPixKeyObj(
        pixKeyLocal: PixKey,
        keysList: List<ProtoListKeyResponse.ProtoListObj>
    ) {
        val pixResponse1 = keysList.filter { pixKeyLocal.id.toString() == it.pixId }
        assertEquals(1, pixResponse1.size)
        assertEquals(pixKeyLocal.id.toString(), pixResponse1[0].pixId)
        assertEquals(pixKeyLocal.clientId.toString(), pixResponse1[0].clientId)
        assertEquals(pixKeyLocal.keyType.name, pixResponse1[0].keyType.name)
        assertEquals(pixKeyLocal.key, pixResponse1[0].key)
        assertEquals(pixKeyLocal.accountType.name, pixResponse1[0].accountType.name)
        assertEquals(convertToProtoTimestamp(pixKeyLocal.createdAt).seconds, pixResponse1[0].createdAt.seconds)
    }

    fun convertToProtoTimestamp(time: LocalDateTime): Timestamp {
        val createdAt = time.atZone(ZoneId.of("UTC")).toInstant()
        return Timestamp.newBuilder()
            .setSeconds(createdAt.epochSecond)
            .setNanos(createdAt.nano)
            .build()
    }

    private fun pixKey(cliendId: UUID): PixKey {
        return PixKey(
            clientId = cliendId,
            keyType = KeyType.RANDOM_KEY,
            accountType = AccountType.CHECKING_ACCOUNT,
            key = UUID.randomUUID().toString(),
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
    class ClientList {
        @Bean
        fun blockingStubList(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ProtoListKeyServiceGrpc.ProtoListKeyServiceBlockingStub? {
            return ProtoListKeyServiceGrpc.newBlockingStub(channel)
        }
    }

}