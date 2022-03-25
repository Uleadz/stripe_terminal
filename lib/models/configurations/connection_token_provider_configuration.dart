class ConnectionTokenProviderConfiguration {
  String backendBaseUrl; //* For example "mybackend.com/api"
  String requestUrl; //* For example "/connection_token"
  String tokenKeyInJson; //* For example "myConnectionToken"
  String userAutherizationToken; //* For example "myUserAutherizationToken"

  ConnectionTokenProviderConfiguration({
    required this.backendBaseUrl,
    required this.requestUrl,
    required this.tokenKeyInJson,
    required this.userAutherizationToken,
  });
}
