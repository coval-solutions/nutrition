import 'dart:async';

import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:google_sign_in/google_sign_in.dart';
import 'package:nutrition/nutrition.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  final GoogleSignIn _googleSignIn = GoogleSignIn();
  final FirebaseAuth _auth = FirebaseAuth.instance;

  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  String _result;

  Future<FirebaseUser> _handleSignIn() async {
    final GoogleSignInAccount googleUser = await widget._googleSignIn.signIn();
    final GoogleSignInAuthentication googleAuth =
        await googleUser.authentication;

    final AuthCredential credential = GoogleAuthProvider.getCredential(
      accessToken: googleAuth.accessToken,
      idToken: googleAuth.idToken,
    );

    final FirebaseUser user =
        (await widget._auth.signInWithCredential(credential)).user;
    print("Signed in " + user.displayName);
    return user;
  }

  @override
  void initState() {
    super.initState();
    initPlatformState();
    _handleSignIn();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await Nutrition.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    DateTime today = DateTime.now();
    DateTime oneWeekAgo = DateTime.now().subtract(Duration(days: 7));
    return MaterialApp(
      home: Scaffold(
        floatingActionButton: FloatingActionButton(
          child: Icon(Icons.sync),
          backgroundColor: Colors.black,
          onPressed: () {
            print('Fab pressed.');
            Nutrition.getData(oneWeekAgo, today).then((result) {
              setState(() {
                print(result);
                _result = result;
              });
            });
          },
        ),
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: <Widget>[
            Text('Running on: $_platformVersion\n'),
            Text(_result ?? ''),
          ],
        ),
      ),
    );
  }
}
