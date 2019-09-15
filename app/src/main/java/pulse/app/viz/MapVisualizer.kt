package pulse.app.viz

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.TransitionOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.style.sources.VectorSource
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

        private lateinit var outerLayer: Layer
        private lateinit var middleLayer: Layer
        private lateinit var innerLayer: Layer

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
            val source = map.style?.getSourceAs<VectorSource>("composite")
            val features =
                source?.querySourceFeatures(arrayOf("building"), literal(true))

            val include = mutableSetOf<Feature>()

            val allIncludes = features?.mapNotNull { feature ->
                song.location ?: return@mapNotNull null

                val geometry = feature.geometry()
                if (geometry is Polygon) {
                    val lngLat = geometry.coordinates()[0][0]

                    return@mapNotNull feature to LatLng(
                        lngLat.latitude(),
                        lngLat.longitude()
                    ).distanceTo(
                        LatLng(
                            song.location!!.latitude,
                            song.location!!.longitude
                        )
                    )

                }

                null
            }

            if (allIncludes != null) {
                val outer = allIncludes.filter { (_, distance) ->
                    distance < 200 && distance >= 100
                }.map { (feature, _) -> feature }

                val middle = allIncludes.filter { (_, distance) ->
                    distance < 100 && distance >= 50
                }.map { (feature, _) -> feature }

                val inner = allIncludes.filter { (_, distance) ->
                    distance < 50
                }.map { (feature, _) -> feature }

                outerLayer = createLayer(outer, 2f)
                map.style?.addLayer(outerLayer)
                middleLayer = createLayer(middle, 5f)
                map.style?.addLayer(middleLayer)
                innerLayer = createLayer(inner, 10f)
                map.style?.addLayer(innerLayer)

                /*if (elapsedTime > 0) {
                    song.beats.forEachIndexed { index, beat ->
                        if (beat.elementStart > elapsedTime) {
                            beatIndex = index
                            return@forEachIndexed
                        }
                    }
                }*/
            }
        }

        private fun createLayer(features: List<Feature>, factor: Float): Layer {
            val collection = FeatureCollection.fromFeatures(features)

            val source = GeoJsonSource("3d-buildings-${this}-$factor", collection)
            map.style?.addSource(source)

            return FillExtrusionLayer(
                "3d-buildings-${this}-$factor",
                "3d-buildings-${this}-$factor"
            ).apply {
                setProperties(
                    fillExtrusionColor(Color.RED),
                    fillExtrusionHeight(
                        literal(factor)
                    ),
                    fillExtrusionBase(
                        literal(factor)
                    ),
                    fillExtrusionOpacity(0.7f)
                )
            }
        }

        private fun makeSwitch(features: List<Feature>): Array<Expression> {
            return features.flatMap { feature ->
                listOf<Expression>(get("\$id"), literal(true))
            }.toTypedArray()
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
                        height = song.beats[beatIndex].elementLoudness.toDouble()

                        outerLayer.setProperties(
                            fillExtrusionHeight(literal(height * 2.0))
                        )
                        middleLayer.setProperties(
                            fillExtrusionHeight(literal(height * 5.0))
                        )
                        innerLayer.setProperties(
                            fillExtrusionHeight(literal(height * 10.0))
                        )
                    }
                } else if (beatIndex >= song.beats.size - 1) {
                    isRunning = false
                    map.style?.removeLayer(outerLayer)
                    map.style?.removeLayer(middleLayer)
                    map.style?.removeLayer(innerLayer)
                } else {
                    handler.post {
                        height -= delta
                        outerLayer.setProperties(
                            fillExtrusionHeight(literal(height * 2.0))
                        )
                        middleLayer.setProperties(
                            fillExtrusionHeight(literal(height * 5.0))
                        )
                        innerLayer.setProperties(
                            fillExtrusionHeight(literal(height * 10.0))
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