package com.harvinder.radiostreamingdemo


import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.metadata.MetadataOutput
import com.google.android.exoplayer2.metadata.icy.IcyHeaders
import com.google.android.exoplayer2.metadata.icy.IcyInfo
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.ads.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.harvinder.radiostreamingdemo.databinding.ActivityMainBinding
import com.harvinder.radiostreamingdemo.others.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import saschpe.exoplayer2.ext.icy.IcyHttpDataSourceFactory
import java.io.IOException
import java.net.URL

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val TAG = "MediaPlayerTag"
    private lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var context: Context
    private lateinit var mediaSession: MediaSession
    private lateinit var stateBuilder: PlaybackState.Builder
    private lateinit var ep_radio_view: PlayerView
    private var mInterstitialAd: InterstitialAd? = null
    private var addStatus: Boolean = true
    var playerNotificationManager:PlayerNotificationManager?=null
    private var notificationId = 123;
    private var channelId = "channelId"
    private val exoPlayerEventListener = ExoPlayerEventListener()
    private var title:String=""
    private var name:String=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_recent
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        MobileAds.initialize(this) {

        }

        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("ABCDEF012345"))
                .build()
        )

        context = this
        ep_radio_view = findViewById(R.id.ep_radio_view)



        play(Constants.MEDIA_URL)
        interstitialAd()
           }
    fun play(url: String) {
        initializeMediaSession()
        initializePlayer()
        var playbackPosition = 0L
        GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT){
            val userAgent = Util.getUserAgent(context, context.getString(R.string.app_name))
            if (exoPlayer == null) {
                exoPlayer = ExoPlayerFactory.newSimpleInstance(
                    applicationContext,
                    DefaultRenderersFactory(applicationContext),
                    DefaultTrackSelector(),
                    DefaultLoadControl()
                ).apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(C.CONTENT_TYPE_MUSIC)
                            .setUsage(C.USAGE_MEDIA)
                            .build(), true
                    )
                }

            }
            exoPlayer.addMetadataOutput {
                for (i in 0 until it.length()) {
                    val entry = it.get(i)
                    if (entry is IcyHeaders) {
                        Log.d(TAG, "onIcyMetaData: icyHeaders=$entry")
                        name= entry.name.toString()
                        binding.llMedia.tvName.text=name
                    }
                    if (entry is IcyInfo) {
                        Log.d(TAG, "onMetadata: icyHeaders=$entry")
                        title= entry.title.toString()
                        if(title.equals("")){
                            binding.llMedia.tvTitle.text="Ads"
                            notificationManager()
                            title="Ads"
                        }else{
                            binding.llMedia.tvTitle.text=title
                            notificationManager()
                        }

                    }
                }
            }

// The MediaSource represents the media to be played
            exoPlayer?.addListener(exoPlayerEventListener)

//
//            // Custom HTTP data source factory which requests Icy metadata and parses it if
//            // the stream server supports it
//            val client = OkHttpClient.Builder().build()
//            val icyHttpDataSourceFactory = IcyHttpDataSourceFactory.Builder(client)
//                .setUserAgent(userAgent)
//                .setIcyHeadersListener { Log.d(TAG, "onIcyMetaData: icyHeaders=$it") }
//                .setIcyMetadataChangeListener {
//                    Log.d(TAG, "onIcyMetaData: icyMetadata=$it")
//
//                }
//
//                .build()
//            // Produces DataSource instances through which media data is loaded
//            val dataSourceFactory = DefaultDataSourceFactory(
//                applicationContext, null, icyHttpDataSourceFactory
//            )
//            // Produces Extractor instances for parsing the media data
//            val extractorsFactory = DefaultExtractorsFactory()
//
//            val mediaSource = ExtractorMediaSource.Factory(dataSourceFactory)
//                .setExtractorsFactory(extractorsFactory)
//                .createMediaSource(Uri.parse(url))

            val mediaSource = ProgressiveMediaSource
                .Factory(DefaultDataSourceFactory(applicationContext, userAgent))
                .createMediaSource(Uri.parse(url))
            exoPlayer.prepare(mediaSource)
            exoPlayer.playWhenReady = false

        }

        exoPlayer.setSeekParameters(SeekParameters.NEXT_SYNC)
        playbackPosition= exoPlayer.getCurrentPosition()/1000;
        exoPlayer.seekTo(0, playbackPosition)
        Log.d("time",""+playbackPosition)
        ep_radio_view.setControllerShowTimeoutMs(0);
        ep_radio_view.setControllerHideOnTouch(false);
        //exoPlayer.playWhenReady = false
        ep_radio_view.player?.addListener(object : Player.DefaultEventListener() {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playWhenReady && playbackState == Player.STATE_READY) {
                    // media actually playing
                    Log.d("state", "" + playbackState)
                    if (addStatus) {
                        showInterstitial()
                    }

                } else {
                    // player paused in any state
                    Log.d("state", "" + playbackState)
                }
            }
        })


        notificationManager()


    }


    private fun initializePlayer() {
        exoPlayer = SimpleExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                ep_radio_view.player = exoPlayer
            }

    }


    private fun initializeMediaSession() {
        mediaSession = MediaSession(context, TAG)
        mediaSession.setFlags(
            MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS
        )
        mediaSession.setMediaButtonReceiver(null)

        stateBuilder = PlaybackState.Builder()
            .setActions(
                PlaybackState.ACTION_PLAY or
                        PlaybackState.ACTION_PAUSE or
                        PlaybackState.ACTION_PLAY_PAUSE or
                        PlaybackState.ACTION_FAST_FORWARD or
                        PlaybackState.ACTION_REWIND
            )
        mediaSession.setPlaybackState(stateBuilder.build())

        mediaSession.setCallback(SessionCallback())

        mediaSession.isActive = true

    }

    private inner class SessionCallback : MediaSession.Callback() {

        private val SEEK_WINDOW_MILLIS = 10000

        override fun onPlay() {
            exoPlayer.playWhenReady = true
        }

        override fun onPause() {
            exoPlayer.playWhenReady = false
        }

        override fun onRewind() {
            exoPlayer.seekTo(exoPlayer.currentPosition - SEEK_WINDOW_MILLIS)
        }

        override fun onFastForward() {
            exoPlayer.seekTo(exoPlayer.currentPosition + SEEK_WINDOW_MILLIS)
        }
    }

    private fun releasePlayer() {
        if (exoPlayer != null) {
            exoPlayer.release()
            playerNotificationManager?.setPlayer(null);
        }
    }

    private fun pausePlayer() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false)
            exoPlayer.getPlaybackState()
            playerNotificationManager?.setPlayer(null);
        }
    }

    private fun resumePlayer() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true)
            exoPlayer.getPlaybackState()
            playerNotificationManager?.setPlayer(null);
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()

    }

    override fun onRestart() {
        super.onRestart()
        resumePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }


    fun interstitialAd() {
        // Create the InterstitialAd and set it up.
        mInterstitialAd = InterstitialAd(this).apply {
            adUnitId = Constants.AD_UNIT_ID
            adListener = (
                    object : AdListener() {
                        override fun onAdLoaded() {
                            if (mInterstitialAd != null) {
                                mInterstitialAd?.show()
                            }

                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            val error =
                                "domain: ${loadAdError.domain}, code: ${loadAdError.code}, " +
                                        "message: ${loadAdError.message}"
                            Toast.makeText(
                                this@MainActivity,
                                "onAdFailedToLoad() with error $error",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onAdClosed() {
                            Log.d(TAG, "closeAd")
                            exoPlayer.playWhenReady = true
                        }
                    }
                    )

        }

    }
    // Show the ad if it's ready.
    private fun showInterstitial() {
        addStatus = false
        if (mInterstitialAd!!.isLoaded) {
            mInterstitialAd?.show()
        } else {
            var adRequest = AdRequest.Builder().build()
            mInterstitialAd?.loadAd(adRequest)
        }
    }
    private inner class ExoPlayerEventListener : Player.EventListener {
        override fun onTimelineChanged(timeline: Timeline, manifest: Any?, reason: Int) = Unit
        override fun onTracksChanged(groups: TrackGroupArray, selections: TrackSelectionArray) = Unit
        override fun onLoadingChanged(isLoading: Boolean) = Unit

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Log.i(
                TAG,
                "onPlayerStateChanged: playWhenReady=$playWhenReady, playbackState=$playbackState"
            )
            when (playbackState) {
//                Player.STATE_IDLE, Player.STATE_BUFFERING, Player.STATE_READY ->
//                    //isPlaying = true
//                Player.STATE_ENDED ->
//                    //stop()
            }
        }}

    fun notificationManager(){
        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
            this,
            "channelId",
            R.string.app_name,
            notificationId,
            object : PlayerNotificationManager.MediaDescriptionAdapter {



                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    // return pending intent
//                    val intent = Intent(context, AudioPlayer::class.java);
//                    return PendingIntent.getActivity(
//                        context, 0, intent,
//                        PendingIntent.FLAG_UPDATE_CURRENT
//                    )

                    return null
                }

                //pass description here
                override fun getCurrentContentText(player: Player): String? {
                    return title
                }

                //pass title (mostly playing audio name)
                override fun getCurrentContentTitle(player: Player): String {
//                    Log.d("title",return "Title")
//                    val window: Int = player.getCurrentWindowIndex()
                    return title
                    // return "Title"
                }

                // pass image as bitmap
                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): Bitmap? {
                    var image: Bitmap? = null
                    try {
                        var url = URL("content image url")
                        image = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                    } catch (e: IOException) {

                    }
                    return image
                }
            },

            )
        //attach player to playerNotificationManager
        playerNotificationManager?.setPlayer(exoPlayer)
    }

}