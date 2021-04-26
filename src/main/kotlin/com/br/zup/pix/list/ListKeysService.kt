package com.br.zup.pix.list

import com.br.zup.ProtoAccountType
import com.br.zup.ProtoKeyType
import com.br.zup.ProtoListKeyResponse
import com.br.zup.pix.PixKeyRepository
import com.br.zup.shared.validation.ValidUUID
import com.google.protobuf.Timestamp
import io.micronaut.validation.Validated
import java.time.ZoneId
import java.util.*
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class ListKeysService(
    val repository: PixKeyRepository
) {
    @Transactional
    fun findKeys(@NotBlank @ValidUUID clientId: String?): List<ProtoListKeyResponse.ProtoListObj> {
        return repository.findByClientId(UUID.fromString(clientId))
            .map {
                ProtoListKeyResponse.ProtoListObj.newBuilder()
                    .setPixId(it.id.toString())
                    .setClientId(it.clientId.toString())
                    .setKeyType(ProtoKeyType.valueOf(it.keyType.name))
                    .setKey(it.key)
                    .setAccountType(ProtoAccountType.valueOf(it.accountType.name))
                    .setCreatedAt(
                        it.createdAt.let { time ->
                            val createdAt = time.atZone(ZoneId.of("UTC")).toInstant()
                            Timestamp.newBuilder()
                                .setSeconds(createdAt.epochSecond)
                                .setNanos(createdAt.nano)
                                .build()
                        })
                    .build()
            }

    }
}