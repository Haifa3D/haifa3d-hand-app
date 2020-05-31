# Protocol

This describes the protocol the app and the hand controller use to talk to each other. It's based on Bluetooth Low Energy.

## Action

One specific action the controller can execute (physical action, thus, moving the fingers) is specified by a byte representation. Every action can consist of multiple movements. Each movement is performed until one of two stop conditions becomes true: a) a specific time passed (i.e. all motors stop after 5 time units, i.e. 5 x 50ms = 250ms) or b) a low/high torque level is reached (based on current measurement). Every movement can involve one or more motors that move in direction A or B.

Every action has a length of 1 + 4*n bytes:

1. Length
2. n Movements
    1. Torque Stop Threshold
    2. Time Stop Threshold
    3. Motors Activated
    4. Motors Direction

Bytes (2) to (5) represent one single physical movement. Each action can consist of multiple such movements. When one movement completed, the controller starts the second movement. For example, a tapping action would first close one finger in the first movement and then open it again in the second movement. A double tapping action would consist of four movements (close, open, close, open). This design allows for flexible sequential actions.

### #2 [Torque Stop Mode Detail](src/android/Haifa3d/app/src/main/java/com/gjung/haifa3d/model/TorqueStopModeDetail.kt)

| Bit     |          |
|---------|----------|
| 0 (LSB) |          |
| 1       |          |
| 2       |          |
| 3       | Finger 4 |
| 4       | Finger 3 |
| 5       | Finger 2 |
| 6       | Finger 1 |
| 7 (MSB) | Turn     |

Every bit value means: 0 = low, 1 = high

Which torque value (i.e. current) is "low" and which is "high" can be set in controller wide configuration (not yet).

### #3 [Time Stop Mode Detail](src/android/Haifa3d/app/src/main/java/com/gjung/haifa3d/model/TimeStopModeDetail.kt)

Just a number after how many `Time Units` (to define in config, i.e. 50ms) the action should stop.

### #4 [Motors Activated](src/android/Haifa3d/app/src/main/java/com/gjung/haifa3d/model/MotorsActivated.kt)

As in #2. 0 = deactivated, 1 = activated.

### #5 [Motors Direction](src/android/Haifa3d/app/src/main/java/com/gjung/haifa3d/model/MotorsDirection.kt)

As in #2. 0 = Dir 1, 1 = Dir 2.

## BLE Services

### Battery Service

The hand provides a Bluetooth SIG standard battery service. Notifications can be enabled. [Android implementation](src/android/Haifa3d/app/src/main/java/com/gjung/haifa3d/ble/BatteryLevelService.kt)

### Direct Execute Service

This is a custom GATT service that is used to execute actions on the hand immediately. It offers one write-only characteristic. If a value is written, the action is executed. The action is represented in raw bytes as defined above.

* Service UUID: `e0198000-7544-42c1-0000-b24344b6aa70`
* Characteristic UUID: `e0198000-7544-42c1-0001-b24344b6aa70`

### Preset Service

This custom GATT service is used to read and write the presets that are saved on the hand. These presets can be configured by the user in the app and can be triggered by any trigger i.e. the app or an external button or sensor. They can be read by the app on a different mobile phone too.

*ToDo*: How many presets? 12?

* Service UUID: `e0198001-7544-42c1-0000-b24344b6aa70`
* Preset Characteristic UUIDs: `e0198001-7544-42c1-<PRESET NUMBER>-b24344b6aa70`
  * Preset 1: `e0198001-7544-42c1-0001-b24344b6aa70`
  * Preset 12: `e0198001-7544-42c1-000C-b24344b6aa70`

### Trigger Service

This is a custom GATT service that is used to trigger presets saved using the preset service to execute them immediately. It offers one write-only characteristic. If a value is written, the action corresponding to the preset number is executed.

* Service UUID: `e0198002-7544-42c1-0000-b24344b6aa70`
* Trigger Characteristic UUID: `e0198002-7544-42c1-0001-b24344b6aa70`

### Configuration Service

This is a custom GATT service that is used to read and write configuration variables on the controller.

* Service UUID: `e0198003-7544-42c1-0000-b24344b6aa70`
* Motor Specific, `<MOTOR #>` is a number, i.e. for the first motor `1`:
  * Low Torque Value Characteristic UUID:        `e0198003-7544-42c1-00<MOTOR #>1-b24344b6aa70`
    * i.e. `e0198003-7544-42c1-0011-b24344b6aa70` for motor `1`
  * Low Torque Slope Value Characteristic UUID:  `e0198003-7544-42c1-00<MOTOR #>2-b24344b6aa70`
  * High Torque Value Characteristic UUID:       `e0198003-7544-42c1-00<MOTOR #>3-b24344b6aa70`
  * High Torque Slope Value Characteristic UUID: `e0198003-7544-42c1-00<MOTOR #>4-b24344b6aa70`
* Torque Measure Start MS Characteristic UUID: `e0198003-7544-42c1-0101-b24344b6aa70`
  * Start measuring the torque/current of the motors after `x` ms. This is needed because the need more current when the start moving.
* Windows Width Filter Characteristic UUID:    `e0198003-7544-42c1-0102-b24344b6aa70`
  * `int`, valid range: [1, 16]
* What more values do we need?
  * Maybe: BLE Device Name Characteristic UUID: `e0198003-7544-42c1-0103-b24344b6aa70`

[Dummy ESP32 implementation](src/esp32/haifa3d/src/main.cpp)  
[Android implementation](src/android/Haifa3d/app/src/main/java/com/gjung/haifa3d/ble/DirectExecuteService.kt)
