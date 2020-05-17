# Protocol

This describes the protocol the app and the hand controller use to talk to each other. It's based on Bluetooth Low Energy.

## Action

One specific action the controller can execute (physical action, thus, moving the fingers) is specified by a byte representation. Every action is performed a) for a specific time (every motor stops when either the time passed or the "low" torque level is reached) or b) until a low/high torque level is reached. Every action can involve one or more motors that move in direction A or B.

Every action has a length of 5 bytes:

1. Length (always equals 5)
2. Action Header
3. Stop Mode Detail
4. Motors Activated
5. Motors Direction

### #2 [Action header](src/android/Haifa3d/app/src/main/java/com/gjung/haifa3d/model/HandActionHeader.kt)

A header describing metadata of the action.

| Bit     |                                |
|---------|--------------------------------|
| 0 (LSB) |                                |
| 1       |                                |
| 2       |                                |
| 3       |                                |
| 4       |                                |
| 5       |                                |
| 6       |                                |
| 7 (MSB) | Stop Mode: 0 = Toque; 1 = Time |

### #3a [Torque Stop Mode Detail](src/android/Haifa3d/app/src/main/java/com/gjung/haifa3d/model/TorqueStopModeDetail.kt)

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

### #3b [Time Stop Mode Detail](src/android/Haifa3d/app/src/main/java/com/gjung/haifa3d/model/TimeStopModeDetail.kt)

Just a number after how many `Time Units` (to define in config, i.e. 50ms) the action should stop.

### #4 [Motors Activated](src/android/Haifa3d/app/src/main/java/com/gjung/haifa3d/model/MotorsActivated.kt)

As in #3a. 0 = deactivated, 1 = activated.

### #5 [Motors Direction](src/android/Haifa3d/app/src/main/java/com/gjung/haifa3d/model/MotorsDirection.kt)

As in #3a. 0 = Dir 1, 1 = Dir 2.

## BLE Services

### Battery Service

The hand provides a Bluetooth SIG standard battery service. Notifications can be enabled. [Android implementation](src/android/Haifa3d/app/src/main/java/com/gjung/haifa3d/ble/BatteryLevelService.kt)

### Direct Execute Service

This is a custom GATT service that is used to execute actions on the hand immediately. It offers one write-only characteristic. If a value is written, the action is executed. The action is represented in raw bytes as defined above.

* Service UUID: `e0198000-7544-42c1-0000-b24344b6aa70`
* Characteristic UUID: `e0198000-7544-42c1-0000-b24344b6aa70`

[Dummy ESP32 implementation](src/esp32/haifa3d/main.cpp)  
[Android implementation](src/android/Haifa3d/app/src/main/java/com/gjung/haifa3d/ble/DirectExecuteService.kt)
