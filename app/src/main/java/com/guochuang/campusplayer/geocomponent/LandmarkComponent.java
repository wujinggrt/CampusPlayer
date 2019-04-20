package com.guochuang.campusplayer.geocomponent;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import com.guochuang.campusplayer.HuxiActivity;
import com.guochuang.campusplayer.R;
import com.supermap.data.AltitudeMode;
import com.supermap.data.GeoPlacemark;
import com.supermap.data.GeoPoint3D;
import com.supermap.data.Point3D;
import com.supermap.realspace.Feature3D;
import com.supermap.realspace.Feature3DSearchOption;
import com.supermap.realspace.Feature3Ds;
import com.supermap.realspace.Layer3D;
import com.supermap.realspace.Layer3DType;
import com.supermap.realspace.Layer3Ds;
import com.supermap.realspace.LookAt;
import com.supermap.realspace.PixelToGlobeMode;
import com.supermap.realspace.Scene;
import com.supermap.realspace.SceneControl;
import com.supermap.realspace.Camera;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;


/**
 * 这个类应该使用单例模式，防止存储多个文件
 */
public class LandmarkComponent {
    public static final String TAG = "On LandmarkComponent";
    private static final String layerName = "Favorite_KML";
    private static final double radius = 15.;
    private static final int flyTime = 3000;

    private HuxiActivity context;
    private SceneControl sceneControl;

    private ArrayList<LandFeature> landFeatures = new ArrayList<>();
    private String rootPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
    private String layerKMlPath = rootPath + "/SuperMap/initKML/default.kml";
    private String cameraPath = rootPath + "/SuperMap/initKML/camera.txt";

    private Layer3Ds layer3Ds;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public LandmarkComponent(HuxiActivity context){
        super();
        this.context = context;
        this.sceneControl = context.findViewById(R.id.scene_control);
        loadFeaturesFromFile();
    }

    public void processSingleTap(Point point) {
        nearByLandmark(point);
    }

    /**
     * 通过传入的 android.graphics.Point 的类和半径来判断，附近是否有点.
     *
     * @param point
     * @return
     */
    private void nearByLandmark(Point point) {
        Point3D point3D = sceneControl.getScene().pixelToGlobe(point, PixelToGlobeMode.TERRAINANDMODEL);
        double minDistance = LandmarkComponent.radius;
        Feature3D nearPoint = null;
        for (LandFeature landFeature:
                landFeatures) {
            Point3D featurePoint3D = landFeature.getFeature3D().getGeometry().getPosition();
            double distance = Math.sqrt(
                    Math.pow(point3D.getX() - featurePoint3D.getX(), 2.) +
                    Math.pow(point3D.getY() - featurePoint3D.getY(), 2.) +
                    Math.pow(point3D.getZ() - featurePoint3D.getZ(), 2.)
            );
            if (distance < minDistance) {
                nearPoint = landFeature.getFeature3D();
                minDistance = distance;
            }
        }
        if (nearPoint != null) {
            Toast.makeText(context, "附近有地标：" + nearPoint.getName(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "附近没有有地标" , Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取全部的地标名字
     *
     * @return
     */
    public String[] getLandmarkNames() {
        String[] ret = new String[landFeatures.size()];
        for (int i = 0; i < landFeatures.size(); i++) {
            ret[i] = landFeatures.get(i).getFeature3D().getName();
        }
        return ret;
    }

    /**
     * 提供地标的名称，点击之后将会飞到那个地方。
     *
     * @param landmarkName
     */
    @SuppressLint("NewApi")
    public void flyToSpecifiedLand(String landmarkName) {
        for (LandFeature landFeature:
                landFeatures) {
            if (Objects.equals(landFeature.getFeature3D().getName(), landmarkName)) {
                flyToLand(landFeature);
                return ;
            }
        }
        Toast.makeText(context, layerKMlPath + ".kml 文件中并没有这个地标", Toast.LENGTH_LONG).show();
    }

    /**
     * 提供地标的序号，点击之后将会飞到那个地方。
     *
     * @param index
     */
    public void flyToSpecifiedLand(int index) {
        if (index < 0 || index >= landFeatures.size()) {
            return ;
        }
        flyToLand(landFeatures.get(index));
    }

    private void flyToLand(LandFeature landFeature) {
        GeoPlacemark geoPlacemark = (GeoPlacemark) landFeature.getFeature3D().getGeometry();
        GeoPoint3D geoPoint3D = (GeoPoint3D) geoPlacemark.getGeometry();
        Point3D point3D = new Point3D(geoPoint3D.getX(), geoPoint3D.getY(), geoPoint3D.getZ() + 150);
        Scene scene = sceneControl.getScene();
//        scene.flyToPoint(point3D, flyTime);
        scene.setCamera(landFeature.getCamera());
        Toast.makeText(context, "正在飞向" + landFeature.getFeature3D().getName(), Toast.LENGTH_LONG).show();
    }

    /**
     * 如果文件没有存在，那么就会创建一个文件
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadFeaturesFromFile() {
        openOrCreateFile(layerKMlPath);
        // 从 kml 中添加
        layer3Ds = sceneControl.getScene().getLayers();
        layer3Ds.addLayerWith(layerKMlPath, Layer3DType.KML, true, layerName);
        Layer3D layer3d = sceneControl.getScene().getLayers().get(layerName);
        if (layer3d != null) {
            Feature3Ds feature3Ds = layer3d.getFeatures();
            Feature3D[] feature3DArray = feature3Ds.getFeatureArray(Feature3DSearchOption.ALLFEATURES);
            landFeatures.clear();
            try {
                FileInputStream fis = new FileInputStream(cameraPath);
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(isr);
                for (Feature3D feature3D :
                    feature3DArray) {
                    String record = bufferedReader.readLine();
                    if (record  == null) {
                        break;
                    }
                    CameraArgs cameraArgs = new CameraArgs(record);
                    landFeatures.add(new LandFeature(feature3D, cameraArgs.toCamera(), cameraArgs.toLookAt()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void hideLandmarks(String hideLayerName) {
        Layer3D layer3D = layer3Ds.get(hideLayerName);
        if (layer3D != null) {
            layer3Ds.removeLayerWithName(hideLayerName);
        }

    }

    /**
     * 根据文件名生成文件，存储 kml 文件的信息。
     *
     * @param filePath
     * @return
     */
    private static void openOrCreateFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class LandFeature {
    private Feature3D feature3D;
    private Camera camera;
    private LookAt lookAt;

    public LandFeature(Feature3D feature3D, Camera camera) {
        this.feature3D = feature3D;
        this.camera = camera;
    }

    public LandFeature(Feature3D feature3D, Camera camera, LookAt lookAt) {
        this(feature3D, camera);
        this.lookAt = lookAt;
    }

    public Feature3D getFeature3D() {
        return feature3D;
    }

    public Camera getCamera() {
        return camera;
    }

    public LookAt getLookAt() {
        return lookAt;
    }
}

class CameraArgs {
    private  double longtitude;
    private  double latitude;
    private  double altitude;
    private AltitudeMode altitudeMode;
    private  double heading;
    private double tilt;

    public CameraArgs(Camera camera) {
        longtitude = camera.getLongitude();
        latitude = camera.getLatitude();
        altitude = camera.getAltitude();
        altitudeMode = camera.getAltitudeMode();
        heading = camera.getHeading();
        tilt = camera.getTilt();
    }

    public CameraArgs(String record) {
        String[] args =  record.split(" ");

        longtitude = Double.valueOf(args[0]);
        latitude = Double.valueOf(args[1]);
        altitude = Double.valueOf(args[2]);

        int altitudeIndex = Integer.valueOf(args[3]);
        if (altitudeIndex == 0) {
            altitudeMode = AltitudeMode.ABSOLUTE;
        } else if (altitudeIndex == 1) {
            altitudeMode = AltitudeMode.CLAMP_TO_GROUND;
        } else {
            altitudeMode = AltitudeMode.RELATIVE_TO_GROUND;
        }
        heading = Double.valueOf(args[4]);
        tilt = Double.valueOf(args[5]);
    }

    @Override
    public String toString() {
        int altitudeIndex;
        if (altitudeMode == AltitudeMode.ABSOLUTE) {
            altitudeIndex = 0;
        } else if (altitudeMode == AltitudeMode.CLAMP_TO_GROUND ) {
            altitudeIndex = 1;
        } else {
            altitudeIndex = 2;
        }
        return longtitude + " " + latitude + " " + altitude + " " + altitudeIndex + " " + heading + " " + tilt;
    }

    public Camera toCamera() {
        return new Camera(longtitude, latitude, altitude, altitudeMode, heading, tilt);
    }

    public LookAt toLookAt() {
        return new LookAt(longtitude, latitude, altitude, altitudeMode, heading, tilt, 100);
    }
}
