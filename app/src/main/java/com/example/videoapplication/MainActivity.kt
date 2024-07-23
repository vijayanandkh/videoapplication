package com.example.videoapplication

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource


class MainActivity : AppCompatActivity(), Player.Listener {

    lateinit var player: ExoPlayer
    lateinit var playerView: PlayerView
    lateinit var webView: WebView
    lateinit var playListener : Player.Listener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        playerView = findViewById(R.id.player_view)
        webView = findViewById(R.id.webview)

        initializePlayer()
        initialzeWebView()

    }

    fun initializePlayer() {

        player = ExoPlayer.Builder(this).build()
        playerView.player = player

//        val meditItem = MediaItem.fromUri(getString(R.string.VIDEO_1_URL))
        val meditItem = MediaItem.fromUri(getString(R.string.VIDEO_2_URL))
//        val meditItem = MediaItem.fromUri(getString(R.string.VIDEO_BASE_URL))
        val mediaSource = HlsMediaSource.Factory(DefaultHttpDataSource.Factory())
            .createMediaSource(meditItem)

        player.setMediaSource(mediaSource)
        player.prepare()
        player.addListener(this)
        player.playbackParameters

    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        if (playbackState == ExoPlayer.STATE_ENDED) {
            callJavaScriptStopVideo()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }


    fun initialzeWebView() {
        // Enable JavaScript
        val webSettings = webView.getSettings()
        webSettings.javaScriptEnabled = true


        webView.setWebViewClient(WebViewClient())

        webSettings.mediaPlaybackRequiresUserGesture = false

        webView.webChromeClient = (object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if(newProgress == 100) {
                    view?.loadUrl("javascript:(function() { document.body.style.backgroundColor = 'transparent'; })()")
                    callJavaScriptPausingVideo()
                }
            }

//            fun onPageFinished(view: WebView, url: String) {
//                view.loadUrl("javascript:(function() { document.body.style.backgroundColor = 'transparent'; })()")
//                callJavaScriptPausingVideo()
//            }

        })

        // Adjust layout settings for proper resolution

        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webSettings.defaultZoom = WebSettings.ZoomDensity.FAR

//        webView.webViewClient = WebViewClient()

        webView.background.alpha = 0
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)
        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.addJavascriptInterface(WebAppInterface(), "Android")
        webView.loadUrl("file:///android_res/raw/webview_content.html")
    }

    private fun callJavaScriptPlayingVideo() {
        val playingFunction = "javascript:playingVideo()"
        webView.evaluateJavascript(playingFunction, null)
    }

    private fun callJavaScriptPausingVideo() {
        val pausingFunction = "javascript:pausingVideo()"
        webView.evaluateJavascript(pausingFunction, null)
    }

    private fun callJavaScriptStopVideo() {
        val stopFunction = "javascript:stopVideo()"
        webView.evaluateJavascript(stopFunction, null)
    }




    inner class WebAppInterface {

        @JavascriptInterface
        fun playVideo() {
            Log.d("VIJAY", "Play Button clicked")
            runOnUiThread {
                if(!player.playWhenReady)
                    player.prepare()
//                webView.visibility= View.GONE
                player.playWhenReady = true
                callJavaScriptPlayingVideo()
            }
        }

        @JavascriptInterface
        fun pauseVideo() {
            Log.d("VIJAY", "pause Button clicked")
            runOnUiThread {
                player.pause()
                player.playWhenReady = false
                callJavaScriptPausingVideo()
            }
        }


        @JavascriptInterface
        fun stopVideo() {
            Log.d("VIJAY", "stop Button clicked")
            runOnUiThread {
                player.stop()
                player.playWhenReady = false
                player.prepare()
                player.seekTo(0)
                callJavaScriptPausingVideo()
            }
        }
        @JavascriptInterface
        fun speedVideo() {
            Log.d("VIJAY", "speed Button clicked")
            runOnUiThread {
                val playbackParameters = PlaybackParameters(5.0F)
                player.playbackParameters = playbackParameters
            }
        }

    }
}