package com.br.zup.pix.register

import com.br.zup.ProtoRegisterKeyRequest
import com.br.zup.ProtoRegisterKeyResponse
import com.br.zup.ProtoRegisterKeyServiceGrpc
import com.br.zup.shared.grpc.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RegisterKeyEndpoint(@Inject private val service: NewKeyPixService)
    : ProtoRegisterKeyServiceGrpc.ProtoRegisterKeyServiceImplBase() {

    override fun registerKey(request: ProtoRegisterKeyRequest,
                             responseObserver: StreamObserver<ProtoRegisterKeyResponse>) {
        val newKey = request.toModel()
        val createdKey = service.register(newKey)

        val response = ProtoRegisterKeyResponse
                .newBuilder()
                .setPixId(createdKey.id.toString())
                .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}