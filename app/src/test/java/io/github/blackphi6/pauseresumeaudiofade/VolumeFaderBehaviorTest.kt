package io.github.blackphi6.pauseresumeaudiofade

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakeVolumeController(initial: Int, override val maxIndex: Int = 15) : VolumeController {
    override val minIndex: Int = 0
    override var currentIndex: Int = initial
        private set

    override fun setIndex(index: Int) {
        currentIndex = index
    }
}

class VolumeFaderBehaviorTest {
    @Test
    fun `fadeIn without a prior fadeOut does nothing — never jumps to max volume`() = runTest {
        val controller = FakeVolumeController(initial = 7) // ユーザーが自分で選んだ、まあまあの音量
        val fader = VolumeFader(controller, TestScope(StandardTestDispatcher(testScheduler)))

        fader.fadeIn(durationMs = 100)
        advanceUntilIdle()

        assertEquals(7, controller.currentIndex)
    }

    @Test
    fun `fadeOut then fadeIn restores the original volume, not max`() = runTest {
        val controller = FakeVolumeController(initial = 7)
        val fader = VolumeFader(controller, TestScope(StandardTestDispatcher(testScheduler)))

        fader.fadeOut(durationMs = 100)
        advanceUntilIdle()
        assertEquals(0, controller.currentIndex)

        fader.fadeIn(durationMs = 100)
        advanceUntilIdle()
        assertEquals(7, controller.currentIndex)
    }
}
