import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:nutrition/nutrition.dart';

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
    List<String> nutrientEnums = [];
    try {
      nutrientEnums = await Nutrition.nutrientEnums;
    } on PlatformException {
      nutrientEnums = [];
    }

    if (!mounted) return;

    setState(() {
      _nutrientEnums = nutrientEnums;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Nutrition'),
        ),
        body: Center(
          child: Text(_nutrientEnums.first),
        ),
      ),
    );
  }
}
