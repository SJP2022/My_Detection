package com.mas.name4;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class MainActivity extends AppCompatActivity {



    static class MapData {
        public static List<Map<String, String>> subject = new ArrayList<>();

    }


    class StoreLocal {
        public List<Map<String,String>> subject2 = new ArrayList<>();

        public void save_local() {
            int i;
            for(i = 0; i < MapData.subject.size(); i++){
                subject2.add(MapData.subject.get(i));
            }



        }

        public void save_to_MapData() {



            int i;
            MainActivity.MapData.subject.clear();
            for(i = 0;i < subject2.size();i++) {
                MapData.subject.add(subject2.get(i));
            }


        }

    }


    public void to_local() {
        StoreLocal sl = new StoreLocal();
        sl.save_local();

        Gson gson = new Gson();
        String s = gson.toJson(sl);
        try {
            FileOutputStream fos = openFileOutput("MapData.txt",MODE_PRIVATE);
            PrintStream ps = new PrintStream(fos);
            ps.print(s);
            ps.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void to_subject() {
        StoreLocal sl = null;

        StringBuilder sb = new StringBuilder("");
        try {
            FileInputStream fis = MainActivity.this.openFileInput("MapData.txt");
            byte[] buff = new byte[1024];
            int hasRead = 0;
            while((hasRead = fis.read(buff)) > 0) {
                sb.append(new String(buff,0,hasRead));
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        sl = gson.fromJson(sb.toString(), StoreLocal.class);
        if(sl != null)
            sl.save_to_MapData();
    }

    Button btn_new;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        initView(0);
        //Toast.makeText(this,MapData.subject.toString(),Toast.LENGTH_LONG).show();
        to_subject();
        refresh_lv();


        btn_new = findViewById(R.id.btn_new);
        btn_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,CreateMod.class);
                startActivity(intent);
                MainActivity.this.finish();
            }
        });
    }



    private void initView(int cnt) {


        Map tmpmap = null;
        //Log.d("sub_size",subject.size()+"");
        int now = MapData.subject.size();
        for (int i = 0; i < cnt; i++) {
            tmpmap = new HashMap();
            tmpmap.put("number", String.valueOf(i+1));
            tmpmap.put("name", "haha");
            tmpmap.put("time","");
            tmpmap.put("func","");
            tmpmap.put("funcimg","");
            tmpmap.put("a","0");
            tmpmap.put("b","0");
            tmpmap.put("loss","0");
            MapData.subject.add(tmpmap);
        }


        ListView listview = findViewById(R.id.lv_main);
        MyAdapter adapter = new MyAdapter(MainActivity.this);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {






                //Toast.makeText(MainActivity.this,"the num is::"+subject.get(i).get("number"),Toast.LENGTH_LONG).show();
                String str = "序号：" + MapData.subject.get(i).get("number");
                str += "\n名称：";
                str += MapData.subject.get(i).get("name");
                str += "\n创建时间：";
                str += MapData.subject.get(i).get("time");


                new AlertDialog.Builder(MainActivity.this).setTitle("模型基本信息").
                        setMessage(str)
                        .setPositiveButton("使用它!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int ii) {


                                Intent intent = new Intent();
                                intent.setAction("main_to_mod_info");
                                //intent.putExtra("data",MapData.subject.get(i).toString());
                                intent.putExtra("name",MapData.subject.get(i).get("name"));
                                intent.putExtra("time",MapData.subject.get(i).get("time"));
                                intent.putExtra("func",MapData.subject.get(i).get("func"));
                                intent.putExtra("funcimg",MapData.subject.get(i).get("funcimg"));
                                intent.putExtra("number",MapData.subject.get(i).get("number"));
                                intent.putExtra("a",MapData.subject.get(i).get("a"));
                                intent.putExtra("b",MapData.subject.get(i).get("b"));
                                intent.putExtra("loss",MapData.subject.get(i).get("loss"));
                                intent.setClass(MainActivity.this, Mod_info.class);
                                startActivity(intent);
                                MainActivity.this.finish();

                            }
                        })
                        .setNegativeButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int ii) {

                                MapData.subject.remove(i);
                                to_local();
                                //Toast.makeText(MainActivity.this,subject.toString(),Toast.LENGTH_LONG).show();
                                refresh_lv();
                                //Toast.makeText(MainActivity.this,subject.toString(),Toast.LENGTH_LONG).show();
                            }
                        }).show();
            }
        });

    }


    public void refresh_lv(){
        //List<Map<String, String>> maptmp = new ArrayList<>();
        int i;
        for(i = 0;i < MapData.subject.size();i++){
            if(MapData.subject.get(i).get("number").equals(String.valueOf(i + 1))){
                continue;
            }
            MapData.subject.get(i).put("number",String.valueOf(i + 1));
        }

        ListView listview = findViewById(R.id.lv_main);
        MyAdapter adapter = new MyAdapter(MainActivity.this);
        listview.setAdapter(adapter);
    }


    class MyAdapter extends BaseAdapter {
        Context context;

        MyAdapter(Context context)
        {
            this.context=context;
        }

        @Override
        public int getCount() {
            return MapData.subject.size();
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {

            view = LayoutInflater.from(context).inflate(R.layout.item,null);

            TextView num = view.findViewById(R.id.num);
            TextView cre = view.findViewById(R.id.cre);
            int no = Integer.valueOf(MapData.subject.get(i).get("number"));
            num.setText(String.valueOf(no));
            cre.setText(MapData.subject.get(i).get("name"));

            return view;
        }

    }


}

