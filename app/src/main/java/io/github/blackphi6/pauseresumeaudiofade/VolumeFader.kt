package io.github.blackphi6.pauseresumeaudiofade

import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** AudioManager を直接ラップせず、テスト用に差し替えられるようにする最小限の窓口 */
interface VolumeController {
    val minIndex: Int
    val maxIndex: Int
    val currentIndex: Int
    fun setIndex(index: Int)
}

/**
 * 一時停止/再開の瞬間、STREAM_MUSIC の音量インデックスを段階的に動かしてフェードさせる。
 * Android の音量は離散ステップ（端末により大体15〜25段階）なので、連続カーブではなく
 * 各ステップへ均等間隔で移動するだけ。ステップ数が少ないため線形で十分——それ以上のカーブは
 * この解像度では体感できない。
 */
class VolumeFader(
    private val controller: VolumeController,
    private val scope: CoroutineScope,
) {
    private var job: Job? = null
    private var restoreIndex: Int? = null

    fun fadeOut(durationMs: Long) {
        job?.cancel()
        val from = restoreIndex ?: controller.currentIndex
        restoreIndex = from
        job = scope.launch { runFade(from, controller.minIndex, durationMs) }
    }

    /**
     * 直前に自分でフェードアウトした記録（restoreIndex）がある時だけ、そこまで戻す。
     * 記録がないのに最大音量などへ「フォールバック」してはいけない——Bluetoothイヤホン接続時など、
     * こちらが一時停止を捉えていないのに再生イベントだけ飛んでくるケースがあり、そこで音量を
     * 動かすと現在の音量を無視して爆音になる事故につながる。何もしないのが安全な既定動作。
     */
    fun fadeIn(durationMs: Long) {
        val to = restoreIndex ?: return
        job?.cancel()
        job = scope.launch {
            runFade(controller.currentIndex, to, durationMs)
            restoreIndex = null
        }
    }

    private suspend fun runFade(from: Int, to: Int, durationMs: Long) {
        val steps = fadeSteps(from, to)
        if (steps.isEmpty()) {
            controller.setIndex(to)
            return
        }
        val stepDuration = durationMs / steps.size
        for (index in steps) {
            controller.setIndex(index)
            delay(stepDuration)
        }
    }

}

/** from から to まで、1ステップずつ辿るインデックス列。純粋関数なのでコルーチン抜きでテストできる */
internal fun fadeSteps(from: Int, to: Int): List<Int> {
    if (from == to) return emptyList()
    val direction = if (to > from) 1 else -1
    return (1..abs(to - from)).map { from + direction * it }
}
