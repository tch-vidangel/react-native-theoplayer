@file:Suppress("unused")

package com.theoplayer.cast

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.theoplayer.util.ViewResolver
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.theoplayer.ReactTHEOplayerView
import com.theoplayer.android.api.cast.chromecast.PlayerCastState

class CastModule(context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {
  private val viewResolver: ViewResolver = ViewResolver(context)

  override fun getName(): String {
    return "THEORCTCastModule"
  }

  @ReactMethod
  fun casting(tag: Int, promise: Promise) {
    viewResolver.resolveViewByTag(tag) { view: ReactTHEOplayerView? ->
      promise.resolve(view?.playerContext?.castIntegration?.isCasting() ?: false)
    }
  }

  @ReactMethod
  fun chromecastCasting(tag: Int, promise: Promise) {
    casting(tag, promise)
  }

  @ReactMethod
  fun chromecastState(tag: Int, promise: Promise) {
    viewResolver.resolveViewByTag(tag) { view: ReactTHEOplayerView? ->
      promise.resolve(
        castStateToString(
          view?.playerContext?.castIntegration?.getState() ?: PlayerCastState.UNAVAILABLE
        )
      )
    }
  }

  private fun castStateToString(state: PlayerCastState): String {
    return when (state) {
      PlayerCastState.AVAILABLE -> "available"
      PlayerCastState.CONNECTED -> "connected"
      PlayerCastState.CONNECTING -> "connecting"
      PlayerCastState.UNAVAILABLE -> "unavailable"
      else -> "unavailable"
    }
  }

  @ReactMethod
  fun chromecastStart(tag: Int) {
    viewResolver.resolveViewByTag(tag) { view: ReactTHEOplayerView? ->
      view?.playerContext?.castIntegration?.start()
    }
  }

  @ReactMethod
  fun chromecastStop(tag: Int) {
    viewResolver.resolveViewByTag(tag) { view: ReactTHEOplayerView? ->
      view?.playerContext?.castIntegration?.stop()
    }
  }

  @ReactMethod
  fun chromecastJoin(tag: Int) {
    viewResolver.resolveViewByTag(tag) { view: ReactTHEOplayerView? ->
      view?.playerContext?.castIntegration?.join()
    }
  }

  @ReactMethod
  fun chromecastLeave(tag: Int) {
    viewResolver.resolveViewByTag(tag) { view: ReactTHEOplayerView? ->
      view?.playerContext?.castIntegration?.leave()
    }
  }
}
