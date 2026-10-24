package com.invictus.hushbro.pipeline

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Probes the coroutine-test scheduling behaviour used by [NotificationPipelineTest].
 *
 * Key finding: with the default StandardTestDispatcher, [TestScope.backgroundScope]
 * coroutines are not guaranteed to have executed when [advanceUntilIdle] returns
 * in the test body. To get deterministic behaviour for the pipeline consumer loop,
 * [NotificationPipelineTest] creates its own [TestScope] backed by
 * [UnconfinedTestDispatcher], where coroutines run eagerly.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProbeBackgroundScopeTest {

    @Test
    fun unconfinedScope_coroutine_runsEagerly() = runTest {
        val scope = TestScope(UnconfinedTestDispatcher())
        var ran = false
        scope.launch {
            ran = true
        }
        scope.testScheduler.advanceUntilIdle()
        assertThat(ran).isTrue()
    }

    @Test
    fun unconfinedScope_coroutine_receivesFromChannel() = runTest {
        val scope = TestScope(UnconfinedTestDispatcher())
        val channel = kotlinx.coroutines.channels.Channel<String>(capacity = 10)
        var received: String? = null
        scope.launch {
            received = channel.receiveCatching().getOrNull()
        }
        channel.trySend("hello")
        scope.testScheduler.advanceUntilIdle()
        assertThat(received).isEqualTo("hello")
    }
}