package com.example.kritiketansharma.byite;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;


public class books extends ActionBarActivity {
       String Second;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);
        ImageButton wt = (ImageButton)findViewById(R.id.b1);
        wt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Second = "Myth Books";
                Intent o1= new Intent(books.this, filter.class).putExtra("Second",Second);
                startActivity(o1);
            }
        });
        ImageButton wo = (ImageButton)findViewById(R.id.b2);
        wo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Second = "Bestsellers Books";
                Intent o2=new Intent(books.this, filter.class).putExtra("Second",Second);
                startActivity(o2);
            }
        });
        ImageButton wa = (ImageButton)findViewById(R.id.b3);
        wa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Second = "Entrance Books";
                Intent o3=new Intent(books.this, filter.class).putExtra("Second",Second);
                startActivity(o3);
            }
        });
        ImageButton wb = (ImageButton)findViewById(R.id.b4);
        wb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Second = "Bios Books";
                Intent o4=new Intent(books.this, filter.class).putExtra("Second",Second);
                startActivity(o4);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_books, menu);
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
