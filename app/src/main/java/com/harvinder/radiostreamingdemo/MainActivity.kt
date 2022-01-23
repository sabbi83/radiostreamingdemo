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
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.ads.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.harvinder.radiostreamingdemo.databinding.ActivityMainBinding
import com.harvinder.radiostreamingdemo.others.Constants
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.net.URL

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val TAG = "MediaPlayerTag"
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var context: Context
    private lateinit var mediaSession: MediaSession
    private lateinit var stateBuilder: PlaybackState.Builder
    private lateinit var ep_radio_view: PlayerView
    private var mInterstitialAd: InterstitialAd? = null
    private var addStatus: Boolean = true
    var playerNotificationManager:PlayerNotificationManager?=null

    private var notificationId = 123;
    private var channelId = "channelId"
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
        val userAgent = Util.getUserAgent(context, context.getString(R.string.app_name))

        val mediaSource = ExtractorMediaSource.Factory(DefaultDataSourceFactory(context, userAgent))
            .setExtractorsFactory(DefaultExtractorsFactory())
            .createMediaSource(Uri.parse(url))

        exoPlayer.setSeekParameters(SeekParameters.NEXT_SYNC)
        exoPlayer.prepare(mediaSource)
        playbackPosition= exoPlayer.getCurrentPosition()/1000;
        exoPlayer.seekTo(0, playbackPosition)
        Log.d("time",""+playbackPosition)
        ep_radio_view.setControllerShowTimeoutMs(0);
        ep_radio_view.setControllerHideOnTouch(false);
        exoPlayer.playWhenReady = false
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
                    return "Description"
                }

                //pass title (mostly playing audio name)
                override fun getCurrentContentTitle(player: Player): String {
                    Log.d("title",return "Title")
                    val window: Int = player.getCurrentWindowIndex()
                    return "Title"
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

}