package com.br.zup.integration.bcb.createPixKey

import com.br.zup.pix.AssociatedAccount
import com.br.zup.pix.PixKey

data class CreatePixKeyRequest(
        val keyType: PixKeyType,
        val key: String,
        val bankAccount: BankAccount,
        val owner: Owner
) {
    companion object {

        fun of(pixKey: PixKey): CreatePixKeyRequest {
            return CreatePixKeyRequest(
                    keyType = PixKeyType.by(pixKey.keyType),
                    key = pixKey.key,
                    bankAccount = BankAccount(
                            participant = AssociatedAccount.ITAU_UNIBANCO_ISPB,
                            branch = pixKey.account.agency,
                            accountNumber = pixKey.account.number,
                            accountType = BankAccount.AccountTypeBank.by(pixKey.accountType),
                    ),
                    owner = Owner(
                            type = Owner.OwnerType.NATURAL_PERSON,
                            name = pixKey.account.nameHolder,
                            taxIdNumber = pixKey.account.cpfHolder
                    )
            )
        }
    }
}