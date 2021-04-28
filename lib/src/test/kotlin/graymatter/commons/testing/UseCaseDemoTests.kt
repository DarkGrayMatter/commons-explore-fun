package graymatter.commons.testing

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance

@Tag("usecase-demo-tests")
@Retention(AnnotationRetention.RUNTIME)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
annotation class UseCaseDemoTests
