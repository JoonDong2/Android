package com.tistory.joondong.awsledbutton;

/**
 * Created by isp40 on 2018-01-17.
 */

public class LEDButtonCommand {
    public final static int LED_OFF = 0;
    public final static int LED_ON = 1;
    public final static int KEY_CHANGE = 2;
    public final static int DEFAULT = 3;

    public String operation_key = "NULL";
    public int operation_code = DEFAULT;
    public String operation_data = "NULL";

    LEDButtonCommand(String op_key) {
        operation_key = op_key;
    }

    LEDButtonCommand(String op_key, int op_code) {
        operation_key = op_key;
        operation_code = op_code;
    }

    LEDButtonCommand(String op_key, int op_code, String op_data) {
        operation_key = op_key;
        operation_code = op_code;
        operation_data = op_data;
    }

    public void setOpcode(int op_code) {
        operation_code = op_code;
    }

    public void setOpkey(String key) {
        operation_key = key;
    }

    public void setOpdata(String data) {
        operation_data = data;
    }
}
