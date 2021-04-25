package com.br.zup.shared.grpc.handlers

import com.br.zup.pix.NotFoundPixKeyException
import com.br.zup.shared.grpc.ExceptionHandler
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class NotFoundPixKeyExceptionHandler : ExceptionHandler<NotFoundPixKeyException> {
    override fun handle(e: NotFoundPixKeyException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(Status.NOT_FOUND
                .withDescription(e.message)
                .withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return e is NotFoundPixKeyException
    }


}