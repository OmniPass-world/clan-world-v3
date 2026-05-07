package io.easya.kickstart

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.BitmapShader
import android.widget.ImageView
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min

object TokenImageLoader {
  private const val GOLD_MINT = "4kWysUHVqtFmxrvwPUxA66exm2iJBMkvD4EBRrNmcieL"
  private val cache = ConcurrentHashMap<String, Bitmap>()

  fun loadInto(imageView: ImageView, iconUrl: String?, fallbackRes: Int = R.drawable.kickstart_icon) {
    val url = normalizeUrl(iconUrl)
    if (url == null) {
      imageView.setImageResource(fallbackRes)
      return
    }
    cache[url]?.let {
      imageView.setImageBitmap(it)
      return
    }
    imageView.setImageResource(fallbackRes)
    Thread {
      val bitmap = loadBitmap(url) ?: return@Thread
      cache[url] = bitmap
      imageView.post {
        imageView.setImageBitmap(bitmap)
      }
    }.start()
  }

  fun widgetCoinBitmap(context: Context, token: KickstartToken?, allowNetwork: Boolean): Bitmap? {
    if (token == null) return null
    if (token.tokenMint == GOLD_MINT) {
      return circleCrop(BitmapFactory.decodeResource(context.resources, R.drawable.gold_widget_logo))
    }
    val url = normalizeUrl(token.iconUrl)
    val source = when {
      url == null -> BitmapFactory.decodeResource(context.resources, R.drawable.kickstart_icon)
      cache[url] != null -> cache[url]
      allowNetwork -> loadBitmap(url)?.also { cache[url] = it }
        ?: BitmapFactory.decodeResource(context.resources, R.drawable.kickstart_icon)
      else -> BitmapFactory.decodeResource(context.resources, R.drawable.kickstart_icon)
    }
    return source?.let { circleCrop(it) }
  }

  fun shouldFetchWidgetImage(token: KickstartToken?): Boolean {
    val selected = token ?: return false
    val url = normalizeUrl(selected.iconUrl) ?: return false
    return selected.tokenMint != GOLD_MINT && cache[url] == null
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
