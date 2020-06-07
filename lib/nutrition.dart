import 'dart:async';

import 'package:flutter/services.dart';

class Nutrition {
  static const MethodChannel _channel = const MethodChannel('nutrition');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<String> getData(
      DateTime startDateTime, DateTime endDateTime) async {
    return await _channel.invokeMethod('getData', <String, dynamic>{
      'startDate': startDateTime.millisecondsSinceEpoch,
      'endDate': endDateTime.millisecondsSinceEpoch,
    });
  }
}
