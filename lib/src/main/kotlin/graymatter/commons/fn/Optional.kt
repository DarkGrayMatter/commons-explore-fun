package graymatter.commons.fn

object None {
    override fun toString(): String = "[NONE]"
}

val NONE = Either.Left(None)

typealias Optional<T> = Either<None, T>

fun <T> optional(): Optional<T> = NONE
fun <T> optional(a: T): Optional<T> = Either.Right(a)


fun <R> Either<*, R>.filter(accept: (R) -> Boolean): Optional<R> {
    return when (this) {
        is Either.Left -> NONE
        is Either.Right -> if (accept(value)) this else NONE
    }
}

val Optional<*>.isEmpty: Boolean get() = this == NONE

inline fun <L, R> Optional<R>.toEither(crossinline noneAsLeft: () -> L): Either<L, R> {
    return mapLeft { noneAsLeft() }
}

fun <L, R> Either<L, R>.toOptional(): Optional<R> {
    return mapLeft { None }
}
