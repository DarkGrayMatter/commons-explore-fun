package graymatter.commons.fn

import assertk.all
import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import graymatter.commons.testing.UseCaseDemoTests
import io.mockk.Called
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.converter.ArgumentConverter
import org.junit.jupiter.params.converter.ConvertWith
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

@DisplayName("Either Monad Demo Use Cases")
@UseCaseDemoTests
internal class EitherTests {

    @Test
    fun `Use get() should return right value of it exists`() {
        val x = either<String, Int> { 12 }
        assertThat(x.get()).isEqualTo(12)
    }

    @Test
    fun `Either-getOrNull() should return  null of the left side is present`() {
        val x = either<String, Int> { left("error1") }
        assertThat(x.getOrNull()).isNull()
    }

    @Test
    fun `Ether-get() should throw an error if left side is given`() {
        val x = either<String, Int> { left("error1") }
        assertThrows<NoSuchElementException> { x.get() }
    }

    @Test
    fun `Ether-getOr{} should return the result of the or part`() {
        val x = either<String, Int> { left("error") }
        assertThat(x.getOr { 1 }).isEqualTo(1)
    }

    @Test
    fun `Either-getOrNull() should return the value of available`() {
        val expected = 12
        val x = either<String, Int> { expected }
        assertThat(x.getOrNull()).isEqualTo(expected)
    }

    @Test
    fun `Test mapping of right value to a new value`() {
        val given = either<String, Int> { 12 }
        val expected = either<String, String> { "12" }
        val actual = given.map(Int::toString)
        assertEquals(expected, actual)
    }

    @Test
    fun `Test mapping of left value to a new value`() {
        val given: Either<String, Int> = Either.Left("12")
        val expected: Either<Int, Int> = Either.Left(12)
        val actual = given.mapLeft { it.toInt() }
        assertEquals(expected, actual)
    }


    @Test
    fun `Test mapping of left should not be called if right value is present`() {
        val given: Either<Int, String> = Either.Right("Jason")
        val expected: Either<String, String> = Either.Right("Jason")
        val actual = given.mapLeft(Int::toString)
        assertEquals(expected, actual)
    }

    @ParameterizedTest()
    @CsvSource(
        nullValues = ["Null"],
        value = [
            "Null   ,'Jane'   ,'Jane'",
            "6      ,Null     ,6",
        ]
    )
    fun testSimpleFold(
        left: Int?,
        right: String?,
        @ConvertWith(ConvertToIntOrKeepAsString::class) expect: Any,
    ) {

        val given: Either<Int, String> = when {
            left != null && right == null -> Either.Left(left)
            right != null && left == null -> Either.Right(right)
            else -> throw IllegalArgumentException()
        }

        val actual = given.fold({ it }, { it })
        assertEquals(expect, actual)
    }

    @Test
    fun `Converting instance to left side`() {
        val given = "1".left()
        assertThat(given).isInstanceOf(Either.Left::class)
        assertThat(given.getLeftOrNull()).isEqualTo("1")
    }

    @Test
    fun `Converting instance to right side`() {
        val given = 1.right()
        assertThat(given).isInstanceOf(Either.Right::class)
        assertThat(given.getOrNull()).isEqualTo(1)
    }

    @Nested
    inner class ConsumeTests {

        private lateinit var given: Either<String, Int>
        private lateinit var consumeLeft: (String) -> Unit
        private lateinit var consumeRight: (Int) -> Unit

        @BeforeEach
        fun init() {
            consumeLeft = mockk(relaxed = true)
            consumeRight = mockk(relaxed = true)
        }


        @Test
        fun `test left side is consumed`() {
            given = "error".left()
            given.consumeLeft(consumeLeft)
            verify { consumeLeft("error") }
            verify { consumeRight(any()) wasNot Called }
        }

        @Test
        fun `test right side is consumed`() {
            given = Either.Right(1)
            given.consumeLeft(consumeLeft)
            given.consumeRight(consumeRight)
            verify { consumeRight(1) }
            verify { consumeLeft(any()) wasNot Called }
        }

        @Test
        fun `test consume Right And Left`() {
            val errorCode = "e".left()
            val ok = 1.right()
            assertAll {
                for (b in arrayOf(true, false)) {
                    given = if (b) errorCode else ok
                    given.consume(consumeLeft, consumeRight)
                    assertThat(given).all {
                        if (b) {
                            isEqualTo(errorCode)
                            verify { consumeLeft(any()) }
                            verify { consumeRight(any()) wasNot Called }
                        } else {
                            isEqualTo(ok)
                            verify { consumeLeft(any()) wasNot Called }
                            verify { consumeRight(any()) }
                        }
                    }
                }
            }
        }

    }

    private object ConvertToIntOrKeepAsString : ArgumentConverter {
        override fun convert(source: Any, context: ParameterContext): Any {
            return when (source) {
                is Int -> source
                is String -> source.toIntOrNull() ?: source
                else -> source.toString().toIntOrNull() ?: "$source"
            }
        }
    }
}



