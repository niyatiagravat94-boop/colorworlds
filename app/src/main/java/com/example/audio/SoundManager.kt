package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.data.local.GamePreferences
import com.example.data.model.WorldId
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.*

class SoundManager(private val preferences: GamePreferences) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val sampleRate = 44100

    // Music streaming state
    private var musicTrack: AudioTrack? = null
    private var musicJob: Job? = null
    private var currentWorldId: WorldId = WorldId.COLOR_GARDEN
    private var isDucked: Boolean = false
    private var currentMusicGain: Float = 0f

    // Precomputed seamless loop waveforms for each world (FloatArray of samples in -1.0..1.0)
    private val worldMusicLoops = ConcurrentHashMap<WorldId, FloatArray>()

    init {
        // Pre-generate the musical loop buffers asynchronously
        scope.launch {
            for (world in WorldId.entries) {
                worldMusicLoops[world] = generateWorldMusicLoop(world)
            }
        }
    }

    /**
     * Plays a one-shot synthesized PCM sound effect, respecting SFX volume and enable state.
     */
    private fun playPcm(
        durationMs: Int,
        baseVolume: Float = 1.0f,
        generateSamples: (t: Double, durationSec: Double) -> Double
    ) {
        if (!preferences.isSfxEnabled || preferences.sfxVolume <= 0f) return

        val sfxGain = (preferences.sfxVolume * baseVolume).coerceIn(0f, 1f)

        scope.launch {
            try {
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)
                val durationSec = durationMs / 1000.0

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val sample = (generateSamples(t, durationSec) * sfxGain).coerceIn(-1.0, 1.0)
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()

                delay(durationMs.toLong() + 80L)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                // Ignore transient audio playback interruptions
            }
        }
    }

    // --- SOUND EFFECTS ---

    fun playButtonClick() {
        playPcm(60, baseVolume = 0.5f) { t, _ ->
            val env = exp(-t * 40.0)
            sin(2.0 * PI * 850.0 * t) * env
        }
    }

    fun playBlockPickup() {
        playPcm(110, baseVolume = 0.45f) { t, dur ->
            val freq = 420.0 + (t / dur) * 320.0
            val env = sin(PI * (t / dur))
            (sin(2.0 * PI * freq * t) + 0.25 * sin(2.0 * PI * freq * 2.0 * t)) * env
        }
    }

    fun playBlockSnap() {
        playPcm(130, baseVolume = 0.6f) { t, _ ->
            val env = exp(-t * 30.0)
            val sub = sin(2.0 * PI * 160.0 * t) * exp(-t * 18.0)
            val pop = sin(2.0 * PI * 720.0 * t) * env
            (pop * 0.6 + sub * 0.4)
        }
    }

    fun playLineClear(comboCount: Int = 1) {
        val baseFreq = when (comboCount) {
            1 -> 523.25 // C5
            2 -> 659.25 // E5
            3 -> 783.99 // G5
            4 -> 1046.50 // C6
            else -> 1318.51 // E6
        }

        playPcm(380, baseVolume = 0.7f) { t, _ ->
            val env = exp(-t * 7.5)
            val fund = sin(2.0 * PI * baseFreq * t)
            val third = sin(2.0 * PI * (baseFreq * 1.2599) * t) * 0.45
            val fifth = sin(2.0 * PI * (baseFreq * 1.4983) * t) * 0.35
            val shimmer = sin(2.0 * PI * (baseFreq * 2.0) * t) * 0.25 * sin(2.0 * PI * 14.0 * t)
            (fund + third + fifth + shimmer) * env
        }
    }

    fun playComboCheer(comboCount: Int) {
        val mult = (comboCount.coerceAtLeast(1) * 110.0).coerceAtMost(550.0)
        playPcm(420, baseVolume = 0.75f) { t, _ ->
            val env = exp(-t * 5.5)
            val freq1 = 600.0 + mult + sin(2.0 * PI * 18.0 * t) * 40.0
            val freq2 = 820.0 + mult
            val wave = sin(2.0 * PI * freq1 * t) * 0.6 + sin(2.0 * PI * freq2 * t) * 0.4
            wave * env
        }
    }

    fun playStarEarned(starIndex: Int) {
        val pitch = 580.0 + starIndex * 260.0
        playPcm(260, baseVolume = 0.7f) { t, _ ->
            val env = exp(-t * 11.0)
            (sin(2.0 * PI * pitch * t) + 0.35 * sin(2.0 * PI * pitch * 2.0 * t)) * env
        }
    }

    fun playLevelComplete() {
        scope.launch {
            val notes = listOf(523.25, 659.25, 783.99, 1046.50, 1318.51)
            for (freq in notes) {
                playPcm(220, baseVolume = 0.65f) { t, _ ->
                    val env = exp(-t * 9.0)
                    (sin(2.0 * PI * freq * t) + 0.3 * sin(2.0 * PI * freq * 2.0 * t)) * env
                }
                delay(70)
            }
        }
    }

    fun playWorldUnlock() {
        scope.launch {
            val fanfare = listOf(440.0, 554.37, 659.25, 880.0, 1108.73, 1318.51)
            for (freq in fanfare) {
                playPcm(340, baseVolume = 0.75f) { t, _ ->
                    val env = exp(-t * 5.5)
                    (sin(2.0 * PI * freq * t) + 0.45 * sin(2.0 * PI * freq * 1.5 * t)) * env
                }
                delay(85)
            }
        }
    }

    fun playInvalidPlacement() {
        playPcm(140, baseVolume = 0.4f) { t, _ ->
            val env = exp(-t * 20.0)
            (sin(2.0 * PI * 135.0 * t) + sin(2.0 * PI * 128.0 * t)) * env
        }
    }

    // --- CONTINUOUS SEAMLESS MUSIC SYSTEM ---

    /**
     * Starts or smoothly updates ambient music for the specified world.
     * Never abruptly restarts if the same world music is already active.
     */
    fun startAmbientMusic(worldId: WorldId) {
        currentWorldId = worldId

        if (!preferences.isMusicEnabled || preferences.musicVolume <= 0f) {
            stopAmbientMusic()
            return
        }

        if (musicJob?.isActive == true) {
            // Already running; loop streamer will automatically blend to new world if changed
            return
        }

        startMusicStreamer()
    }

    /**
     * Smoothly ducks or restores music volume (e.g. during Pause dialog or overlays).
     */
    fun setDucked(ducked: Boolean) {
        isDucked = ducked
    }

    fun updateWorld(worldId: WorldId) {
        currentWorldId = worldId
        if (preferences.isMusicEnabled && musicJob?.isActive != true) {
            startAmbientMusic(worldId)
        }
    }

    fun updateMusicVolume() {
        if (!preferences.isMusicEnabled || preferences.musicVolume <= 0f) {
            stopAmbientMusic()
        } else if (musicJob?.isActive != true) {
            startAmbientMusic(currentWorldId)
        }
    }

    fun stopAmbientMusic() {
        musicJob?.cancel()
        musicJob = null
        try {
            musicTrack?.pause()
            musicTrack?.flush()
            musicTrack?.stop()
            musicTrack?.release()
            musicTrack = null
        } catch (e: Exception) {
            // Ignore teardown errors
        }
        currentMusicGain = 0f
    }

    fun pauseMusic() {
        stopAmbientMusic()
    }

    fun resumeMusic() {
        if (preferences.isMusicEnabled && preferences.musicVolume > 0f) {
            startAmbientMusic(currentWorldId)
        }
    }

    private fun startMusicStreamer() {
        musicJob?.cancel()
        musicJob = scope.launch {
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            musicTrack = track
            track.play()

            val chunkSize = 2048
            val chunkBuffer = ShortArray(chunkSize)
            var samplePos = 0

            var activeWorld = currentWorldId
            var activeLoop = worldMusicLoops[activeWorld] ?: generateWorldMusicLoop(activeWorld).also {
                worldMusicLoops[activeWorld] = it
            }

            while (isActive) {
                // Check if world changed -> smoothly crossfade
                if (currentWorldId != activeWorld) {
                    activeWorld = currentWorldId
                    activeLoop = worldMusicLoops[activeWorld] ?: generateWorldMusicLoop(activeWorld).also {
                        worldMusicLoops[activeWorld] = it
                    }
                    samplePos = 0
                }

                // Compute target gain based on preference, enable toggle, and ducking
                val targetGain = if (preferences.isMusicEnabled) {
                    val baseVol = preferences.musicVolume
                    if (isDucked) baseVol * 0.32f else baseVol * 0.65f // Calm, balanced default
                } else 0f

                // Fill chunk buffer with smooth gain interpolation
                val loopLen = activeLoop.size
                for (i in 0 until chunkSize) {
                    // Smooth gain glide (avoid clicking)
                    currentMusicGain += (targetGain - currentMusicGain) * 0.005f

                    val rawSample = activeLoop[samplePos % loopLen]
                    val finalSample = (rawSample * currentMusicGain).coerceIn(-1f, 1f)
                    chunkBuffer[i] = (finalSample * Short.MAX_VALUE).toInt().toShort()

                    samplePos++
                    if (samplePos >= loopLen) samplePos = 0
                }

                track.write(chunkBuffer, 0, chunkSize)

                // If disabled and gain reached zero, pause briefly
                if (!preferences.isMusicEnabled && currentMusicGain < 0.001f) {
                    delay(200)
                }
            }
        }
    }

    /**
     * Synthesizes an 8-bar seamless, relaxing casual puzzle musical loop for each world.
     * The loop has sample-perfect crossfaded ends for seamless repeat.
     */
    private fun generateWorldMusicLoop(worldId: WorldId): FloatArray {
        val loopSeconds = 12.0
        val totalSamples = (sampleRate * loopSeconds).toInt()
        val loop = FloatArray(totalSamples)

        val chordProgressions = when (worldId) {
            WorldId.COLOR_GARDEN -> listOf(
                // C Major, F Major, G Major, A Minor (Happy, playful, warm)
                listOf(261.63, 329.63, 392.00), // C Maj
                listOf(220.00, 261.63, 349.23), // F Maj
                listOf(246.94, 293.66, 392.00), // G Maj
                listOf(220.00, 261.63, 329.63)  // A Min
            )
            WorldId.OCEAN_WORLD -> listOf(
                // G Major, D Major, E Minor, C Major (Aquatic, relaxing, flowing)
                listOf(196.00, 246.94, 293.66), // G Maj
                listOf(220.00, 293.66, 369.99), // D Maj
                listOf(164.81, 246.94, 329.63), // E Min
                listOf(261.63, 329.63, 392.00)  // C Maj
            )
            WorldId.MOUNTAIN_WORLD -> listOf(
                // A Minor, F Major, C Major, G Major (Peaceful, acoustic adventure)
                listOf(220.00, 261.63, 329.63),
                listOf(174.61, 261.63, 349.23),
                listOf(261.63, 329.63, 392.00),
                listOf(196.00, 246.94, 293.66)
            )
            WorldId.SPACE_WORLD -> listOf(
                // E Minor, B Minor, C Major, D Major (Dreamy, cosmic, floating)
                listOf(164.81, 196.00, 246.94),
                listOf(123.47, 185.00, 220.00),
                listOf(130.81, 196.00, 261.63),
                listOf(146.83, 220.00, 293.66)
            )
            WorldId.CRYSTAL_WORLD -> listOf(
                // D Major, G Major, B Minor, A Major (Magical, sparkling celeste)
                listOf(293.66, 369.99, 440.00),
                listOf(196.00, 293.66, 392.00),
                listOf(246.94, 293.66, 369.99),
                listOf(220.00, 277.18, 329.63)
            )
        }

        val barDuration = loopSeconds / chordProgressions.size
        val beatsPerBar = 4
        val beatDuration = barDuration / beatsPerBar

        for (barIdx in chordProgressions.indices) {
            val chord = chordProgressions[barIdx]
            val barStartSec = barIdx * barDuration

            // Bass note (smooth warm fundamental)
            val bassFreq = chord[0] * 0.5

            // Generate notes in bar
            for (beat in 0 until beatsPerBar) {
                val beatStartSec = barStartSec + beat * beatDuration
                val noteIdx = beat % chord.size
                val melodyFreq = chord[noteIdx] * 2.0 // High pleasant register

                val startSample = (beatStartSec * sampleRate).toInt()
                val noteDurationSamples = (beatDuration * 0.9 * sampleRate).toInt()

                for (s in 0 until noteDurationSamples) {
                    val idx = startSample + s
                    if (idx < totalSamples) {
                        val t = s.toDouble() / sampleRate
                        val noteEnv = exp(-t * 3.5) // Gentle marimba decay
                        val melody = sin(2.0 * PI * melodyFreq * t) * noteEnv * 0.16

                        loop[idx] += melody.toFloat()
                    }
                }
            }

            // Warm atmospheric chord pad underneath
            val barStartSample = (barStartSec * sampleRate).toInt()
            val barDurationSamples = (barDuration * sampleRate).toInt()

            for (s in 0 until barDurationSamples) {
                val idx = barStartSample + s
                if (idx < totalSamples) {
                    val t = s.toDouble() / sampleRate
                    // Slow attack and release pad envelope
                    val padEnv = sin(PI * (s.toDouble() / barDurationSamples))

                    var padWave = 0.0
                    for (freq in chord) {
                        padWave += sin(2.0 * PI * freq * t) + 0.2 * sin(2.0 * PI * (freq * 1.002) * t)
                    }
                    val bassWave = sin(2.0 * PI * bassFreq * t) * 0.5

                    val chordSample = (padWave * 0.04 + bassWave * 0.08) * padEnv
                    loop[idx] += chordSample.toFloat()
                }
            }
        }

        // Apply a 50ms crossfade at the boundaries for seamless looping
        val fadeSamples = (sampleRate * 0.05).toInt()
        for (i in 0 until fadeSamples) {
            val alpha = i.toFloat() / fadeSamples
            val startVal = loop[i]
            val endVal = loop[totalSamples - fadeSamples + i]

            loop[i] = (endVal * (1f - alpha) + startVal * alpha)
        }

        return loop
    }

    fun release() {
        stopAmbientMusic()
        scope.cancel()
    }
}
