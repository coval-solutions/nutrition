package com.covalsolutions.nutrition

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.NonNull
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.tasks.Tasks
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

const val CHANNEL = "nutrition"
const val COVAL_NUTRITION = "COVAL_NUTRITION"
const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1234
const val FIELD_TIMESTAMP = "timestamp"
val DATA_TYPE: DataType = DataType.TYPE_NUTRITION

/** NutritionPlugin */
class NutritionPlugin : FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener {
  private var latestCall: MethodCall? = null
  private var latestResult: Result? = null

  private var registrar: Registrar? = null
  private var channel: MethodChannel? = null

  // Only set activity for v2 embedder. Always access activity from getActivity() method.
  private var activity: Activity? = null

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPluginBinding) {
    initInstance(flutterPluginBinding.binaryMessenger, flutterPluginBinding.applicationContext)
  }

  override fun onDetachedFromEngine(binding: FlutterPluginBinding) {
    channel!!.setMethodCallHandler(null)
    channel = null
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
      val instance = NutritionPlugin()
      instance.registrar = registrar
      instance.initInstance(registrar.messenger(), registrar.context())
    }
  }

  private fun initInstance(messenger: BinaryMessenger, context: Context) {
    channel = MethodChannel(messenger, CHANNEL)
    channel!!.setMethodCallHandler(this)
  }

  // Only access activity with this method.
  private fun getActivity(): Activity? {
    return if (registrar != null) registrar!!.activity() else activity
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Boolean {
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
        Log.d(COVAL_NUTRITION, "Access Granted!")
        latestResult?.success(true)
      } else {
        Log.d(COVAL_NUTRITION, "Access Denied!")
        latestResult?.success(false)
      }
    }

    return false
  }

  private val fitnessOptions = FitnessOptions.builder()
      .addDataType(DATA_TYPE, FitnessOptions.ACCESS_READ)
      .addDataType(DATA_TYPE, FitnessOptions.ACCESS_WRITE)
      .build()

  private fun getData(call: MethodCall, result: Result) {
    var dataPoints: DataSet?
    var nutritionData: List<HashMap<String, String>>
    val googleSignInAccount: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(getActivity()?.applicationContext)
    if (googleSignInAccount === null) {
      result.error(COVAL_NUTRITION + "_NOT_LOGGED_IN_ERROR", "You don't seem to be logged in via Google.", "googleSignInAccount is null.")
    }

    thread {
      val startTime = call.argument<Long>("startDate")!!
      val endTime = call.argument<Long>("endDate")!!
      val response = Fitness.getHistoryClient(getActivity()?.applicationContext!!, googleSignInAccount!!)
          .readData(DataReadRequest.Builder()
              .read(DATA_TYPE)
              .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
              .enableServerQueries()
              .build())

      dataPoints = Tasks.await(response).getDataSet(DATA_TYPE)
      if (dataPoints !== null && !dataPoints!!.isEmpty) {
        nutritionData = dataPoints!!.dataPoints.mapIndexed { _, dataPoint ->
          val nutrients = dataPoint.getValue(Field.FIELD_NUTRIENTS)
          return@mapIndexed hashMapOf(
              FIELD_TIMESTAMP to dataPoint.getEndTime(TimeUnit.MILLISECONDS).toString(),
              getFieldToReturn(Field.NUTRIENT_TOTAL_FAT) to (nutrients?.getKeyValue(Field.NUTRIENT_TOTAL_FAT)?.toString() ?: "0"),
              getFieldToReturn(Field.NUTRIENT_CALCIUM) to (nutrients?.getKeyValue(Field.NUTRIENT_CALCIUM)?.toString() ?: "0"),
              getFieldToReturn(Field.NUTRIENT_SUGAR) to (nutrients?.getKeyValue(Field.NUTRIENT_SUGAR)?.toString() ?: "0"),
              getFieldToReturn(Field.NUTRIENT_DIETARY_FIBER) to (nutrients?.getKeyValue(Field.NUTRIENT_DIETARY_FIBER)?.toString() ?: "0"),
              getFieldToReturn(Field.NUTRIENT_IRON) to (nutrients?.getKeyValue(Field.NUTRIENT_IRON)?.toString() ?: "0"),
              getFieldToReturn(Field.NUTRIENT_POTASSIUM) to (nutrients?.getKeyValue(Field.NUTRIENT_POTASSIUM)?.toString() ?: "0"),
              getFieldToReturn(Field.NUTRIENT_SODIUM) to (nutrients?.getKeyValue(Field.NUTRIENT_SODIUM)?.toString() ?: "0"),
              getFieldToReturn(Field.NUTRIENT_VITAMIN_A) to (nutrients?.getKeyValue(Field.NUTRIENT_VITAMIN_A)?.toString() ?: "0"),
              getFieldToReturn(Field.NUTRIENT_VITAMIN_C) to (nutrients?.getKeyValue(Field.NUTRIENT_VITAMIN_C)?.toString() ?: "0"),
              getFieldToReturn(Field.NUTRIENT_PROTEIN) to (nutrients?.getKeyValue(Field.NUTRIENT_PROTEIN)?.toString() ?: "0"),
              getFieldToReturn(Field.NUTRIENT_CHOLESTEROL) to (nutrients?.getKeyValue(Field.NUTRIENT_CHOLESTEROL)?.toString() ?: "0"),
              getFieldToReturn(Field.NUTRIENT_TOTAL_CARBS) to (nutrients?.getKeyValue(Field.NUTRIENT_TOTAL_CARBS)?.toString() ?: "0")
          )
        }

        getActivity()!!.runOnUiThread { result.success(nutritionData) }
      } else {
        nutritionData = listOf(hashMapOf(
            getFieldToReturn(Field.NUTRIENT_TOTAL_FAT) to "0",
            getFieldToReturn(Field.NUTRIENT_CALCIUM) to "0",
            getFieldToReturn(Field.NUTRIENT_SUGAR) to "0",
            getFieldToReturn(Field.NUTRIENT_DIETARY_FIBER) to "0",
            getFieldToReturn(Field.NUTRIENT_IRON) to "0",
            getFieldToReturn(Field.NUTRIENT_POTASSIUM) to "0",
            getFieldToReturn(Field.NUTRIENT_SODIUM) to "0",
            getFieldToReturn(Field.NUTRIENT_VITAMIN_A) to "0",
            getFieldToReturn(Field.NUTRIENT_VITAMIN_C) to "0",
            getFieldToReturn(Field.NUTRIENT_PROTEIN) to "0",
            getFieldToReturn(Field.NUTRIENT_CHOLESTEROL) to "0",
            getFieldToReturn(Field.NUTRIENT_TOTAL_CARBS) to "0"
        ))

        getActivity()!!.runOnUiThread { result.success(nutritionData) }

      }
    }
  }

  private fun addData(call: MethodCall, result: Result) {
    val nutritionData: HashMap<String, Float> = call.argument<HashMap<String, Float>>("nutrients")!!
    val googleSignInAccount: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(getActivity()?.applicationContext)
    if (googleSignInAccount === null) {
      result.error(COVAL_NUTRITION + "_NOT_LOGGED_IN_ERROR", "You don't seem to be logged in via Google.", "googleSignInAccount is null.")
    }

    val nutritionSource: DataSource = DataSource.Builder()
        .setAppPackageName(getActivity()?.applicationContext!!.packageName)
        .setDataType(DataType.TYPE_NUTRITION)
        .setType(DataSource.TYPE_RAW)
        .build()

    val nutrients: HashMap<String, Float> = HashMap()
    nutritionData.forEach { (key, value) -> nutrients[getFieldToReturnReverse(key)] = value }

    thread {
      val startTime = call.argument<Long>("date")!!
      val dataPoint: DataPoint = DataPoint.builder(nutritionSource)
          .setTimestamp(startTime, TimeUnit.MILLISECONDS)
          .setField(Field.FIELD_MEAL_TYPE, Field.MEAL_TYPE_UNKNOWN)
          .setField(Field.FIELD_NUTRIENTS, nutrients)
          .build()

      val dataSet = DataSet.create(nutritionSource)
      dataSet.add(dataPoint)
      val dataSetResult = Fitness.getHistoryClient(getActivity()?.applicationContext!!, googleSignInAccount!!)
          .insertData(dataSet)

      getActivity()!!.runOnUiThread { result.success(dataSetResult.isSuccessful) }
    }
  }

  private fun requestPermission(call: MethodCall, result: Result) {
    latestResult = result
    val googleSignInAccount: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(getActivity()!!)
    if (googleSignInAccount === null) {
      Log.e(COVAL_NUTRITION, "Unable to retrieve the last signed in account.")
      latestResult?.error(COVAL_NUTRITION + "_NOT_LOGGED_IN_ERROR", "Cannot retrieve the last signed in account.", "googleSignInAccount is null.")
    }

    if (!GoogleSignIn.hasPermissions(googleSignInAccount, fitnessOptions)) {
      GoogleSignIn.requestPermissions(
          getActivity()!!,
          GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
          GoogleSignIn.getLastSignedInAccount(getActivity()?.applicationContext),
          fitnessOptions)
    } else {
      latestResult?.success(true)
      Log.d(COVAL_NUTRITION, "Permission was already granted.")
    }
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    latestCall = call
    latestResult = result
    when (call.method) {
      "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
      "requestPermission" -> requestPermission(call, result)
      "getData" -> getData(call, result)
      "addData" -> addData(call, result)
      else -> result.notImplemented()
    }
  }

  override fun onAttachedToActivity(activityPluginBinding: ActivityPluginBinding) {
    activity = activityPluginBinding.activity
  }

  override fun onDetachedFromActivityForConfigChanges() {
    activity = null
  }

  override fun onReattachedToActivityForConfigChanges(activityPluginBinding: ActivityPluginBinding) {
    activity = activityPluginBinding.activity
  }

  override fun onDetachedFromActivity() {
    activity = null
  }

  private fun getFieldToReturn(field: String): String {
    return when (field) {
      Field.NUTRIENT_TOTAL_FAT -> "total_fat"
      Field.NUTRIENT_CALCIUM -> "calcium"
      Field.NUTRIENT_SUGAR -> "sugar"
      Field.NUTRIENT_DIETARY_FIBER -> "fiber"
      Field.NUTRIENT_IRON -> "iron"
      Field.NUTRIENT_POTASSIUM -> "potassium"
      Field.NUTRIENT_SODIUM -> "sodium"
      Field.NUTRIENT_VITAMIN_A -> "vitamin_a"
      Field.NUTRIENT_VITAMIN_C -> "vitamin_c"
      Field.NUTRIENT_PROTEIN -> "protein"
      Field.NUTRIENT_CHOLESTEROL -> "cholesterol"
      Field.NUTRIENT_TOTAL_CARBS -> "total_carbs"
      else -> field
    }
  }

  private fun getFieldToReturnReverse(nutrient: String): String {
    return when (nutrient) {
      "total_fat" -> Field.NUTRIENT_TOTAL_FAT
      "calcium" -> Field.NUTRIENT_CALCIUM
      "sugar" -> Field.NUTRIENT_SUGAR
      "fiber" -> Field.NUTRIENT_DIETARY_FIBER
      "iron" -> Field.NUTRIENT_IRON
      "potassium" -> Field.NUTRIENT_POTASSIUM
      "sodium" -> Field.NUTRIENT_SODIUM
      "vitamin_a" -> Field.NUTRIENT_VITAMIN_A
      "vitamin_c" -> Field.NUTRIENT_VITAMIN_C
      "protein" -> Field.NUTRIENT_PROTEIN
      "cholesterol" -> Field.NUTRIENT_CHOLESTEROL
      "total_carbs" -> Field.NUTRIENT_TOTAL_CARBS
      else -> nutrient
    }
  }
}
