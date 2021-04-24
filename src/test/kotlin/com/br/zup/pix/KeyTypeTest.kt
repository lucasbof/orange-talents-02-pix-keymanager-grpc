package com.br.zup.pix

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class KeyTypeTest {


    @Nested
    inner class CPF {
        @Test
        internal fun `deve retornar true quando CPF eh valido`() {
            assertTrue(KeyType.CPF.validate("92412703060"))
            assertTrue(KeyType.CPF.validate("20330836080"))
        }

        @Test
        internal fun `deve retornar false quando CPF for nulo, vazio ou so espacos`() {
            assertFalse(KeyType.CPF.validate(null))
            assertFalse(KeyType.CPF.validate(""))
            assertFalse(KeyType.CPF.validate("    "))
        }

        @Test
        internal fun `deve retornar false quando CPF for diferente de 11 numeros`() {
            assertFalse(KeyType.CPF.validate("214"))
            assertFalse(KeyType.CPF.validate("214vd451281"))
            assertFalse(KeyType.CPF.validate("924.127.030-60"))
            assertFalse(KeyType.CPF.validate("924.127.0fbgb60"))
        }

        @Test
        internal fun `deve retornar false quando digito vertificador do CPF for invalido`() {
            assertFalse(KeyType.CPF.validate("92412703050"))
            assertFalse(KeyType.CPF.validate("20330836081"))
        }
    }

    @Nested
    inner class CELLPHONE {
        @Test
        internal fun `deve retornar true quando telefone eh valido`() {
            assertTrue(KeyType.CELL_PHONE.validate("+5585988714077"))
            assertTrue(KeyType.CELL_PHONE.validate("+5585988714077694"))
            assertTrue(KeyType.CELL_PHONE.validate("+551132201278"))
            assertTrue(KeyType.CELL_PHONE.validate("+551"))
        }

        @Test
        internal fun `deve retornar false quando telefone for nulo, vazio ou so espacos`() {
            assertFalse(KeyType.CELL_PHONE.validate(null))
            assertFalse(KeyType.CELL_PHONE.validate(""))
            assertFalse(KeyType.CELL_PHONE.validate("     "))
        }

        // Padrao telefonico: simbolo + | um numero entre 1 e 9 | um numero entre 0 e 9 | de 1 a 14 numeros entre 0 e 9
        @Test
        internal fun `deve retornar false quando telefone for diferente do padrao telefonico`() {
            assertFalse(KeyType.CELL_PHONE.validate("5585988714077"))
            assertFalse(KeyType.CELL_PHONE.validate("+0585988714077"))
            assertFalse(KeyType.CELL_PHONE.validate("+1585988714077p"))
            assertFalse(KeyType.CELL_PHONE.validate("+15"))
            assertFalse(KeyType.CELL_PHONE.validate("+15f88865"))
            assertFalse(KeyType.CELL_PHONE.validate("+ 55 85 9 8871-4077"))
            assertFalse(KeyType.CELL_PHONE.validate("+55859887140774785"))
        }
    }

    @Nested
    inner class EMAIL {
        @Test
        internal fun `deve retornar true quando email eh valido`() {
            assertTrue(KeyType.EMAIL.validate("lucas@gmail.com"))
            assertTrue(KeyType.EMAIL.validate("teste@email"))
            assertTrue(KeyType.EMAIL.validate("teste.testador@email.com.br"))
        }

        @Test
        internal fun `deve retornar false quando email for nulo, vazio ou so espacos`() {
            assertFalse(KeyType.EMAIL.validate(null))
            assertFalse(KeyType.EMAIL.validate(""))
            assertFalse(KeyType.EMAIL.validate("     "))
        }

        @Test
        internal fun `deve retornar false quando email eh invalido`() {
            assertFalse(KeyType.EMAIL.validate("lucas"))
            assertFalse(KeyType.EMAIL.validate("@gmail"))
        }
    }

    @Nested
    inner class RANDOMKEY {
        @Test
        internal fun `deve retornar true quando chave aleatoria for nula, vazia ou so espacos`() {
            assertTrue(KeyType.RANDOM_KEY.validate(null))
            assertTrue(KeyType.RANDOM_KEY.validate(""))
            assertTrue(KeyType.RANDOM_KEY.validate("     "))
        }

        @Test
        internal fun `deve retonar false quando chave aleatorio nao for nula, vazia ou so espacos`() {
            assertFalse(KeyType.RANDOM_KEY.validate("484848484"))
            assertFalse(KeyType.RANDOM_KEY.validate("48dsdfsd"))
            assertFalse(KeyType.RANDOM_KEY.validate("4"))
            assertFalse(KeyType.RANDOM_KEY.validate("   4   "))
        }
    }
}