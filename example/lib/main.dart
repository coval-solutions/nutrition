import 'dart:async';

import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
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
  bool hasPermission = false;
  DateTime startDate;
  DateTime endDate;

  Future<FirebaseUser> _handleSignIn() async {
    final bool isSignedIn = await widget._googleSignIn.isSignedIn();
    if (isSignedIn) {
      return await widget._auth.currentUser();
    }

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
    _handleSignIn().then((value) => {
          if (!hasPermission) {requestPermission()}
        });

    endDate = DateTime.now();
    startDate = DateTime.now().subtract(Duration(days: 7));
  }

  void requestPermission() async {
    await Nutrition.requestPermission().then((value) => setState(() {
          hasPermission = value;
        }));
  }

  @override
  Widget build(BuildContext context) {
    if (!hasPermission) {
      return MaterialApp(
        home: Scaffold(
          appBar: AppBar(
            title: const Text('Nutrition Example App'),
          ),
          body: Center(
            child: CircularProgressIndicator(),
          ),
        ),
      );
    }

    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Nutrition Example App'),
        ),
        body: SingleChildScrollView(
          physics: ScrollPhysics(),
          child: Column(
            children: <Widget>[
              FutureBuilder<List>(
                  future: Nutrition.getData(startDate, endDate),
                  builder:
                      (BuildContext context, AsyncSnapshot<List> snapshot) {
                    if (snapshot.hasData) {
                      return ListView.builder(
                          shrinkWrap: true,
                          physics: NeverScrollableScrollPhysics(),
                          itemCount: snapshot.data.length ?? 0,
                          itemBuilder: (BuildContext context, int index) {
                            Map<String, String> data =
                                Map<String, String>.from(snapshot.data[index]);
                            return ListView.builder(
                                shrinkWrap: true,
                                physics: NeverScrollableScrollPhysics(),
                                itemCount: data.entries.length,
                                itemBuilder: (BuildContext context, int index) {
                                  var entry = data.entries.elementAt(index);
                                  return ListTile(
                                    trailing: Text(entry.value),
                                    title: Text(entry.key),
                                  );
                                });
                          });
                    }

                    if (snapshot.hasError) {
                      Center(child: Text(snapshot.error));
                    }

                    return Center(child: CircularProgressIndicator());
                  }),
            ],
          ),
        ),
      ),
    );
  }
}
