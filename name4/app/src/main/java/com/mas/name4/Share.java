package com.mas.name4;

import static com.mas.name4.ScreenShot.shoot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Share extends AppCompatActivity {


    Button btn_back, btn_share;
    TextView tv1, tv2;
    ImageView img_view1, img_view2;


    String mod_name = "";
    String mod_time = "";
    String mod_func = "";
    String mod_funcimg = "";
    String mod_num = "";
    double a = 0f;
    double b = 0f;
    double loss = 0f;
    double yuzhi = 0f;
    boolean dabiao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }


        mod_name = (String) getIntent().getExtras().get("name");
        mod_time = (String) getIntent().getExtras().get("time");
        mod_func = (String) getIntent().getExtras().get("func");
        mod_funcimg = (String) getIntent().getExtras().get("funcimg");
        mod_num = (String) getIntent().getExtras().get("number");
        a = Double.valueOf((String) getIntent().getExtras().get("a"));
        b = Double.valueOf((String) getIntent().getExtras().get("b"));
        loss = Double.valueOf((String) getIntent().getExtras().get("loss"));
        yuzhi = Double.valueOf((String) getIntent().getExtras().get("yuzhi"));
        dabiao = Boolean.valueOf((String) getIntent().getExtras().get("dabiao"));


        img_view2 = findViewById(R.id.img_view2);
        img_view2.setImageBitmap(Predict.pre_to_share_bm.bitmap);

        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Predict.pre_to_share_bm.bitmap = null;
                Intent intent = new Intent();
                intent.setAction("share_to_predict");
                intent.putExtra("name", mod_name);
                intent.putExtra("time", mod_time);
                intent.putExtra("func", mod_func);
                intent.putExtra("funcimg", mod_funcimg);
                intent.putExtra("number", mod_num);
                intent.putExtra("a", String.valueOf(a));
                intent.putExtra("b", String.valueOf(b));
                intent.putExtra("loss", String.valueOf(loss));
                intent.setClass(Share.this, Predict.class);
                startActivity(intent);
                Share.this.finish();
            }
        });





        btn_share = findViewById(R.id.btn_share);
        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String imageshot = shoot(Share.this);

                Intent intent = new Intent(Intent.ACTION_SEND);
                File file = new File(imageshot);
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("image/*");
                Intent chooser = Intent.createChooser(intent, "Share screen shot");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(chooser);
                }
            }
        });


        tv1 = findViewById(R.id.tv1);
        tv2 = findViewById(R.id.tv2);
        tv2.setText(" (" + getIntent().getExtras().get("yuzhi").toString() + ")");
        if(dabiao){
            tv1.setText("达 标");
            tv1.setTextColor(Color.parseColor("#03FD2C"));
        }
        else {
            tv1.setText("不达标");
            tv1.setTextColor(Color.parseColor("#F10606"));
        }



        initChart(a, b);

    }

    private final List<Entry> listLineData = new ArrayList<>();

    private void initChart(double a, double b) {

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
        double max_X = 0;
        double max_Y = 0;
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
        xAxis.setAxisMaximum((float) max_X + 5);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value + "";
            }
        });

        yAxis.setAxisMinimum(0);
        yAxis.setAxisMaximum((float) max_Y);
        yAxis.setDrawGridLines(false);
        yAxis.setDrawAxisLine(true);
        yAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value + "";
            }
        });


    }
}