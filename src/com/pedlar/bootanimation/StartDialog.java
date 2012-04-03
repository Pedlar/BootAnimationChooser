
package com.pedlar.bootanimation;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StartDialog extends Dialog {

    private static Context mContext;

    private static Button buttonBinary;
    private static Button buttonAlternate;

    private static TextView textMessage;

    private ChoiceListener choiceListener;
    
    public interface ChoiceListener {
        public void choice(String choice);
    }
    
    public StartDialog(Context context, ChoiceListener listener) {
        super(context);
        mContext = context;
        choiceListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_dialog);
        setTitle("Welcome!");

        textMessage = (TextView) findViewById(R.id.start_text);
        textMessage.setText(R.string.start_message);

        /* 
         * Removing Binary Option for now.
          buttonBinary = (Button) findViewById(R.id.binary_button);
          buttonBinary.setOnClickListener(new buttonClickBinary());
        */
        buttonAlternate = (Button) findViewById(R.id.alternate_button);
        buttonAlternate.setOnClickListener(new buttonClickAlternate());
    }

    private class buttonClickBinary implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            choiceListener.choice("binary");
            dismiss();
        }
    }

    private class buttonClickAlternate implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            choiceListener.choice("alternate");
            dismiss();
        }
    }
}
