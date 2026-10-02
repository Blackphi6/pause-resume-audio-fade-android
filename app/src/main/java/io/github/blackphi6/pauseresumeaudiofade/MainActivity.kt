package io.github.blackphi6.pauseresumeaudiofade

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferences = Preferences(this)
        statusText = findViewById(R.id.statusText)
        findViewById<Button>(R.id.openSettingsButton).setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        setupFadeSlider(
            seekBar = findViewById(R.id.fadeOutSeekBar),
            label = findViewById(R.id.fadeOutLabel),
            labelRes = R.string.fade_out_label,
            initial = preferences.fadeOutMs,
            onChanged = { preferences.fadeOutMs = it },
        )
        setupFadeSlider(
            seekBar = findViewById(R.id.fadeInSeekBar),
            label = findViewById(R.id.fadeInLabel),
            labelRes = R.string.fade_in_label,
            initial = preferences.fadeInMs,
            onChanged = { preferences.fadeInMs = it },
        )
    }

    override fun onResume() {
        super.onResume()
        statusText.setText(if (isListenerEnabled()) R.string.status_enabled else R.string.status_disabled)
    }

    private fun setupFadeSlider(
        seekBar: SeekBar,
        label: TextView,
        labelRes: Int,
        initial: Long,
        onChanged: (Long) -> Unit,
    ) {
        seekBar.progress = initial.toInt()
        label.text = getString(labelRes, initial.toInt())
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                label.text = getString(labelRes, progress)
                if (fromUser) onChanged(progress.toLong())
            }

            override fun onStartTrackingTouch(bar: SeekBar?) = Unit
            override fun onStopTrackingTouch(bar: SeekBar?) = Unit
        })
    }

    private fun isListenerEnabled(): Boolean {
        val enabled = Settings.Secure.getString(contentResolver, "enabled_notification_listeners") ?: ""
        return enabled.contains(packageName)
    }
}
