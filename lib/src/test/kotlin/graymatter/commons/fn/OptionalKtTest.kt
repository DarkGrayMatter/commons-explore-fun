package graymatter.commons.fn

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import graymatter.commons.testing.UseCaseDemoTests
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertTrue

@DisplayName("Optional Monad Tests")
@UseCaseDemoTests
internal class OptionalKtTest {

    @Test
    fun `Create empty optional instance`() {
        val empty = optional<Int>()
        assertThat(empty.isEmpty).isTrue()
    }

    @Test
    fun `Filtering an Either should result in an optional when value matches`() {
        val either = either<Int, String> { "hello" }
        val expected: Optional<String> = Either.Right("hello")
        val actual = either.filter { it == "hello" }
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `Filtering an Either should result in None when when value does not match`() {
        val given = either<Int, String> { "hello" }
        val actual = given.filter { it.isEmpty() }
        assertTrue { actual.isEmpty }
    }

    @Test
    fun `Create optional with a value`() {
        val given = optional(1)
        val expected = Either.Right(1)
        assertThat(given).isEqualTo(expected)
    }

    @Test
    fun `Optional should distinguish between null as value and an empty value`() {
        val given = optional<String?>(null)
        assertFalse { given.isEmpty }
    }

    @Test
    fun `Converting an optional to Either`() {
        val customerNotFound = 1
        val customerId = optional<String>()
        val actual = customerId.toEither { 1 }
        assertThat(actual).isInstanceOf(Either.Left::class)
        assertThat(actual.getLeftOrNull()).isEqualTo(customerNotFound)
    }


    @ParameterizedTest
    @MethodSource("eitherAsOptionalFactory")
    fun `Converting a Either to optional`(given: Either<Int, String>, expected: Optional<String>) {
        val actual = given.toOptional()
        assertThat(actual).isEqualTo(expected)
    }

    fun eitherAsOptionalFactory(): List<Arguments> {
        return listOf(
            arguments(Either.Left(3), optional<String>()),
            arguments(Either.Right("Jason"), optional("Jason"))
        )
    }
}
