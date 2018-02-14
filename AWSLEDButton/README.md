# AWS LED Button Controller

This project, implemented on AWS Android SDK, communicate with [**AWS/esp32_led_button**](https://github.com/JoonDong2/AWS/tree/master/esp32_led_button) through AWS IoT and is responsible for client in the mqtt protocol.

To do this, subscribing, publishing and getting/updating shadow of the [AWS Android IoT SDK](https://github.com/aws/aws-sdk-android/tree/master/aws-android-sdk-iot/src/main/java/com/amazonaws/mobileconnectors/iot) are used.

## Video

You can see the video of the entire projects, including Android app client at the bottom of [this post](http://joondong.tistory.com/61?category=651762).  
This blog is written Korean, but I have plan to translate to English.

# Provisioning

This project requires Cognito User Pool and Cognito Identities Pool(ID Pool) to authorize to AWS IoT in order to get temporary credentials in the Security Token Service(STS).

Unlike [AndroidSubPusWebSoket](https://github.com/awslabs/aws-sdk-android-samples/tree/master/AndroidPubSubWebSocket) example, a signed-in user through your User Pool assumes `authenticated role` of ID Pool.

To do this, you should follow the procedure below.

## 1. create User Pool and ID Pool in the Cognito, and link User Pool to your ID Pool, and configure `authenticated role` and trusted relationship of your ID Pool in the IAM like below.

### `Authenticated Role`'s Policies
<pre><code>
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "iot:Connect",
      "Resource": "arn:aws:iot:us-east-1:<account id>:client/LEDButton*"
    },
    {
      "Effect": "Allow",
      "Action": "iot:Receive",
      "Resource": [
        "arn:aws:iot:us-east-1:<account id>:topic/LEDButton*/result",
        "arn:aws:iot:us-east-1:<account id>:topic/$aws/things/LEDButton*/shadow/get/accepted",
        "arn:aws:iot:us-east-1:<account id>:topic/$aws/things/LEDButton*/shadow/get/rejected"
      ]
    },
    {
      "Effect": "Allow",
      "Action": "iot:Subscribe",
      "Resource": [
        "arn:aws:iot:us-east-1:<account id>:topicfilter/LEDButton*/result",
        "arn:aws:iot:us-east-1:<account id>:topicfilter/$aws/things/LEDButton*/shadow/get/accepted",
        "arn:aws:iot:us-east-1:<account id>:topicfilter/$aws/things/LEDButton*/shadow/get/rejected"
      ]
    },
    {
      "Effect": "Allow",
      "Action": "iot:Publish",
      "Resource": [
        "arn:aws:iot:us-east-1:<account id>:topic/LEDButton*/command",
        "arn:aws:iot:us-east-1:<account id>:topic/$aws/things/LEDButton*/shadow/get"
      ]
    },
    {
      "Effect": "Allow",
      "Action": "iot:GetThingShadow",
      "Resource": "arn:aws:iot:us-east-1:<account id>:thing/LEDButton*"
    },
    {
       "Effect": "Allow",
       "Action": [
           "iot:AttachPrincipalPolicy",
           "iot:DetachPrincipalPolicy"
       ],
       "Resource": "*"
     }
  ]
}
</code></pre>

### Trusted Relationship
<pre><code>
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "cognito-identity.amazonaws.com"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "cognito-identity.amazonaws.com:aud": "us-east-1:<user pool id>"
        },
        "ForAnyValue:StringLike": {
          "cognito-identity.amazonaws.com:amr": "authenticated"
        }
      }
    },
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "iot.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
</code></pre>

## 2. create a policy named `LEDCtrPolicy` like below. This is attached temperary credentials about `authenticate role` of the Cognito ID Pool because the credentials created ouside of AWS IoT must get permissions about IoT access as a duplicate.

### Policy
<pre><code>
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "iot:Connect",
      "Resource": "arn:aws:iot:us-east-1:<account id>:client/LEDButton*"
    },
    {
      "Effect": "Allow",
      "Action": "iot:Receive",
      "Resource": [
        "arn:aws:iot:us-east-1:<account id>:topic/LEDButton*/result",
        "arn:aws:iot:us-east-1:<account id>:topic/$aws/things/LEDButton*/shadow/get/accepted",
        "arn:aws:iot:us-east-1:<account id>:topic/$aws/things/LEDButton*/shadow/get/rejected"
      ]
    },
    {
      "Effect": "Allow",
      "Action": "iot:Subscribe",
      "Resource": [
        "arn:aws:iot:us-east-1:<account id>:topicfilter/LEDButton*/result",
        "arn:aws:iot:us-east-1:<account id>:topicfilter/$aws/things/LEDButton*/shadow/get/accepted",
        "arn:aws:iot:us-east-1:<account id>:topicfilter/$aws/things/LEDButton*/shadow/get/rejected"
      ]
    },
    {
      "Effect": "Allow",
      "Action": "iot:Publish",
      "Resource": [
        "arn:aws:iot:us-east-1:<account id>:topic/LEDButton*/command",
        "arn:aws:iot:us-east-1:<account id>:topic/$aws/things/LEDButton*/shadow/get"
      ]
    },
    {
      "Effect": "Allow",
      "Action": "iot:GetThingShadow",
      "Resource": "arn:aws:iot:us-east-1:<account id>:thing/LEDButton*"
    }
  ]
}
</code></pre>

enter private information below in the [AppHelper](https://github.com/JoonDong2/Android/blob/master/AWSLEDButton/app/src/main/java/com/amazonaws/youruserpools/AppHelper.java).

`USER_POOL_ID` <- Your Cognito User Pool ID  
`CLIENT_ID` <- App Client ID of your User Pool  
`CLIENT_SECRET` <- App Client Secret of your User Pool  
`COGNITO_POOL_ID` <- Your Cognito Identities Pool ID  
`CUSTOMER_SPECIFIC_ENDPOIN` <- Your Custom Endpoint of AWS IoT  
`MY_REGION` <- Your Region as REGIONS instatnce  

