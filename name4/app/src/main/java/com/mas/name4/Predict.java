package com.mas.name4;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.mas.name4.CreateMod.Gray;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Predict extends AppCompatActivity {

    static class pre_to_share_bm{
        public static Bitmap bitmap;
    }

    private static final int TAKE_PICTURE_FROM_ALBUM = 1;
    private static final int TAKE_PICTURE_FROM_CAMERA = 2;
    private static final int PICTURE_CROP_CODE = 3;

    double now_gray;
    ImageButton img_btn;
    EditText et;
    Button btn_back,btn_ok;
    Uri picuri;
    boolean imgbtn_flag = false;



    String mod_name = "";
    String mod_time = "";
    String mod_func = "";
    String mod_funcimg = "";
    String mod_num = "";
    double a = 0f;
    double b = 0f;
    double loss = 0f;
    public double yuzhi = 0f;
    boolean dabiao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predict);
        if (getSupportActionBar() != null){
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
        yuzhi = 0f;



        if (ContextCompat.checkSelfPermission(Predict.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(Predict.this, Manifest.permission.CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Predict.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA}, 2);
        }



        //??????????????????imgbutton
        img_btn = findViewById(R.id.img_btn);
        img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgbtn_flag = false;
                new AlertDialog.Builder(Predict.this).setTitle("????????????").setMessage("?????????????????????????????????")
                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int ii) {

                                File pic = new File(getExternalCacheDir(),"pic_predict.png");
                                if(pic.exists()){
                                    pic.delete();
                                }

                                try{
                                    pic.createNewFile();
                                }catch (IOException e){
                                    e.printStackTrace();
                                }

                                picuri = FileProvider.getUriForFile(Predict.this,"com.mas.name4.fileprovider",pic);
                                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                                intent.putExtra(MediaStore.EXTRA_OUTPUT,picuri);
                                startActivityForResult(intent,1);


                            }
                        }).setNegativeButton("????????????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int ii) {



                                Intent choosePicIntent = new Intent(Intent.ACTION_PICK, null);
                                choosePicIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                startActivityForResult(choosePicIntent, 2);

                            }
                        }).show();

            }
        });


        et = findViewById(R.id.et);


        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction("predict_to_mod_info");
                intent.putExtra("name", mod_name);
                intent.putExtra("time", mod_time);
                intent.putExtra("func", mod_func);
                intent.putExtra("funcimg", mod_funcimg);
                intent.putExtra("number", mod_num);
                intent.putExtra("a", String.valueOf(a));
                intent.putExtra("b", String.valueOf(b));
                intent.putExtra("loss", String.valueOf(loss));
                intent.setClass(Predict.this, Mod_info.class);
                startActivity(intent);
                Predict.this.finish();
            }
        });


        btn_ok = findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String yuzhi_str = et.getText().toString().trim();
                if(!yuzhi_str.equals("") && imgbtn_flag == true){
                    yuzhi = Double.valueOf(yuzhi_str);

                    if(check()){
                        dabiao = true;
                    }
                    else {
                        dabiao = false;
                    }
                    Intent intent = new Intent();
                    intent.setAction("predict_to_share");
                    intent.putExtra("name", mod_name);
                    intent.putExtra("time", mod_time);
                    intent.putExtra("func", mod_func);
                    intent.putExtra("funcimg", mod_funcimg);
                    intent.putExtra("number", mod_num);
                    intent.putExtra("a", String.valueOf(a));
                    intent.putExtra("b", String.valueOf(b));
                    intent.putExtra("loss", String.valueOf(loss));

                    intent.putExtra("yuzhi",String.valueOf(yuzhi));
                    intent.putExtra("dabiao",String.valueOf(dabiao));

                    intent.setClass(Predict.this,Share.class);
                    startActivity(intent);
                    Predict.this.finish();
                }
                else{
                    if(yuzhi_str.equals("") && imgbtn_flag == false) {
                        new AlertDialog.Builder(Predict.this).setTitle("????????????????????????")
                                .setMessage("?????????????????????????????????")
                                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();
                    }
                    else if(yuzhi_str.equals("")){
                        new AlertDialog.Builder(Predict.this).setTitle("???????????????")
                                .setMessage("??????????????????")
                                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();
                    }
                    else {
                        new AlertDialog.Builder(Predict.this).setTitle("???????????????")
                                .setMessage("??????????????????")
                                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();
                    }
                }

            }
        });


    }




    /**
     * ??????UCrop??????????????????
     *
     * @param uri
     */
    public void cropRawPhoto(Uri uri) {

        UCrop.Options options = new UCrop.Options();
        // ?????????????????????
        options.setToolbarColor(getResources().getColor(R.color.teal_200));
        // ?????????????????????
        options.setStatusBarColor(getResources().getColor(R.color.teal_700));
        // ??????????????????
        options.setHideBottomControls(true);
        // ????????????
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        // ????????????????????????
        options.setCompressionQuality(100);
        // ???????????????????????????(??????false)???????????????????????????????????????????????????????????????????????????
        // ???????????????????????????????????????????????????????????????
        options.setFreeStyleCropEnabled(true);
        // ????????????????????????
        options.setCompressionQuality(100);
        // ???
        options.setCircleDimmedLayer(true);
        // ??????????????????
        options.setShowCropGrid(false);

        //FileUtils.createOrExistsDir(Config.SAVE_REAL_PATH);

        // ?????????uri?????????uri
        UCrop.of(uri, Uri.fromFile(new File(Environment.getExternalStorageDirectory().toString() + System.currentTimeMillis() + ".PNG")))
                // ?????????
                .withAspectRatio(1, 1)
                // ????????????
                .withMaxResultSize(200, 200)
                // ????????????
                .withOptions(options)
                .start(this);
    }

    public void startUcrop(Uri uri_crop) {
        //Uri uri_crop = Uri.parse(path);
        //???????????????????????????
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "SampleCropImage.jpg"));
        UCrop uCrop = UCrop.of(uri_crop, destinationUri);
        UCrop.Options options = new UCrop.Options();
        //????????????????????????????????????
        options.setAllowedGestures(UCropActivity.NONE, UCropActivity.NONE, UCropActivity.ALL);
        //???????????????????????????????????????
        options.setHideBottomControls(true);
        //??????toolbar??????
        options.setToolbarColor(Color.parseColor("#E6F602"));
        //?????????????????????
        options.setStatusBarColor(Color.parseColor("#E6F602"));
        //????????????????????????
        options.setFreeStyleCropEnabled(true);
        uCrop.withAspectRatio(4,1);
        uCrop.withMaxResultSize(512,128);
        uCrop.withOptions(options);
        uCrop.start(this);
    }




    @SuppressLint("Range")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1:
                if(resultCode == RESULT_OK){

                    //Toast.makeText(getApplicationContext(),"ok",Toast.LENGTH_LONG).show();
                    startUcrop(picuri);

                }
                break;

            case 2:
                if(resultCode == RESULT_OK) {

                    /*
                    try {
                        if(data != null){
                            Uri uri = data.getData();
                            if(!TextUtils.isEmpty(uri.getAuthority())){
                                Cursor cursor = this.getContentResolver().query(uri,new String[]{MediaStore.Images.Media.DATA},
                                        null,null,null);
                                if(null == cursor){
                                    return;
                                }
                                cursor.moveToFirst();
                                //??????????????????path
                                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                                cursor.close();
                                path = "file://"+path;
                                //???????????????????????????????????????
                                Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
                                startUcrop(Uri.parse(path));
                            }
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                     */
                    startUcrop(data.getData());
                    break;

                }
                break;




            case UCrop.REQUEST_CROP:
                if(resultCode == RESULT_OK) {

                    final Uri resultUri = UCrop.getOutput(data);
                    Bitmap bitmap = null;
                    try {
                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(resultUri));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    pre_to_share_bm.bitmap = bitmap;
                    now_gray = Gray(bitmap);

                    img_btn.setImageBitmap(bitmap);
                    imgbtn_flag = true;
                }
                break;

            // ??????????????????
            case UCrop.RESULT_ERROR:
                handleCropError(data);
                break;


            default:break;

        }
    }




    boolean check(){
        return (a * now_gray + b < yuzhi);
    }

    private void handleCropError(Intent result) {
        final Throwable cropError = UCrop.getError(result);
        if (cropError != null) {
            Toast.makeText(this, cropError.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "????????????????????????", Toast.LENGTH_SHORT).show();
        }
    }

}