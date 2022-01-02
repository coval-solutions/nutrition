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

//  private fun getField(type: String): Field {
//    return when (type) {
//      BODY_FAT_PERCENTAGE -> Field.FIELD_PERCENTAGE
//      HEIGHT -> Field.FIELD_HEIGHT
//      WEIGHT -> Field.FIELD_WEIGHT
//      STEPS -> Field.FIELD_STEPS
//      ACTIVE_ENERGY_BURNED -> Field.FIELD_CALORIES
//      HEART_RATE -> Field.FIELD_BPM
//      BODY_TEMPERATURE -> HealthFields.FIELD_BODY_TEMPERATURE
//      BLOOD_PRESSURE_SYSTOLIC -> HealthFields.FIELD_BLOOD_PRESSURE_SYSTOLIC
//      BLOOD_PRESSURE_DIASTOLIC -> HealthFields.FIELD_BLOOD_PRESSURE_DIASTOLIC
//      BLOOD_OXYGEN -> HealthFields.FIELD_OXYGEN_SATURATION
//      BLOOD_GLUCOSE -> HealthFields.FIELD_BLOOD_GLUCOSE_LEVEL
//      MOVE_MINUTES -> Field.FIELD_DURATION
//      DISTANCE_DELTA -> Field.FIELD_DISTANCE
//      WATER -> Field.FIELD_VOLUME
//      SLEEP_ASLEEP -> Field.FIELD_SLEEP_SEGMENT_TYPE
//      SLEEP_AWAKE -> Field.FIELD_SLEEP_SEGMENT_TYPE
//      SLEEP_IN_BED -> Field.FIELD_SLEEP_SEGMENT_TYPE
//      else -> throw IllegalArgumentException("Unsupported dataType: $type")
//    }
//  }

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

//  private fun writeData(call: MethodCall, result: Result) {
//
//    if (activity == null) {
//      result.success(false)
//      return
//    }
//
//    val type = call.argument<String>("dataTypeKey")!!
//    val startTime = call.argument<Long>("startTime")!!
//    val endTime = call.argument<Long>("endTime")!!
//    val value = call.argument<Float>( "value")!!
//
//    // Look up data type and unit for the type key
//    val dataType = keyToHealthDataType(type)
//    val field = getField(type)
//
//    val typesBuilder = FitnessOptions.builder()
//    typesBuilder.addDataType(dataType, FitnessOptions.ACCESS_WRITE)
//
//    val dataSource = DataSource.Builder()
//      .setDataType(dataType)
//      .setType(DataSource.TYPE_RAW)
//      .setDevice(Device.getLocalDevice(activity!!.applicationContext))
//      .setAppPackageName(activity!!.applicationContext)
//      .build()
//
//    val builder = if (startTime == endTime)
//      DataPoint.builder(dataSource)
//        .setTimestamp(startTime, TimeUnit.MILLISECONDS)
//    else
//      DataPoint.builder(dataSource)
//        .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
//
//    // Conversion is needed because glucose is stored as mmoll in Google Fit;
//    // while mgdl is used for glucose in this plugin.
//    val isGlucose = field == HealthFields.FIELD_BLOOD_GLUCOSE_LEVEL
//    val dataPoint = if (!isIntField(dataSource, field))
//      builder.setField(field, if (!isGlucose) value else (value/ MMOLL_2_MGDL).toFloat()).build() else
//      builder.setField(field, value.toInt()).build()
//
//    val dataSet = DataSet.builder(dataSource)
//      .add(dataPoint)
//      .build()
//
//    if (dataType == DataType.TYPE_SLEEP_SEGMENT) {
//      typesBuilder.accessSleepSessions(FitnessOptions.ACCESS_READ)
//    }
//    val fitnessOptions = typesBuilder.build()
//
//
//    try {
//      val googleSignInAccount = GoogleSignIn.getAccountForExtension(activity!!.applicationContext, fitnessOptions)
//      Fitness.getHistoryClient(activity!!.applicationContext, googleSignInAccount)
//        .insertData(dataSet)
//        .addOnSuccessListener {
//          Log.i("FLUTTER_HEALTH::SUCCESS", "DataSet added successfully!")
//          result.success(true)
//        }
//        .addOnFailureListener { e ->
//          Log.w("FLUTTER_HEALTH::ERROR", "There was an error adding the DataSet", e)
//          result.success(false)
//        }
//    } catch (e3: Exception) {
//      result.success(false)
//    }
//  }

  private fun callToHealthTypes(call: MethodCall): FitnessOptions {
    val args = call.arguments as HashMap<*, *>
    val types = (args["types"] as? ArrayList<*>)?.filterIsInstance<String>()

    val typesBuilder = FitnessOptions.builder()
    if (types != null) {
      for (typeKey in types) {
        val healthDataType: HealthDataType = HealthDataType.keyToHealthDataType(typeKey)
        typesBuilder.addDataType(healthDataType.value, FitnessOptions.ACCESS_READ)
        typesBuilder.addDataType(healthDataType.value, FitnessOptions.ACCESS_WRITE)
      }
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
        val typesBuilder = FitnessOptions.builder()
        if (types == null || types.isEmpty()) {
          throw Exception("No types have been provided")
        }

        for (typeKey in types) {
          val healthDataType: HealthDataType = HealthDataType.keyToHealthDataType(typeKey)
          typesBuilder.addDataType(healthDataType.value)

          val fitnessOptions = typesBuilder.build()
          val googleSignInAccount = GoogleSignIn.getAccountForExtension(activity!!.applicationContext, fitnessOptions)

          val response = Fitness.getHistoryClient(activity!!.applicationContext, googleSignInAccount)
            .readData(
              DataReadRequest.Builder()
              .read(healthDataType.value)
              .setTimeRange(fromDate, toDate, TimeUnit.MILLISECONDS)
              .build()
            )

          /// Fetch all data points for the specified DataType
          val dataPoints = Tasks.await<DataReadResponse>(response).getDataSet(healthDataType.value)
          val test = "test"
        }

//        /// For each data point, extract the contents and send them to Flutter, along with date and unit.
//        val healthData = dataPoints.dataPoints.mapIndexed { _, dataPoint ->
//          return@mapIndexed hashMapOf(
//            "value" to getHealthDataValue(dataPoint, field),
//            "date_from" to dataPoint.getStartTime(TimeUnit.MILLISECONDS),
//            "date_to" to dataPoint.getEndTime(TimeUnit.MILLISECONDS),
//            "source_name" to (dataPoint.originalDataSource.appPackageName ?: (dataPoint.originalDataSource.device?.model ?: "" )),
//            "source_id" to dataPoint.originalDataSource.streamIdentifier
//          )
//        }

        activity!!.runOnUiThread { result.success({}) }
      } catch (e: Exception) {
        activity!!.runOnUiThread { result.success(null) }
      }
    }
  }

//  fun getTotalStepsInInterval(call: MethodCall, result: Result) {
//    val start = call.argument<Long>("startDate")!!
//    val end = call.argument<Long>("endDate")!!
//
//    getStepsInRange(start, end) { map: Map<Long, Int>?, e: Throwable? ->
//      if (map != null) {
//        assert(map.size <= 1) { "getTotalStepsInInterval should return only one interval. Found: ${map.size}" }
//        result.success(map.values.firstOrNull())
//      } else {
//        result.error("failed", e?.message, null)
//      }
//    }
//  }

//  private fun getStepsInRange(
//    start: Long,
//    end: Long,
//    result: (Map<Long, Int>?, Throwable?) -> Unit
//  ) {
//    val activity = activity ?: return
//
//    val stepsDataType = keyToHealthDataType(STEPS)
//    val aggregatedDataType = keyToHealthDataType(AGGREGATE_STEP_COUNT)
//
//    val fitnessOptions = FitnessOptions.builder()
//      .addDataType(stepsDataType)
//      .addDataType(aggregatedDataType)
//      .build()
//    val gsa = GoogleSignIn.getAccountForExtension(activity, fitnessOptions)
//
//    val ds = DataSource.Builder()
//      .setAppPackageName("com.google.android.gms")
//      .setDataType(stepsDataType)
//      .setType(DataSource.TYPE_DERIVED)
//      .setStreamName("estimated_steps")
//      .build()
//
//    val duration = (end - start).toInt()
//
//    val request = DataReadRequest.Builder()
//      .aggregate(ds)
//      .bucketByTime(duration, TimeUnit.MILLISECONDS)
//      .setTimeRange(start, end, TimeUnit.MILLISECONDS)
//      .build()
//
//    val response = Fitness.getHistoryClient(activity, gsa).readData(request)
//
//    Thread {
//      try {
//        val readDataResult = Tasks.await(response)
//
//        val map = HashMap<Long, Int>() // need to return to Dart so can't use sparse array
//        for (bucket in readDataResult.buckets) {
//          val dp = bucket.dataSets.firstOrNull()?.dataPoints?.firstOrNull()
//          if (dp != null) {
//            print(dp)
//
//            val count = dp.getValue(aggregatedDataType.fields[0])
//
//            val startTime = dp.getStartTime(TimeUnit.MILLISECONDS)
//            val startDate = Date(startTime)
//            val endDate = Date(dp.getEndTime(TimeUnit.MILLISECONDS))
//            Log.i("FLUTTER_HEALTH::SUCCESS", "returning $count steps for $startDate - $endDate")
//            map[startTime] = count.asInt()
//          } else {
//            val startDay = Date(start)
//            val endDay = Date(end)
//            Log.i("FLUTTER_HEALTH::ERROR", "no steps for $startDay - $endDay")
//          }
//        }
//        activity.runOnUiThread {
//          result(map, null)
//        }
//      } catch (e: Throwable) {
//        Log.e("FLUTTER_HEALTH::ERROR", "failed: ${e.message}")
//
//        activity.runOnUiThread {
//          result(null, e)
//        }
//      }
//
//    }.start()
//  }

  /// Handle calls from the MethodChannel
  override fun onMethodCall(call: MethodCall, result: Result) {
    when (call.method) {
      "getEnums" -> getEnums(result)
      "requestAuthorization" -> requestAuthorization(call, result)
      "getHealthData" -> getHealthData(call, result)
//      "getData" -> getData(call, result)
//      "writeData" -> writeData(call, result)
//      "getTotalStepsInInterval" -> getTotalStepsInInterval(call, result)
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

//import androidx.annotation.NonNull
//
//import io.flutter.embedding.engine.plugins.FlutterPlugin
//import io.flutter.plugin.common.MethodCall
//import io.flutter.plugin.common.MethodChannel
//import io.flutter.plugin.common.MethodChannel.MethodCallHandler
//import io.flutter.plugin.common.MethodChannel.Result
//
///** NutritionPlugin */
//class NutritionPlugin: FlutterPlugin, MethodCallHandler {
//  /// The MethodChannel that will the communication between Flutter and native Android
//  ///
//  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
//  /// when the Flutter Engine is detached from the Activity
//  private lateinit var channel : MethodChannel
//
//  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
//    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "nutrition")
//    channel.setMethodCallHandler(this)
//  }
//
//  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
//    if (call.method == "getPlatformVersion") {
//      result.success("Android ${android.os.Build.VERSION.RELEASE}")
//    } else {
//      result.notImplemented()
//    }
//  }
//
//  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
//    channel.setMethodCallHandler(null)
//  }
//}
