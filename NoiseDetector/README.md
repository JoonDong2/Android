# Noise Detector

This project extends `BleProfile abstract classes`, implementing `Noise Detetoer service` client. This is similer to [HTS](https://github.com/NordicSemiconductor/Android-nRF-Toolbox/tree/master/app/src/main/java/no/nordicsemi/android/nrftoolbox/hts) app.

HTSService communicat with HTSActivity by using `Broadcast` functionality in the `HTS` app but, NDSService communicate with NDSActivity by using `interface` for performance this project

Major functionalities run on `NDSService` extending `Service` finally so, the connection is not disconnected on the background.

Received data is processed in the `BleDataWrappingThread` of `NDSService` then sent to `WaveData` of `NDSActivity`.

You can see the flow chart of this project in [this post](http://joondong.tistory.com/54?category=684945).