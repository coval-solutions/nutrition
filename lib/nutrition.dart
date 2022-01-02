import 'dart:async';

import 'package:flutter/services.dart';

class Nutrition {
  static const MethodChannel _channel =
      MethodChannel('covalsolutions_nutrition');

  static Future<List<String>> get nutrientEnums async {
    return List<String>.from(await _channel.invokeMethod('getEnums') ?? []);
  }
}
