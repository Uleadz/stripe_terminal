import 'package:stripe_terminal/models/configurations/simulate_reader_update.dart';

class BluetoothConnectionConfiguration {
  String locationId;
  SimulateReaderUpdate simulateReaderUpdate;

  BluetoothConnectionConfiguration({
    required this.locationId,
    this.simulateReaderUpdate = SimulateReaderUpdate.NONE,
  });
}
