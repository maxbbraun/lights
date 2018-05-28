# Lights

Design and source code for controlling a ring of LEDs via Bluetooth LE.

[![Lights](https://lh3.googleusercontent.com/OH-w1xSj1Izd6sZjvl7yN8z4loEr-TtxTMGyBkxrAWB8aVCR_1P1bgZP87e-iTz7AI-fO6aQDLEq9TfNImQdnvESX8K-FRoaah8Sq4hlK0Nyo62g9fTs3O74HnY6BqOsH63H1smmuuYFLxkWXhN1yIb-vph0FejwWkcYlYpUp26rqqpfP_66ML47X5aLWl45y6apSQJnS3IHBTteo1aIPQbsQtNed7v7gmu3i6EyftmNMkCXEgHbV8E8CrvuyKLPfkxuhgRE9_n5xQ6uShSrOeGUAmMZ88jVcne4gEvjxelUNOoSvvJ1Yf8akVHQakbcQHLiaroN5_ZuCSaUzDRd_VGEm8N515QW3x-FYdhsQo3LX57up2_ZPTjCQpcxfLdOJZYJxRMEvOGxPzM0emi3sQJNrnBaNzmKYRhM41BXRGKFLwxtH45OZ5nMy5uJ2OLf4B7ML2aer6Rd9WjnHQLbAnhJrVQqAxdB0ISabiNOYgu6fz7eKJdjoQnIeVtkRtVRtOgh8cN34pcDI7yhG2xamWLVqoZYB35h_m1k2uW9g0TEL8vukYu2mYN-Pywdb6Agws1_QNS-65bpldCgp-kFmZHUC3LGVxOQqXzfKmZBzpI4ogQq4ZDUb4FgBRg-hUEcVwym4bTWSlyRsE2kdYsJZ3QHh7PRDdSgFA=s400-no) ![PCB](https://lh3.googleusercontent.com/q0JcapdU0bliwthUc7-MfXul34FklFc5FOGdG2OoW2a702v8seCa_gv5A9XVeKo3ftw1HZVIeS0q0w1uuovP8n692gFbc0G7A-buXkGg2QrIU32h83A_yyXoing1lGUtzOtQVXu9JONxUjsV3ySe_qzWF8SwJJEZrknAeeBcjf_tAjibWLAuhW3w0tGxOAqAfBCY8XkaiOIqz5QcDVh7gyAIaj3yfpAgp4QHp07Txp8Ug6SKcxGkIQv0fE9TMc4Fze-fXT7EL7S9dbgw8EMy4SSn_qVUI_nF1bjZYigcCW5WARjNZ-IqDSDHjmejYeaiPs7--PM-PwtP70Fbq6mZJgyBMuux18TDOqLjVUTHRNe0mk17PQFGMlmP3na_5ngFvl3O2_PAyeNfzSoJkyC6bnEweBNuYPfRhtJg3ABBLme0o4eHat7ZXe7wTiBuzlA0eobJlO4XL8aKMTLmQiJRrkpwFrReaBBC_E-xJc_0whvqSKZ2xzFSPC3HSHKsXK7rMIqIMaZ2Zmq4FHGlFanPf2KGpIaCxjUXs-tKPbg9RX4ZwxY1OOpp9nzCpwWJO0jii4F53q-o69O9fPIGVguUJdj3A4MiW8K2wgwtRIU8gNvtXdz6ZfR3bkmq9r5v0URMV8URdalhvvPeKusA7EgLC7QgfvLbtkj4uQ=s400-no)](https://medium.com/@maxbraun/smarter-mirrors-and-how-theyre-made-327997b9eff7)

## Eagle

The light ring PCB design is defined in an [Eagle project](eagle) ready for fabrication. It uses [APA102 5050 RGB LEDs](https://www.adafruit.com/product/2343).

## Arduino

The PCB (5V) is connected to an [Adafruit Feather with BLE](https://www.adafruit.com/product/2829) (3.3V) with a [logic level converter](https://www.adafruit.com/product/757). After [board setup](https://learn.adafruit.com/adafruit-feather-32u4-bluefruit-le/setup) select `Adafruit Feather 32u4` as the board and `USBtinyISP` as the programmer.

The [Arduino code](arduino/lights.ino) contains the [pin definitions](arduino/lights.ino#L19) and has three library dependencies:
* [APA102](https://github.com/pololu/apa102-arduino#software)
* [Adafruit Bluefruit LE](https://github.com/adafruit/Adafruit_BluefruitLE_nRF51)
* [FastGPIO](https://github.com/pololu/fastgpio-arduino)

## Android

The [Android Studio project](android) builds an apk with a background service maintaining the BLE connection.

Bind to the service from another app and send commands using the [AIDL](android/app/src/main/aidl/net/maxbraun/lights) interface.

You can also use the [debug UI](android/app/src/main/java/net/maxbraun/lights/DebugActivity.java) or send intents to the [debug service](android/app/src/main/java/net/maxbraun/lights/DebugService.java):

```
adb shell am startservice net.maxbraun.lights/.DebugService
adb shell am broadcast -a net.maxbraun.lights.ALL_WHITE
```

## License

Copyright 2017 Max Braun

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
