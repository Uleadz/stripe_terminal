import 'package:stripe_terminal/models/configurations/discovery_method.dart';

class BluetoothDiscoveryConfiguration {
  bool simulated;
  DiscoveryMethod discoveryMethod;

  BluetoothDiscoveryConfiguration({
    required this.simulated,
    required this.discoveryMethod,
  });
}
