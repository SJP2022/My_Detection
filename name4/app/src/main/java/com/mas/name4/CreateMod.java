package com.mas.name4;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.gson.Gson;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateMod extends AppCompatActivity {

    private static final int TAKE_PICTURE_FROM_ALBUM = 2;
    private static final int TAKE_PICTURE_FROM_CAMERA = 1;
    private static final int PICTURE_CROP_CODE = 3;


    Button btn_add;
    Button btn_cancel, btn_cal;
    EditText et_newname;
    Uri picuri;
    List<Map<String, String>> subject = new ArrayList<>();
    //List<Map<String,Bitmap>> bm_list = new ArrayList<>();
    HashMap<String,Bitmap> bm_hashmap = new HashMap<>();
    int scrollTop,scrollPos;

    List<Double> huidu_for_sjp = new ArrayList<>();
    List<Double> nongdu_for_sjp = new ArrayList<>();

    List<Double> gray = new ArrayList<>(); //灰度
    List<Double> potency = new ArrayList<>(); //浓度

    public static class tag{
        public static boolean flag = false;
        public static int num = -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_mod);



        //取消严格模式  FileProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy( builder.build() );
        }



        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        initView(3);




        if (ContextCompat.checkSelfPermission(CreateMod.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(CreateMod.this, Manifest.permission.CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CreateMod.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA}, 2);
        }

        btn_cancel = findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(CreateMod.this,MainActivity.class);
                startActivity(intent);
                CreateMod.this.finish();
            }
        });

        et_newname = findViewById(R.id.et_newname);

        btn_cal = findViewById(R.id.btn_calculate);
        btn_cal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newname = et_newname.getText().toString().trim();
                if (newname.equals("")) {
                    new AlertDialog.Builder(CreateMod.this).setTitle("未输入模型名称")
                            .setMessage("请输入名称！")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                } else {
                    //Toast.makeText(CreateMod.this,subject.toString()+"",Toast.LENGTH_LONG).show();
                    huidu_for_sjp.clear();
                    nongdu_for_sjp.clear();
                    int cnt = 0;
                    for (int i = 0; i < subject.size(); i++) {
                        String tmp_huidu = subject.get(i).get("huidu");
                        String tmp_nongdu = subject.get(i).get("nongdu");
                        if (!tmp_nongdu.equals("") && !tmp_huidu.equals("")) {
                            huidu_for_sjp.add(Double.valueOf(tmp_huidu));
                            nongdu_for_sjp.add(Double.valueOf(tmp_nongdu));
                            cnt += 1;
                        }
                    }

                    if(huidu_for_sjp.size()<=1){
                        new AlertDialog.Builder(CreateMod.this).setTitle("提示")
                                .setMessage("输入数据的数量不足2个，无法拟合模型！")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                }).show();
                        return;
                    }

                    new AlertDialog.Builder(CreateMod.this).setTitle("提示")
                            .setMessage("成功读取到 "+cnt+" 份样本，是否继续？")
                                    .setPositiveButton("继续", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            cal();
                                        }
                                    })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).show();

                }
            }


        });

        btn_add = findViewById(R.id.btn_add);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add_line();
            }
        });

    }

    class StoreLocal {
        public List<Map<String,String>> subject2 = new ArrayList<>();

        public void save_local() {
            int i;
            for(i = 0; i < MainActivity.MapData.subject.size(); i++){
                subject2.add(MainActivity.MapData.subject.get(i));
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


    private void cal(){
        int len = huidu_for_sjp.size();
        double[] x = new double[len];
        double[] y = new double[len];
        for (int i=0;i<len;i++){
            x[i] = huidu_for_sjp.get(i).doubleValue();
            y[i] = nongdu_for_sjp.get(i).doubleValue();
        }

        double sumXY = 0;
        double sumX = 0;
        double sumY = 0;
        double sumX2 = 0;
        double sumY2 = 0;
        for(int i=0;i<x.length;i++) {
            sumXY += x[i]*y[i];
            sumX += x[i];
            sumY += y[i];
            sumX2 += x[i]*x[i];
            sumY2 += y[i]*y[i];
        }
        double numerator = x.length*sumXY - sumX*sumY;
        double denominator = Math.sqrt(x.length*sumX2-sumX*sumX)*Math.sqrt(y.length*sumY2-sumY*sumY);
        double R = numerator/denominator;

        if(Math.abs(R)<0.3){
            new AlertDialog.Builder(CreateMod.this).setTitle("提示")
                    .setMessage("输入数据的相关性太弱，无法拟合模型，请检查输入！")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).show();
            return;
        }


        double[] expx = {

                76.14404844,
                75.44677253,
                74.05817662,
                74.01384969,
                64.93145076,
                53.33592964,
                45.32225338,
                40.62907862,
                30.18355644

        } ;
        double[] expy = {
                0.1,
                0.5,
                1,
                2,
                5,
                8,
                10,
                12,
                15 } ;
        //---------------matters a lot-------------------
        //Map<String,String> mp = Linear.Cal(expx,expy);
        Map<String,String> mp = Linear.Cal(x,y);

        double aa = Double.valueOf(mp.get("a")), bb = Double.valueOf(mp.get("b")), lossss = Double.valueOf(mp.get("loss"));
        if(Double.isNaN(aa)||Double.isNaN(bb)){
            new AlertDialog.Builder(CreateMod.this).setTitle("提示")
                    .setMessage("输入数据无法拟合出单值函数模型，请检查输入！")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).show();
            return;
        }

        Map<String,String> tmpmap = new HashMap<>();

        tmpmap.put("number", String.valueOf(MainActivity.MapData.subject.size()));
        tmpmap.put("name", et_newname.getText().toString().trim());

        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日_HH:mm:ss_E");
        String sim = dateFormat.format(date);

        tmpmap.put("time",sim);

        //double aa = Double.valueOf(mp.get("a")), bb = Double.valueOf(mp.get("b")), lossss = Double.valueOf(mp.get("loss"));
        String aa_str = String.format("%.3f",aa), bb_str = String.format("%.3f",bb), lossss_str = String.format("%.5f",lossss);
        String func = "y = " + aa_str + "x";
        if(bb > 0){
            func += " + " + bb_str;
        }
        else {
            func += bb_str;
        }

        tmpmap.put("func",func);
        tmpmap.put("funcimg","");
        tmpmap.put("a",aa_str);
        tmpmap.put("b",bb_str);
        tmpmap.put("loss",lossss_str);

        MainActivity.MapData.subject.add(tmpmap);
        to_local();

        Intent intent = new Intent();
        intent.setAction("create_to_mod_info");
        intent.putExtra("number", String.valueOf(MainActivity.MapData.subject.size()));
        intent.putExtra("name", tmpmap.get("name"));
        intent.putExtra("time", tmpmap.get("time"));
        intent.putExtra("func", func);
        intent.putExtra("funcimg", "");


        intent.putExtra("x",expx);
        intent.putExtra("y",expy);
        intent.putExtra("a",aa_str);
        intent.putExtra("b",bb_str);
        intent.putExtra("loss",lossss_str);

        intent.setClass(CreateMod.this, Mod_info.class);
        startActivity(intent);
        CreateMod.this.finish();
    }

    private void initView(int cnt) {

        Map tmpmap = null;
        //Log.d("sub_size",subject.size()+"");

        for (int i = 0; i < cnt ; i++) {
            tmpmap = new HashMap();
            tmpmap.put("number", (i + 1) + "");
            tmpmap.put("huidu", "");
            tmpmap.put("nongdu", "");
            tmpmap.put("pic","");
            subject.add(tmpmap);

        }

        //Toast.makeText(this,subject.toString(),Toast.LENGTH_LONG).show();

        ListView listview = findViewById(R.id.lv_main2);
        MyAdapter adapter = new MyAdapter(CreateMod.this);
        listview.setAdapter(adapter);

    }


    private class MyTextChangListener implements TextWatcher {
        private EditText editText;
        //private int type;

        public MyTextChangListener(EditText editText) {
            this.editText = editText;
            //this.type = x;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            int index = (int) editText.getTag();

            //((MainActivity) context).saveEditText(index, s.toString(),type);
            saveEditText(index,s.toString());

        }
    }


    class MyAdapter extends BaseAdapter {
        Context context;

        MyAdapter(Context context)
        {
            this.context = context;
        }

        @Override
        public int getCount() {
            return subject.size();
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

            view = LayoutInflater.from(context).inflate(R.layout.item2,null);

            TextView num = view.findViewById(R.id.num);
            //TextView huidu = view.findViewById(R.id.huidu);

            num.setText(subject.get(i).get("number"));
            //huidu.setText(subject.get(i).get("name"));

            EditText nongdu = view.findViewById(R.id.nongdu);
            nongdu.addTextChangedListener(new MyTextChangListener(nongdu));
            nongdu.setTag(i);
            nongdu.setText(subject.get(i).get("nongdu").toString());
            nongdu.setSelection(subject.get(i).get("nongdu").toString().length());

            ImageButton img = view.findViewById(R.id.img);
            if(!subject.get(i).get("pic").equals("")){
                //img.setImageBitmap(stringtoBitmap(subject.get(i).get("pic")));
                String pic_name = subject.get(i).get("pic");
                img.setImageBitmap(bm_hashmap.get(pic_name));
            }


            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Log.e("clickkk","clicked_addpic_i"+i);


                    new AlertDialog.Builder(CreateMod.this).setTitle("选择图片").setMessage("选择已有照片还是拍照？")
                        .setPositiveButton("拍照", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int ii) {

                                File pic = new File(getExternalCacheDir(),"pic_.png");
                                if(pic.exists()){
                                    pic.delete();
                                }

                                try{
                                    pic.createNewFile();
                                }catch (IOException e){
                                    e.printStackTrace();
                                }

                                picuri = FileProvider.getUriForFile(CreateMod.this,"com.mas.name4.fileprovider",pic);
                                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                                intent.putExtra(MediaStore.EXTRA_OUTPUT,picuri);
                                startActivityForResult(intent,TAKE_PICTURE_FROM_CAMERA);

                                if(tag.flag == false){
                                    tag.flag = true;
                                    tag.num = i;
                                }


                            }
                        }).setNegativeButton("已有照片", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int ii) {



                                Intent choosePicIntent = new Intent(Intent.ACTION_PICK, null);
                                choosePicIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                startActivityForResult(choosePicIntent, TAKE_PICTURE_FROM_ALBUM);

                                if(tag.flag == false){
                                    tag.flag = true;
                                    tag.num = i;
                                }

                            }
                        }).show();

                }
            });

            return view;
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bm = null;
        switch (requestCode){
            case TAKE_PICTURE_FROM_CAMERA:
                if(resultCode == RESULT_OK){

                    startUcrop(picuri);

                }
                break;

            case TAKE_PICTURE_FROM_ALBUM:
                if(resultCode == RESULT_OK) {


                    startUcrop(data.getData());
                    break;

                }
                break;

            case UCrop.REQUEST_CROP:
                if(resultCode == RESULT_OK) {

                    final Uri resultUri = UCrop.getOutput(data);
                    //Bitmap bm = null;
                    try {
                        bm = BitmapFactory.decodeStream(getContentResolver().openInputStream(resultUri));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    int i = 0;
                    if (tag.flag == true) {
                        tag.flag = false;
                        i = tag.num;
                    }
                    subject.get(i).put("pic","pic_"+i);
                    double now_gray = Gray(bm);
                    subject.get(i).put("huidu",String.valueOf(now_gray));
                    //double now_gray = 0;
                    //gray.set(i, Double.valueOf(now_gray));
                    Log.v("gray",i+":"+now_gray);
                    //压缩图片
                    bm = Bitmap.createScaledBitmap(bm, 900, 600, true);
                    bm_hashmap.put("pic_"+i,bm);

                    ListView listview = findViewById(R.id.lv_main2);

                    //记录滚动条位置
                    scrollPos = listview.getFirstVisiblePosition();
                    View v1 = listview.getChildAt(0);
                    scrollTop = (v1 == null) ? 0 : v1.getTop();

                    MyAdapter adapter = new MyAdapter(CreateMod.this);
                    listview.setAdapter(adapter);


                    listview.setSelectionFromTop(scrollPos,scrollTop);

                }

                break;
            // 裁剪图片错误
            case UCrop.RESULT_ERROR:
                handleCropError(data);
                break;

                default: break;
        }

    }


    public void startUcrop(Uri uri_crop) {
        //Uri uri_crop = Uri.parse(path);
        //裁剪后保存到文件中
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "SampleCropImage.jpg"));
        UCrop uCrop = UCrop.of(uri_crop, destinationUri);
        UCrop.Options options = new UCrop.Options();
        //设置裁剪图片可操作的手势
        options.setAllowedGestures(UCropActivity.NONE, UCropActivity.NONE, UCropActivity.ALL);
        //设置隐藏底部容器，默认显示
        options.setHideBottomControls(true);
        //设置toolbar颜色
        options.setToolbarColor(Color.parseColor("#E6F602"));
        //设置状态栏颜色
        options.setStatusBarColor(Color.parseColor("#E6F602"));
        //是否能调整裁剪框
        options.setFreeStyleCropEnabled(true);
        uCrop.withAspectRatio(4,1);
        uCrop.withMaxResultSize(512,128);
        uCrop.withOptions(options);
        uCrop.start(this);
    }

    public static double Gray(Bitmap bitmap){
        double sum = 0;
        //double max = 0;
        for (int i = 0; i < bitmap.getWidth(); i++)
        {
            for (int j = 0; j < bitmap.getHeight(); j++)
            {
                //取图片当前的像素点
                int color = bitmap.getPixel(i, j);

                double gray = (Color.red(color) * 0.299 + Color.green(color) * 0.587 + Color.blue(color) * 0.114);

                //max = Math.max(max, gray);
                sum += gray;
            }
        }
        return sum/(bitmap.getWidth()*bitmap.getHeight());
        //return max;
    }


    public void saveEditText(int i, String str) {

        String way = str;

        subject.get(i).put("nongdu", way);

    }

    public void add_line() {
        Map<String,String> tmp = new HashMap<>();
        tmp.put("number",subject.size()+1+"");
        tmp.put("huidu","");
        tmp.put("nongdu","");
        tmp.put("pic","");
        subject.add(tmp);


        ListView listview = findViewById(R.id.lv_main2);
        MyAdapter adapter = new MyAdapter(CreateMod.this);
        listview.setAdapter(adapter);
        //将窗口滚动到最底部
        listview.setSelection(listview.getHeight());
    }



    private void handleCropError(Intent result) {
        final Throwable cropError = UCrop.getError(result);
        if (cropError != null) {
            Toast.makeText(this, cropError.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "无法剪切选择图片", Toast.LENGTH_SHORT).show();
        }
    }

}




























