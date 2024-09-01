package com.example.goplant;

import android.annotation.SuppressLint;
import android.app.LauncherActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.app.Activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;

import com.example.goplant.object.History;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DeviceAdapter extends BaseAdapter {

    Context ctx;
    ArrayList<ArrayList<String>> list;
    LayoutInflater inflater;
    ArrayList<History> history;
    ActivityResultLauncher<Intent> launcher;
    MainActivity ctxClass;
//    Map<String,ArrayList<ArrayList<String>>> history;
    DatabaseReference db;
    boolean done = false;

    public DeviceAdapter(MainActivity ctxClass, Context ctx, ArrayList<ArrayList<String>> list, DatabaseReference db,  ActivityResultLauncher<Intent> launcher) {

        this.ctx = ctx;
        this.list = list;
        this.inflater = LayoutInflater.from(ctx);
//        this.history = history;
        this.db= db;
        this.launcher = launcher;
        history = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ArrayList<String> data = list.get(i);
        view = inflater.inflate(R.layout.list_device, null);
        TextView txtNama = view.findViewById(R.id.textNama);
        TextView txtTemp = view.findViewById(R.id.tempVal);
        TextView txtHumid = view.findViewById(R.id.humidVal);
        TextView txtSoil = view.findViewById(R.id.soilVal);
        TextView nomor = view.findViewById(R.id.nomor);
        RelativeLayout alert = view.findViewById(R.id.alert);

        String[] date = data.get(4).split("-");
        String[] time = data.get(5).split(":");

        System.out.println("Date: " + Arrays.toString(date));
        System.out.println("Time: " + Arrays.toString(time));




        LocalDateTime lastUpdated = LocalDateTime.of(Integer.parseInt(date[2]),
                Integer.parseInt(date[1]),
                Integer.parseInt(date[0]),
                Integer.parseInt(time[0]),
                Integer.parseInt(time[1]),
                Integer.parseInt(time[2]));

//        LocalDateTime lastUpdated = LocalDateTime.of(2003,02,11,12,0,10);

        long duration = ChronoUnit.SECONDS.between(lastUpdated, LocalDateTime.now());

        String temp = data.get(1)+"\u00B0C";
        String humid = data.get(2)+"%";
        String soil = data.get(3)+"%";

        nomor.setText(String.valueOf(i + 1));
        txtNama.setText(data.get(0)); // Nama Device

        if(duration < 60) {
            alert.setVisibility(View.INVISIBLE);
            txtTemp.setText(temp); // Suhu Udara
            txtHumid.setText(humid); // Kelembapan Udara
            txtSoil.setText(soil); // Kelembapan Tanah
        } else {
            alert.setVisibility(View.VISIBLE);
        }
//        History history = new History(this.history);



        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(ctx, DeviceActivity.class);
//                ctxClass.stopLoop1();
//                launcher.launch(intent);
                getHistory("D-0001");
            }
        });

        return view;
    }

    public void getHistory(String key){
        System.out.println(key);

        db.child(key).child("history").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String t,h,s,date;
                for(DataSnapshot data : snapshot.getChildren()){
                    if(!data.getKey().equals("lastUpdated")) {
                        t = data.child("temp").getValue() != null ? data.child("temp").getValue().toString() : "N/A";
                        h = data.child("humidity").getValue() != null ? data.child("humidity").getValue().toString() : "N/A";
                        s = data.child("soil").getValue() != null ? data.child("soil").getValue().toString() : "N/A";
                        date = data.getKey();
                        System.out.println(date + " : " + s + " " + h + " " + t);
                        history.add(new History(s, h, t, date));
                    }
                }

                done = true;
                System.out.println("size : "+history.size());
                Intent intent = new Intent(ctx, DeviceActivity.class);
                intent.putExtra("history", history);
//                ctxClass.stopLoop1();
                launcher.launch(intent);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
