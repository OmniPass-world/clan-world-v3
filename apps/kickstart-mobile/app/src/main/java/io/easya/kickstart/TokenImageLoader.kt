package io.easya.kickstart

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.BitmapShader
import android.util.LruCache
import android.widget.ImageView
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import kotlin.math.min

object TokenImageLoader {
  private const val GOLD_MINT = "4kWysUHVqtFmxrvwPUxA66exm2iJBMkvD4EBRrNmcieL"

  // Cap total bitmap memory at ~6MB. Each cached icon is small (~16-64KB after
  // BitmapFactory decode, larger if the source is uncompressed PNG), so this
  // budget admits dozens of icons while preventing widget refresh storms from
  // blowing past the heap.
  private const val MAX_CACHE_BYTES = 6 * 1024 * 1024
  private val cache = object : LruCache<String, Bitmap>(MAX_CACHE_BYTES) {
    override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
  }

  // Bounded thread pool — previously each loadInto() spawned a raw Thread,
  // so a leaderboard refresh could trigger 100+ concurrent network sockets +
  // bitmap decodes. Cap at 4 so widget refreshes degrade gracefully under load.
  private val executor = Executors.newFixedThreadPool(4)

  fun loadInto(imageView: ImageView, iconUrl: String?, fallbackRes: Int = R.drawable.kickstart_icon) {
    val url = normalizeUrl(iconUrl)
    if (url == null) {
      imageView.setImageResource(fallbackRes)
      return
    }
    cache.get(url)?.let {
      imageView.setImageBitmap(it)
      return
    }
    imageView.setImageResource(fallbackRes)
    executor.execute {
      val bitmap = loadBitmap(url) ?: return@execute
      cache.put(url, bitmap)
      imageView.post {
        imageView.setImageBitmap(bitmap)
      }
    }
  }

  fun widgetCoinBitmap(context: Context, token: KickstartToken?, allowNetwork: Boolean): Bitmap? {
    if (token == null) return null
    if (token.tokenMint == GOLD_MINT) {
      return circleCrop(BitmapFactory.decodeResource(context.resources, R.drawable.gold_widget_logo))
    }
    val url = normalizeUrl(token.iconUrl)
    val source = when {
      url == null -> BitmapFactory.decodeResource(context.resources, R.drawable.kickstart_icon)
      cache.get(url) != null -> cache.get(url)
      allowNetwork -> loadBitmap(url)?.also { cache.put(url, it) }
        ?: BitmapFactory.decodeResource(context.resources, R.drawable.kickstart_icon)
      else -> BitmapFactory.decodeResource(context.resources, R.drawable.kickstart_icon)
    }
    return source?.let { circleCrop(it) }
  }

  fun shouldFetchWidgetImage(token: KickstartToken?): Boolean {
    val selected = token ?: return false
    val url = normalizeUrl(selected.iconUrl) ?: return false
    return selected.tokenMint != GOLD_MINT && cache.get(url) == null
  }

  private fun loadBitmap(url: String): Bitmap? {
    return runCatching {
      val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = 7_000
        readTimeout = 7_000
        instanceFollowRedirects = true
      }
      connection.inputStream.use { BitmapFactory.decodeStream(it) }
    }.getOrNull()
  }

  private fun normalizeUrl(iconUrl: String?): String? {
    val trimmed = iconUrl?.trim()?.takeIf { it.isNotBlank() } ?: return null
    return if (trimmed.startsWith("ipfs://")) {
      "https://ipfs.io/ipfs/${trimmed.removePrefix("ipfs://").trimStart('/')}"
    } else {
      trimmed
    }
  }

  private fun circleCrop(source: Bitmap): Bitmap {
    val size = min(source.width, source.height)
    val left = (source.width - size) / 2
    val top = (source.height - size) / 2
    val square = Bitmap.createBitmap(source, left, top, size, size)
    val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      shader = BitmapShader(square, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    }
    canvas.drawOval(RectF(0f, 0f, size.toFloat(), size.toFloat()), paint)
    return output
  }
}
