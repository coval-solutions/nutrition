package com.covalsolutions.nutrition

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.util.Log
import androidx.annotation.NonNull
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.tasks.Tasks

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

const val COVAL_NUTRITION = "COVAL_NUTRITION"
const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1234
val DATA_TYPE: DataType = DataType.TYPE_NUTRITION

/** NutritionPlugin */
class NutritionPlugin : FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private var activity : Activity? = null
  private var latestCall : MethodCall? = null

  private var latestResult: Result? = null
  //private var handler: Handler? = null

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.flutterEngine.dartExecutor, "nutrition")
    channel.setMethodCallHandler(this)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  // This static function is optional and equivalent to onAttachedToEngine. It supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both be defined
  // in the same class.
  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "nutrition")
      channel.setMethodCallHandler(NutritionPlugin())
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Boolean {
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
        Log.d(COVAL_NUTRITION, "Access Granted!")
        latestResult?.success(true)
      } else {
        Log.d(COVAL_NUTRITION, "Access Denied!")
      }
    }

    return false
  }

  private val fitnessOptions = FitnessOptions.builder()
          .addDataType(DATA_TYPE, FitnessOptions.ACCESS_READ)
          .build()

  private fun getData(call: MethodCall, result: Result) {
    var dataPoints: DataSet? = null
    val googleSignInAccount: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(activity?.applicationContext)
    if (googleSignInAccount === null) {
      result.error(COVAL_NUTRITION + "_NOT_LOGGED_IN_ERROR", "You don't seem to be logged in via Google", "googleSignInAccount is null")
    }

    thread {
      val startTime = call.argument<Long>("startDate")!!
      val endTime = call.argument<Long>("endDate")!!
      val response = Fitness.getHistoryClient(activity!!.applicationContext, googleSignInAccount!!)
              .readData(DataReadRequest.Builder()
                      .read(DATA_TYPE)
                      .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                      .build())

      dataPoints = Tasks.await(response).getDataSet(DATA_TYPE)
      activity!!.runOnUiThread {
        result.success(dataPoints?.dataPoints?.get(0)?.getValue(Field.FIELD_NUTRIENTS)?.getKeyValue(Field.NUTRIENT_CALORIES))
      }
    }
  }

  private fun requestPermission(call: MethodCall, result: Result) {
    latestResult = result
    if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(activity), fitnessOptions)) {
      GoogleSignIn.requestPermissions(
              activity!!,
              GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
              GoogleSignIn.getLastSignedInAccount(activity),
              fitnessOptions)
    } else {
      latestResult?.success(true)
      Log.d(COVAL_NUTRITION, "Access already granted!")
    }
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    latestCall = call
    latestResult = result
    when (call.method) {
      "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
      "requestPermission" -> requestPermission(call, result)
      "getData" -> getData(call, result)
      else -> result.notImplemented()
    }
  }
  override fun onDetachedFromActivity() {
    TODO("Not yet implemented")
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    TODO("Not yet implemented")
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    this.activity = binding.activity
  }

  override fun onDetachedFromActivityForConfigChanges() {
    TODO("Not yet implemented")
  }
}
