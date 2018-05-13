#include <Arduino.h>
#include <APA102.h>
#include <Adafruit_BluefruitLE_SPI.h>

#include <FastGPIO.h>
#define APA102_USE_FAST_GPIO

// BLE pins.
#define BLUEFRUIT_SPI_CS 8
#define BLUEFRUIT_SPI_IRQ 7
#define BLUEFRUIT_SPI_RST 4

// Commands.
#define CMD_ALL_WHITE "AW"
#define CMD_ALL_OFF "A0"
#define CMD_ONE_RED "1R"
#define CMD_ONE_BLUE "1B"

// LED pins.
const uint8_t dataPin = 5;
const uint8_t clockPin = 6;

// LED constants.
const uint16_t ledCount = 32;

// LED state.
rgb_color* colors;
rgb_color allWhiteColors[ledCount];
rgb_color oneRedColors[ledCount];
rgb_color oneBlueColors[ledCount];
uint16_t brightness = 0;

Adafruit_BluefruitLE_SPI ble(BLUEFRUIT_SPI_CS, BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST);
APA102<dataPin, clockPin> leds;

bool bleBufferEquals(const char* str) {
  return (strcmp(ble.buffer, str) == 0);
}

void allWhite() {
  colors = allWhiteColors;
  brightness = 31;
}

void allOff() {
  colors = allWhiteColors;
  brightness = 0;
}

void oneRed() {
  colors = oneRedColors;
  brightness = 7;
}

void oneBlue() {
  colors = oneBlueColors;
  brightness = 7;
}

void setup() {
  // Initialize BLE.
  ble.begin(false);
  ble.echo(false);
  ble.verbose(false);
  while (!ble.isConnected()) {
    delay(100);
  }

  // Initialize LED colors.
  for (uint16_t i = 0; i < ledCount; i++) {
    allWhiteColors[i].red = 255;
    allWhiteColors[i].green = 255;
    allWhiteColors[i].blue = 255;

    // Only the top LED.
    if (i == 24) {
      oneRedColors[i].red = 255;
      oneBlueColors[i].blue = 255;
    } else {
      oneRedColors[i].red = 0;
      oneBlueColors[i].blue = 0;
    }
    oneRedColors[i].green = 0;
    oneRedColors[i].blue = 0;
    oneBlueColors[i].red = 0;
    oneBlueColors[i].green = 0;
  }

  // Start with LEDs off.
  allOff();
}

void loop() {
  // Always write the LED state.
  leds.write(colors, ledCount, brightness);
  
  // Receive data.
  ble.println("AT+BLEUARTRX");
  ble.readline();
  if (!bleBufferEquals("OK")) {
    // Parse data.
    if (bleBufferEquals(CMD_ALL_WHITE)) {
      allWhite();
    } else if (bleBufferEquals(CMD_ALL_OFF)) {
      allOff();
    } else if (bleBufferEquals(CMD_ONE_RED)) {
      oneRed();
    } else if (bleBufferEquals(CMD_ONE_BLUE)) {
      oneBlue();
    }
    ble.waitForOK();
  }
}

