package com.br.zup.shared.grpc.handlers

import com.br.zup.pix.ExistingPixKeyException
import com.br.zup.shared.grpc.ExceptionHandler
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ExistingPixKeyExceptionHandler : ExceptionHandler<ExistingPixKeyException> {
    override fun handle(e: ExistingPixKeyException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(Status.ALREADY_EXISTS
                .withDescription(e.message)
                .withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return e is ExistingPixKeyException
    }


}