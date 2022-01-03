package com.covalsolutions.nutrition

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.util.Log
import androidx.annotation.NonNull
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import com.google.android.gms.tasks.Tasks
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap
import kotlin.concurrent.thread


const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1111
const val MMOLL_2_MGDL = 18.0 // 1 mmoll= 18 mgdl

class NutritionPlugin: FlutterPlugin, MethodCallHandler, Result, ActivityAware, ActivityResultListener {
  private var result: Result? = null
  private var handler: Handler? = null
  private var activity: Activity? = null

  private lateinit var channel : MethodChannel

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, CHANNEL_NAME)
    channel.setMethodCallHandler(this)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  override fun success(p0: Any?) {
    handler?.post { result?.success(p0) }
  }

  override fun notImplemented() {
    handler?.post { result?.notImplemented() }
  }

  override fun error(
    errorCode: String, errorMessage: String?, errorDetails: Any?) {
    handler?.post { result?.error(errorCode, errorMessage, errorDetails) }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
    if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
      if (resultCode == Activity.RESULT_OK) {
        Logger.debug("onActivityResult","Access Granted")
        result?.success(true)
      } else if (resultCode == Activity.RESULT_CANCELED) {
        Logger.debug("onActivityResult","Access Denied")
        result?.success(false)
      }
    }

    return false
  }

  private var mResult: Result? = null

  private fun isIntField(dataSource: DataSource, unit: Field): Boolean {
    val dataPoint =  DataPoint.builder(dataSource).build()
    val value = dataPoint.getValue(unit)
    return value.format == Field.FORMAT_INT32
  }

  /// Extracts the (numeric) value from a Health Data Point
  private fun getHealthDataValue(dataPoint: DataPoint, field: Field): Any {
    val value = dataPoint.getValue(field)
    // Conversion is needed because glucose is stored as mmoll in Google Fit;
    // while mgdl is used for glucose in this plugin.
    val isGlucose = field == HealthFields.FIELD_BLOOD_GLUCOSE_LEVEL
    return when (value.format) {
      Field.FORMAT_FLOAT -> if (!isGlucose)  value.asFloat() else value.asFloat() * MMOLL_2_MGDL
      Field.FORMAT_INT32 -> value.asInt()
      Field.FORMAT_STRING -> value.asString()
      else -> Log.e("Unsupported format:", value.format.toString())
    }
  }

  private fun callToHealthTypes(call: MethodCall): FitnessOptions {
    val args = call.arguments as HashMap<*, *>
    val types = (args["types"] as? ArrayList<*>)?.filterIsInstance<String>()

    val typesBuilder = FitnessOptions.builder()
    if (types != null) {
      typesBuilder.addDataType(DataType.TYPE_NUTRITION, FitnessOptions.ACCESS_READ)
      typesBuilder.addDataType(DataType.TYPE_NUTRITION, FitnessOptions.ACCESS_WRITE)
      typesBuilder.addDataType(DataType.AGGREGATE_NUTRITION_SUMMARY, FitnessOptions.ACCESS_WRITE)
      typesBuilder.addDataType(DataType.AGGREGATE_NUTRITION_SUMMARY, FitnessOptions.ACCESS_WRITE)
    }

    return typesBuilder.build()
  }

  private fun hasPermissions(call: MethodCall, result: Result) {
    if (activity == null) {
      result.success(false)
      return
    }

    val optionsToRegister = callToHealthTypes(call)
    val isGranted = GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(activity!!), optionsToRegister)

    result.success(isGranted)
  }

  private fun getEnums(result: Result) {
    result.success(HealthDataType.values().map { nutrientsEnum -> nutrientsEnum.name  })
  }

  private fun requestAuthorization(call: MethodCall, result: Result) {
    if (activity == null) {
      result.success(false)
      return
    }

    val optionsToRegister = callToHealthTypes(call)
    mResult = result

    val isGranted = GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(activity!!), optionsToRegister)
    if (isGranted) {
      result.success(true)
      return
    } else {
      GoogleSignIn.requestPermissions(
        activity!!,
        GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
        GoogleSignIn.getLastSignedInAccount(activity!!),
        optionsToRegister)
    }
  }

  private fun getHealthData(call: MethodCall, result: Result) {
    if (activity == null) {
      result.success(null)
      return
    }

    val args = call.arguments as HashMap<*, *>
    val types = (args["types"] as? ArrayList<*>)?.filterIsInstance<String>()

    val fromDate: Long = if (args["fromDate"] is Long) {
      args["fromDate"] as Long
    } else {
      val calendar = Calendar.getInstance()
      calendar.set(Calendar.HOUR_OF_DAY, 0)
      calendar.set(Calendar.MINUTE, 0)
      calendar.set(Calendar.SECOND, 0)
      calendar.set(Calendar.MILLISECOND, 0)
      calendar.add(Calendar.DATE, -6)
      calendar.timeInMillis
    }

    val toDate: Long = if (args["toDate"] is Long) {
      args["toDate"] as Long
    } else {
      val calendar = Calendar.getInstance()
      calendar.timeInMillis
    }

    thread {
      try {
        if (types == null || types.isEmpty()) {
          throw Exception("No types have been provided")
        }

        val fitnessOptionsBuilder: FitnessOptions.Builder = FitnessOptions.builder()
          .addDataType(DataType.TYPE_NUTRITION)
          .addDataType(DataType.AGGREGATE_NUTRITION_SUMMARY)

        val googleSignInAccount = GoogleSignIn.getAccountForExtension(activity!!.applicationContext, fitnessOptionsBuilder.build())

        val readRequest = DataReadRequest.Builder()
          .aggregate(DataType.TYPE_NUTRITION)
          .bucketByTime(1, TimeUnit.DAYS)
          .setTimeRange(fromDate, toDate, TimeUnit.MILLISECONDS)
          .build()

        val response = Fitness.getHistoryClient(activity!!.applicationContext, googleSignInAccount)
          .readData(readRequest)

        val dataReadResponse: DataReadResponse = Tasks.await<DataReadResponse>(response)
        // Used linkedMapOf instead of mutableMapOf to maintain insertion order (i.e. by date)
        val results: LinkedHashMap<Long, Map<String, Double>> = linkedMapOf()
        dataReadResponse.buckets.forEach { bucket ->
          val dataPoints: List<DataPoint> =
                bucket.getDataSet(DataType.AGGREGATE_NUTRITION_SUMMARY)?.dataPoints ?: emptyList()

          val nutrients = mutableMapOf<String, Double>()
          types.forEach { type ->
            if (dataPoints.isEmpty()) {
              nutrients[type] = 0.0
            } else {
              // There should only be one datapoint, since we aggregate the data
              val healthDataType: HealthDataType = HealthDataType.keyToHealthDataType(type)
              val value: Float = dataPoints.first().getValue(Field.FIELD_NUTRIENTS).getKeyValue(healthDataType.value) ?: 0.0F
              nutrients[type] = value.toDouble()
            }
          }

          results[bucket.getStartTime(TimeUnit.MILLISECONDS)] = nutrients
        }

        activity!!.runOnUiThread { result.success(results) }
      } catch (e: Exception) {
        activity!!.runOnUiThread { result.success(null) }
      }
    }
  }

  /// Handle calls from the MethodChannel
  override fun onMethodCall(call: MethodCall, result: Result) {
    when (call.method) {
      "getEnums" -> getEnums(result)
      "requestAuthorization" -> requestAuthorization(call, result)
      "getHealthData" -> getHealthData(call, result)
      "hasPermissions" -> hasPermissions(call, result)
      else -> result.notImplemented()
    }
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    binding.addActivityResultListener(this)
    activity = binding.activity
  }

  override fun onDetachedFromActivityForConfigChanges() {
    onDetachedFromActivity()
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    onAttachedToActivity(binding)
  }

  override fun onDetachedFromActivity() {
    activity = null
  }
}
