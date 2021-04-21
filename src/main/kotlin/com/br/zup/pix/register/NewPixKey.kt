package com.br.zup.pix.register

import com.br.zup.pix.*
import com.br.zup.shared.validation.ValidUUID
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
@Introspected
data class NewPixKey(
        @field:ValidUUID
        @field:NotBlank
        val clientId: String?,
        @field:NotNull
        val typeKey: KeyType?,
        @field:Size(max = 77)
        val key: String?,
        @field:NotNull
        val accountType: AccountType?
) {

    fun toModel(account: AssociatedAccount): PixKey {
        return PixKey(
                clientId = UUID.fromString(this.clientId),
                keyType = KeyType.valueOf(this.typeKey!!.name),
                key = if (this.typeKey == KeyType.RANDOM_KEY) UUID.randomUUID().toString() else this.key!!,
                accountType = AccountType.valueOf(this.accountType!!.name),
                account = account
        )
    }

}