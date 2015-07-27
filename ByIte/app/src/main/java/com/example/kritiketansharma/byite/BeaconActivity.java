package com.example.kritiketansharma.byite;

import android.annotation.TargetApi;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.andtinder.model.CardModel;
import com.andtinder.model.Orientations;
import com.andtinder.view.CardContainer;
import com.andtinder.view.SimpleCardStackAdapter;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;


public class BeaconActivity extends Activity implements BeaconConsumer {
    private Switch emitter, reciever;private static final String TAG = "BeaconActivity";
    private BeaconTransmitter mBeaconTransmitter;
    private RegionBootstrap regionBootstrap;
    private ProgressDialog pDialog;
    private HttpResponse response;
    private String uuid;
    private String category,subcategory, price;
    private ArrayList ProductsNotify;
    BeaconManager beaconManager;
    ArrayList uuidList = new ArrayList(150);
    CardContainer mCardContainer;
    Button btc, goback;
    TextView txt;
    String Distance;
    int qrcode=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        btc = (Button) findViewById(R.id.btc);
        goback = (Button) findViewById(R.id.goback);
        txt = (TextView) findViewById(R.id.txt);
        btc.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if(qrcode==0){
                    Toast.makeText(getApplicationContext(), "No Products in the cart", Toast.LENGTH_LONG).show();
                }
                else{
                    Intent intent = new Intent(BeaconActivity.this, QRCodeActivity.class);
                    intent.putExtra("qrcode",qrcode+"");
                    startActivity(intent);
                }
            }

        });
        txt.setOnClickListener(new View.OnClickListener() {

                                   @Override
                                   public void onClick(View view) {
                                       Intent intent = new Intent(BeaconActivity.this, EmitterActivity.class);
                                       startActivity(intent);
                                   }
                               });
        goback.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BeaconActivity.this, MainActivity.class);
                startActivity(intent);
            }

        });
        mCardContainer = (CardContainer) findViewById(R.id.layoutview);
        mCardContainer.setOrientation(Orientations.Orientation.Disordered);

        ProductsNotify = new ArrayList<ProductsNotifications>();
        //-----------------Switches----------------
        category = getIntent().getExtras().getString("category");
        subcategory = getIntent().getExtras().getString("subcategory");
        price = getIntent().getExtras().getString("price");
        //--------------------Reciever----------------------------------

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(BeaconActivity.this);

    }

    private class VerifyBeacon extends AsyncTask<Void, Void, Void> {
        String uuid;
        double dist;
        VerifyBeacon(String uuid, double dist){
            this.uuid = uuid;
            this.dist = dist;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpClient httpclient = new DefaultHttpClient(); // create new httpClient
            HttpGet httpGet = new HttpGet("http://192.168.2.11:8888/beacons/getProducts.php?uuid="+uuid
                    +"&category=" + category +"&subcategory="+ subcategory + "&price="+price); // create new httpGet object
            System.out.println("http://192.168.2.11:8888/beacons/getProducts.php?uuid="+uuid
                    +"&category=" + category +"&subcategory="+ subcategory + "&price="+price);
            try {
                response = httpclient.execute(httpGet); // execute httpGet

                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    // System.out.println(statusLine);
                    HttpEntity e = response.getEntity();
                    String entity = EntityUtils.toString(e);
                    System.out.println(entity);
                    if (entity != null) {
                        try {
                            JSONObject jsonObj = new JSONObject(entity);
                            //---------------Status---------------------------------
                            String isSuccess = jsonObj.getString("isSuccess").replace("]", "");
                            isSuccess = isSuccess.replace("[","");
                            String [] items = isSuccess.split(",");
                            //-----------------------------------------------------
                            if(items[0].equals("\"true\"")) {
                                for(int i=1;i<Integer.parseInt(items[2]);i++) {
                                    String product = jsonObj.getString("product"+i).replace("]", "");
                                    isSuccess = product.replace("[","");
                                    String [] cols = product.split(",");
                                    ProductsNotify.add(new ProductsNotifications(cols[0],cols[4],cols[1],cols[2],cols[3]));
                                }
                            }
                        } catch (JSONException j) {
                            j.printStackTrace();
                        }
                    } else {
                        Log.e("Verify Beacons","API Not Working");
                    }
                } else {
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                    R.drawable.cart);
            Bitmap pic = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                    R.drawable.download);
// build notification
// the addAction re-use the same intent to keep the example short
            SimpleCardStackAdapter adapter = new SimpleCardStackAdapter(getApplicationContext());
            for(int i=0; i<ProductsNotify.size(); i++){
                final ProductsNotifications productsNotifications = (ProductsNotifications) ProductsNotify.get(i);
                Notification n = new Notification.Builder(getApplicationContext())
                        .setContentTitle(productsNotifications.category.replaceAll("\"", "") + " Found")
                        .setContentText(" Approx "+dist+" meteres from your current location")
                        .setSmallIcon(R.drawable.cart)
                        .setLargeIcon(pic)
                        .setAutoCancel(true)
                        .build();
                n.defaults |= Notification.DEFAULT_SOUND;
                n.defaults |= Notification.DEFAULT_VIBRATE;
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(0, n);

                CardModel card = new CardModel(productsNotifications.name.replaceAll("\"", ""), productsNotifications.price.replaceAll("\"",""), icon);
                card.setOnCardDimissedListener(new CardModel.OnCardDimissedListener() {
                    @Override
                    public void onLike() {
                        qrcode+=Integer.parseInt(productsNotifications.price.replaceAll("\"",""));
                        txt.setText("TOTAL VALUE=Rs."+qrcode);
                        Toast.makeText(getApplicationContext(), "Added to Cart", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onDislike() {
                        Toast.makeText(getApplicationContext(), "Discarded", Toast.LENGTH_LONG).show();
                    }
                });
                adapter.add(card);
            }
            mCardContainer.setAdapter(adapter);

        }

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }
    @Override
    protected void onStop() {
        super.onStop();
        beaconManager.unbind(this);
    }
    @Override
    public void onBeaconServiceConnect() {

        //SetMonitorNotifier for Monitoring Function
        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {

            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see an beacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {

            }
        });
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                try {
                    if (beacons.size() > 0) {

                    }
                    final String s = (beacons.iterator().next().getId1()).toString();
                    if (uuidList.contains(s)) {
                        Log.i(TAG, "AGAIN CAUGHT!!!");
                        Log.i(TAG, "DISTANCE: " + Math.floor(beacons.iterator().next().getDistance() * 100) / 100 + " meters away.");
                    } else {
                        if (beacons.iterator().next().getDistance() <= 2.5) {
                            new VerifyBeacon(beacons.iterator().next().getId1() + ":" + beacons.iterator().next().getId2() + ":" + beacons.iterator().next().getId3(), beacons.iterator().next().getDistance()).execute();
                        }
                        /*     Log.i(TAG, "SIZE:" + beacons.size());
                             Log.i(TAG, "DISTANCE: " + beacons.iterator().next().getDistance() + " meters away.");
                             Log.i(TAG, "UUID: " + beacons.iterator().next().getId1() + ":" + beacons.iterator().next().getId2() + ":" + beacons.iterator().next().getId3());
                             Log.i(TAG, "" + uuidList);
                             Log.i(TAG, "Contains:" + uuidList.contains(s));
                          */
                        if (beacons.iterator().next().getDistance() <= 2.5)
                            uuidList.add(s);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
        }
    }



}
