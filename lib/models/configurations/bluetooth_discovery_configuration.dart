import 'package:stripe_terminal/models/configurations/discovery_method.dart';

class BluetoothDiscoveryConfiguration {
  bool simulated;
  DiscoveryMethod discoveryMethod;
  String locationId;

  BluetoothDiscoveryConfiguration({
    required this.simulated,
    required this.discoveryMethod,
    required this.locationId,
  });
}
