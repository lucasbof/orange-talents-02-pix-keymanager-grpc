package com.br.zup.pix

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface PixKeyRepository: JpaRepository<PixKey, UUID> {

    fun existsByKey(key: String): Boolean
    fun findByIdAndClientId(id: UUID, clientId: UUID): Optional<PixKey>
    fun findByKey(key: String): Optional<PixKey>
}