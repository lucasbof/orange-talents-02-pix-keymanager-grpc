package com.br.zup.pix

import com.br.zup.pix.AccountType
import com.br.zup.pix.AssociatedAccount
import com.br.zup.pix.KeyType
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
@Table(name = "tb_pix_key")
class PixKey(

        @field:NotNull
        @Column(nullable = false)
        val clientId: UUID,

        @field:NotNull
        @Column(nullable = false)
        @Enumerated(EnumType.STRING)
        val keyType: KeyType,

        @field:NotNull
        @Column(name = "account_type", nullable = false)
        @Enumerated(EnumType.STRING)
        val accountType: AccountType,

        @field:NotBlank
        @field:Size(max = 77)
        @Column(nullable = false, length = 77, name = "key", unique = true)
        var key: String,

        @Embedded
        val account: AssociatedAccount
) {
    @Id
    @GeneratedValue
    var id: UUID? = null

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    fun update(key: String): Boolean {
        if(keyType == KeyType.RANDOM_KEY) {
            this.key = key
            return true
        }
        return false
    }
}
