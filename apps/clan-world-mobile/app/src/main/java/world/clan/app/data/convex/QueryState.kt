package world.clan.app.data.convex

/**
 * Per-query lifecycle state. Mirrors the web cockpit's "stub fallback when
 * the live query is undefined OR returns empty" pattern (see useSafeQuery
 * in apps/web/src/hooks/useSafeQuery.ts).
 *
 *  - [Loading] — first poll hasn't returned yet.
 *  - [Live]    — poll succeeded with a non-empty payload; render this.
 *  - [Stub]    — poll succeeded but returned an empty list (cold backend
 *                / nothing to show yet); render the StubData fallback.
 *  - [Error]   — poll failed (transport, decode, non-success status).
 *                Render the StubData fallback so the cockpit stays usable.
 */
sealed interface QueryState<out T> {
  data object Loading : QueryState<Nothing>
  data class Live<T>(val data: T) : QueryState<T>
  data object Stub : QueryState<Nothing>
  data class Error(val cause: Throwable) : QueryState<Nothing>
}
