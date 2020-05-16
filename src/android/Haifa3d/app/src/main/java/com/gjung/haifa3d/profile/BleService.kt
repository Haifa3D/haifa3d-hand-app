package com.gjung.haifa3d.profile

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.gjung.haifa3d.R
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.observer.BondingObserver
import no.nordicsemi.android.ble.observer.ConnectionObserver
import no.nordicsemi.android.ble.utils.ILogger
import no.nordicsemi.android.log.ILogSession
import no.nordicsemi.android.log.Logger

abstract class BleService: Service(), ConnectionObserver, BondingObserver {
    private val TAG = "BleProfileService"

    val BROADCAST_CONNECTION_STATE = "com.gjung.haifa3d.BROADCAST_CONNECTION_STATE"
    val BROADCAST_SERVICES_DISCOVERED = "com.gjung.haifa3d.BROADCAST_SERVICES_DISCOVERED"
    val BROADCAST_DEVICE_READY = "com.gjung.haifa3d.DEVICE_READY"
    val BROADCAST_BOND_STATE = "com.gjung.haifa3d.BROADCAST_BOND_STATE"
    val BROADCAST_ERROR = "com.gjung.haifa3d.BROADCAST_ERROR"

    /**
     * The parameter passed when creating the service. Must contain the address of the sensor that we want to connect to
     */
    val EXTRA_DEVICE_ADDRESS = "com.gjung.haifa3d.EXTRA_DEVICE_ADDRESS"
    /**
     * The key for the device name that is returned in [.BROADCAST_CONNECTION_STATE] with state [.STATE_CONNECTED].
     */
    val EXTRA_DEVICE_NAME = "com.gjung.haifa3d.EXTRA_DEVICE_NAME"
    val EXTRA_DEVICE = "com.gjung.haifa3d.EXTRA_DEVICE"
    val EXTRA_LOG_URI = "com.gjung.haifa3d.EXTRA_LOG_URI"
    val EXTRA_CONNECTION_STATE = "com.gjung.haifa3d.EXTRA_CONNECTION_STATE"
    val EXTRA_BOND_STATE = "com.gjung.haifa3d.EXTRA_BOND_STATE"
    val EXTRA_SERVICE_PRIMARY = "com.gjung.haifa3d.EXTRA_SERVICE_PRIMARY"
    val EXTRA_SERVICE_SECONDARY = "com.gjung.haifa3d.EXTRA_SERVICE_SECONDARY"
    val EXTRA_ERROR_MESSAGE = "com.gjung.haifa3d.EXTRA_ERROR_MESSAGE"
    val EXTRA_ERROR_CODE = "com.gjung.haifa3d.EXTRA_ERROR_CODE"

    val STATE_LINK_LOSS = -1
    val STATE_DISCONNECTED = 0
    val STATE_CONNECTED = 1
    val STATE_CONNECTING = 2
    val STATE_DISCONNECTING = 3

    private lateinit var bleManager: AppBleManager

    /**
     * Returns a handler that is created in onCreate().
     * The handler may be used to postpone execution of some operations or to run them in UI thread.
     */
    private lateinit var handler: Handler

    protected var bound = false
    private var activityIsChangingConfiguration = false
    private var bluetoothDevice: BluetoothDevice? = null
    private var deviceName: String? = null
    private var logSession: ILogSession? = null

    private val bluetoothStateBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state =
                intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)
            val logger: ILogger = getBinder()
            val stateString =
                "[Broadcast] Action received: " + BluetoothAdapter.ACTION_STATE_CHANGED + ", state changed to " + state2String(
                    state
                )
            logger.log(Log.DEBUG, stateString)
            when (state) {
                BluetoothAdapter.STATE_ON -> onBluetoothEnabled()
                BluetoothAdapter.STATE_TURNING_OFF, BluetoothAdapter.STATE_OFF -> onBluetoothDisabled()
            }
        }

        private fun state2String(state: Int): String {
            return when (state) {
                BluetoothAdapter.STATE_TURNING_ON -> "TURNING ON"
                BluetoothAdapter.STATE_ON -> "ON"
                BluetoothAdapter.STATE_TURNING_OFF -> "TURNING OFF"
                BluetoothAdapter.STATE_OFF -> "OFF"
                else -> "UNKNOWN ($state)"
            }
        }
    }

    inner class LocalBinder : Binder(),
        ILogger {
        /**
         * Disconnects from the sensor.
         */
        fun disconnect() {
            val state: Int = bleManager.connectionState
            if (state == BluetoothGatt.STATE_DISCONNECTED || state == BluetoothGatt.STATE_DISCONNECTING) {
                bleManager.close()
                onDeviceDisconnected(bluetoothDevice!!, ConnectionObserver.REASON_TERMINATE_LOCAL_HOST)
                return
            }
            bleManager.disconnect().enqueue()
        }

        /**
         * Sets whether the bound activity if changing configuration or not.
         * If `false`, we will turn off battery level notifications in onUnbind(..) method below.
         *
         * @param changing true if the bound activity is finishing
         */
        fun setActivityIsChangingConfiguration(changing: Boolean) {
            activityIsChangingConfiguration = changing
        }

        /**
         * Returns the device address
         *
         * @return device address
         */
        val deviceAddress: String
            get() = bluetoothDevice!!.address

        /**
         * Returns the device name
         *
         * @return the device name
         */
        val getDeviceName: String
            get() = deviceName!!

        /**
         * Returns the Bluetooth device
         *
         * @return the Bluetooth device
         */
        val bluetoothDevice: BluetoothDevice
            get() = bluetoothDevice!!

        /**
         * Returns `true` if the device is connected to the sensor.
         *
         * @return `true` if device is connected to the sensor, `false` otherwise
         */
        val isConnected: Boolean
            get() = bleManager.isConnected

        /**
         * Returns the connection state of given device.
         *
         * @return the connection state, as in [BleManager.getConnectionState].
         */
        val connectionState: Int
            get() = bleManager.connectionState

        /**
         * Returns the log session that can be used to append log entries.
         * The log session is created when the service is being created.
         * The method returns `null` if the nRF Logger app was not installed.
         *
         * @return the log session
         */
        val getLogSession: ILogSession
            get() = logSession!!

        override fun log(level: Int, message: String) {
            Logger.log(logSession, level, message)
        }

        override fun log(
            level: Int, @StringRes messageRes: Int,
            vararg params: Any
        ) {
            Logger.log(logSession, level, messageRes, *params)
        }
    }

    /**
     * Returns the binder implementation. This must return class implementing the additional manager interface that may be used in the bound activity.
     *
     * @return the service binder
     */
    protected fun getBinder(): LocalBinder { // default implementation returns the basic binder. You can overwrite the LocalBinder with your own, wider implementation
        return LocalBinder()
    }

    override fun onBind(intent: Intent?): IBinder? {
        bound = true
        return getBinder()
    }

    override fun onRebind(intent: Intent?) {
        bound = true
        if (!activityIsChangingConfiguration) onRebind()
    }

    /**
     * Called when the activity has rebound to the service after being recreated.
     * This method is not called when the activity was killed to be recreated when the phone orientation changed
     * if prior to being killed called [LocalBinder.setActivityIsChangingConfiguration] with parameter true.
     */
    protected fun onRebind() { // empty default implementation
    }

    override fun onUnbind(intent: Intent?): Boolean {
        bound = false
        if (!activityIsChangingConfiguration) onUnbind()
        // We want the onRebind method be called if anything else binds to it again
        return true
    }

    /**
     * Called when the activity has unbound from the service before being finished.
     * This method is not called when the activity is killed to be recreated when the phone orientation changed.
     */
    protected fun onUnbind() { // empty default implementation
    }

    override fun onCreate() {
        super.onCreate()
        handler = Handler()
        // Initialize the manager
        bleManager = initializeManager()
        bleManager.setConnectionObserver(this)
        bleManager.setBondingObserver(this)
        // Register broadcast receivers
        registerReceiver(
            bluetoothStateBroadcastReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )
        // Service has now been created
        onServiceCreated()
        // Call onBluetoothEnabled if Bluetooth enabled
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter.isEnabled) {
            onBluetoothEnabled()
        }
    }

    /**
     * Called when the service has been created, before the [.onBluetoothEnabled] is called.
     */
    protected fun onServiceCreated() { // empty default implementation
    }

    /**
     * Initializes the Ble Manager responsible for connecting to a single device.
     *
     * @return a new BleManager object
     */
    protected abstract fun initializeManager(): AppBleManager

    /**
     * This method returns whether autoConnect option should be used.
     *
     * @return true to use autoConnect feature, false (default) otherwise.
     */
    protected fun shouldAutoConnect(): Boolean {
        return false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null || !intent.hasExtra(EXTRA_DEVICE_ADDRESS)) throw UnsupportedOperationException(
            "No device address at EXTRA_DEVICE_ADDRESS key"
        )
        val logUri = intent.getParcelableExtra<Uri>(EXTRA_LOG_URI)
        logSession = Logger.openSession(applicationContext, logUri)
        deviceName = intent.getStringExtra(EXTRA_DEVICE_NAME)
        Logger.i(logSession, "Service started")
        val adapter = BluetoothAdapter.getDefaultAdapter()
        val deviceAddress = intent.getStringExtra(EXTRA_DEVICE_ADDRESS)
        bluetoothDevice = adapter.getRemoteDevice(deviceAddress)
        bleManager!!.setLogger(logSession)
        onServiceStarted()
        bleManager!!.connect(bluetoothDevice!!)
            .useAutoConnect(shouldAutoConnect())
            .retry(3, 100)
            .enqueue()
        return START_REDELIVER_INTENT
    }

    /**
     * Called when the service has been started. The device name and address are set.
     * The BLE Manager will try to connect to the device after this method finishes.
     */
    protected fun onServiceStarted() { // empty default implementation
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // This method is called when user removed the app from Recents.
        // By default, the service will be killed and recreated immediately after that.
        // However, all managed devices will be lost and devices will be disconnected.
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister broadcast receivers
        unregisterReceiver(bluetoothStateBroadcastReceiver)
        // shutdown the manager
        bleManager!!.close()
        Logger.i(logSession, "Service destroyed")
        bluetoothDevice = null
        deviceName = null
        logSession = null
        // handler = null
    }

    /**
     * Method called when Bluetooth Adapter has been disabled.
     */
    protected fun onBluetoothDisabled() { // empty default implementation
    }

    /**
     * This method is called when Bluetooth Adapter has been enabled and
     * after the service was created if Bluetooth Adapter was enabled at that moment.
     * This method could initialize all Bluetooth related features, for example open the GATT server.
     */
    protected fun onBluetoothEnabled() { // empty default implementation
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        val broadcast = Intent(BROADCAST_CONNECTION_STATE)
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice)
        broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_CONNECTING)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        val broadcast = Intent(BROADCAST_CONNECTION_STATE)
        broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_CONNECTED)
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice)
        broadcast.putExtra(EXTRA_DEVICE_NAME, deviceName)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        onDeviceDisconnected(device, reason)
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        // Notify user about changing the state to DISCONNECTING
        val broadcast = Intent(BROADCAST_CONNECTION_STATE)
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice)
        broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_DISCONNECTING)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        // Note 1: Do not use the device argument here unless you change calling onDeviceDisconnected from the binder above
        // Note 2: if BleManager#shouldAutoConnect() for this device returned true, this callback will be
        // invoked ONLY when user requested disconnection (using Disconnect button). If the device
        // disconnects due to a link loss, the onLinkLossOccurred(BluetoothDevice) method will be called instead.
        if (reason == ConnectionObserver.REASON_LINK_LOSS) {
            onLinkLossOccurred(device)
            return
        }
        if (reason == ConnectionObserver.REASON_NOT_SUPPORTED) {
            onDeviceNotSupported(device)
            return
        }

        val broadcast = Intent(BROADCAST_CONNECTION_STATE)
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice)
        broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_DISCONNECTED)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
        if (stopWhenDisconnected()) stopService()
    }

    /**
     * This method should return false if the service needs to do some asynchronous work after if has disconnected from the device.
     * In that case the [.stopService] method must be called when done.
     *
     * @return true (default) to automatically stop the service when device is disconnected. False otherwise.
     */
    protected fun stopWhenDisconnected(): Boolean {
        return true
    }

    protected fun stopService() { // user requested disconnection. We must stop the service
        Logger.v(logSession, "Stopping service...")
        stopSelf()
    }

    fun onLinkLossOccurred(device: BluetoothDevice) {
        val broadcast = Intent(BROADCAST_CONNECTION_STATE)
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice)
        broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_LINK_LOSS)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        val broadcast = Intent(BROADCAST_DEVICE_READY)
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
    }

    fun onDeviceNotSupported(device: BluetoothDevice) {
        val broadcast = Intent(BROADCAST_SERVICES_DISCOVERED)
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice)
        broadcast.putExtra(EXTRA_SERVICE_PRIMARY, false)
        broadcast.putExtra(EXTRA_SERVICE_SECONDARY, false)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
        // no need for disconnecting, it will be disconnected by the manager automatically
    }

    override fun onBondingRequired(device: BluetoothDevice) {
        showToast(R.string.bonding)
        val broadcast = Intent(BROADCAST_BOND_STATE)
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice)
        broadcast.putExtra(EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDING)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
    }

    override fun onBonded(device: BluetoothDevice) {
        showToast(R.string.bonded)
        val broadcast = Intent(BROADCAST_BOND_STATE)
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice)
        broadcast.putExtra(EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDED)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
    }

    override fun onBondingFailed(device: BluetoothDevice) {
        showToast(R.string.bonding_failed)
        val broadcast = Intent(BROADCAST_BOND_STATE)
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice)
        broadcast.putExtra(EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
    }

    fun onError(device: BluetoothDevice, message: String, errorCode: Int) {
        val broadcast = Intent(BROADCAST_ERROR)
        broadcast.putExtra(EXTRA_DEVICE, bluetoothDevice)
        broadcast.putExtra(EXTRA_ERROR_MESSAGE, message)
        broadcast.putExtra(EXTRA_ERROR_CODE, errorCode)
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast)
    }

    /**
     * Shows a message as a Toast notification. This method is thread safe, you can call it from any thread
     *
     * @param messageResId an resource id of the message to be shown
     */
    protected fun showToast(messageResId: Int) {
        handler!!.post {
            Toast.makeText(
                this.baseContext,
                messageResId,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Shows a message as a Toast notification. This method is thread safe, you can call it from any thread
     *
     * @param message a message to be shown
     */
    protected fun showToast(message: String?) {
        handler!!.post {
            Toast.makeText(
                this.baseContext,
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Returns the log session that can be used to append log entries. The method returns `null` if the nRF Logger app was not installed. It is safe to use logger when
     * [.onServiceStarted] has been called.
     *
     * @return the log session
     */
    protected fun getLogSession(): ILogSession? {
        return logSession
    }

    /**
     * Returns the device address
     *
     * @return device address
     */
    protected fun getDeviceAddress(): String? {
        return bluetoothDevice!!.address
    }

    /**
     * Returns the Bluetooth device object
     *
     * @return bluetooth device
     */
    protected fun getBluetoothDevice(): BluetoothDevice? {
        return bluetoothDevice
    }

    /**
     * Returns the device name
     *
     * @return the device name
     */
    protected fun getDeviceName(): String? {
        return deviceName
    }

    /**
     * Returns `true` if the device is connected to the sensor.
     *
     * @return `true` if device is connected to the sensor, `false` otherwise
     */
    protected fun isConnected(): Boolean {
        return bleManager != null && bleManager!!.isConnected
    }
}