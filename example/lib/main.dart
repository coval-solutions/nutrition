import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:nutrition/nutrition.dart';
import 'package:nutrition/health_data_types.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  List<String> _nutrientEnums = [];

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    bool requested =
        await Nutrition.requestAuthorization(HealthDataTypes.values);

    if (requested) {
      final now = DateTime.now();
      final fromDate = now.subtract(const Duration(days: 6));

      List<String> nutrientEnums = [];
      try {
        // nutrientEnums = await Nutrition.nutrientEnums;
        var test = await Nutrition.getHealthData(
            [HealthDataTypes.aggregateNutritionSummary], fromDate, now);
      } on PlatformException {
        nutrientEnums = [];
      }

      setState(() {
        _nutrientEnums = nutrientEnums;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Nutrition'),
        ),
        body: Center(
          child: Text('Test'),
        ),
      ),
    );
  }
}
