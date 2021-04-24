package com.br.zup.integration.bcb.createPixKey

import com.br.zup.pix.KeyType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class PixKeyTypeTest {

    @Test
    internal fun `deve retornar CPF - PixKeyType quando for passado um CPF - KeyType`() {
        assertEquals(PixKeyType.CPF, PixKeyType.by(KeyType.CPF))
    }

    @Test
    internal fun `deve retornar PHONE - PixKeyType quando for passado um CELL_PHONE - KeyType`() {
        assertEquals(PixKeyType.PHONE, PixKeyType.by(KeyType.CELL_PHONE))
    }

    @Test
    internal fun `deve retornar EMAIL - PixKeyType quando for passado um EMAIL - KeyType`() {
        assertEquals(PixKeyType.EMAIL, PixKeyType.by(KeyType.EMAIL))
    }

    @Test
    internal fun `deve retornar RANDOM - PixKeyType quando for passado um RANDOM_KEY - KeyType`() {
        assertEquals(PixKeyType.RANDOM, PixKeyType.by(KeyType.RANDOM_KEY))
    }
}