package com.example.kritiketansharma.byite;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class trade extends ActionBarActivity {
    String resp, er;
    TextView tvg;
    URL ur;
    Button bt1;
    Button bt2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade);
        tvg = (TextView) findViewById(R.id.tvg);
        bt1 = (Button) findViewById(R.id.ol1);
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ur = new URL("https://api.coinsecureis.cool/v0/auth/createbid");
                    new tride().execute();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

            }
        });
        bt2 = (Button) findViewById(R.id.ol2);
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ur = new URL("https://api.coinsecureis.cool/v0/auth/createask");
                    new tride().execute();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    public class tride extends AsyncTask<Void, Void, String> {
//json object is para, the input passed to the async class containing user id and pass word adn secret code


        protected void onPreExecute() {
            super.onPreExecute();
            ProgressDialog progDailog = new ProgressDialog(trade.this);
            progDailog.setMessage("Loading...");
            progDailog.setIndeterminate(true);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(true);
            progDailog.show();
        }


        JSONObject job = new JSONObject();

        @Override
        protected String doInBackground(Void... params) {
            //Apply authentication logic here
            URL url;
            HttpURLConnection conn = null;

            try {
                //server URL
                url=ur;
                //establish connection
                conn = (HttpURLConnection) url.openConnection();
                //Variable length body
                conn.setChunkedStreamingMode(0);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                //request type
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "Application/Json");
                //Json objects containing API key
                job.put("apiKey", "8cUMn7jcO3HUX55PvWJSX4MHFOa3cPSSNN02IhEY");
                //Rate and volume value taken static due to lack of time
                job.put("rate",1000);
                job.put("vol",0.001);
                //send object to server
                //taking instance of output stream writer
                OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
                //writing to output stream
                out.write(job.toString());
                //closing output stream
                out.close();
                //Recieving Input(source input stream)
                InputStream inputStream = conn.getInputStream();
                //Converting byte stream data into char stream
                InputStreamReader isr = new InputStreamReader(inputStream);
                //Reading response in json
                String usr = isr.toString();
                System.out.print(usr);
                JSONObject jsonObject = new JSONObject(isr.toString());
                System.out.print(isr.toString());
                resp = jsonObject.getString("result");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //closing the connection when not in use
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return resp;
        }





        protected void onPostExecute(String resp) {
            tvg.setText(resp);

        }
    }

}