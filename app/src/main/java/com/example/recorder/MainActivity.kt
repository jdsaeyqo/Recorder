package com.example.recorder

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    private var state = State.BEFORE_RECODING
        set(value) {
            field = value
            resetBtn.isEnabled = (value == State.AFTER_RECORDING || value == State.ON_PLAYING)
            recordBtn.updateIconWithState(value)
        }

    private val requiredPermission = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null


    private val recordingFilePath: String by lazy {
        "${externalCacheDir?.absolutePath}/recording.3gp"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        requestAudioPermission()
        initViews()
        bindViews()
        initVariable()


    }

    //권한 결과에 따른 동작
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val audioRecordPermissionGranted =
            requestCode == REQUEST_RECORD_AUDIO_PERMISSION &&
                    grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED

        //권한 거부 시 앱 종료
        if (!audioRecordPermissionGranted) {
            finish()
        }

    }

    //권한 요청 코드
    private fun requestAudioPermission() {
        requestPermissions(requiredPermission, REQUEST_RECORD_AUDIO_PERMISSION)

    }

    private fun initViews() {
        recordBtn.updateIconWithState(state)
    }

    private fun bindViews() {

        soundVisualizerView.onRequestCurrentAmplitude = {
            recorder?.maxAmplitude ?: 0
        }

        resetBtn.setOnClickListener {

            stopPlaying()
            soundVisualizerView.clearVisualization()
            recordTimetextView.clearCount()
            state = State.BEFORE_RECODING

        }

        recordBtn.setOnClickListener {

            when (state) {

                State.BEFORE_RECODING -> {
                    startRecording()
                }
                State.ON_RECODRING -> {
                    stopRecording()
                }
                State.AFTER_RECORDING -> {
                    startPlaying()
                }
                State.ON_PLAYING -> {
                    stopPlaying()
                }
            }

        }
    }

    private fun initVariable() {
        state = State.BEFORE_RECODING
    }


    private fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(recordingFilePath)

            prepare()
        }
        recorder?.start()
        soundVisualizerView.startVisualizing(false)
        recordTimetextView.startCountUp()
        state = State.ON_RECODRING
    }

    private fun stopRecording() {
        recorder?.run {
            stop()
            release()
        }
        recorder = null
        soundVisualizerView.stopVisualizing()
        recordTimetextView.stopCount()
        state = State.AFTER_RECORDING
    }

    private fun startPlaying() {
        player = MediaPlayer().apply {
            setDataSource(recordingFilePath)
            prepare()
        }
        player?.setOnCompletionListener {
            stopPlaying()
            state = State.AFTER_RECORDING
        }
        player?.start()
        soundVisualizerView.startVisualizing(true)
        recordTimetextView.startCountUp()
        state = State.ON_PLAYING

    }

    private fun stopPlaying() {
        player?.release()
        player = null
        soundVisualizerView.stopVisualizing()
        recordTimetextView.stopCount()
        state = State.AFTER_RECORDING
    }

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 201
    }
}