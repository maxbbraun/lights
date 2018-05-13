# Lights

Design and source code for controlling a ring of LEDs via Bluetooth LE.

## Eagle

The light ring PCB design is defined in an [Eagle project](eagle) ready for fabrication.

## Arduino

The PCB is connected to an [Adafruit Feather with BLE](https://www.adafruit.com/product/2829). After [board setup](https://learn.adafruit.com/adafruit-feather-32u4-bluefruit-le/setup) select `Adafruit Feather 32u4` as the board and `USBtinyISP` as the programmer.

The [Arduino code](arduino/lights.ino) has three library dependencies:
* [APA102](https://github.com/pololu/apa102-arduino#software)
* [Adafruit Bluefruit LE](https://github.com/adafruit/Adafruit_BluefruitLE_nRF51)
* [FastGPIO](https://github.com/pololu/fastgpio-arduino)

## Android

The [Android studio project](android) builds an apk with a background service maintaining the BLE connection.

Bind to the service from another app and send commands using the [AIDL](android/app/src/main/aidl/net/maxbraun/lights) interface.

You can also use the [debug UI](android/app/src/main/java/net/maxbraun/lights/DebugActivity.java) or send intents to the [debug service](android/app/src/main/java/net/maxbraun/lights/DebugService.java):

```
adb shell am startservice net.maxbraun.lights/.DebugService
adb shell am broadcast -a net.maxbraun.lights.ALL_WHITE
```
