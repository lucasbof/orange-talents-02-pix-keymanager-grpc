package com.br.zup.pix.list

import com.br.zup.ProtoListKeyRequest
import com.br.zup.ProtoListKeyResponse
import com.br.zup.ProtoListKeyServiceGrpc
import com.br.zup.shared.grpc.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ListKeysEndpoint(
    val service: ListKeysService
) : ProtoListKeyServiceGrpc.ProtoListKeyServiceImplBase() {

    override fun listKeys(
        request: ProtoListKeyRequest,
        responseObserver: StreamObserver<ProtoListKeyResponse>
    ) {
        val keys = service.findKeys(request.clientId)

        responseObserver.onNext(ProtoListKeyResponse.newBuilder()
            .addAllKeys(keys)
            .build())

        responseObserver.onCompleted()
    }
}