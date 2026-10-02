package io.github.blackphi6.pauseresumeaudiofade

import android.content.ComponentName
import android.media.AudioManager
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * 通知リスナーとして接続を許可されることで、システム全体のメディアセッション
 * （どのアプリが再生中/一時停止中か）を読み取れるようになる。通知の内容自体は使わない。
 */
class FadeNotificationListenerService : NotificationListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val controllerCallbacks = mutableMapOf<MediaController, MediaController.Callback>()

    private lateinit var mediaSessionManager: MediaSessionManager
    private lateinit var fader: VolumeFader
    private lateinit var componentName: ComponentName
    private lateinit var preferences: Preferences

    private val sessionsChangedListener =
        MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
            rebindControllers(controllers.orEmpty())
        }

    override fun onListenerConnected() {
        super.onListenerConnected()
        componentName = ComponentName(this, FadeNotificationListenerService::class.java)
        mediaSessionManager = getSystemService(MediaSessionManager::class.java)
        val audioManager = getSystemService(AudioManager::class.java)
        fader = VolumeFader(AndroidVolumeController(audioManager), scope)
        preferences = Preferences(this)

        mediaSessionManager.addOnActiveSessionsChangedListener(sessionsChangedListener, componentName)
        rebindControllers(mediaSessionManager.getActiveSessions(componentName))
    }

    override fun onListenerDisconnected() {
        mediaSessionManager.removeOnActiveSessionsChangedListener(sessionsChangedListener)
        rebindControllers(emptyList())
        scope.cancel()
        super.onListenerDisconnected()
    }

    private fun rebindControllers(next: List<MediaController>) {
        controllerCallbacks.forEach { (controller, callback) -> controller.unregisterCallback(callback) }
        controllerCallbacks.clear()
        next.forEach { controller ->
            val callback = object : MediaController.Callback() {
                override fun onPlaybackStateChanged(state: PlaybackState?) {
                    handleStateChange(state)
                }
            }
            controller.registerCallback(callback)
            controllerCallbacks[controller] = callback
        }
    }

    private fun handleStateChange(state: PlaybackState?) {
        when (state?.state) {
            PlaybackState.STATE_PLAYING -> fader.fadeIn(preferences.fadeInMs)
            PlaybackState.STATE_PAUSED, PlaybackState.STATE_STOPPED -> fader.fadeOut(preferences.fadeOutMs)
            else -> Unit
        }
    }

    // NotificationListenerService の抽象要件。通知の中身そのものは使わない
    override fun onNotificationPosted(sbn: StatusBarNotification) = Unit
    override fun onNotificationRemoved(sbn: StatusBarNotification) = Unit
}
