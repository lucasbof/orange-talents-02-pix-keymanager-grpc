package com.br.zup.integration.bcb.createPixKey

import com.br.zup.pix.AccountType

data class BankAccount(
        val participant: String,
        val branch: String,
        val accountNumber: String,
        val accountType: AccountTypeBank
) {

    enum class AccountTypeBank() {
        CACC, // Current: Account used to post debits and credits when no specific account has been nominated
        SVGS; // Savings: Savings

        companion object {
            fun by(domainType: AccountType): AccountTypeBank {
                return when (domainType) {
                    AccountType.CHECKING_ACCOUNT -> CACC
                    AccountType.SAVINGS_ACCOUNT -> SVGS
                }
            }
        }
    }
}
