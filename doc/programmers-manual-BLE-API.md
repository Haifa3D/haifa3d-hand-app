
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
  * suspend Extract_preset(preset_number: Int)
    * returns a single preset (represneted by an HandAction object), which contains a list of HandMovmente objects.
  * Hand_activation_by_preset(preset_number: Int)
    * this function gets an int in the following range (0-11).
    * activating matching preset.
  * Extract_battery_status()
    * this funtcion returns the battery percentage (int)
   
   
  
