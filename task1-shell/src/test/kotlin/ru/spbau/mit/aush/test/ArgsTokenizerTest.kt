package ru.spbau.mit.aush.test

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.spbau.mit.aush.parse.ArgsTokenizer
import java.util.*

/**
 * Created by: Egor Gorbunov
 * Date: 9/24/16
 * Email: egor-mailbox@ya.com
 */

@RunWith(Parameterized::class)
class ArgsTokenizerTest(val str: String,
                        val expectedSplit: List<String>) {
    companion object {

        @Parameterized.Parameters
        @JvmStatic fun testData(): Collection<Any> {
            return Arrays.asList(
                    arrayOf(" x   b    ", listOf("x", "b")),
                    arrayOf("", emptyList<String>()),
                    arrayOf("a b c", listOf("a", "b", "c")),
                    arrayOf("a 'b' c", listOf("a", "'b'", "c")),
                    arrayOf("a 'b and c' \"d\"", listOf("a", "'b and c'", "\"d\"")),
                    arrayOf("a dfasdfkjashkjasdhfkjashdfkjasdhfkjasdhfkjasdfhajdfhakdjsf",
                            listOf("a", "dfasdfkjashkjasdhfkjashdfkjasdhfkjasdhfkjasdfhajdfhakdjsf")),
                    arrayOf("a | b", listOf("a", "|", "b"))
            )
        }
    }

    @Test fun testParse() {
        val tokenizer = ArgsTokenizer(str)
        val actualParse = tokenizer.tokenize()
//        print(actualParse)
        Assert.assertArrayEquals(expectedSplit.toTypedArray(), actualParse.toTypedArray())
    }
}
