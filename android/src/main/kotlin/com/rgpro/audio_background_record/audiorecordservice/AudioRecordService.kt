package com.rgpro.audio_background_record.audiorecordservice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentResolver
import android.content.Intent

import android.media.AudioManager
import android.media.AudioRecordingConfiguration
import android.media.MediaRecorder
import android.media.MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED
import android.media.MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rgpro.audio_background_record.R
import java.io.IOException
import java.lang.IllegalStateException
import java.util.*

class AudioRecordService : Service(), MediaRecorder.OnInfoListener {
    companion object {
        val TAG = AudioRecordService.javaClass.name
        private var default_outDirectory = "";
        var DEFAULT_OUT_DIRECTORY: String =""
            get() = default_outDirectory
        val default_MaxDuration = 20000 // 20 seconds
        //default notification Text
        private var notificationText = mutableMapOf<String,String> ().also{
            it.put("title", "Audio Recording Service" )
            it.put("ready","Service is ready to record")
            it.put("recording" , "Recording")

        }
        public fun updateNotificationKeyValue(source : Map<String,String> ) {
            if(source.containsKey("title"))
                AudioRecordService.notificationText["title"]= source["title"] as String

            if(source.containsKey("ready"))
                AudioRecordService.notificationText["ready"]= source["ready"] as String

            if(source.containsKey("recording"))
                AudioRecordService.notificationText["recording"]= source["recording"]as String
        }
    }

    inner class AudioRecordServiceBridge(service: AudioRecordService) : Binder() {
        val service: AudioRecordService = service;
    }
    interface OnRecordStatusChangedListener{
        fun onStatusChanged(state : Int , errorMsg : String? = null ) ;
    }

    private var recorder: MediaRecorder? = null
    private var binder = AudioRecordServiceBridge(this)
    private var filename: String = ""
    private var isRecording = false
    private val id: Int = 7
    private lateinit var notificationMgr: NotificationManager

    private var outDirectory: String? = null
    private var maxDuration : Int? = default_MaxDuration

    private var _onStatusChangedListener : OnRecordStatusChangedListener? =null
    public var onStatusChangedListener: OnRecordStatusChangedListener?
        get() {
            return _onStatusChangedListener
        }
        set(value) {
            _onStatusChangedListener = value
        }


    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        recorder?.let {
            if(isRecording)
                it.stop()
            it.release();
        }
        notificationMgr.cancel(id)
    }


    override fun onCreate() {
        super.onCreate()
        notificationMgr = getSystemService(NOTIFICATION_SERVICE) as NotificationManager;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "audioRecordNotification"
            val descriptionText = ""
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("audioRecordNotification", name, importance).apply {
                description = descriptionText
            }
            notificationMgr.createNotificationChannel(channel)
        }
        default_outDirectory = this.application.baseContext.externalCacheDir?.absolutePath!!;
        updateNotification()
    }

//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        Log.d(TAG, "onStartCommand: service onStartCommand started")
//        //instance = this
//        intent?.let {
//            if (it.getBooleanExtra("startAuto", false)) {
//                this.startRecording()
//            }
//        }
//        return super.onStartCommand(intent, flags, startId)
//    }

    private fun updateNotification() {
        val nb = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(this.applicationContext, "audioRecordNotification")
        } else {
            NotificationCompat.Builder(this.applicationContext)
        }
        nb.setContentTitle(notificationText.get("title"))
            .setContentText(
                if (isRecording()) {
                    notificationText.get("recording")
                } else {
                    notificationText.get("ready")
                }
            )
            .setSmallIcon(R.drawable.ic_bg_service_small)

        notificationMgr.notify(id, nb.build())
    }

    private fun prepareNewRecorderInstance(): Boolean {

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this.application.baseContext)
        } else {
            MediaRecorder();
        }
        try {
            recorder?.setOnInfoListener(this)
            recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            //recorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_2_TS);
            recorder?.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
            recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        } catch (e: Exception) {
            Log.e(TAG, "error ${e.message}");
            recorder?.release();
            recorder = null;
            onStatusChangedListener?.onStatusChanged(0,e.message) // error
            return false;
        }
        return true;
    }

    public fun startRecording() {
        prepareNewRecorderInstance()
        val outputDir = if (outDirectory!=null) {
            outDirectory
        } else {
            default_outDirectory
        }
        filename = "$outputDir/${UUID.randomUUID()}.mp3"
        recorder?.setOutputFile(filename);

        recorder?.setMaxDuration(maxDuration!!)

        try {


            recorder?.prepare();
            recorder?.start();
            isRecording = true;
            onStatusChangedListener?.onStatusChanged(1,null);
            updateNotification();
            Log.d(TAG, "startRecording: started")
        } catch (e: IOException) {
            isRecording = false;
            Log.e(TAG, "startRecording: ", e)
            onStatusChangedListener?.onStatusChanged(0,e.message) // error
        } catch (e: IllegalStateException) {
            isRecording = false;
            Log.e(TAG, "startRecording: ", e)
            onStatusChangedListener?.onStatusChanged(0,e.message) // error
        }

    }

    public fun stopRecording() {
        recorder?.let {
            if(isRecording) {
                try {
                    it.stop()
                    onStatusChangedListener?.onStatusChanged(2,null);
                } catch (e:Exception) {
                    onStatusChangedListener?.onStatusChanged(0,e.message) // error
                }

            }
            it.release()

        }
        isRecording = false;
        updateNotification();
        Log.d(TAG, "stopRecording $onStatusChangedListener")
    }

    public fun isRecording(): Boolean {
        return isRecording;
    }
    public fun setOutputDirectory(directory :String) {
        //TODO check if the directory is valid
        outDirectory = directory ;
    }
    public fun setMaxDuration(duration: Int){
        Log.d(TAG,"max duration set to $duration")
        maxDuration = duration ;
    }

    override fun onInfo(mr: MediaRecorder?, what: Int, extra: Int) {
        if(what == MEDIA_RECORDER_INFO_MAX_DURATION_REACHED ||
            what == MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED){
            this.stopRecording()
        }
    }
}