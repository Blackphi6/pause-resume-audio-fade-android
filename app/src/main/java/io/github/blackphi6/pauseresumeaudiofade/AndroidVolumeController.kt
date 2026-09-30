package io.github.blackphi6.pauseresumeaudiofade

import android.media.AudioManager

class AndroidVolumeController(private val audioManager: AudioManager) : VolumeController {
    override val minIndex: Int
        get() = audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)

    override val maxIndex: Int
        get() = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    override val currentIndex: Int
        get() = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

    override fun setIndex(index: Int) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0)
    }
}
