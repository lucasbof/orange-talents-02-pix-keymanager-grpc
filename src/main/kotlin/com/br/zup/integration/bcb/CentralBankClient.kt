package com.br.zup.integration.bcb

import com.br.zup.integration.bcb.createPixKey.CreatePixKeyRequest
import com.br.zup.integration.bcb.createPixKey.CreatePixKeyResponse
import com.br.zup.integration.bcb.removePixKey.DeletePixKeyRequest
import com.br.zup.integration.bcb.removePixKey.DeletePixKeyResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.pix.url}")
interface CentralBankClient {

    @Post(value = "/api/v1/pix/keys", processes = [MediaType.APPLICATION_XML])
    fun createPixKey(@Body request: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>

    @Delete(value = "/api/v1/pix/keys/{key}", processes = [MediaType.APPLICATION_XML])
    fun deletePixKey(
        @PathVariable("key") key: String,
        @Body request: DeletePixKeyRequest
    ): HttpResponse<DeletePixKeyResponse>

}











