package com.br.zup.pix.register

import com.br.zup.ProtoAccountType
import com.br.zup.ProtoKeyType
import com.br.zup.ProtoRegisterKeyRequest
import com.br.zup.ProtoRegisterKeyServiceGrpc
import com.br.zup.integration.bcb.CentralBankClient
import com.br.zup.integration.bcb.createPixKey.*
import com.br.zup.integration.itau.AccountDataResponse
import com.br.zup.integration.itau.HolderResponse
import com.br.zup.integration.itau.InstitutionResponse
import com.br.zup.integration.itau.ItauCustomerAccountsClient
import com.br.zup.pix.*
import com.br.zup.util.violations
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
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

/**
 * TIP: Necessario desabilitar o controle transacional (transactional=false) pois o gRPC Server
 * roda numa thread separada, caso contrário não será possível preparar cenário dentro do método @Test
 */
@MicronautTest(transactional = false)
internal class RegisterKeyEndpointTest(
        val repository: PixKeyRepository,
        val grpcClient: ProtoRegisterKeyServiceGrpc.ProtoRegisterKeyServiceBlockingStub
) {
    @Inject
    lateinit var itauClient: ItauCustomerAccountsClient

    @Inject
    lateinit var bcbClient: CentralBankClient

    companion object {
        val CLIENT_ID: UUID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @MockBean(CentralBankClient::class)
    fun bcbClient(): CentralBankClient? {
        return Mockito.mock(CentralBankClient::class.java)
    }

    @MockBean(ItauCustomerAccountsClient::class)
    fun itauClient(): ItauCustomerAccountsClient? {
        return Mockito.mock(ItauCustomerAccountsClient::class.java)
    }

    @Test
    internal fun `deve registrar nova chave pix`() {
        Mockito.`when`(itauClient.searchAccountByType(CLIENT_ID.toString(),
                AccountType.CHECKING_ACCOUNT.portugueseName))
                .thenReturn(HttpResponse.ok(accountDataResponse()))

        Mockito.`when`(bcbClient.createPixKey(createPixKeyRequest()))
                .thenReturn(HttpResponse.created(createPixKeyResponse()))

        val response = grpcClient.registerKey(ProtoRegisterKeyRequest.newBuilder()
                .setClientId(CLIENT_ID.toString())
                .setKeyType(ProtoKeyType.EMAIL)
                .setKeyValue("lucas@gmail.com")
                .setAccountType(ProtoAccountType.CHECKING_ACCOUNT)
                .build())

        with(response) {
            assertNotNull(pixId)
        }
    }

    @Test
    internal fun `nao deve registrar chave pix quando chave existente`() {
        repository.save(pixKey())

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registerKey(ProtoRegisterKeyRequest.newBuilder()
                    .setClientId(CLIENT_ID.toString())
                    .setKeyType(ProtoKeyType.EMAIL)
                    .setKeyValue("lucas@gmail.com")
                    .setAccountType(ProtoAccountType.CHECKING_ACCOUNT)
                    .build())
        }

        with(thrown) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave Pix 'lucas@gmail.com' existente", status.description)
        }
    }

    @Test
    internal fun `nao deve registrar chave pix quando nao encontrar dados da conta cliente`() {
        Mockito.`when`(itauClient.searchAccountByType(
                CLIENT_ID.toString(),
                AccountType.CHECKING_ACCOUNT.portugueseName))
                .thenReturn(HttpResponse.notFound())

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registerKey(ProtoRegisterKeyRequest.newBuilder()
                    .setClientId(CLIENT_ID.toString())
                    .setKeyType(ProtoKeyType.EMAIL)
                    .setKeyValue("lucas@gmail.com")
                    .setAccountType(ProtoAccountType.CHECKING_ACCOUNT)
                    .build())
        }

        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Cliente não encontrado no Itau", status.description)
        }
    }

    @Test
    internal fun `nao deve registrar chave pix quando nao for possivel registrar chave no BCB`() {
        Mockito.`when`(itauClient.searchAccountByType(CLIENT_ID.toString(),
                AccountType.CHECKING_ACCOUNT.portugueseName))
                .thenReturn(HttpResponse.ok(accountDataResponse()))

        Mockito.`when`(bcbClient.createPixKey(createPixKeyRequest()))
                .thenReturn(HttpResponse.badRequest())

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registerKey(ProtoRegisterKeyRequest.newBuilder()
                    .setClientId(CLIENT_ID.toString())
                    .setKeyType(ProtoKeyType.EMAIL)
                    .setKeyValue("lucas@gmail.com")
                    .setAccountType(ProtoAccountType.CHECKING_ACCOUNT)
                    .build())
        }

        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro ao registrar chave Pix no Banco Central do Brasil (BCB)", status.description)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando parametros forem invalidos`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registerKey(ProtoRegisterKeyRequest.newBuilder().build())
        }
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertThat(violations(), containsInAnyOrder(
                Pair("clientId", "não deve estar em branco"),
                Pair("clientId", "não é um formato válido de UUID"),
                Pair("typeKey", "não deve ser nulo"),
                Pair("accountType", "não deve ser nulo"),
            ))
        }

    }


    // testando a integracao com o @ValidPixKey
    @Test
    fun `nao deve registrar chave pix quando a chave for invalida`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registerKey(ProtoRegisterKeyRequest.newBuilder()
                .setClientId(CLIENT_ID.toString())
                .setKeyType(ProtoKeyType.CPF)
                .setKeyValue("11122233344")
                .setAccountType(ProtoAccountType.CHECKING_ACCOUNT)
                .build())
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertThat(violations(), containsInAnyOrder(
                Pair("key", "chave Pix inválida"),
            ))
        }

    }


    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ProtoRegisterKeyServiceGrpc.ProtoRegisterKeyServiceBlockingStub? {
            return ProtoRegisterKeyServiceGrpc.newBlockingStub(channel)
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

    private fun createPixKeyRequest(): CreatePixKeyRequest {
        return CreatePixKeyRequest(
                keyType = PixKeyType.EMAIL,
                key = "lucas@gmail.com",
                bankAccount = bankAccount(),
                owner = owner()
        )
    }

    private fun createPixKeyResponse(): CreatePixKeyResponse {
        return CreatePixKeyResponse(
                keyType = PixKeyType.EMAIL,
                key = "lucas@gmail.com",
                bankAccount = bankAccount(),
                owner = owner(),
                createdAt = LocalDateTime.now()
        )
    }

    private fun accountDataResponse(): AccountDataResponse {
        return AccountDataResponse(
                tipo = AccountType.CHECKING_ACCOUNT.portugueseName,
                instituicao = InstitutionResponse("UNIBANCO ITAU SA", AssociatedAccount.ITAU_UNIBANCO_ISPB),
                agencia = "1250",
                numero = "542097",
                titular = HolderResponse("Lucas", "63657520325")
        )
    }

    private fun bankAccount(): BankAccount {
        return BankAccount(
                participant = AssociatedAccount.ITAU_UNIBANCO_ISPB,
                branch = "1250",
                accountNumber = "542097",
                accountType = BankAccount.AccountTypeBank.CACC
        )
    }

    private fun owner(): Owner {
        return Owner(
                type = Owner.OwnerType.NATURAL_PERSON,
                name = "Lucas",
                taxIdNumber = "63657520325")
    }
}