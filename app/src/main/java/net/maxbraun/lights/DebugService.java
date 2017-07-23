package net.maxbraun.lights;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class DebugService extends Service {
  private static final String TAG = DebugService.class.getSimpleName();

  private static final String ACTION_ALL_WHITE = "net.maxbraun.lights.ALL_WHITE";
  private static final String ACTION_ALL_OFF = "net.maxbraun.lights.ALL_OFF";
  private static final String ACTION_ONE_RED = "net.maxbraun.lights.ONE_RED";
  private static final String ACTION_ONE_BLUE = "net.maxbraun.lights.ONE_BLUE";

  private final BroadcastReceiver debugReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      Log.d(TAG, "Received action: " + action);

      if (ACTION_ALL_WHITE.equals(action)) {
        allWhite();
      } else if (ACTION_ALL_OFF.equals(action)) {
        allOff();
      } else if (ACTION_ONE_RED.equals(action)) {
        oneRed();
      } else if (ACTION_ONE_BLUE.equals(action)) {
        oneBlue();
      }
    }
  };

  ILightsService lightsService;
  private ServiceConnection lightsServiceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
      Log.d(TAG, "Lights service connected.");
      lightsService = ILightsService.Stub.asInterface(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
      Log.d(TAG, "Lights service disconnected.");
      lightsService = null;
    }
  };

  @Override
  public void onCreate() {
    super.onCreate();

    bindService(new Intent(this, LightsService.class), lightsServiceConnection, BIND_AUTO_CREATE);

    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(ACTION_ALL_WHITE);
    intentFilter.addAction(ACTION_ALL_OFF);
    intentFilter.addAction(ACTION_ONE_RED);
    intentFilter.addAction(ACTION_ONE_BLUE);
    registerReceiver(debugReceiver, intentFilter);
  }

  @Override
  public void onDestroy() {
    unregisterReceiver(debugReceiver);

    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  private void allWhite() {
    if (lightsService == null) {
      Log.e(TAG, "Lights service not connected.");
      return;
    }

    try {
      lightsService.allWhite(createCallback());
    } catch (RemoteException e) {
      Log.e(TAG, "Failed to talk to lights service.");
    }
  }

  private void allOff() {
    if (lightsService == null) {
      Log.e(TAG, "Service not connected.");
      return;
    }

    try {
      lightsService.allOff(createCallback());
    } catch (RemoteException e) {
      Log.e(TAG, "Failed to talk to lights service.");
    }
  }

  private void oneRed() {
    if (lightsService == null) {
      Log.e(TAG, "Service not connected.");
      return;
    }

    try {
      lightsService.oneRed(createCallback());
    } catch (RemoteException e) {
      Log.e(TAG, "Failed to talk to lights service.");
    }
  }

  private void oneBlue() {
    if (lightsService == null) {
      Log.e(TAG, "Service not connected.");
      return;
    }

    try {
      lightsService.oneBlue(createCallback());
    } catch (RemoteException e) {
      Log.e(TAG, "Failed to talk to lights service.");
    }
  }

  private ILightsServiceCallback createCallback() {
    return new ILightsServiceCallback.Stub() {
      @Override
      public void onSuccess() throws RemoteException {
        Log.d(TAG, "Sent command.");
      }

      @Override
      public void onError() throws RemoteException {
        Log.d(TAG, "Failed to send command.");
      }
    };
  }
}
