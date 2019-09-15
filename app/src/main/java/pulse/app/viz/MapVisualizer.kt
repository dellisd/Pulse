package pulse.app.viz

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.TransitionOptions
import pulse.app.Beat
import pulse.app.Song

class MapVisualizer(
    private val context: Context,
    private val map: MapboxMap,
    private val songs: List<Song>
) {

    inner class PlaybackThread(
        val song: Song,
        val fps: Float = 60f
    ) : Thread() {
        var isRunning = true
        var elapsedTime = 0L // System.currentTimeMillis() - song.startTime
            private set
        private var beatIndex = 0
        private var height = 0.0

        private val handler = Handler(context.mainLooper)

        private val layer = FillExtrusionLayer("3d-buildings-${this}", "composite").apply {
            sourceLayer = "building"
            setFilter(eq(`var`("extrude"), true))
            setProperties(
                fillExtrusionColor(Color.LTGRAY),
                fillExtrusionHeight(
                    literal(0)
                ),
                fillExtrusionBase(
                    literal(0)
                ),
                fillExtrusionOpacity(0.6f)
            )
        }

        init {
            map.style?.addLayer(layer)

            /*if (elapsedTime > 0) {
                song.beats.forEachIndexed { index, beat ->
                    if (beat.elementStart > elapsedTime) {
                        beatIndex = index
                        return@forEachIndexed
                    }
                }
            }*/
        }

        override fun run() {
            super.run()

            var last = System.currentTimeMillis()

            while (isRunning) {
                val now = System.currentTimeMillis()
                val delta = now - last
                last = now

                elapsedTime += delta

                if (beatIndex < song.beats.size - 1 && elapsedTime >= song.beats[beatIndex + 1].elementStart) {
                    val duration = song.beats[beatIndex + 1].elementStart - elapsedTime
                    beatIndex++

                    handler.post {
                        height = song.beats[beatIndex].elementLoudness * 10.0
                        layer.setProperties(
                            fillExtrusionHeight(literal(height))
                        )
                    }
                } else if (beatIndex >= song.beats.size - 1) {
                    isRunning = false

                } else {
                    handler.post {
                        height -= delta
                        layer.setProperties(
                            fillExtrusionHeight(literal(height))
                        )
                    }
                }

                val pause = (1000L / fps).toLong() - (System.currentTimeMillis() - now)
                if (pause > 0) {
                    sleep(pause)
                }
            }
        }
    }

    private val fps: Float

    private val songMap: Map<Song, PlaybackThread>

    init {
        val display = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        fps = display.defaultDisplay.refreshRate

        songMap = songs.map {
            it to PlaybackThread(it, fps)
        }.toMap()
    }

    fun start() {
        songMap.values.forEach(PlaybackThread::start)
    }

    fun stop() {

    }


}