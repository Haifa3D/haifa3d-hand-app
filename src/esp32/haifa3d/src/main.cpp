#include <Arduino.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h> //Library to use BLE as server
#include <BLE2902.h> 
#include <bitset>

// All BLE characteristic UUIDs are of the form:
// 0000XXXX-0000-1000-8000-00805f9b34fb

#define HAND_DIRECT_EXECUTE_SERVICE_UUID     "e0198000-7544-42c1-0000-b24344b6aa70"
#define EXECUTE_ON_WRITE_CHARACTERISTIC_UUID "e0198000-7544-42c1-0001-b24344b6aa70"

#define HAND_PRESET_SERVICE_UUID             "e0198001-7544-42c1-0000-b24344b6aa70"

#define HAND_TRIGGER_SERVICE_UUID            "e0198002-7544-42c1-0000-b24344b6aa70"
#define TRIGGER_ON_WRITE_CHARACTERISTIC_UUID "e0198002-7544-42c1-0001-b24344b6aa70"

#define HAND_CONFIG_SERVICE_UUID             "e0198003-7544-42c1-0000-b24344b6aa70"
#define CONFIG_LTV_UUID                      "e0198003-7544-42c1-1000-b24344b6aa70"
#define CONFIG_HTV_UUID                      "e0198003-7544-42c1-1001-b24344b6aa70"
#define CONFIG_TS_UUID                       "e0198003-7544-42c1-1002-b24344b6aa70"
#define CONFIG_WW_UUID                       "e0198003-7544-42c1-1003-b24344b6aa70"
#define CONFIG_LTS_UUID                      "e0198003-7544-42c1-1004-b24344b6aa70"
#define CONFIG_HTS_UUID                      "e0198003-7544-42c1-1005-b24344b6aa70"
#define CONFIG_TF0_UUID                      "e0198003-7544-42c1-1006-b24344b6aa70"
#define CONFIG_TF1_UUID                      "e0198003-7544-42c1-1007-b24344b6aa70"
#define CONFIG_TF2_UUID                      "e0198003-7544-42c1-1008-b24344b6aa70"
#define CONFIG_TF3_UUID                      "e0198003-7544-42c1-1009-b24344b6aa70"
#define CONFIG_TF4_UUID                      "e0198003-7544-42c1-100a-b24344b6aa70"
#define CONFIG_DV_UUID                       "e0198003-7544-42c1-1012-b24344b6aa70"
#define CONFIG_DEB_UUID                      "e0198003-7544-42c1-1013-b24344b6aa70"

bool _BLEClientConnected = false;

#define BatteryService BLEUUID((uint16_t)0x180F) 
BLECharacteristic BatteryLevelCharacteristic(BLEUUID((uint16_t)0x2A19), BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY);
BLEDescriptor BatteryLevelDescriptor(BLEUUID((uint16_t)0x2901));

class MyServerCallbacks : public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      _BLEClientConnected = true;
      Serial.println("Connected");
    };

    void onDisconnect(BLEServer* pServer) {
      _BLEClientConnected = false;
      Serial.println("Disconnected");
    }
};

const char * presetCharacteristicUuid(int presetNumber)
{
  const char *uuids[12];
  uuids[0] = "e0198001-7544-42c1-1000-b24344b6aa70";
  uuids[1] = "e0198001-7544-42c1-1001-b24344b6aa70";
  uuids[2] = "e0198001-7544-42c1-1002-b24344b6aa70";
  uuids[3] = "e0198001-7544-42c1-1003-b24344b6aa70";
  uuids[4] = "e0198001-7544-42c1-1004-b24344b6aa70";
  uuids[5] = "e0198001-7544-42c1-1005-b24344b6aa70";
  uuids[6] = "e0198001-7544-42c1-1006-b24344b6aa70";
  uuids[7] = "e0198001-7544-42c1-1007-b24344b6aa70";
  uuids[8] = "e0198001-7544-42c1-1008-b24344b6aa70";
  uuids[9] = "e0198001-7544-42c1-1009-b24344b6aa70";
  uuids[10] = "e0198001-7544-42c1-100a-b24344b6aa70";
  uuids[11] = "e0198001-7544-42c1-100b-b24344b6aa70";
  return uuids[presetNumber];
}

const char * configCharacteristicUuid(int idx)
{
  const char *uuids[20];

  uuids[0] = CONFIG_LTV_UUID;
  uuids[1] = CONFIG_HTV_UUID;
  uuids[2] = CONFIG_TS_UUID;
  uuids[3] = CONFIG_WW_UUID;
  uuids[4] = CONFIG_LTS_UUID;
  uuids[5] = CONFIG_HTS_UUID;
  uuids[6] = CONFIG_TF0_UUID;
  uuids[7] = CONFIG_TF1_UUID;
  uuids[8] = CONFIG_TF2_UUID;
  uuids[9] = CONFIG_TF3_UUID;
  uuids[10] = CONFIG_TF4_UUID;
  // config [11-17] not defined by protocol but we'll
  // fill the array consequtively though; this might seem
  // unintuitive but is needed and makes no difference
  uuids[11] = CONFIG_DV_UUID;  // config id 18
  uuids[12] = CONFIG_DEB_UUID; // config id 19
  return uuids[idx];
}

void printInterpretation(unsigned char* msg) {
  std::bitset<8> bs;
  bs = std::bitset<8>(msg[0]);

  Serial.println("Torque Stop Modes");
  Serial.printf("Turn Motor: %s\n", bs.test(7 - 0) ? "high" : "low");
  Serial.printf("Finger 1:   %s\n", bs.test(7 - 1) ? "high" : "low");
  Serial.printf("Finger 2:   %s\n", bs.test(7 - 2) ? "high" : "low");
  Serial.printf("Finger 3:   %s\n", bs.test(7 - 3) ? "high" : "low");
  Serial.printf("Finger 4:   %s\n", bs.test(7 - 4) ? "high" : "low");

  Serial.println();
  Serial.print("Timeout; # of time units:");
  Serial.println(msg[1]);
  
  Serial.println();
  Serial.println("Motors activated:");
  bs = std::bitset<8>(msg[2]);
  if (bs.test(7 - 0)) Serial.println("Turn Motor");
  if (bs.test(7 - 1)) Serial.println("Finger 1");
  if (bs.test(7 - 2)) Serial.println("Finger 2");
  if (bs.test(7 - 3)) Serial.println("Finger 3");
  if (bs.test(7 - 4)) Serial.println("Finger 4");
  
  Serial.println();
  Serial.println("Motors direction:");
  bs = std::bitset<8>(msg[3]);
  Serial.printf("Turn Motor: %s\n", bs.test(7 - 0) ? "a" : "b");
  Serial.printf("Finger 1:   %s\n", bs.test(7 - 1) ? "a" : "b");
  Serial.printf("Finger 2:   %s\n", bs.test(7 - 2) ? "a" : "b");
  Serial.printf("Finger 3:   %s\n", bs.test(7 - 3) ? "a" : "b");
  Serial.printf("Finger 4:   %s\n", bs.test(7 - 4) ? "a" : "b");
  
}

class DirectExecuteCallbacks : public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
      unsigned char* dataPtr;
      dataPtr = pCharacteristic->getData();
      short len = dataPtr[0];
      Serial.println("Got bytes:");
      Serial.println(len);
      for (int i = 1; i < len; i++) {
        Serial.println(dataPtr[i]);
      }
      Serial.println("----");
      short movements = (len - 1) / 4;
      for (int i = 0; i < movements; i++) {
        Serial.println();
        Serial.printf("--== Movement %i ==--\n", i);
        printInterpretation(dataPtr + (i * 4) + 1);
      }
    };
};

class PresetCallbacks : public BLECharacteristicCallbacks {
    int presetId = 0;

    void onWrite(BLECharacteristic *pCharacteristic) {
      unsigned char* dataPtr;
      dataPtr = pCharacteristic->getData();
      short len = dataPtr[0];
      Serial.println();
      Serial.printf("--==++ Preset %i written ++==--\n", presetId);
      short movements = (len - 1) / 4;
      for (int i = 0; i < movements; i++) {
        Serial.println();
        Serial.printf("--== Movement %i ==--\n", i);
        printInterpretation(dataPtr + (i * 4) + 1);
      }
    };

    void onRead(BLECharacteristic *pCharacteristic) {
      unsigned char val[5];
      val[0] = 5;
      val[1] = 0;   // torque stop mode
      val[2] = 12;  // time stop mode
      val[3] = 255; // motors activated
      val[4] = 0;   // motors direction
      pCharacteristic->setValue(val, 5);
    }

  public:
    PresetCallbacks(int id) {
      presetId = id;
    }
};

class TriggerCallbacks : public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
      unsigned char* dataPtr;
      dataPtr = pCharacteristic->getData();
      short preset = dataPtr[0];
      Serial.println();
      Serial.printf("--==++ Triggered Preset %i ++==--\n", preset);
    };
};

class ConfigCallbacks : public BLECharacteristicCallbacks {
    int idx = 0;
    short value = 0;

    void onWrite(BLECharacteristic *pCharacteristic) {
      unsigned char* dataPtr;
      dataPtr = pCharacteristic->getData();
      value = dataPtr[0];
      Serial.println();
      // attention: the ids printed here do not correspond to the configIds from the docs
      // the ids printed here are strictly consequtive but the ids from the docs are not
      // consequtively defined
      Serial.printf("--- Config %i written. Set to: %i ---\n", idx, value);
    };

    void onRead(BLECharacteristic *pCharacteristic) {
      unsigned char val[1];
      val[0] = value;
      pCharacteristic->setValue(val, 5);
    }

  public:
    ConfigCallbacks(int id) {
      idx = id;
      value = 100 + id;
    }
};

void InitBLE() {
  BLEDevice::init("Haifa3D");
  // Create the BLE Server
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  // Create the Battery Service
  BLEService *pBattery = pServer->createService(BatteryService);

  pBattery->addCharacteristic(&BatteryLevelCharacteristic);
  BatteryLevelDescriptor.setValue("Percentage 0 - 100");
  BatteryLevelCharacteristic.addDescriptor(&BatteryLevelDescriptor);
  BatteryLevelCharacteristic.addDescriptor(new BLE2902());

  BLEService *pDirectExecService = pServer->createService(HAND_DIRECT_EXECUTE_SERVICE_UUID);
  BLECharacteristic *pExecOnWriteCharacteristic = pDirectExecService->createCharacteristic(
                                         EXECUTE_ON_WRITE_CHARACTERISTIC_UUID,
                                         BLECharacteristic::PROPERTY_WRITE
                                       );
  pExecOnWriteCharacteristic->setCallbacks(new DirectExecuteCallbacks());

  BLEService *pTriggerService = pServer->createService(HAND_TRIGGER_SERVICE_UUID);
  BLECharacteristic *pTriggerOnWriteCharacteristic = pTriggerService->createCharacteristic(
                                         TRIGGER_ON_WRITE_CHARACTERISTIC_UUID,
                                         BLECharacteristic::PROPERTY_WRITE
                                       );
  pTriggerOnWriteCharacteristic->setCallbacks(new TriggerCallbacks());

  // the 32 is important because otherwise we dont have enough handles and just the first 6 characteristics will be visible
  BLEService *pPresetService = pServer->createService(BLEUUID(HAND_PRESET_SERVICE_UUID), 32);
  for (int i = 0; i < 12; i++) {
    BLECharacteristic *pPresetCharacteristic = pPresetService->createCharacteristic(
                                         presetCharacteristicUuid(i),
                                         BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE
                                       );
    pPresetCharacteristic->setCallbacks(new PresetCallbacks(i));
    Serial.printf("Added Preset Characteristic %i", i);
  }

  // the 32 is important because otherwise we dont have enough handles and just the first 6 characteristics will be visible
  BLEService *pConfigService = pServer->createService(BLEUUID(HAND_CONFIG_SERVICE_UUID), 96);
  for (int i = 0; i < 13; i++) {
    BLECharacteristic *pConfigCharacteristic = pConfigService->createCharacteristic(
                                         configCharacteristicUuid(i),
                                         BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE
                                       );
    pConfigCharacteristic->setCallbacks(new ConfigCallbacks(i));
  }

  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(BatteryService);
  pAdvertising->addServiceUUID(HAND_DIRECT_EXECUTE_SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);
  pAdvertising->setMinPreferred(0x12);

  pBattery->start();
  pDirectExecService->start();
  pTriggerService->start();
  pPresetService->start();
  pConfigService->start();
  // Start advertising
  pAdvertising->start();
}

void setup() {
  Serial.begin(9600);
  Serial.println("Starting BLE work!");
  InitBLE();
  /*BLEDevice::init("Long name works now");
  BLEServer *pServer = BLEDevice::createServer();
  BLEService *pService = pServer->createService(SERVICE_UUID);
  BLECharacteristic *pCharacteristic = pService->createCharacteristic(
                                         CHARACTERISTIC_UUID,
                                         BLECharacteristic::PROPERTY_READ |
                                         BLECharacteristic::PROPERTY_WRITE
                                       );

  pCharacteristic->setValue("Hello World says Neil");
  pService->start();
  // BLEAdvertising *pAdvertising = pServer->getAdvertising();  // this still is working for backward compatibility
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);  // functions that help with iPhone connections issue
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();
  Serial.println("Characteristic defined! Now you can read it in your phone!");*/
}

uint8_t level = 57;

void loop() {
  BatteryLevelCharacteristic.setValue(&level, 1);
  BatteryLevelCharacteristic.notify();
  delay(350);

  level--;
  Serial.println(int(level));

  if (int(level)==0)
  level=100;
}