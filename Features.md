# Features
> Features and properties of the existing hardware, thoughts regarding how to design the btle protocol _hand controller_ <-> _app_

Existing raw features:

## Physical
* 4 Fingers, that can open and close
* Rotate the hand against the arm
* Every motor can be started and stopped individually
* When a motor stops can be configured based on the applied torque (via a model based on the current)

## Modes
Enable a motor
* until the end is reached (finger fully open/closed)
* until a torque level is reached (i.e. more pressure for a smartphone then for a paper cup)
* for a set amount of time
* while i.e. the user presses a button

<hr>

Abstractions to be built:

## Presets
* Operations that make sense for the end user, built based on multiple raw operations of the different motors
* i.e. open all 4 fingers, commonly recognised gestures
* they can't assume an initial state because we don't know the motor/finger positions

## Triggers
* can activate a preset
* can be triggered by other devices i.e. sensors. The sensor is connected to the app, the app triggers the controller.
* stop all motors if any are activated, then enable a preset (the newest trigger wins)

<hr>

BTLE protocol features needed:

## Commands

* Trigger
* Read presets
* Merge preset
  * if a preset with this ID exists overwrite, create otherwise
* Delete preset
* Read battery %
* something for config

## Models

### Preset

* Guid
* Name/Caption
* list of commands (serial) _or_ declaration motor => mode

### Config

tbd

<hr>

## ToDo
* Can presets contain serial commands? I.e. open finger 1 completely and close finger 2 when the first action is finished.
  * This would need a little more sophisticated protocol but it's probably not too hard and would offer more flexibility
  * The serial version enables everything, a simple (declarative?) version would offer too + more
* How to connect first-party sensors directly to the controller? How to set the connection up? Does the controller know the sensor and it's trigger or the other way around? How to configure this in-app?
* Calibration? Which parameters to configure? How to know the right values?
* Configuration? I.e. set threshoulds for torque levels
* Which serialization format to use?
  * Anything custom, proprietary, raw byte based
  * Json/Bson/Toml or probably protobuf
    * what is easy to parse on the board? -> Libraries
  * Anything other that makes sense given we use BTLE? Anything that's typical in this field?
  * What hardware/wireless restrictions regarding storage space/packet sizes/...?
* Are there any (storage space) limitations to consider? How relevant is the plain count of bytes i.e. a preset takes?