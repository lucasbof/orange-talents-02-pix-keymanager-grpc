package com.br.zup.pix.remove

import com.br.zup.ProtoRemoveKeyRequest
import com.br.zup.ProtoRemoveKeyResponse
import com.br.zup.ProtoRemoveKeyServiceGrpc
import com.br.zup.shared.grpc.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RemoveKeyEndpoint(@Inject val service: RemoveKeyService)
    : ProtoRemoveKeyServiceGrpc.ProtoRemoveKeyServiceImplBase() {

    override fun removeKey(
        request: ProtoRemoveKeyRequest,
        responseObserver: StreamObserver<ProtoRemoveKeyResponse>
    ) {

        service.remove(request.clientId, request.pixId)

        responseObserver.onNext(ProtoRemoveKeyResponse.newBuilder()
            .setClientId(request.clientId)
            .setPixId(request.pixId)
            .build())

        responseObserver.onCompleted()
    }
}