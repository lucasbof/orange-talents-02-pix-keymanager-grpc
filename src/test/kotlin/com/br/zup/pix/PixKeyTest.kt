package com.br.zup.pix

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class PixKeyTest {

    @Test
    internal fun `deve atualizar a chave e retornar true quando for tipo de chave RANDOM_KEY`() {
        val pixKey = PixKey(
            clientId = UUID.randomUUID(),
            keyType = KeyType.RANDOM_KEY,
            accountType = AccountType.CHECKING_ACCOUNT,
            key = "", // chave deve estar vazia quando for chave aleatoria
            account = AssociatedAccount(
                agency = "8444",
                number = "854862",
                institution = "ITAU",
                cpfHolder = "17911984069",
                nameHolder = "Lucas"
            )
        )
        val newKeyValue = UUID.randomUUID().toString()
        val result = pixKey.update(newKeyValue)

        assertTrue(result)
        assertEquals(newKeyValue, pixKey.key)
    }

    @Test
    internal fun ` nao deve atualizar a chave e deve retornar false quando o tipo de chave e EMAIL`() {
        val pixKey = PixKey(
            clientId = UUID.randomUUID(),
            keyType = KeyType.EMAIL,
            accountType = AccountType.CHECKING_ACCOUNT,
            key = "lucas@gmail.com",
            account = AssociatedAccount(
                agency = "8444",
                number = "854862",
                institution = "ITAU",
                cpfHolder = "17911984069",
                nameHolder = "Lucas"
            )
        )
        val newKeyValue = UUID.randomUUID().toString()
        val result = pixKey.update(newKeyValue)

        assertFalse(result)
        assertEquals("lucas@gmail.com", pixKey.key)
    }

    @Test
    internal fun ` nao deve atualizar a chave e deve retornar false quando o tipo de chave e CELL_PHONE`() {
        val pixKey = PixKey(
            clientId = UUID.randomUUID(),
            keyType = KeyType.CELL_PHONE,
            accountType = AccountType.CHECKING_ACCOUNT,
            key = "+55199958417",
            account = AssociatedAccount(
                agency = "8444",
                number = "854862",
                institution = "ITAU",
                cpfHolder = "17911984069",
                nameHolder = "Lucas"
            )
        )
        val newKeyValue = UUID.randomUUID().toString()
        val result = pixKey.update(newKeyValue)

        assertFalse(result)
        assertEquals("+55199958417", pixKey.key)
    }

    @Test
    internal fun ` nao deve atualizar a chave e deve retornar false quando o tipo de chave e CPF`() {
        val pixKey = PixKey(
            clientId = UUID.randomUUID(),
            keyType = KeyType.CPF,
            accountType = AccountType.CHECKING_ACCOUNT,
            key = "17911984069",
            account = AssociatedAccount(
                agency = "8444",
                number = "854862",
                institution = "ITAU",
                cpfHolder = "17911984069",
                nameHolder = "Lucas"
            )
        )
        val newKeyValue = UUID.randomUUID().toString()
        val result = pixKey.update(newKeyValue)

        assertFalse(result)
        assertEquals("17911984069", pixKey.key)
    }

}