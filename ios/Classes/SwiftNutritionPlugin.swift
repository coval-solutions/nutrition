import Flutter
import UIKit
import HealthKit

var healthStore: HKHealthStore = HKHealthStore()

public class SwiftNutritionPlugin: NSObject, FlutterPlugin {
  let nutrientDataTypes = Set([
    HKObjectType.quantityType(forIdentifier: .dietaryFatTotal)!,
    HKObjectType.quantityType(forIdentifier: .dietaryCalcium)!,
    HKObjectType.quantityType(forIdentifier: .dietarySugar)!,
    HKObjectType.quantityType(forIdentifier: .dietaryFiber)!,
    HKObjectType.quantityType(forIdentifier: .dietaryIron)!,
    HKObjectType.quantityType(forIdentifier: .dietaryPotassium)!,
    HKObjectType.quantityType(forIdentifier: .dietarySodium)!,
    HKObjectType.quantityType(forIdentifier: .dietaryVitaminA)!,
    HKObjectType.quantityType(forIdentifier: .dietaryVitaminC)!,
    HKObjectType.quantityType(forIdentifier: .dietaryProtein)!,
    HKObjectType.quantityType(forIdentifier: .dietaryCholesterol)!,
    HKObjectType.quantityType(forIdentifier: .dietaryCarbohydrates)!,
  ])
  
  
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "nutrition", binaryMessenger: registrar.messenger())
    let instance = SwiftNutritionPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }
  
  func requestPermission(call: FlutterMethodCall, result: @escaping FlutterResult) {
    healthStore.requestAuthorization(toShare: nil, read: nutrientDataTypes) { (success, error) in
      if success {
        result(success)
      } else {
        result(false)
      }
    }
  }
  
  func getData(call: FlutterMethodCall, result: @escaping FlutterResult) {
    let arguments = call.arguments as? NSDictionary
    let startDate = (arguments?["startDate"] as? NSNumber) ?? 0
    let endDate = (arguments?["endDate"] as? NSNumber) ?? 0
    let dataTypeKey = (arguments?["dataType"] as? String) ?? "DEFAULT"
    
    // Convert dates from milliseconds to Date()
    let dateFrom = Date(timeIntervalSince1970: startDate.doubleValue / 1000)
    let dateTo = Date(timeIntervalSince1970: endDate.doubleValue / 1000)
    
    let dataType = dataTypeLookUp(key: dataTypeKey)
    let predicate = HKQuery.predicateForSamples(withStart: dateFrom, end: dateTo, options: .strictStartDate)
    
    let query = HKSampleQuery(sampleType: dataType, predicate: predicate, limit: HKObjectQueryNoLimit, sortDescriptors: []) {
      x, samplesOrNil, error in
      
      guard let samples = samplesOrNil as? [HKQuantitySample] else {
        result(FlutterError(code: "CovalNutrition", message: "Results are null", details: error))
        return
      }
      
      var value = 0.0
      samples.forEach { sample in
        value += sample.quantity.doubleValue(for: self.unitLookUp(key: dataTypeKey))
      }
      
      result(String(value))
      
      return
    }
    
    HKHealthStore().execute(query)
  }
  
  private func unitLookUp(key: String) -> HKUnit {
    switch key {
    case "total_fat", "sugar", "fiber", "protein", "total_carbs":
      return HKUnit.gram()
    case "vitamin_a":
      return HKUnit.gramUnit(with: HKMetricPrefix.micro)
    default:
      return HKUnit.gramUnit(with: HKMetricPrefix.milli)
    }
  }
  
  private func dataTypeLookUp(key: String) -> HKSampleType {
    switch key {
    case "total_fat":
      return HKSampleType.quantityType(forIdentifier: .dietaryFatTotal)!
    case "calcium":
      return HKSampleType.quantityType(forIdentifier: .dietaryCalcium)!
    case "sugar":
      return HKSampleType.quantityType(forIdentifier: .dietarySugar)!
    case "fiber":
      return HKSampleType.quantityType(forIdentifier: .dietaryFiber)!
    case "iron":
      return HKSampleType.quantityType(forIdentifier: .dietaryIron)!
    case "potassium":
      return HKSampleType.quantityType(forIdentifier: .dietaryPotassium)!
    case "sodium":
      return HKSampleType.quantityType(forIdentifier: .dietarySodium)!
    case "vitamin_a":
      return HKSampleType.quantityType(forIdentifier: .dietaryVitaminA)!
    case "vitamin_c":
      return HKSampleType.quantityType(forIdentifier: .dietaryVitaminC)!
    case "protein":
      return HKSampleType.quantityType(forIdentifier: .dietaryProtein)!
    case "cholesterol":
      return HKSampleType.quantityType(forIdentifier: .dietaryCholesterol)!
    case "total_carbs":
      return HKSampleType.quantityType(forIdentifier: .dietaryCarbohydrates)!
    default:
      return HKSampleType.quantityType(forIdentifier: .dietaryFatTotal)!
    }
  }
  
  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
    case "getPlatformVersion":
      result("iOS " + UIDevice.current.systemVersion)
      break;
    case "requestPermission":
      requestPermission(call: call, result: result)
      break;
    case "getData":
      getData(call: call, result: result)
      break;
    default:
      result("Flutter method not implemented on iOS")
    }
  }
}
