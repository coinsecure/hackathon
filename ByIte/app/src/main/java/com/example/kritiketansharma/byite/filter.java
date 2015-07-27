package com.example.kritiketansharma.byite;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


public class filter extends ActionBarActivity {
       public String radio,s="1";
    String [] items;
    TextView tv;
    String p;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        p=getIntent().getStringExtra("Second");
        Button b1=(Button)findViewById(R.id.btr);
        tv =(TextView) findViewById(R.id.tg);
        items = p.split(" ");
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                s= tv.getText().toString();
                Intent intent = new Intent(getApplicationContext(),BeaconActivity.class);
                intent.putExtra("category",items[1]);
                intent.putExtra("subcategory",items[0]);
                intent.putExtra("price",s);
                startActivity(intent);
            }

        });
    }
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radioButton:
                if (checked) {
                    radio = "1";
                    tv.setText(radio);
                }
                    break;
            case R.id.radioButton2:
                if (checked) {
                    radio = "2";
                    tv.setText(radio);
                }
                    break;
            case R.id.radioButton3:
                if (checked) {
                    radio = "3";
                    tv.setText(radio);
                }
                    break;
            case R.id.radioButton4:
                if (checked) {
                    radio = "4";
                    tv.setText(radio);

                }
                    break;
            case R.id.radioButton5:
                if (checked) {
                    radio = "5";
                    tv.setText(radio);
                }
                    break;
        }
    }

}
