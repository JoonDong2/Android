# Android projects

You can see details in each project path.

## Noise Detector

This project communicate with [nRF51 noise_detector](https://github.com/JoonDong2/nRF51/tree/master/noise_detector) project by implementing [BleProfile abstract classes](https://github.com/NordicSemiconductor/Android-nRF-Toolbox/tree/master/app/src/main/java/no/nordicsemi/android/nrftoolbox/profile), also use WaveData project to animate data received from peer(nRF51 noise_detector)

## WaveData

This project animates bars up and down according to data received.

## AWSLEDButton

This project, implemented on AWS Android SDK, communicate with [**AWS/esp32_led_button**](https://github.com/JoonDong2/AWS/tree/master/esp32_led_button) through AWS IoT and is responsible for client in the mqtt protocol.