package com.example.kritiketansharma.byite;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;


public class appa extends ActionBarActivity {
        String Second;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appa);
        ImageButton wt = (ImageButton)findViewById(R.id.ap1);
        wt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Second = "Casual Apparels";
                Intent u1=new Intent(appa.this, filter.class).putExtra("Second",Second);
                startActivity(u1);
            }
        });
        ImageButton wp = (ImageButton)findViewById(R.id.ap2);
        wp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Second = "Formal Apparels";
                Intent u2=new Intent(appa.this, filter.class).putExtra("Second",Second);
                startActivity(u2);
            }
        });
        ImageButton wl = (ImageButton)findViewById(R.id.ap3);
        wl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Second = "Sports Apparels";
                Intent u3=new Intent(appa.this, filter.class).putExtra("Second",Second);
                startActivity(u3);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_appa, menu);
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
