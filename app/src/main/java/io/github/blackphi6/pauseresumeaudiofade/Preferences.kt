package io.github.blackphi6.pauseresumeaudiofade

import android.content.Context

/** フェード時間の保存先。SharedPreferences は設定変更後も次回起動まで保持される */
class Preferences(context: Context) {
    private val prefs = context.getSharedPreferences("fade_prefs", Context.MODE_PRIVATE)

    var fadeOutMs: Long
        get() = prefs.getLong(KEY_FADE_OUT_MS, DEFAULT_FADE_OUT_MS)
        set(value) = prefs.edit().putLong(KEY_FADE_OUT_MS, value).apply()

    var fadeInMs: Long
        get() = prefs.getLong(KEY_FADE_IN_MS, DEFAULT_FADE_IN_MS)
        set(value) = prefs.edit().putLong(KEY_FADE_IN_MS, value).apply()

    companion object {
        const val DEFAULT_FADE_OUT_MS = 260L
        const val DEFAULT_FADE_IN_MS = 300L
        private const val KEY_FADE_OUT_MS = "fade_out_ms"
        private const val KEY_FADE_IN_MS = "fade_in_ms"
    }
}
