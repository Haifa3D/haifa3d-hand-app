# Programmer's Manual

## Structure

### Bigger Picture

To understand how this project works, it's essential to know the basic structure. It does not only consist of the app this repository is about. This project involves

* an embedded hand controller, that is the interface to the hand prothesis motors.
* a Bluetooth Low Energy protocol, that is used by the app and the controller to communicate to each other. This protocol is [defined and documented](/Protocol.md) in the repository too.
* a Kotlin-based Android App, that the end-user of the hand utilizes to control and customize, what his hand does.

### App Project

The app is a typical modern Kotlin-based Android App that sticks to as many of Google's modern development best practices as possible. It is available from the [Play Store](https://play.google.com/store/apps/details?id=com.gjung.haifa3d). The app further utilizes a complete [continuous integration pipeline](https://dev.azure.com/georg-jung/Haifa3d/_build/latest?definitionId=12&branchName=master) that ensures buildability at any commit and provides artifacts. Released versions are [available from GitHub releases](https://github.com/georg-jung/technion-robotic-arm/releases) (deploying there is a continuous deployment step). To provide deterministic and automatic versioning, this project uses [NBGV](https://github.com/dotnet/Nerdbank.GitVersioning) that calculates versions based on [`version.json`](/version.json) and git version height. Android's `versionCode` (just an arbitrary integer, where a higher number will be preferred for installation over a lower number) is [automatically calculated](https://github.com/Technion236503/2020b-Haifa3D/blob/master/build/steps/setAndroidVersionCode.yml) to be the number of commits in the tree up to the build's commit. See [#13](https://github.com/Technion236503/2020b-Haifa3D/issues/13#issuecomment-636913076) for more details.

### App Code

BLE communication is one of the main tasks of the app. Most activities and parts the user can interact with require Bluetooth communication. To simplify this, the app uses [NordicSemiconductor's Android BLE Library](https://github.com/NordicSemiconductor/Android-BLE-Library). Now, the connection should not be dropped immediatley when the app is minimized and there even are future use cases that require the app to stay connected to the hand all the time in the background. To achieve this behaviour with current versions of android, one needs to develop a so-called foreground service. This provides the bluetooth functionality to the app's UI but keeps running when the app is minimized/closed. Every foreground service needs to show a notification to the user consistently to keep them informed that the app is still open. Further, the hand provides different services on it's own. One can read the battery percentage, trigger some action or configure some constants. While this comes all together resulting in the one hand as the user sees it, these services can be split apart to achieve higher quality, more maintainable code.

Sadly, NordicSemi's `ObservableBleManager` doesn't seem to be developed with this in mind. By default, it provides it's core functionality just to inheritants (functions are `protected`), making composition impossible. Because I very much dislike the idea of a god-class for anything bluetooth related and try to stick to [SOLID](https://en.wikipedia.org/wiki/SOLID) as much as possible, I built an interface on top of that that exposes the most relevant parts of the bluetooth communication. Thus, simple classes that model and manage one part of the functionality can be built but the central parts such as connection lifecycle event handling still happens in one central place. For example, there is a [`DirectExecuteService`](/src/android/Haifa3d/app/src/main/java/com/gjung/haifa3d/ble/DirectExecuteService.kt) that models the hand's *execute this action right away* functionality, as well as a [`BatteryLevelService`](/src/android/Haifa3d/app/src/main/java/com/gjung/haifa3d/ble/BatteryLevelService.kt) that does anything related to battery level reading. These services are all [`GattHandler`](/src/android/Haifa3d/app/src/main/java/com/gjung/haifa3d/ble/GattHandler.kt)s that come together in [`AppBleManager`](/src/android/Haifa3d/app/src/main/java/com/gjung/haifa3d/ble/AppBleManager.kt) that does the composition, modelling the complete hand device.

Still, the `AppBleManager` is portable and not bound to be used in the context of a foreground service. There is a [`NotificationService`](/src/android/Haifa3d/app/src/main/java/com/gjung/haifa3d/ble/NotificationService.kt) that is responsible for *beeing always running and showing a notification* and then a derived [`BleService`](/src/android/Haifa3d/app/src/main/java/com/gjung/haifa3d/ble/BleService.kt) that brings this together with the `AppBleManager`. Consequently, all this BLE related code lives in the `ble` package.

#### Protocol Models

To model all the [protocol](/Protocol.md) features and make them easily usable and abstract the technical details away from the UI, I created many model classes that can be instantiated in code and be translated to bytes and send to the hand afterwards. They are grouped together in the `model` package. For example, one could instantiate a [`HandAction`](/src/android/Haifa3d/app/src/main/java/com/gjung/haifa3d/model/HandAction.kt) that represents the corresponding part of the protocol. This action could either be executed directly using the `DirectExecuteService` or saved as a preset using the `PresetService`.

#### UI

The UI is built using fragments and is utilizing Android's databinding, which is the most recent approach to bind inflated android XML views to code. Most of this directly UI related code is packaged under `ui`. The `adapter` package contains code needed for recycler view binding. To avoid duplicating BLE related code while still almost every view needs to interact via BLE, I created [`BleFragment`](/src/android/Haifa3d/app/src/main/java/com/gjung/haifa3d/BleFragment.kt) as well as [`BleActivity`](/src/android/Haifa3d/app/src/main/java/com/gjung/haifa3d/BleActivity.kt) that are inherited by the specific UI components. Some parts of the UI code are inspired by NordicSemi's nRF Blinky example app and are in java for that reason.

## Flow

The apps flow is mostly split in two major parts:

* connected to the hand
* not connected yet

Before the connection is established there is essentially a device discovery screen that guides the user to connect to his hand. When the connection is established, there are different fragments that model the interaction with specific features. Navigation is implemented using Android's XML navigation definition.

### Overview

* Not connected
  * Activate Bluetooth / grant permissions
  * Select device and connect to it
* Connected
  * Menu structure
  * Status (check battery level etc.)
  * Configure presets
    * Each preset consists of one action
    * Each action consists of 1 - n movements
      * Each movement can move one or more motors of the hand until a specified condition is met
  * Trigger presets
  * Execute actions directly by pressing buttons

## Further information

Currently, the app does not need any database to store information. Everything the user sets up is directly stored on the hand controller or does not need to be stored in the first place. This app uses Firebase to collect telemetry and information about crashes using crashlytics. The app is minified using proguard as part of the release/deploy process. Deployment is done via AAB instead of APK to benefit from the new packaging format's advantages.

## BLE API

Following the description of the use and importance of the BLE communication in apps in general, and in our app in particular (as described in the app code), we have developed a BLE library.This library enables the use of this service, as well as other services which are available in the app, so that in the future, apps which include further optionality would be able to interface with the prosthesis.

### BLE API flow

* bind(callback:BleListener,context: Context, intent: Intent):
- BleListner: an interface that includes 2 functions that needs to be implemended by the user
   1. onServiceConnected(bleService: BleService)
   2. onServiceDisconnected()
* connect(device: BluetoothDevice)
* disconnect()
* unbind(context: Context)
 
 ### BLE API services
 
* after calling to bind() and connect(), the user can acsses to the following functions:
  * Extract_preset_anotations()
    * returns a list of hand actions, every action is list of movments.
  * Hand_activation_by_preset(preset_number: Int)
    * this function gets an int in the following range (0-11).
    * activating matching preset.
  * Extract_battery_status()
    * this funtcion returns the battery percentage (int)
   
   
  

