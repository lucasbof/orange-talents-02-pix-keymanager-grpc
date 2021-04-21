package com.br.zup.integration.bcb

import com.br.zup.integration.bcb.createPixKey.CreatePixKeyRequest
import com.br.zup.integration.bcb.createPixKey.CreatePixKeyResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.pix.url}")
interface CentralBankClient {

    @Post("/api/v1/pix/keys", processes = [MediaType.APPLICATION_XML])
    fun createPixKey(@Body request: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>

}











