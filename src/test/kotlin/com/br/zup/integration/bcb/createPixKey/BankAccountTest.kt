package com.br.zup.integration.bcb.createPixKey

import com.br.zup.pix.AccountType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class BankAccountTest {

    @Test
    internal fun `deve retornar CACC quando o tipo de conta for CHECKING_ACCOUNT`() {
        assertEquals(BankAccount.AccountTypeBank.CACC, BankAccount.AccountTypeBank.by(AccountType.CHECKING_ACCOUNT))
    }

    @Test
    internal fun `deve retornar SVGS quando o tipo de conta for SAVINGS_ACCOUNT`() {
        assertEquals(BankAccount.AccountTypeBank.SVGS, BankAccount.AccountTypeBank.by(AccountType.SAVINGS_ACCOUNT))
    }

}