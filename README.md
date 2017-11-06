# Lights

LED light ring

## Arduino

Adafruit Feather 32u4 Bluefruit LE

[Board setup](https://learn.adafruit.com/adafruit-feather-32u4-bluefruit-le/setup)

[APA102 library](https://github.com/pololu/apa102-arduino#software)

[Adafruit Bluefruit LE library](https://github.com/adafruit/Adafruit_BluefruitLE_nRF51)

[FastGPIO library](https://github.com/pololu/fastgpio-arduino)

[`lights.ino`](lights.ino)

## Android

```
adb shell am startservice net.maxbraun.lights/.DebugService
adb shell am broadcast -a net.maxbraun.lights.ALL_WHITE
```
