@file:Suppress("FunctionName")

package graymatter.commons.fn

sealed class Either<out L, out R> {
    data class Left<L>(val value: L) : Either<L, Nothing>()
    data class Right<R>(val value: R) : Either<Nothing, R>()
    companion object {
        @JvmStatic
        fun <L, R> cond(b: Boolean, left: () -> L, right: () -> R): Either<L, R> {
            return when (b) {
                true -> Either.Right(right())
                else -> Either.Left(left())
            }
        }
    }
}

sealed class LeftCaptureContext<L> {
    abstract fun left(left: L): Nothing
}


fun <T> Either<*, T>.get(): T {
    return when (this) {
        is Either.Left -> throw NoSuchElementException()
        is Either.Right -> value
    }
}

fun <L> Either<L, *>.getLeftOrNull(): L? {
    return when (this) {
        is Either.Left -> value
        is Either.Right -> null
    }
}

fun <T> Either<*, T>.getOrNull(): T? {
    return when (this) {
        is Either.Left -> null
        is Either.Right -> value
    }
}

inline fun <T> Either<*, T>.getOr(other: () -> T): T {
    return getOrNull() ?: other()
}


fun <L, R, T> Either<L, R>.map(map: (R) -> T): Either<L, T> {
    return when (this) {
        is Either.Left -> this
        is Either.Right -> Either.Right(map(value))
    }
}

fun <L, R, T> Either<L, R>.mapLeft(mapLeft: (L) -> T): Either<T, R> {
    return when (this) {
        is Either.Left -> Either.Left(mapLeft(value))
        is Either.Right -> this
    }
}

fun <L, R, T> Either<L, R>.fold(mapLeft: (L) -> T, map: (R) -> T): T {
    return when (this) {
        is Either.Left -> mapLeft(value)
        is Either.Right -> map(value)
    }
}


private class EitherInvocationContext<L> : LeftCaptureContext<L>() {
    var left: L? = null
    override fun left(left: L): Nothing {
        this.left = left
        throw EitherIsLeftThrowable
    }
}


private object EitherIsLeftThrowable : Throwable()

fun <L, T> either(action: LeftCaptureContext<L>.() -> T): Either<L, T> {
    return EitherInvocationContext<L>().run {
        try {
            Either.Right(action())
        } catch (_: EitherIsLeftThrowable) {
            Either.Left(left!!)
        }
    }
}

fun <T> T.left(): Either.Left<T> = Either.Left(this)
fun <T> T.right(): Either.Right<T> = Either.Right(this)


fun <L, R> Either<L, R>.consumeRight(consumerRight: (R) -> Unit) {
    map(consumerRight)
}

fun <L, R> Either<L, R>.consumeLeft(consumerLeft: (L) -> Unit) {
    mapLeft(consumerLeft)
}

fun <L, R> Either<L, R>.consume(consumerLeft: (L) -> Unit, consumerRight: (R) -> Unit) {
    fold(consumerLeft, consumerRight)
}
