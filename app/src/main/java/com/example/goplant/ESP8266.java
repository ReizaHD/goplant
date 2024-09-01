package com.example.goplant;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class ESP8266 {
    private final String URL = "http://192.168.4.1";

    private boolean found = false;
    private boolean requestDone = false;
    private boolean sent = false;
    private String resp = null;
    private Handler mHandler;

    private AlertDialog ad;
    private AlertDialog.Builder alert;
    private Context ctx;

    public ESP8266(Context ctx) {
        this.ctx = ctx;
        mHandler = new Handler();
        loading.run();
    }

    Runnable loading = new Runnable() {
        @Override
        public void run() {
            if(sent){
                ad.dismiss();
                resp = null;
            }
            mHandler.postDelayed(this, 2000);
        }
    };

    public boolean isFound(){
        return found;
    }

    public boolean isDone(){
        return requestDone;
    }

    public String getResponse(){
        return  resp;
    }

    public void reset(){
        found = false;
        requestDone = false;
    }

    public void findEsp(){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        StringRequest sendData = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                found = true;
                requestDone = true;
                Toast.makeText(ctx, "SUCCESS", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                found = false;
                requestDone = true;
                Toast.makeText(ctx, "FAILED", Toast.LENGTH_SHORT).show();

            }
        });
        queue.add(sendData);
    }

    public void sendWiFiInfo(String ssid, String password){
        RequestQueue queue = Volley.newRequestQueue(ctx);
        StringRequest sendData = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                resp = "Berhasil";
                sent = true;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                resp = "Gagal";
                sent = true;
            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String ,String> data = new HashMap<>();
                data.put("ssid", ssid);
                data.put("password", password);
                return data;
            }
        };
        sendData.setShouldCache(false);
        queue.add(sendData);

        ProgressBar pb = new ProgressBar(ctx);
        alert = new AlertDialog.Builder(ctx);
        if(pb.getParent() != null)
            ((ViewGroup)pb.getParent()).removeView(pb);
        alert.setView(pb);
        alert.setCancelable(false);
        alert.setTitle("Mengirim Data");
        ad = alert.show();
    }


}
