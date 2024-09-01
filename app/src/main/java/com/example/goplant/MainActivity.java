package com.example.goplant;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MenuItem;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 123;

    ListView listView;
    DeviceAdapter adapter;
    ArrayList<ArrayList<String>> deviceList;
    Map<String, ArrayList<ArrayList<String>>> deviceHistory;
    ArrayList<ArrayList<String>> hist;
    FirebaseAuth fAuth;
    DatabaseReference mDatabase;
    ActivityResultLauncher<Intent> launchDevice;

    ImageView addDeviceBtn;
    ImageView moreBtn;
    LayoutInflater inflater;
    View addDevice;


    private Handler mHandler;
    AlertDialog.Builder alert;
    AlertDialog ad;
    ESP8266 esp8266;
    private WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_CODE);
        }
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fAuth = FirebaseAuth.getInstance();

        if(fAuth.getCurrentUser() == null){
            Toast.makeText(this, "LMAAOOAOA", Toast.LENGTH_SHORT).show();
        }
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        addDevice = inflater.inflate(R.layout.add_device_window, null);

        esp8266 = new ESP8266(this);
        mHandler = new Handler();
        listView = findViewById(R.id.listDevice);
        addDeviceBtn = findViewById(R.id.addDevice);
        deviceList = new ArrayList<>();
        deviceHistory = new HashMap<>();
        hist = new ArrayList<>();
        moreBtn = findViewById(R.id.more);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("devices");

        launchDevice = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == RESULT_CANCELED){
                        startLoop1();
                    }
                });

        getDevice();
        startLoop1();

        addDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popUpDevice();
                startLoop2();
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(MainActivity.this,moreBtn);
                popup.inflate(R.menu.menu_more);
                popup.getMenu().add(0,0,0,"DD");
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int option;
                        option = menuItem.getItemId();

                        switch (option){
                            case R.id.logout:
                                fAuth.signOut();
                                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                finish();
                                return true;
                            case R.id.insert:
                                scanWifi();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popup.show();
            }
        });

        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);


    }

    public void getDevice(){
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                deviceList.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String key;

                    ArrayList<String> data = new ArrayList<>();
                    data.add(snapshot.getKey());
                    key = snapshot.getKey();

                    data.add(snapshot.child("temp").getValue().toString());
                    data.add(snapshot.child("humidity").getValue().toString());
                    data.add(snapshot.child("soil").getValue().toString());
                    data.add(snapshot.child("date").getValue().toString());
                    data.add(snapshot.child("time").getValue().toString());
                    if(hist.size()!=0) {
                        deviceHistory.put(key, hist);
                    }
                    deviceList.add(data);
                }
                adapter = new DeviceAdapter(MainActivity.this, MainActivity.this, deviceList, mDatabase, launchDevice);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                error.getMessage();
            }
        });
    }

    public void scanWifi(){
        wifiManager.startScan();
        List<ScanResult> results = wifiManager.getScanResults();
        ArrayList<String> wifi = new ArrayList<>();
        for (ScanResult result : results) {
            System.out.println(result.SSID);
            wifi.add(result.SSID);
        }
        Toast.makeText(MainActivity.this, wifi.toString(), Toast.LENGTH_SHORT).show();
    }

    public void startLoop1(){
        mRunnable1.run();
    }
    public void startLoop2(){ //Find esp device
        mRunnable2.run();
    }

    public void stopLoop1(){
        mHandler.removeCallbacks(mRunnable1);
    }
    public void stopLoop2(){
        mHandler.removeCallbacks(mRunnable2);
    }

    private final Runnable mRunnable1 = new Runnable() {
        @Override
        public void run() {
            getDevice();
            if(esp8266.getResponse()!=null){
                Log.d("Response", esp8266.getResponse());
            }
            if(esp8266.isFound()) {
                changeAddDeviceAttr(1);
                stopLoop2();
            }
            mHandler.postDelayed(this,2000);
        }
    };

    private final Runnable mRunnable2 = new Runnable() {
        @Override
        public void run() {
            Log.e("DONE", String.valueOf(esp8266.isDone()));
            esp8266.findEsp();
            mHandler.postDelayed(this,10500);
        }
    };

    public void popUpDevice(){
        EditText ssidTxt, passTxt;
        Button submitBtn;

        ssidTxt = addDevice.findViewById(R.id.ssid);
        passTxt = addDevice.findViewById(R.id.password);
        submitBtn = addDevice.findViewById(R.id.submitBtn);

        if(addDevice.getParent() != null) {
            ((ViewGroup)addDevice.getParent()).removeView(addDevice);
        }

        alert = new AlertDialog.Builder(MainActivity.this, R.style.CustomAlertDialog);
        alert.setView(addDevice);
        alert.setCancelable(true);
        alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Log.d("CANCEL","TRUE");
                stopLoop2();
                esp8266.reset();
                changeAddDeviceAttr(0);
            }
        });

        ad = alert.show();

        ssidTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(MainActivity.this,ssidTxt);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int option;
                        option = menuItem.getItemId();

                        switch (option){
                            case 0:
                                fAuth.signOut();
                                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                finish();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popup.show();
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.setCancelable(true);
                stopLoop2();
                String ssid = String.valueOf(ssidTxt.getText());
                String password = String.valueOf(passTxt.getText());
                Toast.makeText(MainActivity.this, "PRESSED", Toast.LENGTH_SHORT).show();
                ad.dismiss();
                esp8266.sendWiFiInfo(ssid, password);
                changeAddDeviceAttr(0);
                esp8266.reset();

            }
        });
    }

    public void changeAddDeviceAttr(int i){
        TextView message;
        EditText ssidTxt, passTxt;

        ssidTxt = addDevice.findViewById(R.id.ssid);
        passTxt = addDevice.findViewById(R.id.password);
        message = addDevice.findViewById(R.id.message);

        if(i==1){
            Log.d("CHANGE : " , String.valueOf(i));
            addDevice.findViewById(R.id.wifiLoading).setVisibility(View.GONE);
            addDevice.findViewById(R.id.wifiForm).setVisibility(View.VISIBLE);
            message.setText("Menambahkan Perangkat");
        } else if (i==0){
            Log.d("CHANGE : " , String.valueOf(i));
            addDevice.findViewById(R.id.wifiLoading).setVisibility(View.VISIBLE);
            addDevice.findViewById(R.id.wifiForm).setVisibility(View.GONE);
            ssidTxt.setText("");
            passTxt.setText("");
            message.setText("Mencari Perangkat");
        }
    }

}