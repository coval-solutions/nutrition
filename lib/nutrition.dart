import 'dart:async';

import 'package:flutter/services.dart';

class Nutrition {
  static const MethodChannel _channel = const MethodChannel('nutrition');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<List> getData(
      DateTime startDateTime, DateTime endDateTime) async {
    final bool requestPermission =
        await _channel.invokeMethod('requestPermission');

    if (requestPermission) {
      var test = await _channel.invokeMethod('getData', <String, dynamic>{
        'startDate': startDateTime.millisecondsSinceEpoch,
        'endDate': endDateTime.millisecondsSinceEpoch,
      });

      return [test];
    }

    return [];
  }
}
