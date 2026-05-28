package com.example.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

class AudioChordAnalyzer {
    private val TAG = "AudioChordAnalyzer"

    // Real-time audio analyzer states mapping to UI
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _rmsVolume = MutableStateFlow(0f)
    val rmsVolume: StateFlow<Float> = _rmsVolume.asStateFlow()

    private val _frequencyHz = MutableStateFlow(0f)
    val frequencyHz: StateFlow<Float> = _frequencyHz.asStateFlow()

    private val _nearestNote = MutableStateFlow("None")
    val nearestNote: StateFlow<String> = _nearestNote.asStateFlow()

    private val _detectedChord = MutableStateFlow("None")
    val detectedChord: StateFlow<String> = _detectedChord.asStateFlow()

    private val _waveformPoints = MutableStateFlow<List<Float>>(emptyList())
    val waveformPoints: StateFlow<List<Float>> = _waveformPoints.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val sampleRate = 22050 // lower sample rate for higher pitch resolution and lighter autocorrelation
    private val bufferSize = 2048

    // For simulation support when mic is blocked or in silent conditions
    private var simulationJob: Job? = null
    private var isSimulated = false

    @SuppressLint("MissingPermission")
    fun startListening(scope: CoroutineScope, useSimulationFallback: Boolean = false) {
        if (_isRecording.value) return

        _isRecording.value = true
        isSimulated = useSimulationFallback

        if (useSimulationFallback) {
            startSimulation(scope)
            return
        }

        recordingJob = scope.launch(Dispatchers.IO) {
            val minBufSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val finalBufSize = maxOf(minBufSize, bufferSize)

            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    finalBufSize
                )

                if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    Log.e(TAG, "AudioRecord could not initialize. Falling back to simulation mode.")
                    withContext(Dispatchers.Main) {
                        isSimulated = true
                        startSimulation(scope)
                    }
                    return@launch
                }

                audioRecord?.startRecording()
                val buffer = ShortArray(bufferSize)

                while (isActive && _isRecording.value) {
                    val readResult = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                    if (readResult > 0) {
                        processAudioBuffer(buffer, readResult)
                    }
                    delay(50) // Analyze ~20 times per second for smooth visual responses
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception starting microphone: ${e.message}. Falling back to simulation.")
                withContext(Dispatchers.Main) {
                    isSimulated = true
                    startSimulation(scope)
                }
            } finally {
                stopNativeRecord()
            }
        }
    }

    private fun startSimulation(scope: CoroutineScope) {
        simulationJob = scope.launch(Dispatchers.Default) {
            var counter = 0f
            while (isActive && _isRecording.value) {
                // Generate simulated cozy guitar waveform
                val points = List(40) { i ->
                    val angle = (i * 0.4f) + counter
                    val noise = (Math.sin(angle * 2.0).toFloat() * 0.4f) + 
                                (Math.sin(angle * 5.0).toFloat() * 0.2f)
                    noise * 0.8f
                }
                _waveformPoints.value = points

                // Simulate mild background room volume
                val currentVol = 0.15f + (Math.sin(counter.toDouble()).toFloat() * 0.05f)
                _rmsVolume.value = currentVol

                // Choose a random note/chord periodically
                if (counter.toInt() % 30 == 0) {
                    val simOptions = listOf(
                        Triple(196.0f, "G3", "G Major"),
                        Triple(130.8f, "C3", "C Major"),
                        Triple(146.8f, "D3", "D Major"),
                        Triple(110.0f, "A2", "A Major"),
                        Triple(164.8f, "E3", "E Minor"),
                        Triple(174.6f, "F3", "F Major")
                    )
                    val chosen = simOptions.random()
                    _frequencyHz.value = chosen.first + (Math.random().toFloat() * 2f - 1f)
                    _nearestNote.value = chosen.second
                    _detectedChord.value = chosen.third
                }

                counter += 0.3f
                delay(80)
            }
        }
    }

    // Direct pitch assessment using autocorrelation
    private fun processAudioBuffer(buffer: ShortArray, size: Int) {
        // 1. Calculate RMS volume
        var sumSquares = 0.0
        for (i in 0 until size) {
            sumSquares += buffer[i] * buffer[i]
        }
        val rms = sqrt(sumSquares / size).toFloat()
        // Standardize volume bounds (max short is 32767)
        val normalizedVolume = minOf(rms / 4000f, 1f)
        _rmsVolume.value = normalizedVolume

        // Generate nice visual waveform from a subset of buffer values
        val step = size / 40
        val points = List(40) { i ->
            val idx = i * step
            if (idx < size) buffer[idx] / 32768f else 0f
        }
        _waveformPoints.value = points

        // Only search for pitch if there is actual input noise/strum of significance
        if (normalizedVolume < 0.04f) {
            _frequencyHz.value = 0f
            _nearestNote.value = "Quiet"
            _detectedChord.value = "None"
            return
        }

        // 2. Perform Autocorrelation pitch finding
        // Focus on general guitar frequencies (80Hz to 400Hz)
        val minLag = sampleRate / 400
        val maxLag = sampleRate / 75
        var bestLag = -1
        var bestCorrelation = -1f

        val correlation = FloatArray(maxLag + 1)
        for (lag in minLag..maxLag) {
            var sum = 0f
            var meanSquare = 0f
            for (i in 0 until (size - lag)) {
                sum += (buffer[i].toFloat() / 32768f) * (buffer[i + lag].toFloat() / 32768f)
            }
            correlation[lag] = sum
            if (sum > bestCorrelation) {
                bestCorrelation = sum
                bestLag = lag
            }
        }

        if (bestLag > 0 && bestCorrelation > 0.01f) {
            val freq = sampleRate.toFloat() / bestLag
            _frequencyHz.value = freq
            mapFrequencyToGuitar(freq)
        } else {
            _frequencyHz.value = 0f
            _nearestNote.value = "Inaudible"
            _detectedChord.value = "None"
        }
    }

    private fun mapFrequencyToGuitar(freq: Float) {
        // Nearest string / core note mapping (Standard Tuning notes)
        val guitarNotes = listOf(
            82.4f to "E2 (String 6)",
            110.0f to "A2 (String 5)",
            146.8f to "D3 (String 4)",
            196.0f to "G3 (String 3)",
            246.9f to "B3 (String 2)",
            329.6f to "E4 (String 1)",
            130.8f to "C3 (C Note)",
            174.6f to "F3 (F Note)"
        )

        val closestNote = guitarNotes.minByOrNull { Math.abs(it.first - freq) }
        val noteName = closestNote?.second ?: "Fret sound"
        _nearestNote.value = noteName

        // Chord detection based on core fundamental guitar frequency clusters
        val chord = when {
            freq in 190.0f..202.0f -> "G Major"
            freq in 126.0f..135.0f -> "C Major"
            freq in 142.0f..152.0f -> "D Major"
            freq in 105.0f..115.0f -> "A Major"
            freq in 160.0f..168.0f -> "E Minor"
            freq in 170.0f..178.0f -> "F Major"
            freq in 215.0f..225.0f -> "A Minor"
            else -> "Interference"
        }
        _detectedChord.value = chord
    }

    fun stopListening() {
        _isRecording.value = false
        recordingJob?.cancel()
        recordingJob = null
        simulationJob?.cancel()
        simulationJob = null
        stopNativeRecord()
    }

    private fun stopNativeRecord() {
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping AudioRecord: ${e.message}")
        } finally {
            audioRecord = null
        }
    }

    // Force inject a specific fret sound for visual practice
    fun mockTriggerChordStrung(chordName: String) {
        if (!isSimulated) return
        _detectedChord.value = chordName
        val mappedNote = when (chordName) {
            "G Major" -> "G3"
            "C Major" -> "C3"
            "D Major" -> "D3"
            "A Major" -> "A2"
            "E Minor" -> "E3"
            "F Major" -> "F3"
            else -> "A3"
        }
        _nearestNote.value = "$mappedNote (Strung)"
        _rmsVolume.value = 0.95f
    }
}
