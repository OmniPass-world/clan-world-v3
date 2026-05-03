import { useRef, useState, useCallback, useEffect } from 'react'
import RealmMap from './RealmMap'
import './MapRevealSlider.css'

/**
 * Before/after image slider: left half shows the RealmMap concept sketch,
 * right half reveals the realized in-game map photo.
 * Pointer Events API covers mouse + touch + pen.
 */
export default function MapRevealSlider() {
  const [position, setPosition] = useState(50) // percent
  const dragging = useRef(false)
  const containerRef = useRef<HTMLDivElement>(null)

  const clamp = (v: number) => Math.max(0, Math.min(100, v))

  const updateFromPointer = useCallback((clientX: number) => {
    const el = containerRef.current
    if (!el) return
    const rect = el.getBoundingClientRect()
    const pct = clamp(((clientX - rect.left) / rect.width) * 100)
    setPosition(pct)
  }, [])

  const onPointerDown = useCallback((e: React.PointerEvent) => {
    dragging.current = true
    ;(e.currentTarget as HTMLElement).setPointerCapture(e.pointerId)
    updateFromPointer(e.clientX)
  }, [updateFromPointer])

  const onPointerMove = useCallback((e: React.PointerEvent) => {
    if (!dragging.current) return
    updateFromPointer(e.clientX)
  }, [updateFromPointer])

  const onPointerUp = useCallback(() => {
    dragging.current = false
  }, [])

  // Keyboard support: arrow keys move slider
  const onKeyDown = useCallback((e: React.KeyboardEvent) => {
    if (e.key === 'ArrowLeft') setPosition(p => clamp(p - 2))
    else if (e.key === 'ArrowRight') setPosition(p => clamp(p + 2))
  }, [])

  // Container-level touch-action: prevent page scroll during drag
  useEffect(() => {
    const el = containerRef.current
    if (!el) return
    const prevent = (e: TouchEvent) => { if (dragging.current) e.preventDefault() }
    el.addEventListener('touchstart', prevent, { passive: false })
    el.addEventListener('touchmove', prevent, { passive: false })
    return () => {
      el.removeEventListener('touchstart', prevent)
      el.removeEventListener('touchmove', prevent)
    }
  }, [])

  return (
    <div
      ref={containerRef}
      className="mrs-container"
      onPointerDown={onPointerDown}
      onPointerMove={onPointerMove}
      onPointerUp={onPointerUp}
      onPointerCancel={onPointerUp}
    >
      {/* Left layer: concept map (always visible, clips right) */}
      <div
        className="mrs-layer mrs-layer-left"
        style={{ clipPath: `inset(0 ${100 - position}% 0 0)` }}
        aria-hidden="true"
      >
        <RealmMap className="mrs-realm-map" />
      </div>

      {/* Right layer: realized game map (clips left) */}
      <div
        className="mrs-layer mrs-layer-right"
        style={{ clipPath: `inset(0 0 0 ${position}%)` }}
        aria-hidden="true"
      >
        <img
          src="/game-map.jpg"
          alt="Realized in-game map at tick 174"
          className="mrs-game-map"
          loading="lazy"
          draggable={false}
        />
      </div>

      {/* Slider handle */}
      <div
        className="mrs-handle"
        style={{ left: `${position}%` }}
        role="slider"
        aria-label="Map reveal slider"
        aria-valuenow={Math.round(position)}
        aria-valuemin={0}
        aria-valuemax={100}
        tabIndex={0}
        onKeyDown={onKeyDown}
      >
        <div className="mrs-handle-line" />
        <div className="mrs-handle-knob" aria-hidden="true">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M6 10 L2 6 L2 14 Z" fill="currentColor" />
            <path d="M14 10 L18 6 L18 14 Z" fill="currentColor" />
            <line x1="9" y1="10" x2="11" y2="10" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
          </svg>
        </div>
      </div>
    </div>
  )
}
