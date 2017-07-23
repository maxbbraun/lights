// ILightsServiceCallback.aidl
package net.maxbraun.lights;

interface ILightsServiceCallback {

  /** Called when the command has been sent successfully. */
  void onSuccess();

  /** Called when the command failed to send. */
  void onError();
}
