import Flutter
import UIKit
import StripeTerminal

@available(iOS 11.0, *)
public class SwiftStripeTerminalPlugin: NSObject, FlutterPlugin {
  private var channel: FlutterMethodChannel

  private var availableReaders = [Reader]()

  private var paymentIntent: PaymentIntent?

  var taskCancelable: Cancelable? = nil

  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "stripe_terminal", binaryMessenger: registrar.messenger())
    
    let instance = SwiftStripeTerminalPlugin(channel: channel)
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  init(channel : FlutterMethodChannel) {
    self.channel = channel
    super.init()
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    let args = call.arguments as? Dictionary<String, Any>
    if(call.method == "setupConnectionTokenProvider") {
      self.setupConnectionTokenProvider(
        backendBaseUrl: args!["backendBaseUrl"] as! String,
        requestUrl: args!["requestUrl"] as! String,
        tokenKeyInJson: args!["tokenKeyInJson"] as! String,
        userAutherizationToken: args!["userAutherizationToken"] as! String,
        result: result
      )
    } else if(call.method == "discoverReaders") {
      self.discoverReaders(
        simulated: args!["simulated"] as! Bool,
        result: result
      )
    } else if(call.method == "connectBluetoothReader") {
      self.connectBluetoothReader(
        selectedReaderSerialNumber: args!["selectedReaderSerialNumber"] as! String,
        locationId: args!["locationId"] as! String,
        result: result
      )
    } else if(call.method == "disconnectBluetoothReader") {
      self.disconnectBluetoothReader(
        result: result
      )
    } else if(call.method == "charge") {
      self.charge(
        paymentIntentSecret: args!["paymentIntent"] as! String,
        result: result
      )
    } else if(call.method == "cancelCurrentTask") {
      self.cancelCurrentTask(result: result)
    } else if(call.method == "clearCachedCredentials") {
      self.clearCachedCredentials()
    } else if(call.method == "cancelPaymentIntent") {
      self.cancelPaymentIntent(result: result)
    }
  }

  //MARK: - Handlers
  private func clearCachedCredentials() {
    Terminal.shared.clearCachedCredentials();
  }

  private func setupConnectionTokenProvider(backendBaseUrl: String, requestUrl: String, tokenKeyInJson: String, userAutherizationToken: String, result: @escaping FlutterResult) {
    print("setupConnectionTokenProvider called")
    if(Terminal.hasTokenProvider()) {
      print("Terminal already have connection token connected")
      result(nil);
      return;
    }

    Terminal.setTokenProvider(
      APIClient(
        backendBaseUrl: backendBaseUrl,
        requestUrl: requestUrl,
        tokenKeyInJson: tokenKeyInJson,
        userAutherizationToken: userAutherizationToken
      )
    )

    result(nil);
  }

  private func discoverReaders(simulated: Bool, result: @escaping FlutterResult) {
    print("discoverReaders called")

    let config = DiscoveryConfiguration(
      discoveryMethod: .bluetoothScan,
      simulated: simulated
    )

    do {
      try self.taskCancelable = Terminal.shared.discoverReaders(config, delegate: self) { error in
        if let error = error {
          print("discoverReaders failed: \(error)")

          result(
            FlutterError(code: "discoverReadersFailed", 
              message: error.localizedDescription,
              details: "error"
            )
          );
        } else {
          print("discoverReaders succeeded")

          result(nil)
        }
      }
    } catch let error {
      result(
        FlutterError(code: "discoverReadersFailed", 
          message: error.localizedDescription,
          details: "error"
        )
      );
    }
  }

  private func cancelPaymentIntent(result: @escaping FlutterResult) {
    if(self.paymentIntent == nil) {
      print("No payment intent to cancel")
      return;
    }

    do {
      try Terminal.shared.cancelPaymentIntent(self.paymentIntent!) { intent, error in
        if let error = error {
          print("Canceling payment intent failed: \(error)")

          result(
            FlutterError(code: "cancelPaymentIntent", 
              message: error.localizedDescription,
              details: "error"
            )
          );
        } else {
          print("cancelPaymentIntent succeeded")

          result(nil)
        }
      }
    } catch let error {
      result(
        FlutterError(code: "cancelPaymentIntent", 
          message: error.localizedDescription,
          details: "error"
        )
      );
    }
  }

  private func connectBluetoothReader(selectedReaderSerialNumber: String, locationId: String, result: @escaping FlutterResult) {
    let selectedReader = self.availableReaders.filter{ $0.serialNumber == selectedReaderSerialNumber }.first
    
    if selectedReader == nil {
      print("Unknown reader selected")
      result(
        FlutterError(code: "connectBluetoothReaderFailed", 
          message: "Unknown reader selected",
          details: "Unknown reader selected"
        )
      );
      return
    }

    let connectionConfig = BluetoothConnectionConfiguration(
      locationId: locationId
    )

    Terminal.shared.connectBluetoothReader(selectedReader!, delegate: self, connectionConfig: connectionConfig) { reader, error in
            if let reader = reader {
                print("Successfully connected to reader: \(reader)")
                result(nil)
            } else if let error = error {
                print("connectReader failed: \(error)")
                result(
                  FlutterError(code: "connectBluetoothReaderFailed", 
                    message: error.localizedDescription,
                    details: "error"
                  )
                );
            }
        }

  }

  private func disconnectBluetoothReader(result: @escaping FlutterResult) {
    Terminal.shared.disconnectReader({ error in
            if error == nil {
                print("Successfully disconnected from reader")
                result(nil)
            } else if let error = error {
                print("disconnectReader failed: \(error)")
                result(
                  FlutterError(code: "disconnectBluetoothReaderFailed", 
                    message: error.localizedDescription,
                    details: "error"
                  )
                );
            }
      }
    )
  }

  private func cancelCurrentTask(result: @escaping FlutterResult) {
    if(self.taskCancelable == nil) {
      print("Nothing to cancel")
      result(nil);
    } else {
      self.taskCancelable!.cancel({error in 
        if error == nil {
          print("Successfully canceled task")
          result(nil)
        } else if let error = error {
          print("taskCancelable failed: \(error)")
          result(
            FlutterError(code: "taskCancelable",
              message: error.localizedDescription,
              details: "error"
            )
          );
        }
      })
    }
  }

  private func charge(paymentIntentSecret: String, result: @escaping FlutterResult) {
    Terminal.shared.retrievePaymentIntent(clientSecret: paymentIntentSecret) {
      createResult, createError in
        if let error = createError {
          print("createPaymentIntent failed: \(error)")
          result(
            FlutterError(code: "connectBluetoothReaderFailed", 
              message: error.localizedDescription,
              details: "error"
            )
          );
        } else if let paymentIntent = createResult {
          self.paymentIntent = paymentIntent;
          print("createPaymentIntent succeeded")
          self.taskCancelable = Terminal.shared.collectPaymentMethod(paymentIntent) { collectResult, collectError in
            if let error = collectError {
              print("collectPaymentMethod failed: \(error)")
              result(
                FlutterError(code: "collectPaymentMethodFailed", 
                  message: error.localizedDescription,
                  details: "error"
                )
              );
            } else if let paymentIntent = collectResult {
              print("collectPaymentMethod succeeded")

              Terminal.shared.processPayment(paymentIntent) { processResult, processError in
                if let error = processError {
                  print("processPayment failed: \(error)")
                  result(
                    FlutterError(code: "processPaymentFailed", 
                      message: error.localizedDescription,
                      details: "error"
                    )
                  );
                } else if let processPaymentPaymentIntent = processResult {
                  print("processPayment succeeded")
                  result(processPaymentPaymentIntent.stripeId)
                }
              }
            }
          }
        }
    }
  }
}

@available(iOS 11.0, *)
extension SwiftStripeTerminalPlugin: DiscoveryDelegate {
  public func terminal(_ terminal: Terminal, didUpdateDiscoveredReaders readers: [Reader]) {
    self.availableReaders = readers
    print("didUpdateDiscoveredReaders called")

    var readersJson = [] as [[String: Any?]]

    for currReader in readers {
      readersJson.append(
        [
          "serialNumber": currReader.serialNumber,
          "batteryLevel": currReader.batteryLevel,
          "locationId": currReader.locationId,
        ]
      )
    }

    print("Start:")
    print(readersJson)
    print("End")

    channel.invokeMethod("didUpdateDiscoveredReaders", arguments: readersJson)
  }
}

@available(iOS 11.0, *)
extension SwiftStripeTerminalPlugin: BluetoothReaderDelegate {
    public func reader(_ reader: Reader, didReportAvailableUpdate update: ReaderSoftwareUpdate) {}

    public func reader(_ reader: Reader, didStartInstallingUpdate update: ReaderSoftwareUpdate, cancelable: Cancelable?) {}

    public func reader(_ reader: Reader, didFinishInstallingUpdate update: ReaderSoftwareUpdate?, error: Error?) {
        
    }

    public func reader(_ reader: Reader, didReportReaderSoftwareUpdateProgress progress: Float) {
    }

    public func reader(_ reader: Reader, didRequestReaderInput inputOptions: ReaderInputOptions = []) {
      print("didRequestReaderInput: \(inputOptions)")
    }

    public func reader(_ reader: Reader, didRequestReaderDisplayMessage displayMessage: ReaderDisplayMessage) {
      print("didRequestReaderDisplayMessage: \(displayMessage)")
    }
}

//MARK: API
class APIClient: ConnectionTokenProvider {
    //For example "mybackend.com/api"
    let backendBaseUrl: String
    //For example "/connection_token"
    let requestUrl: String
    //For example "myConnectionToken"
    let tokenKeyInJson: String
    //For example "myUserAutherizationToken"
    let userAutherizationToken: String

    init(backendBaseUrl: String, requestUrl: String, tokenKeyInJson: String, userAutherizationToken: String){
        self.backendBaseUrl = backendBaseUrl
        self.requestUrl = requestUrl
        self.tokenKeyInJson = tokenKeyInJson
        self.userAutherizationToken = userAutherizationToken
    }

    func fetchConnectionToken(_ completion: @escaping ConnectionTokenCompletionBlock) {
        let config = URLSessionConfiguration.default
        let session = URLSession(configuration: config)
        let url = URL(string: requestUrl, relativeTo: URL(string: backendBaseUrl))!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.addValue("Bearer \(userAutherizationToken)", forHTTPHeaderField: "Authorization")
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        

        let task = session.dataTask(with: request) { (data, response, error) in
            if let data = data {
                do {
                    // Warning: casting using 'as? [String: String]' looks simpler, but isn't safe:
                    let json = try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any]
                    if let secret = json?[self.tokenKeyInJson] as? String {
                        completion(secret, nil)
                    } else {
                        let error = NSError(domain: "com.stripe-terminal-ios.example",
                                            code: 2000,
                                            userInfo: [NSLocalizedDescriptionKey: "Missing '[tokenKeyInJson]' in ConnectionToken JSON response"])
                        completion(nil, error)
                    }
                } catch {
                    completion(nil, error)
                }
            } else {
                let error = NSError(domain: "com.stripe-terminal-ios.example",
                                    code: 1000,
                                    userInfo: [NSLocalizedDescriptionKey: "No data in response from ConnectionToken endpoint"])
                completion(nil, error)
            }
        }

        task.resume()
    }

    func capturePaymentIntent(_ paymentIntentId: String, completion: @escaping ErrorCompletionBlock) {
        completion(nil)
    }
}