import { useRef, useState, useCallback, useEffect } from 'react'
import RealmMap from './RealmMap'
import './MapRevealSlider.css'

/**
 * Before/after image slider: left half shows the RealmMap concept sketch,
 * right half reveals the realized in-game map photo.
 * Pointer Events API covers mouse + touch + pen.
 *
 * Intro animation: when the slider first scrolls into view, the handle
 * sweeps right (0→75), back left (75→30), then bounce-corrects to 33.
 * Runs once per page load. User pointer input is ignored mid-animation
 * so the choreography isn't broken by a mid-show tap. Respects
 * prefers-reduced-motion.
 */
export default function MapRevealSlider() {
  const [position, setPosition] = useState(0) // percent — starts at 0 for intro sweep
  const dragging = useRef(false)
  const containerRef = useRef<HTMLDivElement>(null)
  const hasAnimatedRef = useRef(false)
  const isAnimatingRef = useRef(false)

  const clamp = (v: number) => Math.max(0, Math.min(100, v))

  const updateFromPointer = useCallback((clientX: number) => {
    const el = containerRef.current
    if (!el) return
    const rect = el.getBoundingClientRect()
    const pct = clamp(((clientX - rect.left) / rect.width) * 100)
    setPosition(pct)
  }, [])

  const onPointerDown = useCallback((e: React.PointerEvent) => {
    if (isAnimatingRef.current) return // don't let user hijack the intro show
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
    if (isAnimatingRef.current) return
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

  // Intro animation: sweep right → sweep left → bounce-stop at 33%.
  // Fires once on first intersection (threshold 0.3). Respects reduced-motion.
  useEffect(() => {
    const el = containerRef.current
    if (!el) return

    const reducedMotion =
      typeof window !== 'undefined' &&
      typeof window.matchMedia === 'function' &&
      window.matchMedia('(prefers-reduced-motion: reduce)').matches

    if (reducedMotion) {
      hasAnimatedRef.current = true
      setPosition(33)
      return
    }

    const easeInOutCubic = (t: number) =>
      t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2
    const easeOutQuad = (t: number) => 1 - (1 - t) * (1 - t)

    const phases = [
      { from: 0, to: 75, duration: 800, ease: easeInOutCubic },   // right sweep
      { from: 75, to: 30, duration: 700, ease: easeInOutCubic },  // left sweep — overshoots 33 by 3pp
      { from: 30, to: 33, duration: 220, ease: easeOutQuad },     // bounce correct up to 33
    ]

    let raf = 0
    const runIntro = () => {
      isAnimatingRef.current = true
      let phaseIdx = 0
      let phaseStart = performance.now()
      const step = (now: number) => {
        const phase = phases[phaseIdx]
        if (!phase) {
          isAnimatingRef.current = false
          return
        }
        const elapsed = now - phaseStart
        const t = Math.min(1, elapsed / phase.duration)
        const eased = phase.ease(t)
        const value = phase.from + (phase.to - phase.from) * eased
        setPosition(value)
        if (t >= 1) {
          phaseIdx++
          if (phaseIdx >= phases.length) {
            isAnimatingRef.current = false
            return
          }
          phaseStart = now
        }
        raf = requestAnimationFrame(step)
      }
      raf = requestAnimationFrame(step)
    }

    const observer = new IntersectionObserver(
      (entries) => {
        const entry = entries[0]
        if (entry?.isIntersecting && !hasAnimatedRef.current) {
          hasAnimatedRef.current = true
          runIntro()
        }
      },
      { threshold: 0.3 }
    )
    observer.observe(el)

    return () => {
      observer.disconnect()
      if (raf) cancelAnimationFrame(raf)
      isAnimatingRef.current = false
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

      {/* BEFORE / AFTER labels — overlaid on image layers, below handle */}
      <span className="mrs-label mrs-label-before">BEFORE</span>
      <span className="mrs-label mrs-label-after">AFTER</span>

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
