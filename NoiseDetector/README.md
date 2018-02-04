# Noise Detector

This project communicate with nRF51 [noise_detector](https://github.com/JoonDong2/nRF51/tree/master/noise_detector) project. To do this, this extends `BleProfile abstract classes`, implementing `Noise Detetoer service` **client**. There are more details aoub `Noise Detector service` in above link.

This app is similer to [HTS](https://github.com/NordicSemiconductor/Android-nRF-Toolbox/tree/master/app/src/main/java/no/nordicsemi/android/nrftoolbox/hts) app except that `HTSService` communicate with `HTSActivity` by using `Broadcast` functionality, but `NDSService` communicate with `NDSActivity` by using `interface` for performance in this project.

Major functionalities run on `NDSService` finally extending `Service` so, the connection is not disconnected on the background.

Received data is processed in the `BleDataWrappingThread` of `NDSService` then sent to [WaveData](https://github.com/JoonDong2/Android/tree/master/WaveData) module of `NDSActivity`.

Noise Detector service is composed of two characteristics (Detected Noise Value characteristic and Noise Detector Service Control Point characteristic.)

`Base UUID` f673**XXXX**-0994-4967-bdf9-5e7702990a50

`Noise Detector Service UUID` **8d00**

`Detected Value Characteristic` **8d01**

`Noise Detector Controlpoint Characteristic` **8d02**


## Detected Noise Value characteristic

Data collected through ADC is written to this characteristic's value attribute in the **server**(nRF51 noise_detector) and broadcasted to the **client**(this project).

## Noise Detector Service Control Point characteristic.

This characteristic can be written or broadcasted, not read.

Value attribute of this is composed of two bytes.

`index 0` Command code

>0x0 : Reserved
>
>0x1 : Request turn off nRF51 noise_detector
>
>0x2 : Request turn on noise detector


`index 1` Result code

>0x0 : Reserved
>
>0x1 : SUCCESS
>
>0x2 : FAIL

Client(this project) only populates `index 0` and send it to the server(this project).

Then, server(nRF51 noise_detector) executes an operation according to `Command code`, populating the result and back it to client again. 

You can see more details in [this post](http://joondong.tistory.com/46)

## Flow chart

You can see the flow chart of this project in [this post](http://joondong.tistory.com/54?category=684945)

## Video

You can see the video showing that this project communicate with nRF noise_detector at the bottom of [this post](http://joondong.tistory.com/28?category=651762)

Above blog is written in Korean, but I have plan to translate to English.