package com.br.zup.integration.bcb.findByKey

import com.br.zup.integration.bcb.createPixKey.BankAccount
import com.br.zup.integration.bcb.createPixKey.Owner
import com.br.zup.integration.bcb.createPixKey.PixKeyType
import com.br.zup.pix.*
import com.br.zup.pix.query.PixKeyInfo
import java.time.LocalDateTime

data class PixKeyDetailsResponse(
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
) {

    fun toModel(): PixKeyInfo {
        return PixKeyInfo(
            key = key,
            type = keyType.domainType!!,
            accountType = when(bankAccount.accountType) {
                BankAccount.AccountTypeBank.CACC -> AccountType.CHECKING_ACCOUNT
                BankAccount.AccountTypeBank.SVGS -> AccountType.SAVINGS_ACCOUNT
            },
            createdAt = createdAt,
            account = AssociatedAccount(
                agency = bankAccount.branch,
                number = bankAccount.accountNumber,
                institution = Institutions.name(bankAccount.participant),
                cpfHolder = owner.taxIdNumber,
                nameHolder = owner.name
            )
        )
    }
}
