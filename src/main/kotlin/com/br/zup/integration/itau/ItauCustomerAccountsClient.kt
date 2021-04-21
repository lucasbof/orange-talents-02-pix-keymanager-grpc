package com.br.zup.integration.itau

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${itau.contas.url}")
interface ItauCustomerAccountsClient {

    @Get("/api/v1/clientes/{clientId}/contas")
    fun searchAccountByType(@PathVariable clientId: String, @QueryValue(value = "tipo") accountType: String): HttpResponse<AccountDataResponse>

}