/**
 HVC-C1B_Unity_Android_Plugin

 Copyright (c) 2015 ProgMind

 This software is released under the MIT License.
 http://opensource.org/licenses/mit-license.php
 */
 
package com.progmind.hvccplugin;

import com.unity3d.player.UnityPlayerNativeActivity;
import com.unity3d.player.UnityPlayer;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import omron.HVC.BleDeviceSearch;
import omron.HVC.HVC;
import omron.HVC.HVC_BLE;
import omron.HVC.HVC_PRM;
import omron.HVC.HVC_RES;
import omron.HVC.HVC_RES.DetectionResult;
import omron.HVC.HVC_RES.FaceResult;
import omron.HVC.HVCBleCallback;

public class MainActivity extends UnityPlayerNativeActivity {

    public static final int EXECUTE_STOP = 0;
    public static final int EXECUTE_START = 1;
    public static final int EXECUTE_END = -1;

    private HVC_BLE hvcBle = null;
    private HVC_PRM hvcPrm = null;
    private HVC_RES hvcRes = null;

    private static int isExecute = 0;
    private static List<BluetoothDevice> deviceList = null;

    private HVCDeviceThread hvcThread = null;

    private int body_size = 0;
    private int body_posX = 0;
    private int body_posY = 0;
    private int body_confidence = 0;

    private int face_size = 0;
    private int face_posX = 0;
    private int face_posY = 0;
    private int face_confidence = 0;

    private String expression = "";
    private int face_score = 0;
    private int face_degree = 0;

    private int body_detect = 0;
    private int face_detect = 0;

    private String device_name = "OMRON_HVC.*|omron_hvc.*";
    private boolean hvcThreadStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        hvcBle = new HVC_BLE();
        hvcPrm = new HVC_PRM();
        hvcRes = new HVC_RES();

        hvcBle.setCallBack(hvcCallback);
        hvcThread = new HVCDeviceThread();

        isExecute = EXECUTE_STOP;

    }

    @Override
    public void onDestroy() {
        isExecute = EXECUTE_END;
        while (isExecute == EXECUTE_END) ;
        if (hvcBle != null) {
            try {
                hvcBle.finalize();
            } catch (Throwable e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        hvcBle = null;
        super.onDestroy();
    }

    public void showToast(final String str) {

        final Activity activity = UnityPlayer.currentActivity;

        // Show toast
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, str, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * isExecuteの切り替え
     * @param name
     */
    public void EXECuteChange(String name) {

        device_name = name;

        if (!hvcThreadStart) {

            hvcThread.start();
            hvcThreadStart = true;

        }

        showToast("しばらくお待ちください");

        isExecute = EXECUTE_START;

    }

    private class HVCDeviceThread extends Thread {
        @Override
        public void run() {

            while (isExecute != EXECUTE_END) {

                Log.d("Unity", "HVCDeviceThread called");
                Log.d("Unity", "device_name:" + device_name);

                BluetoothDevice device = SelectHVCC(device_name);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if ((device == null) || (isExecute != EXECUTE_START)) {
                    continue;
                }

                // HVCとBLE接続
                hvcBle.connect(getApplicationContext(), device);
                wait(15);

                hvcPrm.cameraAngle = HVC_PRM.HVC_CAMERA_ANGLE.HVC_CAMERA_ANGLE_0;
                hvcPrm.face.MinSize = 100;
                hvcPrm.face.MaxSize = 400;
                hvcBle.setParam(hvcPrm);
                wait(15);

                while (isExecute == EXECUTE_START) {
                    int nUseFunc = HVC.HVC_ACTIV_BODY_DETECTION |
                            HVC.HVC_ACTIV_FACE_DETECTION |
                            HVC.HVC_ACTIV_EXPRESSION_ESTIMATION;
                    hvcBle.execute(nUseFunc, hvcRes);
                    wait(30);
                }
                hvcBle.disconnect();
            }
            isExecute = EXECUTE_STOP;
        }

        public void wait(int nWaitCount) {
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (!hvcBle.IsBusy()) {
                    return;
                }
                nWaitCount--;
            } while (nWaitCount > 0);
        }


    }

    /**
     * HVC-C本体を検索するメソッド
     *
     * @param regStr
     * @return List or null
     */
    private BluetoothDevice SelectHVCC(String regStr) {

        //デバイス
        BleDeviceSearch bleSearch = new BleDeviceSearch(getApplicationContext());
        boolean getdevice = false;
        int Listnum = 0;

        while (!getdevice) {

            //BLEデバイス探索
            deviceList = bleSearch.getDevices();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            for (int i = 0; i < deviceList.size(); i++) {

                //nullのデバイスは破棄
                if (deviceList.get(i).getName() == null) {
                    break;
                }

                //正規表現による検索
                Pattern p = Pattern.compile(regStr);
                Matcher m = p.matcher(deviceList.get(i).getName());

                //指定デバイスの検索
                if (m.find()) {
                    //リストの番を変数Listnumに格納
                    Listnum = i;
                    getdevice = true;
                }

            }

        }

        bleSearch.stopDeviceSearch(getApplicationContext());

        return deviceList.get(Listnum);

    }


    private final HVCBleCallback hvcCallback = new HVCBleCallback() {

        @Override
        public void onConnected() {
            showToast("Selected device has connected");
            UnityPlayer.UnitySendMessage("HVCC", "onCallBack", "onConnected");
        }

        @Override
        public void onDisconnected() {
            showToast("Selected device has disconnected");
            isExecute = EXECUTE_STOP;
        }

        @Override
        public void onPostSetParam(int nRet, byte outStatus) {
            String str = "Set parameters : " + String.format("ret = %d / status = 0x%02x", nRet, outStatus);
            showToast(str);
        }

        @Override
        public void onPostGetParam(int nRet, byte outStatus) {
            String str = "Get parameters : " + String.format("ret = %d / status = 0x%02x", nRet, outStatus);
            showToast(str);
        }

        @Override
        public void onPostExecute(int nRet, byte outStatus) {
            if (nRet != HVC.HVC_NORMAL || outStatus != 0) {
                String str = "Execute : " + String.format("ret = %d / status = 0x%02x", nRet, outStatus);
                showToast(str);
            } else {

                int body_size_tmp = 0;
                int body_posX_tmp = 0;
                int body_posY_tmp = 0;
                int body_confidence_tmp = 0;

                int face_size_tmp = 0;
                int face_posX_tmp = 0;
                int face_posY_tmp = 0;
                int face_confidence_tmp = 0;

                String expression_tmp = "";
                int face_score_tmp = 0;
                int face_degree_tmp = 0;

                int body_detect_tmp = hvcRes.body.size();

                //人体検知
                for (DetectionResult bodyResult : hvcRes.body) {

                    body_size_tmp = bodyResult.size;
                    body_posX_tmp = bodyResult.posX;
                    body_posY_tmp = bodyResult.posY;
                    body_confidence_tmp = bodyResult.confidence;

                    break;

                }

                int face_detect_tmp = hvcRes.face.size();

                //顔検知
                for (FaceResult faceResult : hvcRes.face) {

                    if ((hvcRes.executedFunc & HVC.HVC_ACTIV_FACE_DETECTION) != 0) {

                        face_size_tmp = faceResult.size;
                        face_posX_tmp = faceResult.posX;
                        face_posY_tmp = faceResult.posY;
                        face_confidence_tmp = faceResult.confidence;

                    }

                    if ((hvcRes.executedFunc & HVC.HVC_ACTIV_EXPRESSION_ESTIMATION) != 0) {

                        //表情推定
                        expression_tmp = String.format("%s",
                                faceResult.exp.expression == HVC.HVC_EX_NEUTRAL ? "Neutral" :
                                        faceResult.exp.expression == HVC.HVC_EX_HAPPINESS ? "Happiness" :
                                                faceResult.exp.expression == HVC.HVC_EX_SURPRISE ? "Supprise" :
                                                        faceResult.exp.expression == HVC.HVC_EX_ANGER ? "Anger" :
                                                                faceResult.exp.expression == HVC.HVC_EX_SADNESS ? "Sadness" : "");

                        //表情のスコア値
                        face_score_tmp = faceResult.exp.score;

                        //ポジティブ/ネガティブ表情度
                        face_degree_tmp = faceResult.exp.degree;

                    }

                    break;
                }

                body_detect = body_detect_tmp;
                body_size = body_size_tmp;
                body_posX = body_posX_tmp;
                body_posY = body_posY_tmp;
                body_confidence = body_confidence_tmp;

                face_detect = face_detect_tmp;
                face_size = face_size_tmp;
                face_posX = face_posX_tmp;
                face_posY = face_posY_tmp;
                face_confidence = face_confidence_tmp;

                expression = expression_tmp;
                face_score = face_score_tmp;
                face_degree = face_degree_tmp;

                UnityPlayer.UnitySendMessage("HVCC", "onCallBack", "onPostExecute");

            }

        }

    };
}