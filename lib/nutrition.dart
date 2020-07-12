import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';
import 'package:nutrition/nutrition_enum.dart';

class Nutrition {
  static const MethodChannel _channel = const MethodChannel('nutrition');
  static final bool _isAndroid = Platform.isAndroid;
  static final Map<NutritionEnum, String> dataTypes = const {
    NutritionEnum.FAT: 'total_fat',
    NutritionEnum.CALCIUM: 'calcium',
    NutritionEnum.SUGAR: 'sugar',
    NutritionEnum.FIBRE: 'fiber',
    NutritionEnum.IRON: 'iron',
    NutritionEnum.POTASSIUM: 'potassium',
    NutritionEnum.SODIUM: 'sodium',
    NutritionEnum.VITAMIN_A: 'vitamin_a',
    NutritionEnum.VITAMIN_C: 'vitamin_c',
    NutritionEnum.PROTEIN: 'protein',
    NutritionEnum.CHOLESTEROL: 'cholesterol',
    NutritionEnum.CARBOHYDRATES: 'total_carbs',
  };

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool> requestPermission() async {
    return await _channel.invokeMethod('requestPermission');
  }

  static void addData(Map<NutritionEnum, double> nutrients,
      DateTime startDateTime, DateTime endDateTime) async {
    Map<String, double> nutrientsToSend = {};
    nutrients.forEach((key, value) {
      if (value > 0) {
        nutrientsToSend.addAll({dataTypes[key]: value});
      }
    });

    if (_isAndroid) {
      var test = await _channel.invokeMethod('addData', <String, dynamic>{
        'nutrients': nutrientsToSend,
        'startDate': startDateTime.millisecondsSinceEpoch,
      });

      return;
    }

    nutrientsToSend.forEach((key, value) async {
      await _channel.invokeMethod('addData', <String, dynamic>{
        'dataType': key,
        'value': value,
        'startDate': startDateTime.millisecondsSinceEpoch,
        'endDate': endDateTime.millisecondsSinceEpoch,
      });
    });

    return;
  }

  static Future<List> getData(
      DateTime startDateTime, DateTime endDateTime) async {
    if (_isAndroid) {
      return await _channel.invokeMethod('getData', <String, dynamic>{
        'startDate': startDateTime.millisecondsSinceEpoch,
        'endDate': endDateTime.millisecondsSinceEpoch,
      });
    }

    Map<String, String> nutrients = {};
    for (String dataType in dataTypes.values.toList()) {
      var nutrientValue =
          await _channel.invokeMethod('getData', <String, dynamic>{
        'dataType': dataType,
        'startDate': startDateTime.millisecondsSinceEpoch,
        'endDate': endDateTime.millisecondsSinceEpoch,
      });

      nutrients.addAll({
        dataType: nutrientValue,
      });
    }

    nutrients.addAll({
      'timestamp': endDateTime.millisecondsSinceEpoch.toString(),
    });

    return [nutrients];
  }
}
