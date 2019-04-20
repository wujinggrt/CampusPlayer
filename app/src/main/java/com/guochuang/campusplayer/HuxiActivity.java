package com.guochuang.campusplayer;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.guochuang.campusplayer.geocomponent.FlyComponent;
import com.guochuang.campusplayer.geocomponent.GestureListenerForLandmark;
import com.guochuang.campusplayer.geocomponent.LandmarkComponent;
import com.guochuang.campusplayer.ui.ActivityCollector;
import com.guochuang.campusplayer.ui.MenuBar;
import com.guochuang.campusplayer.ui.SearchDialog;
import com.supermap.data.Environment;
import com.supermap.data.LicenseStatus;
import com.supermap.data.Workspace;
import com.supermap.data.WorkspaceConnectionInfo;
import com.supermap.data.WorkspaceType;
import com.supermap.realspace.Camera;
import com.supermap.realspace.Scene;
import com.supermap.realspace.SceneControl;

// for pull test
public class HuxiActivity extends AppCompatActivity {
    public static final String TAG = "HuxiActivity, Tag";
    public static final String rootPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String workspacePath = rootPath + "/SuperMap/data/CBD_android/CBD_android.sxwu";
    public static final String localSceneDirPath = rootPath + "/SuperMap/data/CBD_android/";
    public static final String routePathName = "1";

    private SearchDialog searchDialog;
    private SceneControl sceneControl;
    private FlyComponent flyComponent;
    private LandmarkComponent landmarkComponent;

    private Camera camera;

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: is Onstart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        // 添加到活动收集器上
        ActivityCollector.addActivity(HuxiActivity.this);
        setContentView(R.layout.activity_huxi);
        // 这个控件没有重新创造出来，还是指向那个，还没有被GC的哪一个
        sceneControl = findViewById(R.id.scene_control);
        sceneControl.sceneControlInitedComplete(success -> initAllComponent());
    }

    // 判断许可是否可用
    private boolean isLicenseAvailable() {
        LicenseStatus licenseStatus = Environment.getLicenseStatus();
        if (!licenseStatus.isLicenseExsit()) {
            Toast.makeText(this, "许可不存在，场景打开失败，请加入许可", Toast.LENGTH_LONG).show();
            return false;
        } else if (!licenseStatus.isLicenseValid()) {
            Toast.makeText(this, "许可过期，场景打开失败，请更换有效许可", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    // 打开一个本地场景
    private void openLocalScene() {
        // 新建一个工作空间对象
        Workspace workspace = new Workspace();
        WorkspaceConnectionInfo workspaceConnectionInfo = new WorkspaceConnectionInfo();
        // 根据工作空间类型，设置服务路径和类型信息。
        workspaceConnectionInfo.setServer(workspacePath);
        workspaceConnectionInfo.setType(WorkspaceType.SXWU );
        // 场景关联工作空间
        boolean status = workspace.open(workspaceConnectionInfo);
        if (!status) {
            Log.d(TAG, "openLocalScene: Fail to open workspace");
            return ;
        }
        Scene scene = sceneControl.getScene();
        scene.setWorkspace(workspace);
        String sceneName = workspace.getScenes().get(0);
        // 打开场景
        boolean succeed = scene.open(sceneName);
        if (succeed) {
            Toast.makeText(HuxiActivity.this, "打开场景成功", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "openLocalScene: Open Failed");
        }
    }

    private void initAllComponent() {
        initGeoComponent();
        initUIComponent();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initGeoComponent() {
        boolean status = isLicenseAvailable();
        if (!status) {
            return;
        }
        openLocalScene();
        flyComponent = new FlyComponent(sceneControl);
        landmarkComponent = new LandmarkComponent(this);
        // 场景浏览
        flyComponent.prepareFly(HuxiActivity.this, routePathName, localSceneDirPath);
        // 设置长按监听，地标相关操作
        GestureListenerForLandmark gestureListenerForLandmark = new GestureListenerForLandmark(HuxiActivity.this, landmarkComponent);
        GestureDetector gestureDetector = new GestureDetector(HuxiActivity.this, gestureListenerForLandmark);
        sceneControl.setGestureDetector(gestureDetector);
        findViewById(R.id.full_screen_image_campus_d).setVisibility(View.GONE);
        camera = sceneControl.getScene().getCamera();
    }

    private void initUIComponent() {
        // 初始化button
        setButtonBackAndExitListen();
        // 设置搜索框
        setSearchDialog(landmarkComponent.getLandmarkNames());
        // menu 点击事件
        MenuBar menuBar = findViewById(R.id.menu_bar);
        findViewById(R.id.bar_upper_left).setOnClickListener(v -> {
            menuBar.loadAnimation(v);
            showListDialogInUpperLeftBar();
        });
        findViewById(R.id.bar_upper_right).setOnClickListener(v -> {
            menuBar.loadAnimation(v);
            searchDialog.show();
        });
        findViewById(R.id.bar_bottom_left).setOnClickListener(menuBar::loadAnimation);
        findViewById(R.id.bar_bottom_left).setOnClickListener(menuBar::loadAnimation);
    }

    //设置搜索框
    private void setSearchDialog(String[] listViewItems) {
        //获取searchdialog并且对类进行初始化
        View v = getLayoutInflater().inflate(R.layout.search_dialog, null);
        searchDialog = new SearchDialog(this, 0, 0, v, R.style.DialogTypeTheme);
        searchDialog.setCancelable(true);
        SearchView searchView = searchDialog.getSearchView();
        ListView listView = searchDialog.getListView();
        final ArrayAdapter arrayAdapter;
        //对布局内的控件进行设置
        arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, listViewItems);
        listView.setAdapter(arrayAdapter);
        //listview启动过滤
        listView.setTextFilterEnabled(true);
        //一开始不显示
        listView.setVisibility(View.VISIBLE);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            landmarkComponent.flyToSpecifiedLand((String)arrayAdapter.getItem(position));
            searchDialog.hide();
        });
        //显示搜索按钮
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //单击搜索按钮的监听
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            //输入字符的监听
            @Override
            public boolean onQueryTextChange(String newText) {
                arrayAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    private void setButtonBackAndExitListen() {
        Button button_back = findViewById(R.id.button_back);
        Button button_exit = findViewById(R.id.button_exit);
        button_back.setOnClickListener(v -> System.exit(0));
        button_exit.setOnClickListener(v -> {
            AlertDialog alertDialog = new AlertDialog.Builder(HuxiActivity.this)
                    .setTitle("退出程序")
                    .setMessage("是否退出程序")
                    .setPositiveButton("确定", (dialogInterface, i) -> ActivityCollector.finishAll())
                    .setNegativeButton("取消", (dialogInterface, i) -> {
                    }).create();
            alertDialog.show();
        });
    }

    private void showListDialogInUpperLeftBar() {
        final String[] items3 = new String[]{"Route1", "Route2", "Route3", "Route4"};//创建item
        //添加列表
        AlertDialog.Builder builder =  new AlertDialog.Builder(this);
        builder.setTitle("选择要浏览的路线");
        builder.setItems(items3, (dialog, which) -> {
            switch (which) {
                case 0:
                    flyComponent.startOrPauseFly(HuxiActivity.this);
                    break;
                default:
                    Toast.makeText(HuxiActivity.this, "你选择了：" + items3[which], Toast.LENGTH_SHORT).show();
                    break;
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public Camera getCamera() {
        return camera;
    }
}
