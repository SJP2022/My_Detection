package com.mas.name4;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Mod_info extends AppCompatActivity {

    TextView textView;
    Button btn_back, btn_share, btn_predict;
    ImageButton img_btn;
    Uri picuri;
    ImageView imgv;


    String mod_name = "dasa";
    String mod_time = "";
    String mod_func = "";
    String mod_funcimg = "";
    String mod_num = "";
    double a = 0f;
    double b = 0f;
    double loss = 0f;


    private String imagePath = "";

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mod_info);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        initPhotoError();
        //Toast.makeText(Predict.this,getIntent().getExtras().get("data")+"",Toast.LENGTH_LONG).show();





        //if(getIntent().getAction().equals("main_to_mod_info")) {
            mod_name = (String) getIntent().getExtras().get("name");
            mod_time = (String) getIntent().getExtras().get("time");
            mod_func = (String) getIntent().getExtras().get("func");
            mod_funcimg = (String) getIntent().getExtras().get("funcimg");
            mod_num = (String) getIntent().getExtras().get("number");
            a = Double.valueOf((String) getIntent().getExtras().get("a"));
            b = Double.valueOf((String) getIntent().getExtras().get("b"));
            loss = Double.valueOf((String) getIntent().getExtras().get("loss"));
        //}





        textView = findViewById(R.id.tv);
        String tv_show = " 名称：" + mod_name;
        tv_show += "\n 函数：" + mod_func;
        tv_show += "\n 创建时间：" + mod_time;
        tv_show += "\n 序号：" + mod_num;

        tv_show = " 序号：" + mod_num;
        tv_show += "\n 名称：" + mod_name;
        tv_show += "\n 创建时间：" + mod_time;
        tv_show += "\n 函数：" + mod_func;
        tv_show += "\n 百分比误差：" + String.valueOf((double)Math.round(loss * 10000) / 100) + " %";


        textView.setText(tv_show);

        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(Mod_info.this, MainActivity.class);
                startActivity(intent);
                Mod_info.this.finish();
            }
        });




        btn_predict = findViewById(R.id.btn_predict);
        btn_predict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction("mod_info_to_preditc");
                intent.putExtra("name", mod_name);
                intent.putExtra("time", mod_time);
                intent.putExtra("func", mod_func);
                intent.putExtra("funcimg", mod_funcimg);
                intent.putExtra("number", mod_num);
                intent.putExtra("a", String.valueOf(a));
                intent.putExtra("b", String.valueOf(b));
                intent.putExtra("loss", String.valueOf(loss));
                intent.setClass(Mod_info.this, Predict.class);
                startActivity(intent);
                Mod_info.this.finish();
            }
        });



        initChart(a, b);

    }



    private final List<Entry> listLineData = new ArrayList<>();
    private final List<Entry> listData = new ArrayList<>();

    private void initChart(double a, double b){

        double min_X;
        double max_X;
        double max_Y;
        if(a<0){
            min_X = 4;
            max_X = (0.01-b)/a;
            max_Y = b;
        }
        else if(a>0) {
            min_X = (0.01-b)/a;
            max_X = (min_X<75)? 75: min_X+5;
            max_Y = b+a*max_X;
        }
        else{
            min_X = 0;
            max_X = 75;
            max_Y = b*2;
        }
        listLineData.add(new Entry((float)min_X,(float)(b+a*min_X)));
        listLineData.add(new Entry((float)max_X,(float)(b+a*max_X)));


/*
        //double max_X = 0;
        //double max_Y = 0;
        if(a<0){
            for(double i=4 ; b + a*i > 0.01 ; i+=0.01) {
                listLineData.add(new Entry((float)i,(float)(b+a*i)));
                max_X = i;
            }
            max_Y = b;
        }
        else{
            double j = (0.01-b)/a;
            max_X = (j<75)? 75: j+5;
            for(double i=j ; i<max_X ; i+=0.01) {
                listLineData.add(new Entry((float)i,(float)(b+a*i)));
                max_Y = b+a*i;
            }
        }

 */

//        一些初始化参数，大部分样式都可以参照官方文档进行修改
        LineChart lineChart = findViewById(R.id.lc);
        LineDataSet lineDataSet = new LineDataSet(listLineData, "折线图");

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        YAxis yAxis = lineChart.getAxisLeft();
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineDataSet.setColor(Color.RED);


        lineDataSet.setDrawCircles(false);

        lineDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "";
            }
        });

        // 隐藏不希望显示部分，包括坐标轴标题、刻度等等，并禁用了双指放大等功能
        lineChart.getAxisRight().setEnabled(false);
        //lineChart.getAxisLeft().setEnabled(true);
        lineChart.getLegend().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setTouchEnabled(false);

        xAxis.setDrawLabels(true);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum((float) max_X+5);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f",value)+"";
            }
        });

        yAxis.setAxisMinimum(0);
        yAxis.setAxisMaximum((float) max_Y);
        yAxis.setDrawGridLines(false);
        yAxis.setDrawAxisLine(true);
        yAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f",value)+"";
            }
        });


        //-------散点图，画不出来

/*
        for(int i = 0;i < x.length; i += 1){
            Log.v("x", String.valueOf(i));
            Log.v("y", String.valueOf(i));
            listData.add(new Entry((float) x[i],(float) y[i]));
        }
//        添加散点图，参数基本和折线图一样
        ScatterChart scatterChart = findViewById(R.id.sc);
        ScatterDataSet scatterDataSet = new ScatterDataSet(listData, "散点图");

        ScatterData scatterData = new ScatterData(scatterDataSet);
        scatterChart.setData(scatterData);
        xAxis = scatterChart.getXAxis();
        yAxis = scatterChart.getAxisLeft();
        scatterChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        scatterDataSet.setColor(Color.parseColor("#1886f7"));
//        这里需要注意，将散点图的背景色改为透明
        scatterChart.setBackgroundColor(Color.parseColor("#00000000"));

        scatterDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "";
            }
        });

        // 隐藏不希望显示部分，包括坐标轴标题、刻度等等，并禁用了双指放大等功能
        scatterChart.getAxisRight().setEnabled(false);
        scatterChart.getLegend().setEnabled(false);
        scatterChart.getDescription().setEnabled(false);
        scatterChart.setScaleEnabled(false);
        scatterChart.setTouchEnabled(false);

        xAxis.setAxisMinimum(0);
        //xAxis.setAxisMaximum(45);
        xAxis.setAxisMaximum((float) (max_X+5));
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value+"";
            }
        });

        yAxis.setAxisMinimum(0);
        yAxis.setAxisMaximum((float) b);
        //yAxis.setAxisMaximum(120);
        yAxis.setDrawGridLines(false);
        yAxis.setDrawAxisLine(true);
        yAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value+"";
            }
        });

 */






        /*
        LineData barData = lineChart.getData();
        if (barData != null) {
            // 这是横坐标的单位 limitLine X Unit
            // 注意这里 limit 高度设置要和 Chart 的高度匹配上
            LimitLine lmtLineXUnit = new LimitLine(barData.getYMin() - (float) 0.01, "Gray Value");
            lmtLineXUnit.setLineWidth(0);
            lmtLineXUnit.setLineColor(Color.alpha(0));
            lmtLineXUnit.setTextColor(Color.BLACK);
            lmtLineXUnit.setTextSize(20f);
            lmtLineXUnit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
            if (lmtLineXUnit.isDashedLineEnabled()) {
                lmtLineXUnit.enableDashedLine(1f, 3f, 0);
            } else {
                lmtLineXUnit.disableDashedLine();
            }

            //这是纵坐标的单位 limitLine Y Unit
            LimitLine lmtLineYUnit = new LimitLine(barData.getYMax() + (float) 0.01, "Drug Concentration");
            lmtLineYUnit.setLineWidth(0);
            lmtLineYUnit.setLineColor(Color.alpha(0));
            lmtLineYUnit.setTextColor(Color.BLACK);
            lmtLineYUnit.setTextSize(20f);
            lmtLineYUnit.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_BOTTOM);
            if (lmtLineYUnit.isDashedLineEnabled()) {
                lmtLineYUnit.enableDashedLine(1f, 3f, 0);
            } else {
                lmtLineYUnit.disableDashedLine();
            }

            YAxis leftAxis = lineChart.getAxisLeft();
            leftAxis.setDrawLimitLinesBehindData(false);
            // 绘图前先清除先前的限制线，防止叠加绘制
            leftAxis.removeAllLimitLines();
            // 添加限制线
            leftAxis.addLimitLine(lmtLineXUnit);
            leftAxis.addLimitLine(lmtLineYUnit);
        }
        lineChart.invalidate();
         */
    }





    private void initPhotoError(){
        // android 7.0系统解决拍照的问题
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
    }


    //截取屏幕的方法
    private void screenshot() {
        // 获取屏幕
        View dView = getWindow().getDecorView();
        dView.setDrawingCacheEnabled(true);
        dView.buildDrawingCache();
        Bitmap bmp = dView.getDrawingCache();
        if (bmp != null)
        {
            try {
                // 获取内置SD卡路径
                String sdCardPath = Environment.getExternalStorageDirectory().getPath();
                // 图片文件路径
                imagePath = sdCardPath + File.separator + "screenshot.png";
                File file = new File(imagePath);
                FileOutputStream os = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
                Log.v("abcdef",bmp.toString()+"");
                //os.flush();
                //os.close();

            } catch (Exception e) {
            }
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1:
                if(resultCode == RESULT_OK){

                    Toast.makeText(getApplicationContext(),"ok",Toast.LENGTH_LONG).show();
                    Bitmap bm = null;
                    try {
                        bm = BitmapFactory.decodeStream(getContentResolver().openInputStream(picuri));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    //int i = (int) getIntent().getLongExtra("pic_num",30);
                    int i = 0;
                    if(CreateMod.tag.flag == true){
                        CreateMod.tag.flag = false;
                        i = CreateMod.tag.num;
                    }
                    Log.v("pic_num","i:"+i);
                    //Log.v("extrasss","i:"+data.getExtras().getString("pic_num"));
                    img_btn.setImageBitmap(bm);


                }
                break;

            case 2:
                if(resultCode == RESULT_OK){

                    String imagePath = null;
                    Uri uri = data.getData();
                    if (DocumentsContract.isDocumentUri(this, uri)) {
                        //如果是document类型的uri，则通过document id处理
                        String docId = DocumentsContract.getDocumentId(uri);
                        if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                            String id = docId.split(":")[1];
                            String selection = MediaStore.Images.Media._ID + "=" + id;
                            imagePath = getImagePath( MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
                        } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                            Uri contentUri = ContentUris.withAppendedId(Uri.parse("content:" +
                                    "//downloads/public_downloads"), Long.valueOf(docId));
                            imagePath = getImagePath(contentUri, null);
                        }
                    } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                        //如果是content类型的uri，则使用普通方式处理
                        imagePath = getImagePath(uri, null);
                    } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                        //如果是File类型的uri，直接获取图片路径即可
                        imagePath = uri.getPath();
                    }


                    if(imagePath != null){
                        Bitmap bm=BitmapFactory.decodeFile(imagePath);
                        //将图片放置在控件上
                        int i = 0;
                        img_btn.setImageBitmap(bm);
                        Log.v("pic_num","i:"+i);
                        //Log.v("extrasss","i:"+data.getExtras().getString("pic_num"));


                    }
                }
                break;
            default:break;
        }
    }

    @SuppressLint("Range")
    private String getImagePath(Uri uri, String selection){
        String path=null;
        Cursor cursor=getContentResolver().query(uri,null,selection,null,null);
        if(cursor!=null){
            if(cursor.moveToFirst()){
                path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }



}


