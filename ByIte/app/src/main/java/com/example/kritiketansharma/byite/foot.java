package com.example.kritiketansharma.byite;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;


public class foot extends ActionBarActivity {
    String Second;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foot);
        String footwear=getIntent().getStringExtra("Footwear");
        //casual
        ImageButton wt = (ImageButton)findViewById(R.id.e1);
        wt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Second = "Casuals Footwear";
                Intent p1=new Intent(foot.this, filter.class).putExtra("Second",Second);
                startActivity(p1);
            }
        });
        //formal
        ImageButton wz = (ImageButton)findViewById(R.id.e2);
        wz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Second = "Formals Footwear";
                Intent p2=new Intent(foot.this, filter.class).putExtra("Second",Second);
                startActivity(p2);
            }
        });
        //sports
        ImageButton wc = (ImageButton)findViewById(R.id.e3);
        wc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Second = "Sports Footwear";
                Intent p3=new Intent(foot.this, filter.class).putExtra("Second",Second);
                startActivity(p3);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_foot, menu);
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
