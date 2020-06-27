import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

class Nutrition {
  static const MethodChannel _channel = const MethodChannel('nutrition');
  static bool _isAndroid = Platform.isAndroid;
  static const DATA_TYPES = [
    "total_fat",
    "calcium",
    "sugar",
    "fiber",
    "iron",
    "potassium",
    "sodium",
    "vitamin_a",
    "vitamin_c",
    "protein",
    "cholesterol",
    "total_carbs",
  ];

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool> requestPermission() async {
    return await _channel.invokeMethod('requestPermission');
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
    for (String dataType in DATA_TYPES) {
      var value = await _channel.invokeMethod('getData', <String, dynamic>{
        'dataType': dataType,
        'startDate': startDateTime.millisecondsSinceEpoch,
        'endDate': endDateTime.millisecondsSinceEpoch,
      });

      nutrients.addAll({
        dataType: value,
      });
    }

    nutrients.addAll({
      'timestamp': endDateTime.millisecondsSinceEpoch.toString(),
    });

    return [nutrients];
  }
}
