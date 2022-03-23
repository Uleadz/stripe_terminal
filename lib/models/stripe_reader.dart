class StripeReader {
  String serialNumber;
  num? batteryLevel;
  String? locationId;

  StripeReader({
    required this.serialNumber,
    required this.batteryLevel,
    this.locationId,
  });

  factory StripeReader.fromJson(json) {
    print('StripeReader.fromJson');
    return StripeReader(
      serialNumber: json['serialNumber'],
      batteryLevel: json['batteryLevel'],
      locationId: json['locationId'],
    );
  }
}
