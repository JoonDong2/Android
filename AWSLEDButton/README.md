# AWS LED Button Controller

This project, implemented on AWS Android SDK, communicate with [**AWS/esp32_led_button**](https://github.com/JoonDong2/AWS/tree/master/esp32_led_button) through AWS IoT and is responsible for client in the mqtt protocol.

To do this, subscribing, publishing and getting/updating shadow of the [AWS Android IoT SDK](https://github.com/aws/aws-sdk-android/tree/master/aws-android-sdk-iot/src/main/java/com/amazonaws/mobileconnectors/iot) are used.

## Video

You can see the video of the entire projects, including Android app client at the bottom of [this post](http://joondong.tistory.com/61?category=651762).  
This blog is written Korean, but I have plan to translate to English.

# Provisioning

This project requires Cognito User Pool and Cognito Identities Pool(ID Pool) to authorize to AWS IoT in order to get temporary credentials in the Security Token Service(STS).

Unlike [AndroidSubPusWebSoket](https://github.com/awslabs/aws-sdk-android-samples/tree/master/AndroidPubSubWebSocket) example, a signed-in user through the Cognito User Pool assumes `authenticated role` of ID Pool.

To do this, your User Pool should be linked to your ID Pool, and enter private information below in the [AppHelper](https://github.com/JoonDong2/Android/blob/master/AWSLEDButton/app/src/main/java/com/amazonaws/youruserpools/AppHelper.java).

`USER_POOL_ID` <- Your Cognito User Pool ID  
`CLIENT_ID` <- App Client ID of your User Pool  
`CLIENT_SECRET` <- App Client Secret of your User Pool  
`COGNITO_POOL_ID` <- Your Cognito Identities Pool ID  
`CUSTOMER_SPECIFIC_ENDPOIN` <- Your Custom Endpoint of AWS IoT  
`MY_REGION` <- Your Region as REGIONS instatnce  

