package com.example.kritiketansharma.byite;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;


public class shop extends ActionBarActivity {
    String First;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        ImageButton foot = (ImageButton) findViewById(R.id.ib4);
        ImageButton books = (ImageButton) findViewById(R.id.ib5);
        ImageButton electro = (ImageButton) findViewById(R.id.ib6);
        ImageButton appar = (ImageButton) findViewById(R.id.ib7);
        ImageButton access = (ImageButton) findViewById(R.id.ib8);
        ImageButton sports = (ImageButton) findViewById(R.id.ib9);
        //footwear selected
        foot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent in1 = new Intent(shop.this, foot.class);
                startActivity(in1);
            }
        });
        //books selected
        books.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent in2 = new Intent(shop.this, books.class);
                startActivity(in2);

            }
        });
        //electronics selected
        electro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent in4 = new Intent(shop.this, electro.class);
                startActivity(in4);

            }
        });
        //Apparels selected
        appar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent in3 = new Intent(shop.this, appa.class);
                startActivity(in3);

            }
        });
        //Accessories selected
        access.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent in6 = new Intent(shop.this, access.class);
                startActivity(in6);

            }
        });
        //Sports selected
        sports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent in5 = new Intent(shop.this, sports.class);
                startActivity(in5);

            }
        });
    }

}