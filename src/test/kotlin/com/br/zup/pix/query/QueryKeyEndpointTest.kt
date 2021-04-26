package com.br.zup.pix.query

import com.br.zup.*
import com.br.zup.integration.bcb.CentralBankClient
import com.br.zup.integration.bcb.createPixKey.BankAccount
import com.br.zup.integration.bcb.createPixKey.Owner
import com.br.zup.integration.bcb.createPixKey.PixKeyType
import com.br.zup.integration.bcb.findByKey.PixKeyDetailsResponse
import com.br.zup.pix.*
import com.br.zup.pix.register.RegisterKeyEndpointTest
import com.br.zup.util.violations
import com.google.protobuf.Timestamp
import com.google.rpc.Code.INVALID_ARGUMENT_VALUE
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
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.validation.constraints.NotBlank

@MicronautTest(transactional = false)
internal class QueryKeyEndpointTest(
    val repository: PixKeyRepository,
    val grpcClient: ProtoQueryKeyServiceGrpc.ProtoQueryKeyServiceBlockingStub
) {

    @Inject
    lateinit var bcbClient: CentralBankClient

    companion object {
        val CLIENT_ID: UUID = UUID.randomUUID()
    }

    @MockBean(CentralBankClient::class)
    fun bcbClient(): CentralBankClient? {
        return Mockito.mock(CentralBankClient::class.java)
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }


    @Test
    internal fun `deve trazer consulta quando clientId e pixId estao corretos`() {
        val pixKeyLocal = repository.save(pixKey())

        val response = grpcClient.queryKey(
            ProtoQueryKeyRequest.newBuilder()
                .setPixId(
                    ProtoQueryKeyRequest.ProtoPixIdClientId.newBuilder()
                        .setClientId(pixKeyLocal.clientId.toString())
                        .setPixId(pixKeyLocal.id.toString())
                        .build()
                )
                .build()
        )

        with(response) {
            assertEquals(pixKeyLocal.clientId.toString(), clientId)
            assertEquals(pixKeyLocal.id.toString(), pixId)
            assertEquals(pixKeyLocal.key, pixKey.key)
            assertEquals(convertToProtoTimestamp(pixKeyLocal.createdAt).seconds, pixKey.createdAt.seconds)
            assertEquals(ProtoKeyType.valueOf(pixKeyLocal.keyType.name), pixKey.type)
            assertEquals(ProtoAccountType.valueOf(pixKeyLocal.accountType.name), pixKey.account.type)
            assertEquals(pixKeyLocal.account.institution, pixKey.account.institution)
            assertEquals(pixKeyLocal.account.nameHolder, pixKey.account.holderName)
            assertEquals(pixKeyLocal.account.cpfHolder, pixKey.account.cpfHolder)
            assertEquals(pixKeyLocal.account.agency, pixKey.account.agency)
            assertEquals(pixKeyLocal.account.number, pixKey.account.accountNumber)
        }
    }

    @Test
    internal fun `deve retornar NOT_FOUND quando o pixId nao existir`() {
        val thrown = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.queryKey(
                ProtoQueryKeyRequest.newBuilder()
                    .setPixId(
                        ProtoQueryKeyRequest.ProtoPixIdClientId.newBuilder()
                            .setClientId(UUID.randomUUID().toString())
                            .setPixId(UUID.randomUUID().toString())
                            .build()
                    )
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("pixId nao encontrado ou nao pertence ao clientId", status.description)
        }
    }

    @Test
    internal fun `deve retornar NOT_FOUND quando o pixId existir mas o clientId nao for o dono da chave`() {
        val pixKey = repository.save(pixKey())

        val thrown = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.queryKey(
                ProtoQueryKeyRequest.newBuilder()
                    .setPixId(
                        ProtoQueryKeyRequest.ProtoPixIdClientId.newBuilder()
                            .setClientId(UUID.randomUUID().toString())
                            .setPixId(pixKey.id.toString())
                            .build()
                    )
                    .build()
            )
        }

        assertTrue(repository.existsById(pixKey.id!!))
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("pixId nao encontrado ou nao pertence ao clientId", status.description)
        }
    }


    @Test
    internal fun `deve consultar chave quando informado o valor de uma chave eh correto e existente`() {
        val pixKeyLocal = repository.save(pixKey())

        val response = grpcClient.queryKey(
            ProtoQueryKeyRequest.newBuilder()
                .setKey(pixKeyLocal.key)
                .build()
        )

        with(response) {
            assertEquals(pixKeyLocal.clientId.toString(), clientId)
            assertEquals(pixKeyLocal.id.toString(), pixId)
            assertEquals(pixKeyLocal.key, pixKey.key)
            assertEquals(convertToProtoTimestamp(pixKeyLocal.createdAt).seconds, pixKey.createdAt.seconds)
            assertEquals(ProtoKeyType.valueOf(pixKeyLocal.keyType.name), pixKey.type)
            assertEquals(ProtoAccountType.valueOf(pixKeyLocal.accountType.name), pixKey.account.type)
            assertEquals(pixKeyLocal.account.institution, pixKey.account.institution)
            assertEquals(pixKeyLocal.account.nameHolder, pixKey.account.holderName)
            assertEquals(pixKeyLocal.account.cpfHolder, pixKey.account.cpfHolder)
            assertEquals(pixKeyLocal.account.agency, pixKey.account.agency)
            assertEquals(pixKeyLocal.account.number, pixKey.account.accountNumber)
        }
    }

    @Test
    internal fun `deve consultar chave quando a chave nao existir na base do app mas existir na base do BCB`() {
        val responseBCB = responseBCB()
        val info = responseBCB.toModel()

        Mockito.`when`(bcbClient.findByKey(responseBCB.key))
            .thenReturn(HttpResponse.ok(responseBCB))

        val response = grpcClient.queryKey(
            ProtoQueryKeyRequest.newBuilder()
                .setKey(responseBCB.key)
                .build()
        )

        assertFalse(repository.existsByKey(info.key))
        with(response) {
            assertEquals("", clientId)
            assertEquals("", pixId)
            assertEquals(info.key, pixKey.key)
            assertEquals(convertToProtoTimestamp(info.createdAt).seconds, pixKey.createdAt.seconds)
            assertEquals(ProtoKeyType.valueOf(info.type.name), pixKey.type)
            assertEquals(ProtoAccountType.valueOf(info.accountType.name), pixKey.account.type)
            assertEquals(info.account.institution, pixKey.account.institution)
            assertEquals(info.account.nameHolder, pixKey.account.holderName)
            assertEquals(info.account.cpfHolder, pixKey.account.cpfHolder)
            assertEquals(info.account.agency, pixKey.account.agency)
            assertEquals(info.account.number, pixKey.account.accountNumber)
        }

    }

    @Test
    internal fun `deve retornar NOT_FOUND quando a chave nao existir nem na base do app e nem do BCB`() {
        val keyLocal = UUID.randomUUID().toString()

        Mockito.`when`(bcbClient.findByKey(keyLocal))
            .thenReturn(HttpResponse.notFound())

        val thrown = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.queryKey(
                ProtoQueryKeyRequest.newBuilder()
                    .setKey(keyLocal)
                    .build()
            )
        }

        assertFalse(repository.existsByKey(keyLocal))

        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada", status.description)
        }

    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT quando nem chave e nem combinacao pixId e clientId forem informados`() {
        val thrown = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.queryKey(
                ProtoQueryKeyRequest.newBuilder()
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("nem uma chave e nem uma combinacao de pixId e clientId foram informados", status.description)
        }
    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT com VIOLATIONS no metadado quando pixId ou clientId for invalidos`() {
        val thrown = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.queryKey(
                ProtoQueryKeyRequest.newBuilder()
                    .setPixId(
                        ProtoQueryKeyRequest.ProtoPixIdClientId.newBuilder()
                            .setClientId("")
                            .setPixId("")
                            .build()
                    )
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
                    Pair("pixId", "não deve estar em branco"),
                    Pair("pixId", "não é um formato válido de UUID"),
                )
            )
        }
    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT com VIOLATIONS no metadado quando chave for invalida`() {
        val thrown = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.queryKey(
                ProtoQueryKeyRequest.newBuilder()
                    .setKey("")
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            MatcherAssert.assertThat(
                violations(), Matchers.containsInAnyOrder(
                    Pair("key", "não deve estar em branco"),
                )
            )
        }
    }

    private fun responseBCB(): PixKeyDetailsResponse {
        return PixKeyDetailsResponse(
            keyType = PixKeyType.EMAIL,
            key = "lucas@gmail.com",
            bankAccount = BankAccount(
                participant = AssociatedAccount.ITAU_UNIBANCO_ISPB,
                branch = "7845",
                accountType = BankAccount.AccountTypeBank.CACC,
                accountNumber = "854152"
            ),
            owner = Owner(
                type = Owner.OwnerType.NATURAL_PERSON,
                name = "Lucas",
                taxIdNumber = "02467781054"
            ),
            createdAt = LocalDateTime.now()
        )
    }

    @Factory
    class ClientQuery {
        @Bean
        fun blockingStubQuery(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ProtoQueryKeyServiceGrpc.ProtoQueryKeyServiceBlockingStub? {
            return ProtoQueryKeyServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun pixKey(): PixKey {
        return PixKey(
            clientId = RegisterKeyEndpointTest.CLIENT_ID,
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

    fun convertToProtoTimestamp(time: LocalDateTime): Timestamp {
        val createdAt = time.atZone(ZoneId.of("UTC")).toInstant()
        return Timestamp.newBuilder()
            .setSeconds(createdAt.epochSecond)
            .setNanos(createdAt.nano)
            .build()
    }
}