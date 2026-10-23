package com.music.streamx.skins.dot

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.net.HttpURLConnection
import java.net.URL
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntSize
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.music.streamx.data.model.ShelfItem
import com.music.streamx.data.model.Song
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor

private const val RAMP = " .,:;irsXA253hMHGS#9B&@"
private const val GLYPH_SIZE = 24f
private const val BASELINE_SHIFT = 0.22f
private const val MIN_ROWS = 8
private const val MAX_ROWS = 96

private fun asciiPaint(): Paint = Paint().apply {
    typeface = Typeface.MONOSPACE
    isAntiAlias = true
    textSize = GLYPH_SIZE
}

private val Paint.cellAdvance: Float get() = measureText("M")
private val Paint.cellAspect: Float get() = cellAdvance / textSize

private fun gammaLift(channel: Int): Int {
    val normalized = channel / 255f
    val lifted = 255f * normalized.pow(0.7f)
    return lifted.roundToInt().coerceIn(0, 255)
}

private fun symbolFor(intensity: Float): Char {
    val lastIndex = RAMP.length - 1
    val index = (intensity * lastIndex).roundToInt().coerceIn(0, lastIndex)
    return RAMP[index]
}

private fun renderAsciiGrid(
    paint: Paint,
    cols: Int,
    rows: Int,
    cell: (x: Int, y: Int, paint: Paint) -> Char,
): Bitmap {
    val advance = paint.cellAdvance
    val lineH = paint.textSize
    val width = (cols * advance).roundToInt().coerceAtLeast(1)
    val height = (rows * lineH).roundToInt().coerceAtLeast(1)

    val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(out)
    val glyph = CharArray(1)

    for (y in 0 until rows) {
        for (x in 0 until cols) {
            val symbol = cell(x, y, paint)
            if (symbol == ' ') continue
            glyph[0] = symbol
            val textX = x * advance
            val baselineY = (y + 1) * lineH - lineH * BASELINE_SHIFT
            canvas.drawText(glyph, 0, 1, textX, baselineY, paint)
        }
    }
    return out
}

fun Bitmap.toAsciiBitmap(cols: Int = 96): Bitmap {
    val paint = asciiPaint()
    val safe = if (config == Bitmap.Config.HARDWARE) copy(Bitmap.Config.ARGB_8888, false) else this
    val rows = (cols.toFloat() * safe.height / safe.width * paint.cellAspect)
        .roundToInt()
        .coerceIn(MIN_ROWS, MAX_ROWS)

    // Multi-stage scale for smooth anti-aliased character luminance mapping
    val step1 = Bitmap.createScaledBitmap(safe, (cols * 2).coerceAtLeast(2), (rows * 2).coerceAtLeast(2), true)
    val small = Bitmap.createScaledBitmap(step1, cols.coerceAtLeast(1), rows.coerceAtLeast(1), true)

    val result = renderAsciiGrid(paint, cols, rows) { x, y, cellPaint ->
        val pixel = small.getPixel(x.coerceIn(0, small.width - 1), y.coerceIn(0, small.height - 1))
        val red = gammaLift(AndroidColor.red(pixel))
        val green = gammaLift(AndroidColor.green(pixel))
        val blue = gammaLift(AndroidColor.blue(pixel))
        val lum = (0.299f * red + 0.587f * green + 0.114f * blue) / 255f
        val symbol = symbolFor(lum)
        if (symbol != ' ') {
            val peak = maxOf(red, green, blue).coerceAtLeast(1)
            val gain = (210f / peak).coerceIn(1f, 2.2f)
            cellPaint.color = AndroidColor.rgb(
                (red * gain).roundToInt().coerceIn(0, 255),
                (green * gain).roundToInt().coerceIn(0, 255),
                (blue * gain).roundToInt().coerceIn(0, 255),
            )
        }
        symbol
    }
    if (small !== safe && small !== step1) small.recycle()
    if (step1 !== safe) step1.recycle()
    if (safe !== this) safe.recycle()
    return result
}

fun generateAsciiPlaceholder(seed: Long, cols: Int = 96): Bitmap {
    val paint = asciiPaint()
    val rows = (cols * paint.cellAspect).roundToInt().coerceIn(MIN_ROWS, MAX_ROWS)
    val random = Random(seed)
    val f1 = 0.10f + random.nextFloat() * 0.25f
    val f2 = 0.10f + random.nextFloat() * 0.25f
    val f3 = 0.05f + random.nextFloat() * 0.15f
    val p1 = random.nextFloat() * 6.28f
    val p2 = random.nextFloat() * 6.28f
    val p3 = random.nextFloat() * 6.28f
    val hue = random.nextFloat() * 360f
    val hsv = FloatArray(3)

    return renderAsciiGrid(paint, cols, rows) { x, y, cellPaint ->
        val v = (sin(x * f1 + p1) + sin(y * f2 + p2) + sin((x + y) * f3 + p3) + 3f) / 6f
        val symbol = symbolFor(v)
        if (symbol != ' ') {
            hsv[0] = hue
            hsv[1] = 0.30f
            hsv[2] = 0.20f + v * 0.45f
            cellPaint.color = AndroidColor.HSVToColor(hsv)
        }
        symbol
    }
}

private val asciiCacheLock = Any()
private val asciiCache = object : LinkedHashMap<String, Bitmap>(32, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Bitmap>?): Boolean = size > 96
}

private fun getCachedAscii(key: String): Bitmap? = synchronized(asciiCacheLock) { asciiCache[key] }
private fun putCachedAscii(key: String, bitmap: Bitmap) {
    synchronized(asciiCacheLock) {
        asciiCache[key] = bitmap
        // Evicted images may still be displayed by Compose, so let GC reclaim them.
        while (asciiCache.size > 1 && asciiCache.values.sumOf { it.allocationByteCount.toLong() } > 24L * 1024 * 1024) {
            asciiCache.remove(asciiCache.keys.first())
        }
    }
}

/** Skin-owned decoder: executable skins cannot rely on host-internal Coil extensions after R8. */
private fun loadCoverPixels(context: Context, source: Any): Bitmap {
    if (source is Bitmap) return source.copy(Bitmap.Config.ARGB_8888, false)
    val uri = when(source) { is Uri -> source; is String -> Uri.parse(source); else -> error("Unsupported cover") }
    val bytes = when(uri.scheme) {
        "https", "http" -> {
            var target=URL(uri.toString())
            var result: ByteArray?=null
            repeat(5) {
                if(result!=null)return@repeat
                val connection=(target.openConnection() as HttpURLConnection).apply {
                    connectTimeout=10000;readTimeout=15000;instanceFollowRedirects=false
                    setRequestProperty("User-Agent","StreamX-dot/1.0")
                }
                try {
                    val code=connection.responseCode
                    if(code in 300..399) {
                        val location=connection.getHeaderField("Location") ?: error("Missing cover redirect")
                        val next=URL(target,location)
                        require(next.protocol in setOf("https","http") && !(target.protocol=="https" && next.protocol!="https"))
                        target=next
                    } else {
                        check(code in 200..299){"Cover request failed"}
                        result=connection.inputStream.use { readCoverBytes(it) }
                    }
                } finally {connection.disconnect()}
            }
            result ?: error("Too many cover redirects")
        }
        "content", "file", "android.resource" -> context.contentResolver.openInputStream(uri)?.use { readCoverBytes(it) }
            ?: error("Cover unavailable")
        else -> error("Unsupported cover URI")
    }
    val bounds=BitmapFactory.Options().apply {inJustDecodeBounds=true}
    BitmapFactory.decodeByteArray(bytes,0,bytes.size,bounds)
    require(bounds.outWidth>0 && bounds.outHeight>0){"Invalid cover"}
    var sample=1
    while(maxOf(bounds.outWidth,bounds.outHeight)/sample>768)sample*=2
    return BitmapFactory.decodeByteArray(bytes,0,bytes.size,BitmapFactory.Options().apply {inSampleSize=sample;inPreferredConfig=Bitmap.Config.ARGB_8888})
        ?: error("Could not decode cover")
}
private fun readCoverBytes(input: java.io.InputStream): ByteArray {
    val out=ByteArrayOutputStream()
    val buffer=ByteArray(8192)
    while(true) {val count=input.read(buffer);if(count<0)break;require(out.size()+count<=8*1024*1024){"Cover too large"};out.write(buffer,0,count)}
    return out.toByteArray()
}

@Composable
fun rememberAsciiCoverBitmap(
    artworkSource: Any?,
    cols: Int = DotOptions.cols,
    seed: Long = 42L,
): Bitmap {
    val context = LocalContext.current
    val sourceKey = (artworkSource as? String) ?: artworkSource?.toString() ?: "seed:$seed"
    val cacheKey = "cover-v2:$sourceKey:$cols"

    var bitmap by remember(cacheKey) {
        mutableStateOf(getCachedAscii(cacheKey) ?: generateAsciiPlaceholder(seed, cols))
    }

    LaunchedEffect(cacheKey) {
        getCachedAscii(cacheKey)?.let {
            bitmap = it
            return@LaunchedEffect
        }
        if (artworkSource == null) return@LaunchedEffect

        try {
            val raw = withContext(Dispatchers.IO) { loadCoverPixels(context, artworkSource) }
            val ascii = withContext(Dispatchers.Default) {
                try { raw.toAsciiBitmap(cols) } finally { raw.recycle() }
            }
            putCachedAscii(cacheKey, ascii)
            bitmap = ascii
            Log.d("DotArtwork", "ASCII cover ready ($cols columns)")
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            // No URLs, credentials or provider payloads in logs.
            Log.w("DotArtwork", "Cover unavailable: ${failure.javaClass.simpleName}")
        }

    }
    return bitmap
}

@Composable
fun DotArtworkThumbnail(
    model: Any?,
    modifier: Modifier = Modifier,
    cols: Int = 40,
    playing: Boolean = false,
    wave: Boolean = false,
    contentDescription: String? = null,
) {
    if (DotOptions.asciiArt) {
        val seed = remember(model) {
            when (model) {
                is Song -> model.videoId.hashCode().toLong()
                is ShelfItem -> (model.videoId ?: model.browseId ?: model.title).hashCode().toLong()
                is String -> model.hashCode().toLong()
                else -> 42L
            }
        }
        val coverArt = when (model) {
            is Song -> model.thumbnailUrl
            is ShelfItem -> model.thumbnailUrl
            else -> model
        }
        val ascii = rememberAsciiCoverBitmap(
            artworkSource = coverArt,
            cols = cols,
            seed = seed,
        )
        AsciiCover(
            cover = ascii,
            playing = playing,
            wave = wave,
            modifier = modifier,
        )
    } else {
        val target = when (model) {
            is Song -> model.thumbnailUrl
            is ShelfItem -> model.thumbnailUrl
            else -> model
        }
        AsyncImage(
            model = target,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
fun AsciiCover(
    cover: Bitmap,
    playing: Boolean,
    modifier: Modifier = Modifier,
    wave: Boolean = true,
) {
    val image = remember(cover) { cover.asImageBitmap() }
    if (playing && wave) {
        WaveCover(image, modifier)
    } else {
        Canvas(modifier = modifier) {
            drawImage(
                image = image,
                dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
                filterQuality = FilterQuality.Medium,
            )
        }
    }
}

@Composable
private fun WaveCover(
    image: ImageBitmap,
    modifier: Modifier,
) {
    val transition = rememberInfiniteTransition(label = "wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase",
    )
    Canvas(
        modifier = modifier
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen),
    ) {
        drawImage(
            image = image,
            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
            filterQuality = FilterQuality.Medium,
        )
        val cx = (-0.6f + phase * 2.8f) * size.width
        val band = size.width * 0.28f
        drawRect(
            brush = Brush.linearGradient(
                colorStops = arrayOf(
                    0.0f to Color.Transparent,
                    0.3f to Color.White.copy(alpha = 0.12f),
                    0.5f to Color.White.copy(alpha = 0.55f),
                    0.7f to Color.White.copy(alpha = 0.12f),
                    1.0f to Color.Transparent,
                ),
                start = Offset(cx - band, 0f),
                end = Offset(cx + band, size.height * 0.45f),
            ),
            blendMode = BlendMode.SrcAtop,
        )
    }
}
