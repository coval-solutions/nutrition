import 'package:flutter/material.dart';
import 'dart:async';

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
  bool _hasPermission = false;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    bool hasPermission =
        await Nutrition.requestAuthorization(HealthDataTypes.values);
    setState(() {
      _hasPermission = hasPermission;
    });
  }

  @override
  Widget build(BuildContext context) {
    final now = DateTime.now();
    final lastMidnight = DateTime(now.year, now.month, now.day);
    final fromDate = lastMidnight.subtract(const Duration(days: 6));
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Nutrition'),
        ),
        body: _hasPermission
            ? FutureBuilder(
                future: Nutrition.getHealthData(
                    HealthDataTypes.values, fromDate, now),
                builder: (context, snapshot) {
                  switch (snapshot.connectionState) {
                    case ConnectionState.waiting:
                      return const Center(child: CircularProgressIndicator());
                    default:
                      if (snapshot.hasError) {
                        return Center(child: Text('Error: ${snapshot.error}'));
                      } else {
                        return Center(child: Text('Result: ${snapshot.data}'));
                      }
                  }
                },
              )
            : const Center(
                child: CircularProgressIndicator(),
              ),
      ),
    );
  }
}
