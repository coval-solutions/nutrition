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
    healthStore.requestAuthorization(toShare: nutrientDataTypes, read: nutrientDataTypes) { (success, error) in
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
    
    let query = HKSampleQuery(sampleType: dataType.1, predicate: predicate, limit: HKObjectQueryNoLimit, sortDescriptors: []) {
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
  
  func addData(call: FlutterMethodCall, result: @escaping FlutterResult) {
    let arguments = call.arguments as? NSDictionary
    let dataTypeKey = (arguments?["dataType"] as? String) ?? "DEFAULT"
    let value = (arguments?["value"] as? Double) ?? 0.0
    let startDate = (arguments?["startDate"] as? NSNumber) ?? 0
    let endDate = (arguments?["endDate"] as? NSNumber) ?? 0
    let dataType = dataTypeLookUp(key: dataTypeKey)
    
    // Convert dates from milliseconds to Date()
    let startDateObj = Date(timeIntervalSince1970: startDate.doubleValue / 1000)
    let endDateObj = Date(timeIntervalSince1970: endDate.doubleValue / 1000)
    
    
    let nutritent = HKQuantitySample.init(type: dataType.0,
                                          quantity: HKQuantity.init(unit: unitLookUp(key: dataTypeKey), doubleValue: value),
                                          start: startDateObj,
                                          end: endDateObj)
    
    healthStore.save(nutritent) { success, error in
      if (error != nil) {
        result(error)
      }
      
      if success {
        result(success)
      }
    }
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
  
  private func dataTypeLookUp(key: String) -> (HKQuantityType, HKSampleType) {
    switch key {
      case "total_fat":
        return (HKObjectType.quantityType(forIdentifier: .dietaryFatTotal)!, HKSampleType.quantityType(forIdentifier: .dietaryFatTotal)!)
      case "calcium":
        return (HKObjectType.quantityType(forIdentifier: .dietaryCalcium)!, HKSampleType.quantityType(forIdentifier: .dietaryCalcium)!)
      case "sugar":
        return (HKObjectType.quantityType(forIdentifier: .dietarySugar)!, HKSampleType.quantityType(forIdentifier: .dietarySugar)!)
      case "fiber":
        return (HKObjectType.quantityType(forIdentifier: .dietaryFiber)!, HKSampleType.quantityType(forIdentifier: .dietaryFiber)!)
      case "iron":
        return (HKObjectType.quantityType(forIdentifier: .dietaryIron)!, HKSampleType.quantityType(forIdentifier: .dietaryIron)!)
      case "potassium":
        return (HKObjectType.quantityType(forIdentifier: .dietaryPotassium)!, HKSampleType.quantityType(forIdentifier: .dietaryPotassium)!)
      case "sodium":
        return (HKObjectType.quantityType(forIdentifier: .dietarySodium)!, HKSampleType.quantityType(forIdentifier: .dietarySodium)!)
      case "vitamin_a":
        return (HKObjectType.quantityType(forIdentifier: .dietaryVitaminA)!, HKSampleType.quantityType(forIdentifier: .dietaryVitaminA)!)
      case "vitamin_c":
        return (HKObjectType.quantityType(forIdentifier: .dietaryVitaminC)!, HKSampleType.quantityType(forIdentifier: .dietaryVitaminC)!)
      case "protein":
        return (HKObjectType.quantityType(forIdentifier: .dietaryProtein)!, HKSampleType.quantityType(forIdentifier: .dietaryProtein)!)
      case "cholesterol":
        return (HKObjectType.quantityType(forIdentifier: .dietaryCholesterol)!, HKSampleType.quantityType(forIdentifier: .dietaryCholesterol)!)
      case "total_carbs":
        return (HKObjectType.quantityType(forIdentifier: .dietaryCarbohydrates)!, HKSampleType.quantityType(forIdentifier: .dietaryCarbohydrates)!)
      default:
        return (HKObjectType.quantityType(forIdentifier: .dietaryFatTotal)!, HKSampleType.quantityType(forIdentifier: .dietaryFatTotal)!)
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
    case "addData":
      addData(call: call, result: result)
      break;
    default:
      result("Flutter method not implemented on iOS")
    }
  }
}
