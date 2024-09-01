package com.example.goplant;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.goplant.object.History;
import com.example.goplant.util.Epoch;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceActivity extends AppCompatActivity {

    ArrayList<History> obj;
    DeviceAdapterX adapter;
    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        list = findViewById(R.id.history);
        Intent extras = getIntent();
        if (extras != null) {
            System.out.println("not null");
            obj = (ArrayList<History>) extras.getSerializableExtra("history"); //Obtaining data
            System.out.println(obj.size());

        }
        adapter = new DeviceAdapterX(DeviceActivity.this, obj);
        list.setAdapter(adapter);
    }
}

class DeviceAdapterX extends BaseAdapter{

    Context ctx;
    ArrayList<History> obj;
    LayoutInflater inflater;

    public DeviceAdapterX(Context ctx, ArrayList<History> obj) {
        this.ctx = ctx;
        this.obj = obj;
        this.inflater = LayoutInflater.from(ctx);
    }

    @Override
    public int getCount() {
        return obj.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        String date, epoch, time;

        view = inflater.inflate(R.layout.list_history, null);
        TextView tgl = view.findViewById(R.id.textTgl);
        TextView temp = view.findViewById(R.id.tempVal);
        TextView soil = view.findViewById(R.id.soilVal);
        TextView humid = view.findViewById(R.id.humidVal);

        epoch = obj.get(i).getDate();

        date = Epoch.toDate(epoch,"dd-MM-yyyy");
        time = Epoch.toTime(epoch,"HH:mm:ss");
        System.out.println(date);
        System.out.println(time);

        tgl.setText(date + " " + time);
        temp.setText(obj.get(i).getTemp());
        soil.setText(obj.get(i).getSoil());
        humid.setText(obj.get(i).getHumidity());
        return view;
    }
}