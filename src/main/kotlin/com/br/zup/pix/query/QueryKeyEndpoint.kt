package com.br.zup.pix.query

import com.br.zup.ProtoQueryKeyRequest
import com.br.zup.ProtoQueryKeyResponse
import com.br.zup.ProtoQueryKeyServiceGrpc
import com.br.zup.integration.bcb.CentralBankClient
import com.br.zup.pix.PixKeyRepository
import com.br.zup.shared.grpc.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@ErrorHandler
@Singleton
class QueryKeyEndpoint(
    @Inject private val repository: PixKeyRepository,
    @Inject private val bcbClient: CentralBankClient,
    @Inject private val validator: Validator,
) : ProtoQueryKeyServiceGrpc.ProtoQueryKeyServiceImplBase() {

    override fun queryKey(
        request: ProtoQueryKeyRequest,
        responseObserver: StreamObserver<ProtoQueryKeyResponse>
    ) {

        val queryKeyFilter = request.toModel(validator)
        val pixKeyInfo = queryKeyFilter.filter(repository = repository, bcbClient = bcbClient)

        responseObserver.onNext(CreateProtoQueryKeyResponse.create(pixKeyInfo))
        responseObserver.onCompleted()
    }
}