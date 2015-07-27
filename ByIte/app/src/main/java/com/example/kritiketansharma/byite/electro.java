package com.example.kritiketansharma.byite;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;


public class electro extends ActionBarActivity {
        String Second;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_electro);
        ImageButton wt = (ImageButton)findViewById(R.id.f1);
        wt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Second = "Mobiles Electronics";
                Intent i1=new Intent(electro.this, filter.class).putExtra("Second",Second);
                startActivity(i1);
            }
        });
        ImageButton wl = (ImageButton)findViewById(R.id.f2);
        wl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Second = "MobA Electronics";
                Intent i2=new Intent(electro.this, filter.class).putExtra("Second",Second);
                startActivity(i2);
            }
        });
        ImageButton wc = (ImageButton)findViewById(R.id.f3);
        wc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Second = "Laptop Electronics";
                Intent i3=new Intent(electro.this, filter.class).putExtra("Second",Second);
                startActivity(i3);
            }
        });
        ImageButton wz = (ImageButton)findViewById(R.id.f4);
        wz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Second="LapA Electronics";
                Intent i4=new Intent(electro.this, filter.class).putExtra("Second",Second);
                startActivity(i4);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_electro, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
