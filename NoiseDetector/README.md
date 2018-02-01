# Noise Detector

This project communicate with nRF51 [noise_detector](https://github.com/JoonDong2/nRF51/tree/master/noise_detector) project. To do this, this extends `BleProfile abstract classes`, implementing `Noise Detetoer service` client. 

This app is similer to [HTS](https://github.com/NordicSemiconductor/Android-nRF-Toolbox/tree/master/app/src/main/java/no/nordicsemi/android/nrftoolbox/hts) app except that `HTSService` communicate with `HTSActivity` by using `Broadcast` functionality in the `HTS` app but, `NDSService` communicate with `NDSActivity` by using `interface` for performance this project

Major functionalities run on `NDSService` finally extending `Service` so, the connection is not disconnected on the background.

Received data is processed in the `BleDataWrappingThread` of `NDSService` then sent to `WaveData` of `NDSActivity`.

## Flow chart

You can see the flow chart of this project in [this post](http://joondong.tistory.com/54?category=684945)

## Video

You can see the video showing that this project communicate with nRF noise_detector in [this post](http://joondong.tistory.com/28?category=651762)

Above blog is written in Korean, but I have plan to translate to English.