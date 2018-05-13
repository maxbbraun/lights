// ILightsService.aidl
package net.maxbraun.lights;

import net.maxbraun.lights.ILightsServiceCallback;

interface ILightsService {

  /** Set all LEDs to white at full brightness. */
  void allWhite(ILightsServiceCallback callback);

  /** Turn all LEDs off. */
  void allOff(ILightsServiceCallback callback);

  /** Set the top one LED to red at full brightness. */
  void oneRed(ILightsServiceCallback callback);

  /** Set the top one LED to blue at full brightness. */
  void oneBlue(ILightsServiceCallback callback);
}
