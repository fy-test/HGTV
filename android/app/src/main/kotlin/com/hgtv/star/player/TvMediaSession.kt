// SPDX-License-Identifier: GPL-3.0-only
package com.hgtv.star.player

import android.content.Context
import android.os.Build

/** Foreground-only session; no background playback service or extra runtime dependency. Safe on Android 4.4 (API 19). */
class TvMediaSession(context: Context,onPlay: ()->Unit,onPause: ()->Unit,onSeek: (Long)->Unit,onSkip: (Int)->Unit,onStop: ()->Unit) {
    private interface SessionImpl {
        fun update(title: String,episode: Int,duration: Long,position: Long,speed: Float,state: Int,previous: Boolean,next: Boolean)
        fun deactivate()
        fun release()
    }

    private val impl: SessionImpl? = if(Build.VERSION.SDK_INT>=21) {
        MediaSessionApi21Impl(context,onPlay,onPause,onSeek,onSkip,onStop)
    } else null

    fun update(title: String,episode: Int,duration: Long,position: Long,speed: Float,state: Int,previous: Boolean,next: Boolean) {
        impl?.update(title,episode,duration,position,speed,state,previous,next)
    }
    fun deactivate() { impl?.deactivate() }
    fun release() { impl?.release() }

    @androidx.annotation.RequiresApi(21)
    private class MediaSessionApi21Impl(context: Context,onPlay: ()->Unit,onPause: ()->Unit,onSeek: (Long)->Unit,onSkip: (Int)->Unit,onStop: ()->Unit) : SessionImpl {
        private val session=android.media.session.MediaSession(context,"HGTV").apply {
            setCallback(object: android.media.session.MediaSession.Callback() {
                override fun onPlay()=onPlay.invoke()
                override fun onPause()=onPause.invoke()
                override fun onSeekTo(pos: Long)=onSeek(pos)
                override fun onSkipToNext()=onSkip(1)
                override fun onSkipToPrevious()=onSkip(-1)
                override fun onStop()=onStop.invoke()
            },android.os.Handler(android.os.Looper.getMainLooper()))
        }
        private var metadataKey=""
        override fun update(title: String,episode: Int,duration: Long,position: Long,speed: Float,state: Int,previous: Boolean,next: Boolean) {
            val key="$title/$episode/$duration"
            if(metadataKey!=key) {
                metadataKey=key
                session.setMetadata(android.media.MediaMetadata.Builder().putString(android.media.MediaMetadata.METADATA_KEY_TITLE,title).putString(android.media.MediaMetadata.METADATA_KEY_DISPLAY_TITLE,title)
                    .putString(android.media.MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE,"第 ${episode+1} 集")
                    .putLong(android.media.MediaMetadata.METADATA_KEY_DURATION,duration).build())
            }
            var actions=android.media.session.PlaybackState.ACTION_PLAY or android.media.session.PlaybackState.ACTION_PAUSE or android.media.session.PlaybackState.ACTION_PLAY_PAUSE or android.media.session.PlaybackState.ACTION_STOP
            if(duration>0) actions=actions or android.media.session.PlaybackState.ACTION_SEEK_TO
            if(previous) actions=actions or android.media.session.PlaybackState.ACTION_SKIP_TO_PREVIOUS
            if(next) actions=actions or android.media.session.PlaybackState.ACTION_SKIP_TO_NEXT
            session.setPlaybackState(android.media.session.PlaybackState.Builder().setActions(actions).setState(state,position,if(state==android.media.session.PlaybackState.STATE_PLAYING) speed else 0f).build())
            session.isActive=true
        }
        override fun deactivate() { session.isActive=false }
        override fun release() { session.release() }
    }
}
