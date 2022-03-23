// ignore_for_file: avoid_print

import 'dart:async';
import 'package:flutter/services.dart';
import 'package:stripe_terminal/models/configurations/bluetooth_connection_configuration.dart';
import 'package:stripe_terminal/models/configurations/bluetooth_discovery_configuration.dart';
import 'package:stripe_terminal/models/configurations/connection_token_provider_configuration.dart';
import 'package:stripe_terminal/models/configurations/simulate_reader_update.dart';
import 'package:stripe_terminal/models/stripe_reader.dart';

class StripeTerminal {
  static Stream get didUpdateDiscoveredReader {
    return _discoverReaderStreamController.stream;
  }

  static Stream<bool> get isUpdateRequired {
    return _isUpdateRequiredSteamController.stream;
  }

  static Stream<double> get updateProgress {
    return _updateProgressStreamController.stream;
  }

  static Stream<bool> get hasFinishedIntallingUpdate {
    return _hasFinishedIntallingUpdateStreamController.stream;
  }

  static final StreamController _discoverReaderStreamController =
      StreamController.broadcast();

  static final StreamController<bool>
      _hasFinishedIntallingUpdateStreamController =
      StreamController<bool>.broadcast();

  static final StreamController<bool> _isUpdateRequiredSteamController =
      StreamController<bool>.broadcast();

  static final StreamController<double> _updateProgressStreamController =
      StreamController.broadcast();

  static const MethodChannel _methodChannel = MethodChannel('stripe_terminal');

  static void setup() async {
    _methodChannel.setMethodCallHandler(methodCallHandler);

    await cancelCurrentTask();
    await disconnectBluetoothReader();
  }

  static Future<void> dispose() async {
    await cancelCurrentTask();
    await disconnectBluetoothReader();

    return;
  }

  static Future<void> clearCachedCredentials() async {
    await _methodChannel.invokeMethod(
      "clearCachedCredentials",
    );
  }

  // static void sendLastDiscoveredReadersIfNeeded() {
  //   if (_lastDiscoveredReaders.isNotEmpty) {
  //     _discoverReaderStreamController.add(_lastDiscoveredReaders);
  //   }
  // }

  static Future<void> setupConnectionTokenProvider(
      {required ConnectionTokenProviderConfiguration config}) async {
    try {
      // Make sure location permissions are granted otherwise it will throw an exception
      await _methodChannel.invokeMethod(
        "setupConnectionTokenProvider",
        {
          'backendBaseUrl': config.backendBaseUrl,
          'requestUrl': config.requestUrl,
          'tokenKeyInJson': config.tokenKeyInJson,
          'userAutherizationToken': config.userAutherizationToken,
        },
      );
    } catch (e) {
      print(e);
    }
    print('');
  }

  static Future<void> discoverReaders({
    required BluetoothDiscoveryConfiguration config,
  }) async {
    await _methodChannel.invokeMethod(
      "discoverReaders",
      {
        'simulated': config.simulated,
      },
    );
  }

  static Future<void> connectBluetoothReader({
    required StripeReader reader,
    required BluetoothConnectionConfiguration config,
  }) async {
    print(
        'config.simulateReaderUpdate.toString(): ${config.simulateReaderUpdate.name}');
    await _methodChannel.invokeMethod(
      "connectBluetoothReader",
      {
        'selectedReaderSerialNumber': reader.serialNumber,
        'locationId': config.locationId,
        'simulateReaderUpdate': config.simulateReaderUpdate.name,
      },
    );
  }

  static Future<void> disconnectBluetoothReader() async {
    try {
      await _methodChannel.invokeMethod(
        "disconnectBluetoothReader",
      );

      return;
    } catch (e) {
      print('No device is connected so cannot disconnect');
      print(e);
    }
  }

  static Future<void> charge({required String paymentIntent}) async {
    await _methodChannel.invokeMethod(
      'charge',
      {
        'paymentIntent': paymentIntent,
      },
    );
  }

  static Future<void> cancelCurrentTask() async {
    try {
      await _methodChannel.invokeMethod(
        'cancelCurrentTask',
      );

      return;
    } catch (e) {
      print(e);

      return;
    }
  }

  static Future<void> cancelPaymentIntent() async {
    await _methodChannel.invokeMethod(
      'cancelPaymentIntent',
    );
    return;
  }

  static Future<void> updateReader() async {
    await _methodChannel.invokeMethod(
      'updateReader',
    );
    return;
  }

  //MARK: - Handler
  static Future<void> methodCallHandler(MethodCall methodCall) async {
    switch (methodCall.method) {
      case 'didUpdateDiscoveredReaders':
        _didUpdateDiscoveredReaders(methodCall.arguments);
        break;
      case 'isUpdateRequired':
        updateRequired(methodCall.arguments);
        break;
      case 'updateProgress':
        didProgressUpdate(methodCall.arguments);
        break;
      case 'onFinishInstallingUpdate':
        didFinishInstallingUpdate(methodCall.arguments);
        break;
      default:
        throw PlatformException(
          code: 'notimpl',
          message: 'not implemented',
        );
    }
  }

  static void _didUpdateDiscoveredReaders(readersListJson) {
    print('Here inside _didUpdateDiscoveredReaders ${readersListJson}');
    final List<StripeReader> readers = [];

    for (var currReaderJson in readersListJson) {
      print('Inside loop $currReaderJson');
      print('Inside loop ${currReaderJson.runtimeType}');
      print('serialNumber${currReaderJson["serialNumber"]}');

      var currReader =
          StripeReader.fromJson(Map<String, dynamic>.from(currReaderJson));
      print('currReader $currReader');

      readers.add(currReader);
    }
    print('StripeReaders Size ${readers.length}');
    _discoverReaderStreamController.add(readers);
  }

  static void updateRequired(bool arguments) {
    print('updateRequired arguments $arguments');
    print('updateRequired arguments type ${arguments.runtimeType}');
  }

  static void didProgressUpdate(double arguments) {
    print('didProgressUpdate arguments $arguments');
    print('didProgressUpdate arguments type ${arguments.runtimeType}');

    _updateProgressStreamController
        .add(double.parse(arguments.toStringAsFixed(2)));
  }

  static void didFinishInstallingUpdate(bool arguments) {
    print('didProgressUpdate arguments $arguments');
    print('didProgressUpdate arguments type ${arguments.runtimeType}');

    _hasFinishedIntallingUpdateStreamController.add(arguments);
  }
}
