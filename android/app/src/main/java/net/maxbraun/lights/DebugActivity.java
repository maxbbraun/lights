package net.maxbraun.lights;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class DebugActivity extends AppCompatActivity {
  private static final String TAG = DebugActivity.class.getSimpleName();

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
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_debug);

    bindService(new Intent(this, LightsService.class), lightsServiceConnection, BIND_AUTO_CREATE);
  }

  @Override
  protected void onDestroy() {
    unbindService(lightsServiceConnection);

    super.onDestroy();
  }

  public void allWhite(View view) {
    // TODO
  }

  public void allOff(View view) {
    // TODO
  }
}
