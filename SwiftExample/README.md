# SwiftExample

This app demonstrates an integration with AffiniPay iOS SDK to enable payments within an iOS app. This app is implemented in [Swift](https://swift.org/) and uses [Cocoapods](https://cocoapods.org/) for dependency management.

## Configuration

#### Cocoapods

Run the CocoaPods command to install dependencies.

```
pod install
```

#### Affinipay SDK

Drop the latest version of the `AffiniPaySDK.framework` SDK file into the `SwiftExample` directory.

#### Example backend app

Download the app from the [GitHub page](https://github.com/affinipay/app-integration-example-ruby), configure and run the app by following the README. Use this command to run the app:

```
rackup config.ru -o 0.0.0.0
```

After the app is running, click the `Connect To AffiniPay` button and log in with valid AffiniPay merchant credentials.

## Set Server IP & Run

* Open the `SwiftExample.xcworkspace` file in Xcode.
* In Example -> SwiftExample -> Info.plist , set the value of `affinipaySdkBaseUrl` to the IP of your Example backend app (the default setting will work when running on a simulator).
* Build and run the `SwiftExample` scheme.

## Contribute
Contributions in the form of GitHub pull requests are welcome. Please adhere to the following guidelines:
  - Before embarking on a significant change, please create an issue to discuss the proposed change and ensure that it is likely to be merged.
  - Follow the coding conventions used in the specific the project.
  - Any contributions must be licensed under the GPL license.

## License
  [MIT](./LICENSE) © AffiniPay LLC
