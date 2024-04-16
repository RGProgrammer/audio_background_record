package com.rgpro.audio_background_record

import android.content.*
import android.content.Context.MODE_PRIVATE
import androidx.annotation.NonNull
import android.os.IBinder
import android.util.Log
import com.rgpro.audio_background_record.audiorecordservice.AudioRecordService
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.File

/** AudioBackgroundRecordPlugin */
class AudioBackgroundRecordPlugin: FlutterPlugin, MethodCallHandler,ServiceConnection,AudioRecordService.OnRecordStatusChangedListener {
  companion object{
    val TAG : String = AudioBackgroundRecordPlugin.javaClass.name
  }
  private lateinit var channel : MethodChannel
  private lateinit var context: Context
  private lateinit var audioRecordServiceIntent : Intent
  private lateinit var prefs : SharedPreferences
  private var service:AudioRecordService? =null //retrieve the service reference if possible




  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "audio_background_record")
    channel.setMethodCallHandler(this)

    context = flutterPluginBinding.applicationContext
    audioRecordServiceIntent = Intent(context , AudioRecordService::class.java)
    //if the service exist and ready we will be bound to it (this.service will be initialized)
    context.bindService(audioRecordServiceIntent, this, 0);
    prefs = context.getSharedPreferences("AudioBackgroundRecordConfig",MODE_PRIVATE);


  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {

    if(call.method == "startRecording"){

      if(this.service == null){
         result.success(false);
      }else{
        if(service!!.isRecording()){
          result.success(false);
        }else {
          service!!.startRecording() ;
          result.success(true);
        }
      }

    }
    else if(call.method =="stopRecording") {
      if (this.service == null) {
        result.success(false);
      } else {
        if (!service!!.isRecording()) {
          result.success(false);
        } else {
          service!!.stopRecording();
          result.success(true);
        }
      }
    }
    else if(call.method == "isRecording"){

      if(this.service == null){
        result.success(false);
     }else{
       result.success(service!!.isRecording())
     }
    }
    else if(call.method == "startService"){
      if(this.service!=null){
        Log.d(TAG,"service already started " )

        result.success(true)
      }else{
        if(context.startService(audioRecordServiceIntent)!=null) {
          Log.d(TAG,"service started successfully" )
          context.bindService(audioRecordServiceIntent,this,0);

          result.success(true);
        }else{
          Log.e(TAG,"service starting failed" )
          result.success(false);
        }
      }
    }else if(call.method == "stopService"){
      if(this.service==null){
        Log.d(TAG,"service already stopped " )
        result.success(true)
      }else{
          //this.service!!.unbindService(this);
          if(this.context.stopService(audioRecordServiceIntent)){
            this.service!!.onStatusChangedListener = null
            this.service = null
            result.success(true)
          }else{
            result.success(false);
          }

          Log.d(TAG,"service stopped " )
      }
    }else if(call.method == "isServiceRunning"){

      if(this.service!=null){
        result.success(true)
      }else{
        result.success(false)
      }
    }
    else if(call.method == "getRecordingDirectory"){

      val dir = prefs.getString("directory","")
      if(dir!= ""){
        result.success(dir);
      }else{
        result.success(AudioRecordService.DEFAULT_OUT_DIRECTORY)
      }

    } else if(call.method == "getMaxRecordDuration"){

      val duration = prefs.getInt("duration",AudioRecordService.default_MaxDuration);
      result.success(duration)

    }else if(call.method == "setConfiguration"){

        val argumentsMap = call.arguments as Map<String, Any?>?
        if (argumentsMap == null) {
          Log.e(TAG, "Could not convert channel arguments to Map<String,Any> ")
        } else {
          Log.d(TAG, "Arguments retrieved successfully $argumentsMap")
          (argumentsMap["directory"] as String?)?.let { directory ->

            prefs.edit()
              .putString("directory", directory)
              .apply();
            this.service?.setOutputDirectory(directory)

          }


          (argumentsMap["duration"] as Int?)?.let { duration ->
            prefs.edit()
              .putInt("duration",duration)
              .apply()

            this.service?.setMaxDuration(duration)
          }

          //TODO apply new notification text
          (argumentsMap["notificationText"] as Map<String, String>?)?.let{ notificationTextMap ->
              AudioRecordService.updateNotificationKeyValue(notificationTextMap) ;
          }

        }
       result.success(null);

    }else {
      result.notImplemented()
    }
  }

  override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
     val bridge = service as AudioRecordService.AudioRecordServiceBridge?
    bridge?.let{
      this.service = it.service ;
      this.service!!.onStatusChangedListener = this
      //applying any saved configuration :
      //outputDirectory
      prefs.getString("directory",null)?.let {
        this.service?.setOutputDirectory(it)
      }

      this.service?.setMaxDuration(
        prefs.getInt("duration",AudioRecordService.default_MaxDuration)
      )
    }

  }

  override fun onServiceDisconnected(name: ComponentName?) {
    this.service = null ;
  }

  override fun onStatusChanged(status:Int , errorMsg : String?) {
    channel.invokeMethod("recordStoppedCallBack", hashMapOf( "status" to status,"error" to errorMsg ));
  }

}
