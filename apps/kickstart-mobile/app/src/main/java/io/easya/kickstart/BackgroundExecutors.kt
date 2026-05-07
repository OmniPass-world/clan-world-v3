package io.easya.kickstart

import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Shared bounded executors for off-UI work.
 *
 * Replaces ad-hoc `Thread { ... }.start()` calls that previously fanned out
 * unboundedly during widget refresh storms (one fresh OS thread per widget
 * instance per onUpdate). Capping at 2 threads is enough headroom for the
 * widget HTTP fetch + cache write to overlap, but bounded enough that a
 * homescreen with N widgets won't trigger N concurrent network sockets +
 * SharedPreferences writers.
 *
 * Note: TokenImageLoader has its own dedicated 4-thread pool for bitmap
 * decode work; that's intentionally a separate pool because bitmap decode
 * is much heavier per task than the widget refresh fetches and we don't
 * want one to head-of-line block the other.
 */
object BackgroundExecutors {
  /** For widget data refresh + config-screen async work. Cap at 2. */
  val widget: Executor = Executors.newFixedThreadPool(2)
}
