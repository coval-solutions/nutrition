import 'dart:async';

import 'package:flutter/services.dart';
import 'package:nutrition/health_data_types.dart';

class Nutrition {
  static const MethodChannel _channel =
      MethodChannel('covalsolutions_nutrition');

  static Future<List<String>> get nutrientEnums async {
    return List<String>.from(await _channel.invokeMethod('getEnums') ?? []);
  }

  static Future<bool> requestAuthorization(List<HealthDataTypes> types) async {
    return await _channel.invokeMethod(
        'requestAuthorization', {'types': types.map((e) => e.name).toList()});
  }

  static Future<dynamic> getHealthData(
      List<HealthDataTypes> types, DateTime fromDate, DateTime toDate) async {
    return await _channel.invokeMethod('getHealthData', {
      'types': types.map((e) => e.name).toList(),
      'fromDate': fromDate.millisecondsSinceEpoch,
      'toDate': toDate.millisecondsSinceEpoch
    });
  }
}
