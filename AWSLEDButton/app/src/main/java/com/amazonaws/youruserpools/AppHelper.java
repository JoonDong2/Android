/*
 * Copyright 2013-2017 Amazon.com,
 * Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Amazon Software License (the "License").
 * You may not use this file except in compliance with the
 * License. A copy of the License is located at
 *
 *      http://aws.amazon.com/asl/
 *
 * or in the "license" file accompanying this file. This file is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, express or implied. See the License
 * for the specific language governing permissions and
 * limitations under the License.
 */

package com.amazonaws.youruserpools;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentityprovider.AmazonCognitoIdentityProvider;
import com.amazonaws.services.cognitoidentityprovider.AmazonCognitoIdentityProviderClient;
import com.amazonaws.services.cognitoidentityprovider.model.AttributeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppHelper {
    private static final String TAG = "AppHelper";
    // App settings

    private static List<String> attributeDisplaySeq;
    private static Map<String, String> signUpFieldsC2O;
    private static Map<String, String> signUpFieldsO2C;

    private static AppHelper appHelper;
    private static CognitoUserPool userPool;
    private static String user;
    private static CognitoDevice newDevice;

    private static CognitoUserAttributes attributesChanged;
    private static List<AttributeType> attributesToDelete;

    private static List<ItemToDisplay> currDisplayedItems;
    private static  int itemCount;

    private static List<ItemToDisplay> trustedDevices;
    private static int trustedDevicesCount;
    private static List<CognitoDevice> deviceDetails;
    private static CognitoDevice thisDevice;
    private static boolean thisDeviceTrustState;

    private static List<ItemToDisplay> firstTimeLogInDetails;
    private static Map<String, String> firstTimeLogInUserAttributes;
    private static List<String> firstTimeLogInRequiredAttributes;
    private static int firstTimeLogInItemsCount;
    private static Map<String, String> firstTimeLogInUpDatedAttributes;
    private static String firstTimeLoginNewPassword;

    private static List<ItemToDisplay> mfaOptions;
    private static List<String> mfaAllOptionsCode;

    private static CognitoCachingCredentialsProvider credentialsProvider;

    // Change the next three lines of code to run this demo on your user pool

    /**
     * Add your pool id here
     */
    private static final String userPoolId = Set your Cognito User Pool ID;

    /**
     * Add you app id : Your client id here
     */
    private static final String clientId = Set your App Client ID of User Pool;

    /**
     * App secret associated with your app id - if the App id does not have an associated App secret,
     * set the App secret to null.
     * e.g. clientSecret = null;
     */
    private static final String clientSecret = Set your App Client Secret of User Pool;

    /**
     * Set Your User Pools region.
     * e.g. if your user pools are in US East (N Virginia) then set cognitoRegion = Regions.US_EAST_1.
     */

    // Cognito pool ID. For this app, pool needs to be unauthenticated pool with
    // AWS IoT permissions.
    private static final String COGNITO_POOL_ID = Set your Cognito ID Pool ID;

    // Customer specific IoT endpoint
    // AWS Iot CLI describe-endpoint call returns: XXXXXXXXXX.iot.<region>.amazonaws.com,
    private static final String CUSTOMER_SPECIFIC_ENDPOINT = Set your IoT Custom Endpoint;
    // Name of the AWS IoT policy to attach to a newly created certificate
    private static final String AWS_IOT_BASIC_POLICY_NAME = "LEDCtrPolicy";
    private static final String AWS_IOT_BASIC_THING_NAME = "LEDButton";

    // Region of AWS IoT
    private static final Regions MY_REGION = Set your Region as the Regions instance.

    // User details from the service
    private static CognitoUserSession currSession;
    private static CognitoUserDetails userDetails;

    // User details to display - they are the current values, including any local modification
    private static boolean phoneVerified;
    private static boolean emailVerified;

    private static boolean phoneAvailable;
    private static boolean emailAvailable;

    private static Set<String> currUserAttributes;

    private static boolean iotDisconnectionStatus; // true : connected, false : disconnected
    private static boolean awsLEDStatus; // true : on, false : off

    public static void init(Context context) {
        setData();

        if (appHelper != null && userPool != null) {
            return;
        }

        if (appHelper == null) {
            appHelper = new AppHelper();
        }

        if (userPool == null) {

            // Create a user pool with default ClientConfiguration
            //userPool = new CognitoUserPool(context, userPoolId, clientId, clientSecret, MY_REGION);

            // This will also work
            ClientConfiguration clientConfiguration = new ClientConfiguration();
            AmazonCognitoIdentityProvider cipClient = new AmazonCognitoIdentityProviderClient(new AnonymousAWSCredentials(), clientConfiguration);
            cipClient.setRegion(Region.getRegion(MY_REGION));
            userPool = new CognitoUserPool(context, userPoolId, clientId, clientSecret, cipClient);
        }

        phoneVerified = false;
        phoneAvailable = false;
        emailVerified = false;
        emailAvailable = false;

        currUserAttributes = new HashSet<String>();
        currDisplayedItems = new ArrayList<ItemToDisplay>();
        trustedDevices = new ArrayList<ItemToDisplay>();
        firstTimeLogInDetails = new ArrayList<ItemToDisplay>();
        firstTimeLogInUpDatedAttributes= new HashMap<String, String>();

        newDevice = null;
        thisDevice = null;
        thisDeviceTrustState = false;

        mfaOptions = new ArrayList<ItemToDisplay>();

        credentialsProvider = new CognitoCachingCredentialsProvider(
                context, // context
                COGNITO_POOL_ID, // Identity Pool ID
                MY_REGION // Region
        );
    }

    public static CognitoUserPool getPool() {
        return userPool;
    }

    public static Map<String, String> getSignUpFieldsC2O() {
        return signUpFieldsC2O;
    }

    public static  Map<String, String> getSignUpFieldsO2C() {
        return signUpFieldsO2C;
    }

    public static List<String> getAttributeDisplaySeq() {
        return attributeDisplaySeq;
    }

    public static void setCurrSession(CognitoUserSession session) {
        currSession = session;
    }

    public static  CognitoUserSession getCurrSession() {
        return currSession;
    }

    public static void setUserDetails(CognitoUserDetails details) {
        userDetails = details;
        refreshWithSync();
    }

    public static  CognitoUserDetails getUserDetails() {
        return userDetails;
    }

    public static String getCurrUser() {
        return user;
    }

    public static void setUser(String newUser) {
        user = newUser;
    }

    public static boolean isPhoneVerified() {
        return phoneVerified;
    }

    public static boolean isEmailVerified() {
        return emailVerified;
    }

    public static boolean isPhoneAvailable() {
        return phoneAvailable;
    }

    public static boolean isEmailAvailable() {
        return emailAvailable;
    }

    public static void setPhoneVerified(boolean phoneVerif) {
        phoneVerified = phoneVerif;
    }

    public static void setEmailVerified(boolean emailVerif) {
        emailVerified = emailVerif;
    }

    public static void setPhoneAvailable(boolean phoneAvail) {
        phoneAvailable = phoneAvail;
    }

    public static void setEmailAvailable(boolean emailAvail) {
        emailAvailable = emailAvail;
    }

    public static void clearCurrUserAttributes() {
        currUserAttributes.clear();
    }

    public static void addCurrUserattribute(String attribute) {
        currUserAttributes.add(attribute);
    }

    public static List<String> getNewAvailableOptions() {
        List<String> newOption = new ArrayList<String>();
        for(String attribute : attributeDisplaySeq) {
            if(!(currUserAttributes.contains(attribute))) {
                newOption.add(attribute);
            }
        }
        return  newOption;
    }

    public static String formatException(Exception exception) {
        String formattedString = "Internal Error";
        Log.e(TAG, " -- Error: "+exception.toString());
        Log.getStackTraceString(exception);

        String temp = exception.getMessage();

        if(temp != null && temp.length() > 0) {
            formattedString = temp.split("\\(")[0];
            if(temp != null && temp.length() > 0) {
                return formattedString;
            }
        }

        return  formattedString;
    }

    public  static  int getItemCount() {
        return itemCount;
    }

    public static int getDevicesCount() {
        return trustedDevicesCount;
    }

    public static int getFirstTimeLogInItemsCount() {
        return  firstTimeLogInItemsCount;
    }

    public  static ItemToDisplay getItemForDisplay(int position) {
        return  currDisplayedItems.get(position);
    }

    public static ItemToDisplay getDeviceForDisplay(int position) {
        if (position >= trustedDevices.size()) {
            return new ItemToDisplay(" ", " ", " ", Color.BLACK, Color.DKGRAY, Color.parseColor("#37A51C"), 0, null);
        }
        return trustedDevices.get(position);
    }

    public static ItemToDisplay getUserAttributeForFirstLogInCheck(int position) {
        return firstTimeLogInDetails.get(position);
    }

    public static void setUserAttributeForDisplayFirstLogIn(Map<String, String> currAttributes, List<String> requiredAttributes) {
        firstTimeLogInUserAttributes = currAttributes;
        firstTimeLogInRequiredAttributes = requiredAttributes;
        firstTimeLogInUpDatedAttributes = new HashMap<String, String>();
        refreshDisplayItemsForFirstTimeLogin();
    }

    public static void setUserAttributeForFirstTimeLogin(String attributeName, String attributeValue) {
        if (firstTimeLogInUserAttributes ==  null) {
            firstTimeLogInUserAttributes = new HashMap<String, String>();
        }
        firstTimeLogInUserAttributes.put(attributeName, attributeValue);
        firstTimeLogInUpDatedAttributes.put(attributeName, attributeValue);
        refreshDisplayItemsForFirstTimeLogin();
    }

    public static Map<String, String> getUserAttributesForFirstTimeLogin() {
        return firstTimeLogInUpDatedAttributes;
    }

    public static void setPasswordForFirstTimeLogin(String password) {
        firstTimeLoginNewPassword = password;
    }

    public static String getPasswordForFirstTimeLogin() {
        return firstTimeLoginNewPassword;
    }

    private static void refreshDisplayItemsForFirstTimeLogin() {
        firstTimeLogInItemsCount = 0;
        firstTimeLogInDetails = new ArrayList<ItemToDisplay>();

        for(Map.Entry<String, String> attr: firstTimeLogInUserAttributes.entrySet()) {
            if ("phone_number_verified".equals(attr.getKey()) || "email_verified".equals(attr.getKey())) {
                continue;
            }
            String message = "";
            if ((firstTimeLogInRequiredAttributes != null) && (firstTimeLogInRequiredAttributes.contains(attr.getKey()))) {
                message = "Required";
            }
            ItemToDisplay item = new ItemToDisplay(attr.getKey(), attr.getValue(), message, Color.BLACK, Color.DKGRAY, Color.parseColor("#329AD6"), 0, null);
            firstTimeLogInDetails.add(item);
            firstTimeLogInRequiredAttributes.size();
            firstTimeLogInItemsCount++;
        }

        for (String attr: firstTimeLogInRequiredAttributes) {
            if (!firstTimeLogInUserAttributes.containsKey(attr)) {
                ItemToDisplay item = new ItemToDisplay(attr, "", "Required", Color.BLACK, Color.DKGRAY, Color.parseColor("#329AD6"), 0, null);
                firstTimeLogInDetails.add(item);
                firstTimeLogInItemsCount++;
            }
        }
    }

    public static void newDevice(CognitoDevice device) {
        newDevice = device;
    }

    public static void setDevicesForDisplay(List<CognitoDevice> devicesList) {
        trustedDevicesCount = 0;
        thisDeviceTrustState = false;
        deviceDetails = devicesList;
        trustedDevices = new ArrayList<ItemToDisplay>();
        for(CognitoDevice device: devicesList) {
            if (thisDevice != null && thisDevice.getDeviceKey().equals(device.getDeviceKey())) {
                thisDeviceTrustState = true;
            } else {
                ItemToDisplay item = new ItemToDisplay("", device.getDeviceName(), device.getCreateDate().toString(), Color.BLACK, Color.DKGRAY, Color.parseColor("#329AD6"), 0, null);
                item.setDataDrawable("checked");
                trustedDevices.add(item);
                trustedDevicesCount++;
            }
        }
    }

    public static CognitoDevice getDeviceDetail(int position) {
        if (position <= trustedDevicesCount) {
            return deviceDetails.get(position);
        } else {
            return null;
        }
    }

    public static void setMfaOptionsForDisplay(List<String> options, Map<String, String> parameters) {
        mfaAllOptionsCode = options;
        mfaOptions = new ArrayList<ItemToDisplay>();
        String textToDisplay = "";
        for (String option: options) {
            if ("SMS_MFA".equals(option)) {
                textToDisplay = "Send SMS";
                if (parameters.containsKey("CODE_DELIVERY_DESTINATION")) {
                    textToDisplay = textToDisplay + " to "+ parameters.get("CODE_DELIVERY_DESTINATION");
                }
            } else if ("SOFTWARE_TOKEN_MFA".equals(option)) {
                textToDisplay = "Use TOTP";
                if (parameters.containsKey("FRIENDLY_DEVICE_NAME")) {
                    textToDisplay = textToDisplay + ": " + parameters.get("FRIENDLY_DEVICE_NAME");
                }
            }
            ItemToDisplay item = new ItemToDisplay("", textToDisplay, "", Color.BLACK, Color.DKGRAY, Color.parseColor("#329AD6"), 0, null);
            mfaOptions.add(item);
            textToDisplay = "Unsupported MFA";
        }
    }

    public static List<String> getAllMfaOptions() {
        return mfaAllOptionsCode;
    }

    public static String getMfaOptionCode(int position) {
        return mfaAllOptionsCode.get(position);
    }

    public static ItemToDisplay getMfaOptionForDisplay(int position) {
        if (position >= mfaOptions.size()) {
            return new ItemToDisplay(" ", " ", " ", Color.BLACK, Color.DKGRAY, Color.parseColor("#37A51C"), 0, null);
        }
        return mfaOptions.get(position);
    }

    public static int getMfaOptionsCount() {
        return mfaOptions.size();
    }

    //public static

    public static CognitoDevice getNewDevice() {
        return newDevice;
    }

    public static CognitoDevice getThisDevice() {
        return thisDevice;
    }

    public static void setThisDevice(CognitoDevice device) {
        thisDevice = device;
    }

    public static boolean getThisDeviceTrustState() {
        return thisDeviceTrustState;
    }

    public static void clearCredentialsProvider() {
        credentialsProvider.clear();
    }

    public static boolean updateCredentialsProvider() {
        String idToken = null;
        if(currSession != null) {
            idToken = currSession.getIdToken().getJWTToken();
            if(idToken == null) {
                return false;
            }
            Map<String, String> logins = new HashMap<String, String>();
            logins.put("cognito-idp.us-east-1.amazonaws.com/" + userPoolId, idToken);
            credentialsProvider.setLogins(logins);
            // TODO : necessary ??
            Thread refresh = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        credentialsProvider.refresh();
                    } catch (Exception e) {
                        Log.e(TAG, "Credential refresh error.", e);
                    }
                }
            });
            refresh.start();

            return true;
        }
        return false;
    }

    public static CognitoCachingCredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    public static Regions getRigion() {
        return MY_REGION;
    }

    public static String getBasicPolicyName() {
        return AWS_IOT_BASIC_POLICY_NAME;
    }

    public static String getBasicThingName() {
        return AWS_IOT_BASIC_THING_NAME;
    }

    public static String getAWSIoTEndpoint() {
        return CUSTOMER_SPECIFIC_ENDPOINT;
    }

    public static void setIoTDisconnectionStatus(boolean status) {
        iotDisconnectionStatus = status;
    }

    public static boolean getIoTDisconnectionStatus() {
        return iotDisconnectionStatus;
    }

    public static void setAWSLEDStatus(boolean status) {
        awsLEDStatus = status;
    }

    public static boolean getAWSLEDStatus() {
        return awsLEDStatus;
    }

    private static void setData() {
        // Set attribute display sequence
        attributeDisplaySeq = new ArrayList<String>();
        attributeDisplaySeq.add("given_name");
        attributeDisplaySeq.add("middle_name");
        attributeDisplaySeq.add("family_name");
        attributeDisplaySeq.add("nickname");
        attributeDisplaySeq.add("phone_number");
        attributeDisplaySeq.add("email");

        signUpFieldsC2O = new HashMap<String, String>();
        signUpFieldsC2O.put("Given name", "given_name");
        signUpFieldsC2O.put("Family name", "family_name");
        signUpFieldsC2O.put("Nick name", "nickname");
        signUpFieldsC2O.put("Phone number", "phone_number");
        signUpFieldsC2O.put("Phone number verified", "phone_number_verified");
        signUpFieldsC2O.put("Email verified", "email_verified");
        signUpFieldsC2O.put("Email","email");
        signUpFieldsC2O.put("Middle name","middle_name");

        signUpFieldsO2C = new HashMap<String, String>();
        signUpFieldsO2C.put("given_name", "Given name");
        signUpFieldsO2C.put("family_name", "Family name");
        signUpFieldsO2C.put("nickname", "Nick name");
        signUpFieldsO2C.put("phone_number", "Phone number");
        signUpFieldsO2C.put("phone_number_verified", "Phone number verified");
        signUpFieldsO2C.put("email_verified", "Email verified");
        signUpFieldsO2C.put("email", "Email");
        signUpFieldsO2C.put("middle_name", "Middle name");

    }

    private static void refreshWithSync() {
        // This will refresh the current items to display list with the attributes fetched from service
        List<String> tempKeys = new ArrayList<>();
        List<String> tempValues = new ArrayList<>();

        emailVerified = false;
        phoneVerified = false;

        emailAvailable = false;
        phoneAvailable = false;

        currDisplayedItems = new ArrayList<ItemToDisplay>();
        currUserAttributes.clear();
        itemCount = 0;

        for(Map.Entry<String, String> attr: userDetails.getAttributes().getAttributes().entrySet()) {

            tempKeys.add(attr.getKey());
            tempValues.add(attr.getValue());

            if(attr.getKey().contains("email_verified")) {
                emailVerified = attr.getValue().contains("true");
            }
            else if(attr.getKey().contains("phone_number_verified")) {
                phoneVerified = attr.getValue().contains("true");
            }

            if(attr.getKey().equals("email")) {
                emailAvailable = true;
            }
            else if(attr.getKey().equals("phone_number")) {
                phoneAvailable = true;
            }
        }

        // Arrange the input attributes per the display sequence
        Set<String> keySet = new HashSet<>(tempKeys);
        for(String det: attributeDisplaySeq) {
            if(keySet.contains(det)) {
                // Adding items to display list in the required sequence

                ItemToDisplay item = new ItemToDisplay(signUpFieldsO2C.get(det), tempValues.get(tempKeys.indexOf(det)), "",
                        Color.BLACK, Color.DKGRAY, Color.parseColor("#37A51C"),
                        0, null);

                if(det.contains("email")) {
                    if(emailVerified) {
                        item.setDataDrawable("checked");
                        item.setMessageText("Email verified");
                    }
                    else {
                        item.setDataDrawable("not_checked");
                        item.setMessageText("Email not verified");
                        item.setMessageColor(Color.parseColor("#E94700"));
                    }
                }

                if(det.contains("phone_number")) {
                    if(phoneVerified) {
                        item.setDataDrawable("checked");
                        item.setMessageText("Phone number verified");
                    }
                    else {
                        item.setDataDrawable("not_checked");
                        item.setMessageText("Phone number not verified");
                        item.setMessageColor(Color.parseColor("#E94700"));
                    }
                }
                
                currDisplayedItems.add(item);
                currUserAttributes.add(det);
                itemCount++;
            }
        }
    }

    private static void modifyAttribute(String attributeName, String newValue) {
        //

    }

    private static void deleteAttribute(String attributeName) {

    }


}
