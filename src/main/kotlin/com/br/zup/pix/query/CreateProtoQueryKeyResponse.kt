package com.br.zup.pix.query

import com.br.zup.ProtoAccountType
import com.br.zup.ProtoKeyType
import com.br.zup.ProtoQueryKeyResponse
import com.google.protobuf.Timestamp
import java.time.ZoneId

class CreateProtoQueryKeyResponse {

    companion object {
        fun create(pixKeyInfo: PixKeyInfo): ProtoQueryKeyResponse {
            return ProtoQueryKeyResponse.newBuilder()
                .setClientId(pixKeyInfo.clientId?.toString() ?: "")
                .setPixId(pixKeyInfo.pixId?.toString() ?: "")
                .setPixKey(
                    ProtoQueryKeyResponse.ProtoQueryPixKey.newBuilder()
                        .setType(ProtoKeyType.valueOf(pixKeyInfo.type.name))
                        .setKey(pixKeyInfo.key)
                        .setCreatedAt(
                            pixKeyInfo.createdAt.let {
                                val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                                Timestamp.newBuilder()
                                    .setSeconds(createdAt.epochSecond)
                                    .setNanos(createdAt.nano)
                                    .build()
                            })
                        .setAccount(ProtoQueryKeyResponse.ProtoQueryPixKey.ProtoQueryAccountInfo.newBuilder()
                            .setType(ProtoAccountType.valueOf(pixKeyInfo.accountType.name))
                            .setInstitution(pixKeyInfo.account.institution)
                            .setHolderName(pixKeyInfo.account.nameHolder)
                            .setCpfHolder(pixKeyInfo.account.cpfHolder)
                            .setAgency(pixKeyInfo.account.agency)
                            .setAccountNumber(pixKeyInfo.account.number)
                        .build())
                )
                .build()
        }
    }
}
