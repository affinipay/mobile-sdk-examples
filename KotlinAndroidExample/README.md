## Android

### Kotlin
Kotlin requires Android Studio 3.0. Otherwise, the Kotlin example app can be imported and run normally from within Android Studio.


# KotlinAndroidExample

This app demonstrates an integration with AffiniPay Android SDK to enable payments within an Android app. This app is implemented using [Kotlin for Android](https://developer.android.com/kotlin/get-started.html).

## Configuration

#### Affinipay SDK

Drop the latest version of the `AffiniPay-MobileSDK.aar` SDK file into the `sample-app/libs` directory

#### Example backend app

Download the app from the [GitHub page](https://github.com/affinipay/app-integration-example-ruby), configure and run the app by following the README. Use this command to run the app:

```
rackup config.ru -o 0.0.0.0
```

After the app is running, click the `Connect To AffiniPay` button and log in with valid AffiniPay merchant credentials.

## Run

Open the top-level `build.gradle` file in Android Studio, build and run the `sample-app` configuration. You must be using Android Studio v3.0 or above.

## Contribute
Contributions in the form of GitHub pull requests are welcome. Please adhere to the following guidelines:
  - Before embarking on a significant change, please create an issue to discuss the proposed change and ensure that it is likely to be merged.
  - Follow the coding conventions used in the specific the project.
  - Any contributions must be licensed under the GPL license.

## License
[MIT](./LICENSE) © AffiniPay LLC
