package net.maxbraun.lights;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RestartReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    context.startService(new Intent(context, LightsService.class));
    context.startService(new Intent(context, DebugService.class));
  }
}
