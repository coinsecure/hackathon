package com.example.kritiketansharma.byite;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;


public class access extends ActionBarActivity {
        String Second;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access);
        ImageButton wt = (ImageButton)findViewById(R.id.a1);
        wt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Second = "Watches Accessories";
                Intent t1=new Intent(access.this, filter.class).putExtra("Second",Second);
                startActivity(t1);
            }
        });
        ImageButton wq = (ImageButton)findViewById(R.id.a2);
        wq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Second = "Rings Accessories";
                Intent t2=new Intent(access.this, filter.class).putExtra("Second",Second);
                startActivity(t2);
            }
        });
        ImageButton wp = (ImageButton)findViewById(R.id.a3);
        wp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Second = "Sunglasses Accessories";
                Intent t3=new Intent(access.this, filter.class).putExtra("Second",Second);
                startActivity(t3);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_access, menu);
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
