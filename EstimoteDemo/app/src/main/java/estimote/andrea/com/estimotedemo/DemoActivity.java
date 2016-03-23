package estimote.andrea.com.estimotedemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.List;
import java.util.UUID;
import com.estimote.sdk.Utils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;


public class DemoActivity extends AppCompatActivity {
    private final String TAG_DEBUG = "DemoActivity";
    final private String DEBUG_TAG= TAG_DEBUG;
    final private int REQUEST_ENABLE_BT = 125;
    private  BeaconManager beaconManager;
    private int request=0, max_request=99;
    ProgressBar progressBar, progressBarDistance;
    MyCountDownTimer myCountDownTimer;
    final int tot = 30000, intervalUpdate= 1000;
    TextView tv,tvRem, tvBar, tvMode, tvId;

    final public static String REGISTER = "register";
    final public static  String UNREGISTER = "unregister";
    private String device_model;
    private int ID_response = -1;
    private String distance="UNKNOW";

    //variabile che mi tiene lo stato del sistema
    private String mode = UNREGISTER;
    //private static AppCompatActivity ac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(DEBUG_TAG, "onCreate()");
        setContentView(R.layout.activity_demo);
        tvRem = (TextView)findViewById(R.id.secRem);
        tvRem.setVisibility(View.INVISIBLE);
        tvBar = (TextView)findViewById(R.id.DistanceBar);
        tvMode =(TextView)findViewById(R.id.mode);
        tvMode.setVisibility(View.INVISIBLE);

        tvId = (TextView)findViewById(R.id.id);
        tvId.setText("ID on system: "+ID_response);

        progressBar=(ProgressBar)findViewById(R.id.progressBarRegister);
        progressBar.setMax(tot / 1000);

        progressBarDistance=(ProgressBar)findViewById(R.id.progressBarDistance);
        progressBarDistance.setMax(3);
        // GONE, INVISIBLE, or VISIBLE
        progressBar.setVisibility(View.INVISIBLE);

        device_model = getDeviceName();
        Log.d(TAG_DEBUG, "device:"+device_model);
        //SARA: Major 17957  Minor 56571
        //mio: Major 61272 Minor 53723
        beaconManager = new BeaconManager(this);
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(new Region("monitored region",
                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), 61272, 53723));
            }
        });
        myCountDownTimer = new MyCountDownTimer(tot, intervalUpdate);//, progressBar, tv, tvRem,tvMode, REGISTER, getApplicationContext(), beaconManager);


        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                Log.d(TAG_DEBUG, "onEnteredRegion");
                tv.append("\n\nEstimote Beacond found.. \n\nStill up to 30s and you are registered in our system..");
                beaconManager.startRanging(region);

                myCountDownTimer.cancel();
                progressBar.setProgress(0);
                tvRem.setText("");
                    if(ID_response!=-1) {//sono già registrato do vado?
                        Log.d(DEBUG_TAG, "sei già registrato");
                        progressBar.setVisibility(View.INVISIBLE);
                        tvMode.setVisibility(View.INVISIBLE);
                        tvId.setText("ID on system: " + ID_response + " (you are just registered)");
                    }else{
                        //faccio partire il timer X
                        progressBar.setVisibility(View.VISIBLE);
                        tvMode.setVisibility(View.VISIBLE);
                        mode = REGISTER;
                        tvMode.setText("Mode: " + mode);
                        myCountDownTimer.start();
                    }
            }

            @Override
            public void onExitedRegion(Region region) {
                // could add an "exit" notification too if you want (-:
                Log.d(TAG_DEBUG, "onExitedRegion");

                //se esce faccio partire il timer Y
                myCountDownTimer.cancel();
                progressBar.setProgress(0);
                tvRem.setText("");
              //  myCountDownTimer = new MyCountDownTimer(tot, intervalUpdate);
                if(ID_response!=-1) {
                    progressBar.setVisibility(View.VISIBLE);
                    tvMode.setVisibility(View.VISIBLE);
                    mode = UNREGISTER;
                    tvMode.setText("Mode: " + mode);
                    myCountDownTimer.start();
                    beaconManager.stopRanging(region);
                }else{
                    Log.d(DEBUG_TAG,"non sei registrato che ti sregistri a fare?");
                    progressBar.setVisibility(View.INVISIBLE);
                    tvMode.setVisibility(View.INVISIBLE);
                    tvId.setText("ID on system: " + ID_response + " (you are not registered)");
                }

            }
        });

       // beaconManager.setForegroundScanPeriod(1000, 1000);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Beacon nearestBeacon = list.get(0);
                    nearestBeacon.getRssi();
                    nearestBeacon.getMeasuredPower();
                    Utils.Proximity pos = Utils.computeProximity(nearestBeacon);
                    Log.d(TAG_DEBUG, "  Utils.computeProximity(nearestBeacon): " + Utils.computeProximity(nearestBeacon));
                    String msg = "";
                    if (pos == Utils.Proximity.IMMEDIATE) {
                        msg = "IMMEDIATE";
                        progressBarDistance.setProgress(3);
                    } else if (pos == Utils.Proximity.NEAR) {
                        msg = "NEAR";
                        progressBarDistance.setProgress(2);
                    } else if (pos == Utils.Proximity.FAR) {
                        msg = "FAR";
                        progressBarDistance.setProgress(1);

                    } else if (pos == Utils.Proximity.UNKNOWN) {
                        progressBarDistance.setProgress(0);
                        msg = "UNKNOWN";
                    }
                    tvBar.setText("Distance from Beacon: "+msg);
                    distance=msg;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(DEBUG_TAG, "onResume()");
        // SystemRequirementsChecker.checkWithDefaultDialogs(this);
        // ENABLE BLUETooth
        tv = (TextView)findViewById(R.id.log);
        enableBluetoothPermission();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(DEBUG_TAG, "onPause()");
        //nregitered();
    }

    @Override
    protected  void onStop(){
        super.onStop();
        Log.d(DEBUG_TAG, "onStop()");
        //unregitered();
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        Log.d(DEBUG_TAG, "onDestroy()");
        unregitered();
    }
    private void unregitered(){
        Log.d(DEBUG_TAG,"unregitered()");
        if(ID_response!=-1){
            //unregistered
            ConnectionDetector cd = new ConnectionDetector(this);
            AlertDialogManager alert = new AlertDialogManager();
            // Check if Internet present
            if (!cd.isConnectingToInternet()) {
                // Internet Connection is not present
                alert.showAlertDialog(DemoActivity.this,
                        "Internet Connection absent",
                        "Check your internet connection! \nRestart Demo" , false,this);

            }else {
                //display in long period of time
                Toast.makeText(getApplicationContext(), "Destroy App, you are unregistrationing, Restart Demo "+
                        "if you want to re-registered", Toast.LENGTH_LONG).show();
               /* alert.showAlertDialogDestroy(DemoActivity.this,
                        "Destroy App",
                        "You are unregistrationing \nRestart Demo if you want to re-registered" , false);*/
                AsyncHttpClient client = new AsyncHttpClient();
                RequestParams params = new RequestParams();
                String url = "";

                params.put("id", ID_response);
                url = "http://beaconcontrolflow.altervista.org/delete.php";
                Log.d("CLIENT", "send...");
                client.get(url, params, new TextHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, String res) {
                                // called when response HTTP status is "200 OK"
                                Log.d("CLIENT", "response: " + res);
                                ID_response = -1;
                                tvId.setText("ID on system: " + ID_response);

                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                            }
                        }
                );
            }
        }
    }
    private void enableBLT(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }
        if(request<max_request) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

            } else{
                if(ID_response!=-1){

                }else {
                    tv.setText("Looking for Estimote Beacon (wait more or less 30s if you just are in the range..");
                }
            }
        }else{
            Log.d(DEBUG_TAG,"request more than 4");
        }

    }
    @Override
    //return from  startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT) in enableBLT()
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user open the bluethoot.
                // The Intent's data Uri identifies which contact was selected.
                Log.d(DEBUG_TAG,"L utente ha dato il permesso");
                tv.setText("Looking for Estimote Beacon (wait more or less 30s if you just are in the range..");
                // Do something with the contact here (bigger example below)
            }else{ //if(requestCode == RESULT_CANCELED){
                Log.d(DEBUG_TAG,"L utente non ha dato il permesso");
                request++;

            }
        }
    }


    private void enableBluetoothPermission() {
        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(DemoActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            //shouldShowRequestPermissionRationale() = If this function is called on pre-M, it will always return false.
            if (!ActivityCompat.shouldShowRequestPermissionRationale(DemoActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                showMessageOKCancel("You need to allow access for BLT scanning on Android 6.0 and above.",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(DemoActivity.this,
                                        new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                                        REQUEST_ENABLE_BT);
                            }
                        });

                return;
            }
            //If this function is called on pre-M, OnRequestPermissionsResultCallback will be suddenly called with correct PERMISSION_GRANTED or PERMISSION_DENIED result.
            ActivityCompat.requestPermissions(DemoActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_ENABLE_BT);
            return;
        }
        //once i have got the ACCESS_COARSE_LOCATION i can check if the blt is turns on.
        enableBLT();
    }

    //return from  enableBluetoothPermission()
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    Log.d(DEBUG_TAG,"PERMESSO DATO!!!");
                } else {
                    // Permission Denied
                    Toast.makeText(DemoActivity.this, "PERMISSION_GRANTED Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(DemoActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


    public void Client(){
        ConnectionDetector cd = new ConnectionDetector(this);
        AlertDialogManager alert = new AlertDialogManager();
        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialog(DemoActivity.this,
                    "Internet Connection absent",
                    "Check your internet connection! \nRestart Demo" , false,this);

        }else{
            if(mode.equals(DemoActivity.REGISTER)){
                tv.append("\n\nGood! You are REGISTER, if yuo will leave the beacon's area for another 30s you will be unregister to our system..");
            }else{
                tv.setText("\n\nGood! You are UNREGISTER, if yuo want re-register you have to enter in the beacon's area for another 30s..");
            }

            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            String url="";
            if(mode.equals(DemoActivity.REGISTER)){
                Log.d("CLIENT", DemoActivity.REGISTER);
                params.put("distance", distance + "");
                params.put("mobile", device_model);
                Log.d("CLIENT", "distance: " + distance + " device_model: " + device_model);
                url = "http://beaconcontrolflow.altervista.org/put.php";
                Log.d("CLIENT", "send...");
                client.get(url, params, new TextHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, String res) {
                                // called when response HTTP status is "200 OK"
                                ID_response = Integer.parseInt(res);
                                tvId.setText("ID on system: "+ID_response);
                                Log.d("CLIENT","id: "+res);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                            }
                        }
                );
            }else if (mode.equals(DemoActivity.UNREGISTER)) {
                params.put("id",  ID_response);
                url = "http://beaconcontrolflow.altervista.org/delete.php";
                Log.d("CLIENT", "send...");
                client.get(url, params, new TextHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, String res) {
                                // called when response HTTP status is "200 OK"
                                Log.d("CLIENT", "response: " + res);
                                ID_response=-1;
                                tvId.setText("ID on system: "+ID_response);

                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                            }
                        }
                );
            }
            // params.put("key", "value");      params.put("more", "data");
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_demo, menu);
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

    /** Returns the consumer friendly device name */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        String phrase = "";
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += Character.toUpperCase(c);
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase += c;
        }
        return phrase;
    }


    public class MyCountDownTimer extends CountDownTimer {

        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            int progress = (int) (millisUntilFinished/1000);
            tvRem.setVisibility(View.VISIBLE);
            tvRem.setText(progress+"");
            progressBar.setProgress(progressBar.getMax() - progress);
        }

        @Override
        public void onFinish() {
            progressBar.setProgress(progressBar.getMax());
            tvRem.setText("0");
            progressBar.setVisibility(View.INVISIBLE);
            tvRem.setVisibility(View.INVISIBLE);
            tvMode.setVisibility(View.INVISIBLE);
            //beaconManager.stopRanging(new Region("monitored region",UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), 61272, 53723));

            Client();
        }

    }
}
