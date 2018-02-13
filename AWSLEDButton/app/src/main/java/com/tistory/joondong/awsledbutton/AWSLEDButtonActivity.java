package com.tistory.joondong.awsledbutton;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPrincipalPolicyRequest;
import com.amazonaws.services.iot.model.DetachPrincipalPolicyRequest;
import com.amazonaws.services.iotdata.AWSIotDataClient;
import com.amazonaws.services.iotdata.model.GetThingShadowRequest;
import com.amazonaws.services.iotdata.model.GetThingShadowResult;
import com.amazonaws.youruserpools.AboutApp;
import com.amazonaws.youruserpools.AppHelper;
import com.amazonaws.youruserpools.ChangePasswordActivity;
import com.amazonaws.youruserpools.UserActivity;
import com.google.gson.Gson;

import java.util.UUID;

public class AWSLEDButtonActivity extends AppCompatActivity {
    static final String TAG = AWSLEDButtonActivity.class.getCanonicalName();

    EditText txtDSN;
    private String dsn;

    EditText txtCurrKey;
    private String currKey;
    EditText txtNewKey;
    Button btnChangeKey;

    Button btnConnect;
    Button btnLEDSwitch;

    TextView txtConsole;

    ImageView imgLight;

    // IoT objects
    AWSIotClient mIotAndroidClient;
    AWSIotDataClient iotDataClient;
    AWSIotMqttManager mqttManager;
    String clientId = AppHelper.getBasicThingName() + UUID.randomUUID().toString();

    private NavigationView nDrawer;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar toolbar;
    private AlertDialog userDialog;
    private ProgressDialog waitDialog;
    private ListView attributesList;

    // Cognito user objects
    private CognitoUser user;
    private CognitoUserSession session;
    private CognitoUserDetails details;

    // Information store
    private SharedPreferences mInfoStore;
    private SharedPreferences.Editor mInfoStoreEditor;
    private final static String INFO_STORE_NAME = "InfoStore";
    private final static String DSN_KEY = "dsn";
    private final static String CURR_KEY_KEY = "curr_key";


    // User details
    private String username;

    Thread deattachPrincipalPolicyThread;

    // Thread for attaching policy to temporary credentials in the STS.
    Runnable attachPrincipalPolicyRunnalbe = new Runnable() {
        @Override
        public void run() {
            Region region = Region.getRegion(AppHelper.getRigion());
            AttachPrincipalPolicyRequest attachPrincipalPolicyRequest = new AttachPrincipalPolicyRequest();
            // IoT Client (for creation of certificate if needed)
            mIotAndroidClient = new AWSIotClient(AppHelper.getCredentialsProvider());
            mIotAndroidClient.setRegion(region);
            attachPrincipalPolicyRequest.setPrincipal(AppHelper.getCredentialsProvider().getIdentityId());
            attachPrincipalPolicyRequest.setPolicyName(AppHelper.getBasicPolicyName() + dsn);
            try {
                mIotAndroidClient.attachPrincipalPolicy(attachPrincipalPolicyRequest);
            } catch (Exception e) {
                Log.e(TAG, "Attach error.", e);
            }
            AppHelper.getCredentialsProvider().refresh();

        }
    };

    // Thread for dettaching policy from temporary credentials in the STS.
    Runnable detachPrincipalPolicyRunnalbe = new Runnable() {
        @Override
        public void run() {
            Region region = Region.getRegion(AppHelper.getRigion());
            DetachPrincipalPolicyRequest detachPrincipalPolicyRequest = new DetachPrincipalPolicyRequest();
            // IoT Client (for creation of certificate if needed)
            mIotAndroidClient = new AWSIotClient(AppHelper.getCredentialsProvider());
            mIotAndroidClient.setRegion(region);
            detachPrincipalPolicyRequest.setPrincipal(AppHelper.getCredentialsProvider().getIdentityId());
            detachPrincipalPolicyRequest.setPolicyName(AppHelper.getBasicPolicyName() + dsn);
            try {
                mIotAndroidClient.detachPrincipalPolicy(detachPrincipalPolicyRequest);
            } catch (Exception e) {
                Log.e(TAG, "Detach error.", e);
            }
            AppHelper.getCredentialsProvider().refresh();
        }
    };

    @Override protected void onDestroy() {
        //user.signOut();
        AppHelper.clearCredentialsProvider();
        super.onDestroy();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aws_led_button);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mInfoStore = this.getSharedPreferences(INFO_STORE_NAME, 0);
        mInfoStoreEditor = mInfoStore.edit();
        dsn = mInfoStore.getString(DSN_KEY, null);
        currKey = mInfoStore.getString(CURR_KEY_KEY, null);

        // Set toolbar for this screen
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle("");
        TextView main_title = (TextView) findViewById(R.id.main_toolbar_title);
        main_title.setText("AWS LED Button");
        setSupportActionBar(toolbar);

        // Set navigation drawer for this screen
        mDrawer = (DrawerLayout) findViewById(R.id.user_drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        mDrawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        nDrawer = (NavigationView) findViewById(R.id.nav_view);
        setNavDrawer();
        init();
        View navigationHeader = nDrawer.getHeaderView(0);
        TextView navHeaderSubTitle = (TextView) navigationHeader.findViewById(R.id.textViewNavUserSub);
        navHeaderSubTitle.setText(username);

        imgLight = (ImageView) findViewById(R.id.light);

        txtDSN = (EditText) findViewById(R.id.dsn);
        txtDSN.setText(dsn);
        txtDSN.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Called when text is changed
                dsn = txtDSN.getText().toString();
                mInfoStoreEditor.putString(DSN_KEY, dsn);
                mInfoStoreEditor.commit();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Called when text input is complete
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Called before text is entered
            }
        });

        txtCurrKey = (EditText) findViewById(R.id.current_key);
        txtCurrKey.setText(currKey);
        txtCurrKey.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Called when text is changed
                currKey = txtCurrKey.getText().toString();
                mInfoStoreEditor.putString(CURR_KEY_KEY, currKey);
                mInfoStoreEditor.commit();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Called when text input is complete
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Called before text is entered
            }
        });

        txtNewKey = (EditText) findViewById(R.id.new_key);
        btnChangeKey = (Button) findViewById(R.id.change_key);
        btnChangeKey.setOnClickListener(changeClick);

        btnConnect = (Button) findViewById(R.id.connect);
        btnConnect.setOnClickListener(connectClick);
        btnConnect.setEnabled(false);
        btnLEDSwitch = (Button) findViewById(R.id.led_switch);
        btnLEDSwitch.setOnClickListener(switchClick);

        txtConsole = (TextView) findViewById(R.id.console);

        mqttManager = new AWSIotMqttManager(clientId, AppHelper.getAWSIoTEndpoint());
        iotDataClient = new AWSIotDataClient(AppHelper.getCredentialsProvider());
        iotDataClient.setEndpoint(AppHelper.getAWSIoTEndpoint());

        // The following block uses a Cognito credentials provider for authentication with AWS IoT.
        new Thread(new Runnable() {
            @Override
            public void run() {
                //awsCredentials = credentialsProvider.getCredentials();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnConnect.setEnabled(true);
                    }
                });
            }
        }).start();

        // The connect button operates only when the device serial number is entered.
        if(txtDSN.getText().toString() != null && !txtDSN.getText().toString().equals("")) {
            txtDSN.setEnabled(false);
            connectAWSIoT();
        }
    }

    // Handle when the a navigation item is selected
    private void setNavDrawer() {
        nDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                performAction(item);
                return true;
            }
        });
    }

    // Perform the action for the selected navigation item
    private void performAction(MenuItem item) {
        // Close the navigation drawer
        mDrawer.closeDrawers();

        // Find which item was selected
        switch (item.getItemId()) {
            case R.id.nav_user:
                // Add a new attribute
                view_user();
                break;
            case R.id.nav_user_change_password:
                // Change password
                changePassword();
                break;
            case R.id.nav_user_sign_out:
                // Sign out from this account
                signOut();
                break;
            case R.id.nav_user_about:
                // For the inquisitive
                Intent aboutAppActivity = new Intent(this, AboutApp.class);
                startActivity(aboutAppActivity);
                break;
        }
    }

    // Show user profile
    private void view_user() {
        Intent userSettingsActivity = new Intent(this, UserActivity.class);
        startActivityForResult(userSettingsActivity, 20);
    }

    // Change user password
    private void changePassword() {
        Intent changePssActivity = new Intent(this, ChangePasswordActivity.class);
        startActivity(changePssActivity);
    }

    // Sign out user
    private void signOut() {
        user.signOut();
        exit();
    }

    private void exit() {
        Intent intent = new Intent();
        if (username == null)
            username = "";
        intent.putExtra("name", username);
        setResult(RESULT_OK, intent);
        finish();
    }

    // Initialize this activity
    private void init() {
        // Get the user name
        Bundle extras = getIntent().getExtras();
        username = AppHelper.getCurrUser();
        user = AppHelper.getPool().getUser(username);
        getDetails();
    }

    // Get user details from CIP service
    private void getDetails() {
        AppHelper.getPool().getUser(username).getDetailsInBackground(detailsHandler);
    }

    GetDetailsHandler detailsHandler = new GetDetailsHandler() {
        @Override
        public void onSuccess(CognitoUserDetails cognitoUserDetails) {
            closeWaitDialog();
            // Store details in the AppHandler
            AppHelper.setUserDetails(cognitoUserDetails);
            // Trusted devices?
            handleTrustedDevice();
        }

        @Override
        public void onFailure(Exception exception) {
            closeWaitDialog();
            showDialogMessage("Could not fetch user details!", AppHelper.formatException(exception), true);
        }
    };

    private void showWaitDialog(String message) {
        closeWaitDialog();
        waitDialog = new ProgressDialog(this);
        waitDialog.setTitle(message);
        waitDialog.show();
    }

    private void closeWaitDialog() {
        try {
            waitDialog.dismiss();
        } catch (Exception e) {
            //
        }
    }

    private void handleTrustedDevice() {
        CognitoDevice newDevice = AppHelper.getNewDevice();
        if (newDevice != null) {
            AppHelper.newDevice(null);
            trustedDeviceDialog(newDevice);
        }
    }

    private void trustedDeviceDialog(final CognitoDevice newDevice) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remember this device?");
        //final EditText input = new EditText(UserActivity.this);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        //input.setLayoutParams(lp);
        //input.requestFocus();
        //builder.setView(input);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    //String newValue = input.getText().toString();
                    showWaitDialog("Remembering this device...");
                    updateDeviceStatus(newDevice);
                    userDialog.dismiss();
                } catch (Exception e) {
                    // Log failure
                }
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                } catch (Exception e) {
                    // Log failure
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    private void updateDeviceStatus(CognitoDevice device) {
        device.rememberThisDeviceInBackground(trustedDeviceHandler);
    }

    GenericHandler trustedDeviceHandler = new GenericHandler() {
        @Override
        public void onSuccess() {
            // Close wait dialog
            closeWaitDialog();
        }

        @Override
        public void onFailure(Exception exception) {
            closeWaitDialog();
            showDialogMessage("Failed to update device status", AppHelper.formatException(exception), true);
        }
    };

    private void showDialogMessage(String title, String body, final boolean exit) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    userDialog.dismiss();
                    if (exit) {
                        exit();
                    }
                } catch (Exception e) {
                    // Log failure
                    Log.e(TAG, " -- Dialog dismiss failed");
                    if (exit) {
                        exit();
                    }
                }
            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    View.OnClickListener connectClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Log.d(TAG, "clientId = " + clientId);

            if (!AppHelper.getIoTDisconnectionStatus()) { // if device is already connected to AWS IoT
                try {
                    //txtDSN.setEnabled(true); // TODO : Enable ??
                    //deattachPrincipalPolicyThread = new Thread(detachPrincipalPolicyRunnalbe);
                    //deattachPrincipalPolicyThread.start();
                    mqttManager.disconnect();
                } catch (Exception e) {
                    Log.e(TAG, "Disconnect error.", e);
                }
            } else { // if device is not connected to AWS IoT
                if(dsn != null)
                    txtDSN.setEnabled(false);
                connectAWSIoT();
            }
        }
    };

    private void connectAWSIoT() {
        deattachPrincipalPolicyThread = new Thread(attachPrincipalPolicyRunnalbe);
        deattachPrincipalPolicyThread.start();
        try {
            mqttManager.connect(AppHelper.getCredentialsProvider(), new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(final AWSIotMqttClientStatus status,
                                            final Throwable throwable) {
                    Log.d(TAG, "Status = " + String.valueOf(status));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (status == AWSIotMqttClientStatus.Connecting) {
                                btnConnect.setText("DISCONNECT");
                                AppHelper.setIoTDisconnectionStatus(false);
                                txtConsole.setText("Connecting...");
                            } else if (status == AWSIotMqttClientStatus.Connected) {
                                txtConsole.setText("Connected");
                                AppHelper.setIoTDisconnectionStatus(false);
                                getShadows();
                                subscribeForWaitingResult();
                            } else if (status == AWSIotMqttClientStatus.Reconnecting) {
                                if (throwable != null) {
                                    Log.e(TAG, "Connection error.", throwable);
                                }
                                btnConnect.setText("DISCONNECT");
                                AppHelper.setIoTDisconnectionStatus(false);
                                txtConsole.setText("Reconnecting...");
                            } else if (status == AWSIotMqttClientStatus.ConnectionLost) {
                                if (throwable != null) {
                                    Log.e(TAG, "Connection error.", throwable);
                                    throwable.printStackTrace();
                                }
                                btnConnect.setText("CONNECT");
                                txtDSN.setEnabled(true);
                                txtConsole.setText("Disconnected");
                                deattachPrincipalPolicyThread = new Thread(detachPrincipalPolicyRunnalbe);
                                deattachPrincipalPolicyThread.start();
                                AppHelper.setIoTDisconnectionStatus(true);
                            } else {
                                btnConnect.setText("CONNECT");
                                txtDSN.setEnabled(true);
                                txtConsole.setText("Disconnected");
                                deattachPrincipalPolicyThread = new Thread(detachPrincipalPolicyRunnalbe);
                                deattachPrincipalPolicyThread.start();
                                AppHelper.setIoTDisconnectionStatus(true);
                            }
                        }
                    });
                }
            });
        } catch (final Exception e) {
            Log.e(TAG, "Connection error.", e);
            txtConsole.setText("Error! " + e.getMessage());
        }
    }

    View.OnClickListener switchClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (currKey != null && !currKey.equals("") && !AppHelper.getIoTDisconnectionStatus()) {
                String topic = AppHelper.getBasicThingName() + dsn + "/command";
                Log.i(TAG, "switchClick : topic = " + topic);
                Gson gson = new Gson();
                LEDButtonCommand command = new LEDButtonCommand(txtCurrKey.getText().toString());
                String msg;// = gson.toJson(command);

                // {"operation_key":%s,"operation_code":true/false}
                if (AppHelper.getAWSLEDStatus()) {// if led is on
                    command.setOpcode(LEDButtonCommand.LED_OFF);
                } else { // if led is off
                    command.setOpcode(LEDButtonCommand.LED_ON);
                }
                command.setOpkey(currKey);
                msg = gson.toJson(command);
                Log.i(TAG, "topic : " + topic);
                Log.i(TAG, "msg : " + msg);

                try {
                    mqttManager.publishString(msg, topic, AWSIotMqttQos.QOS0);
                } catch (Exception e) {
                    Log.e(TAG, "Publish error.", e);
                }
            } else if (currKey == null || currKey.equals("")) {
                Toast.makeText(AWSLEDButtonActivity.this, "enter \"Current KEY\"", Toast.LENGTH_SHORT).show();
            }
        }
    };

    View.OnClickListener changeClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String topic = AppHelper.getBasicThingName() + dsn + "/command";
            Gson gson = new Gson();
            LEDButtonCommand command = new LEDButtonCommand(currKey,
                    LEDButtonCommand.KEY_CHANGE,
                    txtNewKey.getText().toString());

            String msg = gson.toJson(command);
            Log.i(TAG, "topic : " + topic);
            Log.i(TAG, "msg : " + msg);

            try {
                mqttManager.publishString(msg, topic, AWSIotMqttQos.QOS0);
            } catch (Exception e) {
                Log.e(TAG, "Publish error.", e);
            }
        }
    };

    // if disconnected, AWSIotMqttManager instance(mqttManager) automatically unsubscribe subscribing topic.
    private void unsubscribeResult() {
        String topic = AppHelper.getBasicThingName() + dsn + "/result";

        try {
            mqttManager.unsubscribeTopic(topic);
        } catch (Exception e) {
            Log.e(TAG, "Unsubscription error.", e);
        }
    }

    private void subscribeForWaitingResult() {
        String topic = AppHelper.getBasicThingName() + dsn + "/result";
        Log.i(TAG, "subscribeForWaitingResult : topic = " + topic);

        try {
            mqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0,
                    new AWSIotMqttNewMessageCallback() {
                        @Override
                        public void onMessageArrived(final String topic, final byte[] data) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Gson gson = new Gson();
                                    String opcodeString = "Received json document ";
                                    String resultString = "is wrong";
                                    String messageString = "nothing";
                                    boolean ledStatus = false;
                                    try {
                                        String message = new String(data, "UTF-8");
                                        // exception occur when failed to convert from json document to object.
                                        LEDButtonResult result = gson.fromJson(message, LEDButtonResult.class);

                                        switch (result.operation_code) {
                                            case LEDButtonCommand.LED_OFF:
                                                opcodeString = "led OFF ";
                                                break;
                                            case LEDButtonCommand.LED_ON:
                                                opcodeString = "led ON ";
                                                ledStatus = true;
                                                break;
                                            case LEDButtonCommand.KEY_CHANGE:
                                                opcodeString = "Key change ";
                                                break;
                                            default:
                                                break;
                                        }

                                        if ((result.operation_code == LEDButtonCommand.LED_OFF ||
                                                result.operation_code == LEDButtonCommand.LED_ON) &&
                                                result.operation_result == true) {
                                            AppHelper.setAWSLEDStatus(ledStatus);
                                            setLightImg(ledStatus);
                                        }

                                        if (result.operation_code != LEDButtonCommand.DEFAULT) {
                                            if (result.operation_result == true) {
                                                resultString = "success";
                                                if(result.operation_code == LEDButtonCommand.KEY_CHANGE) {
                                                    currKey = txtNewKey.getText().toString();
                                                    txtCurrKey.setText(currKey);
                                                    txtNewKey.setText("");
                                                    mInfoStoreEditor.putString(CURR_KEY_KEY, currKey);
                                                }
                                            } else {
                                                resultString = "fail";
                                            }
                                        }

                                        if (result.result_data != null) {
                                            messageString = result.result_data;
                                        }

                                    } catch (Exception e) {
                                        Log.e(TAG, "Message encoding error.", e);
                                    }

                                    txtConsole.setText(opcodeString + resultString + "\n" + "message : " + messageString);
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Subscription error.", e);
        }
    }

    public void getShadows() {
        GetShadowTask getStatusShadowTask = new GetShadowTask(AppHelper.getBasicThingName() + dsn);
        getStatusShadowTask.execute();
    }

    private class GetShadowTask extends AsyncTask<Void, Void, AsyncTaskResult<String>> {

        private final String thingName;

        public GetShadowTask(String name) {
            thingName = name;
        }

        @Override
        protected AsyncTaskResult<String> doInBackground(Void... voids) {
            try {
                GetThingShadowRequest getThingShadowRequest = new GetThingShadowRequest().withThingName(thingName);
                GetThingShadowResult result = iotDataClient.getThingShadow(getThingShadowRequest);
                byte[] bytes = new byte[result.getPayload().remaining()];
                result.getPayload().get(bytes);
                String resultString = new String(bytes);
                return new AsyncTaskResult<String>(resultString);
            } catch (Exception e) {
                Log.e("E", "getShadowTask", e);
                return new AsyncTaskResult<String>(e);
            }
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<String> result) {
            if (result.getError() == null) {
                // exception occur when failed to convert from json document to object.
                Gson gson = new Gson();
                LEDButtonShadow shadow = gson.fromJson(result.getResult(), LEDButtonShadow.class);
                Boolean ledStatus = shadow.state.reported.ledStatus;
                Log.i(GetShadowTask.class.getCanonicalName(), ledStatus.toString());
                setLightImg(ledStatus);
                AppHelper.setAWSLEDStatus(ledStatus);
                //Log.i(GetShadowTask.class.getCanonicalName(), result.getResult());
            } else {
                Log.e(GetShadowTask.class.getCanonicalName(), "getShadowTask", result.getError());
            }
        }
    }

    private void setConsole() {
        // TODO : 콘솔 내용을 규격화
    }

    private void setLightImg(boolean light) {
        if(imgLight != null) {
            if(light) {
                imgLight.setImageResource(R.drawable.light_on);
            } else {
                imgLight.setImageResource(R.drawable.light_off);
            }
        }
    }
}
