package com.theoplayer.presentation

import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.os.Build
import android.util.Rational
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.facebook.react.uimanager.ThemedReactContext
import com.theoplayer.PlayerEventEmitter
import com.theoplayer.android.api.THEOplayerView
import com.theoplayer.android.api.player.PresentationMode

class PresentationManager(
  private val view: THEOplayerView,
  private val reactContext: ThemedReactContext,
  private val eventEmitter: PlayerEventEmitter,
  private val config: PresentationConfig,
) {
  private var fullscreen = false
  private var pip = false
  private var onUserLeaveHintReceiver: BroadcastReceiver? = null
  private var onPictureInPictureModeChanged: BroadcastReceiver? = null

  init {
    onUserLeaveHintReceiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        // Optionally into PiP mode when the app goes to background.
        if (config.canStartPictureInPictureAutomaticallyFromInline == true) {
          setPresentation(PresentationMode.PICTURE_IN_PICTURE)
        }
      }
    }
    onPictureInPictureModeChanged = object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        // Dispatch event on every PiP mode change
        pip = intent?.getBooleanExtra("isInPictureInPictureMode", false) ?: false
        eventEmitter.emitPresentationModeChange(if (pip) PresentationMode.PICTURE_IN_PICTURE else PresentationMode.INLINE)
      }
    }

    reactContext.currentActivity?.registerReceiver(
      onUserLeaveHintReceiver,
      IntentFilter("onUserLeaveHint")
    )
    reactContext.currentActivity?.registerReceiver(
      onPictureInPictureModeChanged,
      IntentFilter("onPictureInPictureModeChanged")
    )
  }

  /**
   * Whether playback should be allowed to continue when the activity is paused.
   *
   * Pause if:
   * - No PiP available: Build.VERSION.SDK_INT < Build.VERSION_CODES.N (API 24), or;
   * - Not in PiP mode, and;
   * - Not configured to automatically go into PiP when going to the background.
   */
  val shouldPauseOnHostPause: Boolean
    get() =
      Build.VERSION.SDK_INT < Build.VERSION_CODES.N ||
        (reactContext.currentActivity?.isInPictureInPictureMode != true &&
          config.canStartPictureInPictureAutomaticallyFromInline != true)

  fun onDestroy() {
    try {
      reactContext.currentActivity?.unregisterReceiver(onUserLeaveHintReceiver)
      reactContext.currentActivity?.unregisterReceiver(onPictureInPictureModeChanged)
    } catch (ignore: Exception) {
    }
  }

  fun setPresentation(mode: PresentationMode) {
    when (mode) {
      PresentationMode.INLINE -> {
        setFullscreen(false)
        setPip(false)
      }
      PresentationMode.FULLSCREEN -> {
        setFullscreen(true)
        setPip(false)
      }
      PresentationMode.PICTURE_IN_PICTURE -> {
        setFullscreen(false)
        setPip(true)
      }
    }
  }

  private fun getContentViewRect(view: ViewGroup): Rect? {
    for (i in 0 until view.childCount) {
      val child: View = view.getChildAt(i)
      if (child is ViewGroup) {
        return getContentViewRect(child)
      } else if (child as? SurfaceView != null || child as? TextureView != null) {
        val visibleRect = Rect()
        child.getGlobalVisibleRect(visibleRect)
        return visibleRect
      }
    }
    return null
  }

  private fun setPip(pip: Boolean) {
    val wasInPip = this.pip
    if (wasInPip == pip) {
      // Already in right PiP state
      return
    }
    if (pip) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val visibleRect = getContentViewRect(view)
        val aspectRatio = if (view.player.videoHeight > 0) {
          Rational(view.player.videoWidth, view.player.videoHeight)
        } else {
          // Default aspect ratio
          Rational(16, 9)
        }
        reactContext.currentActivity?.enterPictureInPictureMode(
          PictureInPictureParams.Builder()
            .setSourceRectHint(visibleRect)
            .setAspectRatio(aspectRatio)
            // The active MediaSession will connect the controls
            .build()
        )
      }
    }
  }

  @SuppressLint("ObsoleteSdkInt")
  fun setFullscreen(fullscreen: Boolean) {
    if (fullscreen == this.fullscreen) {
      // Already in right fullscreen state
      return
    }
    this.fullscreen = fullscreen
    val activity = reactContext.currentActivity ?: return
    val window = activity.window
    val decorView = window.decorView
    val uiOptions: Int
    if (fullscreen) {
      uiOptions = if (Build.VERSION.SDK_INT >= 19) { // 4.4+
        (FrameLayout.SYSTEM_UI_FLAG_HIDE_NAVIGATION
          or FrameLayout.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
          or FrameLayout.SYSTEM_UI_FLAG_FULLSCREEN)
      } else {
        (FrameLayout.SYSTEM_UI_FLAG_HIDE_NAVIGATION
          or FrameLayout.SYSTEM_UI_FLAG_FULLSCREEN)
      }
      decorView.systemUiVisibility = uiOptions
      eventEmitter.emitPresentationModeChange(PresentationMode.FULLSCREEN)
    } else {
      uiOptions = FrameLayout.SYSTEM_UI_FLAG_VISIBLE
      decorView.systemUiVisibility = uiOptions
      eventEmitter.emitPresentationModeChange(PresentationMode.INLINE)
    }
  }
}
