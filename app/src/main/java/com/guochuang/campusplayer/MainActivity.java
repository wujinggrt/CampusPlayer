package com.guochuang.campusplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

import com.eicky.ViewPagerGallery;
import com.guochuang.campusplayer.ui.ActivityCollector;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("On MainActivity", "onDestroy: Main destroied");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //添加到活动收集器上
        ActivityCollector.addActivity(MainActivity.this);
        setContentView(R.layout.activity_main);

        // 申请权限和证书设置
        PermissionAndLicenseManager.getPermimssionAndLicense(MainActivity.this);

        initGallery();
    }
    private void initGallery(){
        RelativeLayout activitymain = findViewById(R.id.activity_main);
        ViewPagerGallery gallery = findViewById(R.id.gallery);
        List<Integer> list = new ArrayList<>();
        int id = getResources().getIdentifier("campus_d", "drawable", getPackageName());
        list.add(id);
        id = getResources().getIdentifier("campus_b", "drawable", getPackageName());
        list.add(id);
        id = getResources().getIdentifier("campus_a", "drawable", getPackageName());
        list.add(id);
        gallery.setOnClickListener((ViewPagerGallery.GalleryOnClickListener) position -> {
            Intent intent = new Intent(MainActivity.this, HuxiActivity.class);
            startActivity(intent);
        });
        gallery.setImgResources(list);
    }
}
