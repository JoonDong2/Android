package com.tistory.joondong.awsledbutton;

/**
 * Created by isp40 on 2018-01-30.
 */

public class LEDButtonShadow {
    public State state;

    LEDButtonShadow() {
        state = new State();
    }

    public class State {
        public Reported reported;
        State(){
            reported = new Reported();
        }

        public class Reported {
            public Boolean ledStatus;
            Reported(){}
        }

    }
}
