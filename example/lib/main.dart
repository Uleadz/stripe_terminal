// ignore_for_file: avoid_print

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:stripe_terminal/models/configurations/bluetooth_connection_configuration.dart';
import 'package:stripe_terminal/models/configurations/bluetooth_discovery_configuration.dart';
import 'package:stripe_terminal/models/configurations/connection_token_provider_configuration.dart';
import 'package:stripe_terminal/models/configurations/discovery_method.dart';
import 'package:stripe_terminal/models/configurations/simulate_reader_update.dart';
import 'package:stripe_terminal/models/stripe_reader.dart';
import 'package:stripe_terminal/stripe_terminal.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  StripeReader? connectedReader;
  bool isLoadingReaderConnection = false;
  String connectionMessage = 'Connecting...';

  @override
  void initState() {
    super.initState();
    initPlatformState();

    StripeTerminal.isUpdateRequired.listen((event) {
      print('Is update required $event');
      if (event) {
        setState(() {
          connectionMessage = 'Updating reader';
        });
      }
    });

    StripeTerminal.updateProgress.listen((event) {
      print('Update progress $event');
      setState(() {
        connectionMessage = 'Updating reader\nProgress: ${event * 100}%';
      });
    });

    StripeTerminal.hasFinishedIntallingUpdate.listen((event) {
      print('hasFinishedIntallingUpdate: $event');
      if (event) {
        setState(() {
          connectionMessage = 'Connecting...';
        });
      }
    });
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    StripeTerminal.setup();

    try {
      StripeTerminal.setupConnectionTokenProvider(
        config: ConnectionTokenProviderConfiguration(
          backendBaseUrl: 'https://ucallz.ngrok.io',
          requestUrl: '/Api/ConnectionToken',
          tokenKeyInJson: 'Data',
          userAutherizationToken:
              'YsrwiRbUjIp5D0oUzn-FlYM0JXabsG-bvk_aVxO1gBLPl0bnDfQdmMv3AIQYX4fk8ncOIoD5iWx0g-W4dYGS8_W1UvcAf3Ek4HEhMouCq7NFKQWKQEeBE8wdo9punNgHsIgU61vc_1kGP1trLEcxMov2e-GSuN5huUp7Deigg-3rusUEN9D7hg_OOljheNuMREW6K5rcJ5AOrcSueWUaMiVZG1mcLLsS3vuf5rN0pY90wDSIYp_foK_fID1u2HUIDpx7_no0wBMBlbWFjZbtBqx3owQvOZTRxcB6768joBbPjze4x3dgf-frhHzEHQmWB3_Fz5yhWFyASFJv8i08KjWa6asrRm2dA0KQw7Sc-uhzCOzipmXXcXLgcnXTTDaCn6TjGtRQhTyG2t3JP1O7YQ9yT0_tLDFaCYh7qY-0pkJI4XdSvAe94tly20F1l0-3BlqeFWCjlPKU_fa630RgKzYczl1I9xhjnAnEr2da89LcDvy38QPGw2zZsAM2cGIP7Xm6avIARSvfM4mEKqgC4gy-0J6F9qs3-p4ELrnM4dI',
        ),
      );
    } on PlatformException catch (e) {
      print(e);
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              MaterialButton(
                child: const Text('Discvoer'),
                color: Colors.blue,
                onPressed: () async {
                  try {
                    await StripeTerminal.discoverReaders(
                      config: BluetoothDiscoveryConfiguration(
                        simulated: false,
                        discoveryMethod: DiscoveryMethod.LOCAL_MOBILE,
                        locationId: 'tml_EibM5gDe0QpBZt',
                      ),
                    );
                  } catch (e) {
                    print(e);
                    print('');
                  }
                },
              ),
              const SizedBox(height: 50),
              connectedReader == null
                  ? StreamBuilder(
                      stream: StripeTerminal.didUpdateDiscoveredReader,
                      builder: (context, snapshot) {
                        if (isLoadingReaderConnection) {
                          return Column(
                            children: [
                              const CircularProgressIndicator(),
                              const SizedBox(
                                height: 16,
                              ),
                              Text(
                                connectionMessage,
                                style: const TextStyle(fontWeight: FontWeight.w600),
                              )
                            ],
                          );
                        }

                        if (snapshot.hasData) {
                          final readers = snapshot.data as List<StripeReader>;
                          if (readers.isEmpty) {
                            return const Text('No readers detected');
                          }

                          return ListView.separated(
                            shrinkWrap: true,
                            itemCount: readers.length,
                            itemBuilder: (context, index) {
                              final currReader = readers[index];

                              return ListTile(
                                title: Text(currReader.serialNumber),
                                tileColor: Colors.grey[100],
                                onTap: () async {
                                  setState(() {
                                    isLoadingReaderConnection = true;
                                  });

                                  try {
                                    await StripeTerminal.connectLocalMobileReader(
                                      reader: currReader,
                                      config: BluetoothConnectionConfiguration(
                                        // locationId: 'tml_EiQJwXfwQkiMuF',
                                        locationId: 'tml_EibM5gDe0QpBZt',
                                        simulateReaderUpdate: SimulateReaderUpdate.NONE,
                                      ),
                                    );
                                    // await StripeTerminal.connectBluetoothReader(
                                    //   reader: currReader,
                                    //   config: BluetoothConnectionConfiguration(
                                    //     // locationId: 'tml_EiQJwXfwQkiMuF',
                                    //     locationId: 'tml_EibM5gDe0QpBZt',
                                    //     simulateReaderUpdate: SimulateReaderUpdate.NONE,
                                    //   ),
                                    // );

                                    setState(() {
                                      connectedReader = currReader;
                                    });
                                  } catch (e) {
                                    print(e);
                                  }

                                  setState(() {
                                    isLoadingReaderConnection = false;
                                  });
                                },
                              );
                            },
                            separatorBuilder: (context, index) {
                              return const Divider();
                            },
                          );
                        } else {
                          return const Text('Doesnt have data');
                        }
                      },
                    )
                  : Text('Connected to reader: ${connectedReader!.serialNumber}'),
              const SizedBox(height: 50),
              if (connectedReader != null)
                MaterialButton(
                  child: const Text('Charge 100'),
                  color: Colors.blue,
                  onPressed: () async {
                    // const paymentIntent = 'YOUR_PAYMENT_INTENT_HERE';
                    const paymentIntent =
                        // 'pi_3KgH7fLBnGF2noZq11fv4gy0_secret_fHhbANK4bJAfYwSJcXBpPBwOR';
                        // 'pi_3KgHOALBnGF2noZq1rh8woqi_secret_jxE7iJg8F9lZFziAkHKGxQVXT';
                        // 'pi_3KgTVnLBnGF2noZq0H6XQZpM_secret_e4P6fiTfBayUSodGhptlPNdyh';
                        // 'pi_3KgTfN2R2OSHGJzg1bsrCCxh_secret_5tDpnX1VTpA38b2UZzR7bvEAb';
                        // 'pi_3KgTk92R2OSHGJzg1L8EyoxH_secret_Z2QQ2n316MZuUdYnqmCoEGYoN';
                        // 'pi_3KgTlW2R2OSHGJzg1cdZUfQk_secret_x9ROZsVpEgDGsIG08UEKf9gZN';
                        // 'pi_3KgTld2R2OSHGJzg1sq4CAuP_secret_SkdV348oYKL6yb1FLSLNkobBw';
                        'pi_3KgTlk2R2OSHGJzg0VdKEusy_secret_HM3kIZb3H8nEItEiiu1fGDic4';

                    try {
                      await StripeTerminal.charge(paymentIntent: paymentIntent);

                      print("Payment success: $paymentIntent");
                    } catch (e) {
                      print(e);
                    }
                  },
                ),
            ],
          ),
        ),
      ),
    );
  }
}
