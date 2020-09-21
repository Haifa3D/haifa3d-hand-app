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

Currently, we support 12 presets, thus, 11 / `B` beeing the highest preset number.

* Service UUID: `e0198001-7544-42c1-0000-b24344b6aa70`
* Preset Characteristic UUIDs: `e0198001-7544-42c1-10<PRESET NUMBER BYTE>-b24344b6aa70`
  * Preset 0: `e0198001-7544-42c1-1000-b24344b6aa70`
  * Preset 1: `e0198001-7544-42c1-1001-b24344b6aa70`
  * Preset 11: `e0198001-7544-42c1-100B-b24344b6aa70`

### Trigger Service

This is a custom GATT service that is used to trigger presets saved using the preset service to execute them immediately. It offers one write-only characteristic. If a value is written, the action corresponding to the preset number is executed.

* Service UUID: `e0198002-7544-42c1-0000-b24344b6aa70`
* Trigger Characteristic UUID: `e0198002-7544-42c1-0001-b24344b6aa70`

### Configuration Service

This is a custom GATT service that is used to read and write configuration variables on the controller.

* Service UUID: `e0198003-7544-42c1-0000-b24344b6aa70`
* Configuration Value Characteristic UUID: `e0198003-7544-42c1-00<ConfigId>-b24344b6aa70` where `<ConfigId>` is a two digit hex number such as `00`, `0a` or `12`.

#### Configuration Values

| Id | Name                    | Short | Description                                                                                                                                  |
|----|-------------------------|-------|----------------------------------------------------------------------------------------------------------------------------------------------|
| 0  | Low Torque Value        | LTV   | VALUE x dV is the threshold for LOW torque actions. the higher the value, the higher the torque.                                             |
| 1  | High Torque Value       | HTV   | VALUE x dV is the threshold for HIGH torque actions. the higher the value, the higher the torque.                                            |
| 2  | Torque Measure Start    | TS    | start applying torque control after VALUE[msec] due to the motors high starting current.                                                     |
| 3  | Window Width Filter     | WW    | VALUE[#of samples] is the window width for smoothing. the higher the value, the signal is smoother but the delay can increase.               |
| 4  | Low Torque Slope Value  | LTS   | VALUE x dt[msec] is the time threshold an increasing torque is allowed for LOW torque actions. the higher the value, the higher the torque.  |
| 5  | High Torque Slope Value | HTS   | VALUE x dt[msec] is the time threshold an increasing torque is allowed for HIGH torque actions. the higher the value, the higher the torque. |
| 6  | Threshold Factor Motor0 | TF0   | all torque threshold values of motor 0 are multiplied by the factor VALUE[%]/100. the higher the value, the higher the torque.               |
| 7  | Threshold Factor Motor1 | TF1   | all torque threshold values of motor 1 are multiplied by the factor VALUE[%]/100 . the higher the value, the higher the torque.              |
| 8  | Threshold Factor Motor2 | TF2   | all torque threshold values of motor 2 are multiplied by the factor VALUE[%]/100 . the higher the value, the higher the torque.              |
| 9  | Threshold Factor Motor3 | TF3   | all torque threshold values of motor 3 are multiplied by the factor VALUE[%]/100 . the higher the value, the higher the torque.              |
| 10 | Threshold Factor Motor4 | TF4   | all torque threshold values of motor 4 are multiplied by the factor VALUE[%]/100 . the higher the value, the higher the torque.              |
| 11 |                         |       | undefined                                                                                                                                    |
| 12 |                         |       | undefined                                                                                                                                    |
| 13 |                         |       | undefined                                                                                                                                    |
| 14 |                         |       | undefined                                                                                                                                    |
| 15 |                         |       | undefined                                                                                                                                    |
| 16 |                         |       | undefined                                                                                                                                    |
| 17 |                         |       | undefined                                                                                                                                    |
| 18 | Threshold Value Unit    | DV    | the VALUE[measurement samples] is the value unit, dV, that multiplies the Threshold Value parameters.                                        |
| 19 | Debugging               | DEB   | if VALUE is 1: when the hand board is connected to a computer the debugging values will appear on the COM.                                   |

[Dummy ESP32 implementation](src/esp32/haifa3d/src/main.cpp)  
[Android implementation](src/android/Haifa3d/app/src/main/java/com/gjung/haifa3d/ble/DirectExecuteService.kt)
