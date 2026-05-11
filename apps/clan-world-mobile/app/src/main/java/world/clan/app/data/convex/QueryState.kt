package world.clan.app.data.convex

/**
 * Per-query lifecycle state. Debug builds keep the old cockpit demo fallback;
 * release builds surface empty/error live states so shipped APKs never show
 * fake backend rows.
 *
 *  - [Loading] — first poll hasn't returned yet.
 *  - [Live]    — poll succeeded; render this, even when the payload is empty.
 *  - [Stub]    — debug builds only: poll returned empty or failed; render the
 *                StubData fallback so the cockpit stays usable while hacking.
 *  - [Error]   — release builds: poll failed (transport, decode,
 *                non-success status); render an empty/error live state.
 */
sealed interface QueryState<out T> {
  data object Loading : QueryState<Nothing>
  data class Live<T>(val data: T) : QueryState<T>
  data object Stub : QueryState<Nothing>
  data class Error(val cause: Throwable) : QueryState<Nothing>
}
