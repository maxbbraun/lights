package net.maxbraun.lights;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class LightsService extends Service {
  private static final String TAG = LightsService.class.getSimpleName();

  private static final long TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(10);
  private static final long RETRY_DELAY_MILLIS = TimeUnit.SECONDS.toMillis(1);

  private static final String FEATHER_ADDRESS = "FA:04:9E:15:B9:BA";

  private static final UUID UART_SERVICE_UUID =
      UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");

  private static final UUID TX_CHARACTERISTIC_UUID =
      UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");

  private static final String ALL_WHITE_COMMAND = "AW";
  private static final String ALL_OFF_COMMAND = "A0";
  private static final String ONE_RED_COMMAND = "1R";
  private static final String ONE_BLUE_COMMAND = "1B";

  private static final ScanFilter scanFilter = new ScanFilter.Builder()
      .setDeviceAddress(FEATHER_ADDRESS)
      .build();

  private static final ScanSettings scanSettings = new ScanSettings.Builder()
      .setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH)
      .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
      .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
      .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
      .setReportDelay(0)
      .build();

  private final ScanCallback scanCallback = new ScanCallback() {
    @Override
    public void onScanResult(int callbackType, ScanResult result) {
      Log.v(TAG, "Scan successful: " + result);

      assertState(State.SCANNING,
          State.READY /* on reconnect */,
          State.CONNECTING /* on reconnect */);

      device = result.getDevice();

      state = State.DISCONNECTED;
      connect();
    }

    @Override
    public void onScanFailed(int errorCode) {
      Log.e(TAG, "Scan failed: " + errorCode);

      assertState(State.SCANNING);
      state = State.INITIALIZED;

      retryFromHere();
    }
  };

  private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
      Log.v(TAG, String.format("New connection state: %d (status %d)", newState, status));

      if (newState == BluetoothProfile.STATE_CONNECTED) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
          Log.d(TAG, "Connected.");
          state = State.CONNECTED;
          discover();
        } else {
          Log.w(TAG, "Failed to connect.");
          retryFromHere();
        }
      } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
          Log.d(TAG, "Disconnected.");
          state = State.DISCONNECTED;
          connect();
        }
      }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
      if (status == BluetoothGatt.GATT_SUCCESS) {
        Log.d(TAG, "Services discovered.");
        state = State.READY;
      } else {
        Log.d(TAG, "Failed to discover services.");
        retryFromHere();
      }
    }
  };

  private final ILightsService.Stub binder = new ILightsService.Stub() {
    @Override
    public void allWhite(ILightsServiceCallback callback) throws RemoteException {
      if (send(ALL_WHITE_COMMAND)) {
        callback.onSuccess();
      } else {
        callback.onError();
      }
    }

    @Override
    public void allOff(ILightsServiceCallback callback) throws RemoteException {
      if (send(ALL_OFF_COMMAND)) {
        callback.onSuccess();
      } else {
        callback.onError();
      }
    }

    @Override
    public void oneRed(ILightsServiceCallback callback) throws RemoteException {
      if (send(ONE_RED_COMMAND)) {
        callback.onSuccess();
      } else {
        callback.onError();
      }
    }

    @Override
    public void oneBlue(ILightsServiceCallback callback) throws RemoteException {
      if (send(ONE_BLUE_COMMAND)) {
        callback.onSuccess();
      } else {
        callback.onError();
      }
    }
  };

  private final Handler handler = new Handler(Looper.getMainLooper());

  private enum State {
    INITIALIZED,
    SCANNING,
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCOVERING,
    READY,
  }

  private State state;

  private BluetoothLeScanner scanner;
  private BluetoothDevice device;
  private BluetoothGatt gatt;

  @Override
  public void onCreate() {
    Log.v(TAG, "Service created.");
    super.onCreate();

    BluetoothManager bluetoothManager =
        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
    BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

    if ((bluetoothAdapter == null) || !bluetoothAdapter.isEnabled()) {
      throw new IllegalStateException("Bluetooth is not enabled.");
    }

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      throw new IllegalStateException("Location permission has not been granted.");
    }

    scanner = bluetoothAdapter.getBluetoothLeScanner();

    state = State.INITIALIZED;
    scan();
  }

  @Override
  public void onDestroy() {
    Log.v(TAG, "Service destroyed.");
    close();

    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }

  private void scan() {
    assertState(State.INITIALIZED);

    Log.d(TAG, "Starting scan.");
    state = State.SCANNING;

    scanner.startScan(Arrays.asList(scanFilter), scanSettings, scanCallback);

    startTimeout(State.INITIALIZED, new Runnable() {
      @Override
      public void run() {
        scanner.stopScan(scanCallback);
      }
    });
  }

  private void connect() {
    assertState(State.DISCONNECTED);

    Log.d(TAG, "Connecting to device: " + device);
    state = State.CONNECTING;

    gatt = device.connectGatt(this, true, gattCallback);

    startTimeout(State.DISCONNECTED, null);
  }

  private void close() {
    Log.d(TAG, "Closing.");

    scanner.stopScan(scanCallback);

    if (gatt != null) {
      gatt.close();
      gatt = null;
    }

    state = State.INITIALIZED;
  }

  private void discover() {
    assertState(State.CONNECTED);

    if (gatt.discoverServices()) {
      Log.d(TAG, "Discovering.");
      state = State.DISCOVERING;
    } else {
      Log.e(TAG, "Failed to start discovery.");
      retryFromHere();
    }
  }

  private boolean send(String command) {
    if (state != State.READY) {
      retryFromHere();
      return false;
    }

    Log.d(TAG, "Sending command: " + command);

    BluetoothGattService gattService = gatt.getService(UART_SERVICE_UUID);
    BluetoothGattCharacteristic txCharacteristic =
        gattService.getCharacteristic(TX_CHARACTERISTIC_UUID);

    txCharacteristic.setValue(command);
    gatt.writeCharacteristic(txCharacteristic);

    return true;
  }

  private void assertState(State... expected) {
    if (!Arrays.asList(expected).contains(state)) {
      throw new IllegalStateException(String.format("Expected %s, but was %s.",
          Arrays.toString(expected), state));
    }
  }

  private void startTimeout(final State failState, final Runnable cancel) {
    final State startState = state;
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        if (state == startState) {
          Log.w(TAG, String.format("Timed out after %d ms in %s.", TIMEOUT_MILLIS, startState));
          if (cancel != null) {
            cancel.run();
          }
          state = failState;
          retryFromHere();
        }
      }
    }, TIMEOUT_MILLIS);
  }

  private void retryFromHere() {
    Log.d(TAG, String.format("Retrying from %s in %d ms.", state, RETRY_DELAY_MILLIS));

    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        switch (state) {
          case INITIALIZED:
            scan();
            break;
          case SCANNING:
            // Wait.
            break;
          case DISCONNECTED:
            connect();
            break;
          case CONNECTING:
            // Wait.
            break;
          case CONNECTED:
            discover();
            break;
          case DISCOVERING:
            // Wait.
            break;
          case READY:
            // Done.
            break;
          default:
            throw new IllegalArgumentException("Not handling state: " + state);
        }
      }
    }, RETRY_DELAY_MILLIS);
  }
}
