package com.japps.joe.cableteller;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    final static double LOW_LOSS_RG6 = 1.4;
    final static double HIGH_LOSS_RG6 = 5.6;
    final static int LOW_ERROR_MARGIN = 1;
    final static int HIGH_ERROR_MARGIN = 3;
    double low;
    double high;
    double low2;
    double high2;
    String output = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button btn = (Button)findViewById(R.id.btnCalc);
        final EditText lowDem = (EditText)findViewById(R.id.numLowDemarc);
        final EditText highDem = (EditText)findViewById(R.id.numHighDemarc);
        final EditText lowDev = (EditText)findViewById(R.id.numLowDevice);
        final EditText highDev = (EditText)findViewById(R.id.numHighDevice);
        final TextView result = (TextView)findViewById(R.id.txtResult);
        final InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        lowDem.requestFocus();
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                try {
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                    result.setText("");
                    low = Double.parseDouble(lowDem.getText().toString());
                    high = Double.parseDouble(highDem.getText().toString());
                    low2 = Double.parseDouble(lowDev.getText().toString());
                    high2 = Double.parseDouble(highDev.getText().toString());
                    calculateLength(low,high,low2,high2);
                    result.setText(output);
                    output = "";
                }
                catch(NumberFormatException x){
                    result.setText("Please enter levels.");
                }
            }
        });
    }

    public void calculateLength(double l, double h, double l2, double h2){
        double lowLost = l - l2;
        double hiLost = h - h2;
        if (lowLost <= 0 || hiLost <= 0){
            output = ("End signal equal/higher than source.");
            return;
        }
        boolean splitFound = false;
        int feet =  0;
        int lowLength;
        int highLength;
        double lowRollOff;
        double highRollOff;

        lowLength = (int)(lowLost/(LOW_LOSS_RG6/100.0));
        highLength = (int)(hiLost/(HIGH_LOSS_RG6/100.0));
        if(lowLength < highLength){
            feet = lowLength;
            highRollOff = (highLength - lowLength)*(HIGH_LOSS_RG6/100.0);
            if(highRollOff > HIGH_ERROR_MARGIN){
                output += ("High frequency is at least " + Math.round(highRollOff) +
                        " dB less than it should be. Possible bad drop.\n" +
                        "Based on the levels alone,\n");
            }
        }
        if(highLength < lowLength){
            feet = highLength;
            lowRollOff = (lowLength - highLength)*(LOW_LOSS_RG6/100.0);
            if(lowRollOff > LOW_ERROR_MARGIN){
                output += ("Low Frequency issue.\n");
                if(splitCheck(l-l2, h-h2, 3.5,"*Possible Two-way splitter.\n")){
                    splitFound = true;
                }
                if(!splitFound && splitCheck(l-l2, h-h2, 7,"*Possible 7dB splitter leg.\n")){
                    splitFound = true;
                }
                if(!splitFound && splitCheck(l-l2, h-h2, 10.5,"*Possible multi-splitter config.\n")){
                    splitFound = true;
                }
                if(!splitFound && splitCheck(l-l2, h-h2, 14,"*Possible multi-splitter config.\n")){
                    splitFound = true;
                }
                if (!splitFound){
                    output += ("No splitters calculated. \nLook for defective connections.\n");
                }
                output += "Without any splitters,\n";
            }
        }
        if (highLength == lowLength){
            feet = lowLength;
        }
        output += ("This drop may be " + feet + " feet of RG-6.\n");
    }

    public boolean splitCheck(double l, double h, double s, String y){
        double splitterLow = l - s;
        double splitterHigh = h - s;
        if (splitterLow < 0 || splitterHigh < 0){
            return false;
        }
        boolean isFound = false;
        int feet = 0;
        int lowLength;
        int highLength;
        double lowRollOff;
        double highRollOff;

        lowLength = (int)(splitterLow/(LOW_LOSS_RG6/100.0));
        highLength = (int)(splitterHigh/(HIGH_LOSS_RG6/100.0));
        if(lowLength < highLength){
            feet = lowLength;
            highRollOff = (highLength - lowLength)*(HIGH_LOSS_RG6/100.0);
            if(highRollOff > HIGH_ERROR_MARGIN){
                isFound = true;
                output += y;
                output += ("On a -" + s + " dB split, high frequency is about "
                        + Math.round(highRollOff) + " dB less than it should be.\n");
                if(feet != 0){
                    output += "Possible bad drop with " + feet + " feet of RG-6.\n";
                }
            }
            else{
                isFound = true;
                output += y;
                if (feet != 0) {
                    output += ("On a -" + s + " dB split, this drop may" + " be " + feet +
                            " feet of RG-6.\n");
                }
            }
        }
        if(highLength < lowLength){
            feet = highLength;
            lowRollOff = (lowLength - highLength)*(LOW_LOSS_RG6/100.0);
            if(lowRollOff > LOW_ERROR_MARGIN){
                isFound = false;
            }
            else{
                isFound = true;
                output += y;
                if (feet != 0) {
                    output += ("On a -" + s + " dB split, this drop may" + " be " + feet +
                            " feet of RG-6.\n");
                }
            }
        }
        if (highLength == lowLength){
            isFound = true;
            output += y;
            feet = lowLength;
            if (feet != 0) {
                output += ("On a -" + s + " dB split, this drop may" + " be " + feet +
                        " feet of RG-6.\n");
            }
        }
        return isFound;
    }


}
