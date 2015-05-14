# HVC-C1B_Unity_Android_Plugin

## 1. コード内容
本コードは、株式会社オムロンから販売されているHVC-C1BをUnityで利用するためのUnity Android プラグインです。  
HVC-Cから取得した一部の値をUnity側で利用できるようにする機能を提供します。  
現在、本プラグインを使って取得できるHVC-Cの値は下記の通りです。

* 一人分の人体検出、顔検出、表情推定の結果の取得
  * 人体検出結果（人体検出数、座標XY, 検出サイズ、信頼度）
  * 顔検出結果（顔検出数、座標XY, 検出サイズ、信頼度）
  * 表情推定（TOPスコアの表情、TOPスコア、ネガティブ/ポジティブ表情度）

## 2. ディレクトリ構成
    HVCC_Unity_Plugin_sample/        サンプルプログラムのUnityプロジェクト一式
    HVCCPlugin/                      Unity Android プラグインのAndroid Studioプロジェクト一式
      Plugins/
        HVCC_Unity_Plugin.jar        コードから生成したJARファイル（HVC-C1B_Unity_Android_Plugin）
    HVCC_Unity_Plugin_Sample.apk     サンプルプログラムのAPKファイル

## 3. プラグインの利用方法

* 事前にオムロン公式またはOmronSensingEggProjectで公開されている
[SDK](https://github.com/OmronSensingEggProject/HVC-C1B_SimpleDemo-Android)を取得し、
bin下にあるhvc_c1b_sdk.jarを入手します。

* 現在、動作を確認済みのHVC-C1B_SDKのバージョン、Unityのバージョンは下記の通りです。
     * HVC-C1B_SDK：1.2.0
     * Unity：5.0.1f1


* 　詳細はプラグイン利用するためのサンプルプログラム(HVCC_Unity_Plugin_sample)をご確認下さい。
     * STARTボタンを押すことでスクリプト中で指定されているデバイス名のHVC-Cと接続し、値の取得を行うサンプルです。

### 利用手順

(1) HVC-C1B_Unity_Android_Pluginを利用するUnityプロジェクトのAsset/Plugins/Androidフォルダに
hvc_c1b_sdk.jarとHVCC_Unity_Plugin.jarを配置します。

(2) Asset/Plugins/Android下にAndroidManifest.xmlを配置し、
[Unity公式ドキュメント](http://docs.unity3d.com/ja/current/Manual/PluginsForAndroid.html)に従い、
UnityPlayerNativeActivityを使用する際のマニュフェストファイルの変更をおこないます。

* activity android:nameで指定するパッケージ名は同梱のプラグイン（HVCC_Unity_Plugin.jar）の場合、
「com.progmind.hvccplugin.MainActivity」を指定します。

(3) HVCCを使用するシーンを選択し、Hierarchy上に空のGameObjectを作成します。
作成後、オブジェクト名を「HVCC」に変更します。

(4) 作成したGameObjectに任意のC#スクリプトを紐付け、そのスクリプト内でプラグインを呼び出す処理を記述します。

```
//コード抜粋

// プラグイン関係
private AndroidJavaObject cureent_activty = null;

// Use this for initialization
	void Start () {

		#if UNITY_ANDROID

		// unityPlayerインスタンスを取得
		AndroidJavaClass unityPlayer = new AndroidJavaClass("com.unity3d.player.UnityPlayer");

		//現在使用中のアクティビティを取得
		cureent_activty = unityPlayer.GetStatic<AndroidJavaObject>("currentActivity");

		#endif
	}

  public void StartButton(){

  		Debug.Log("--- START ---");

      //プラグイン側のEXECuteChangeメソッドを呼び出し、HVCCの検索〜値の取得を開始します。
      //引数にはHVCCのデバイス名（正規表現）を指定します。
  		cureent_activty.Call("EXECuteChange", "OMRON_HVC.*|omron_hvc.*");

  	}
　
  //プラグインから値を受け取るメソッド
  //プラグイン側からUnitySendMessageを使い、結果を取得できたことをUnity側（GameObject:HVCCのonCallBackメソッド）に送ります。
  public void onCallBack(string str){
  		switch (str) {
  		case "onConnected":
  			Debug.Log ("onCallBack:onConnected");
  			break;

      //受け取り後、プラグイン側の該当の変数を取得します。
  		case "onPostExecute":
  			Debug.Log ("onCallBack:onPostExecute");
  			expression = cureent_activty.Get<string> ("expression");
  			face_score = cureent_activty.Get<int> ("face_score");
  			face_degree = cureent_activty.Get<int> ("face_degree");
  			break;
  		}
  	}
```


## 4. プラグイン（HVCCPlugin）の改変およびビルド方法

* ビルド環境
  * Android Studio 1.2
  * Minimum API：Android 4.4


* libフォルダにhvc_c1b_sdk.jarとclasses.jarをインポートしてください。
   * classes.jarの場所(OSXの場合):Unity.app/Contents/PlaybackEngines/AndroidPlayer/release/bin/classes.jar


* プラグインのソースコードは、app/src/main/java/com/progmind/hvccplugin/MainActivity.javaになります。


* ビルドおよびjarファイルの生成・削除を行うGradleのタスク「makejar」と「clearjar」を用意しています。
    * タスク実行後、Pluginsフォルダにgradle.propertiesで指定したファイル名の.jarファイルが生成または削除されます。

## ライセンス
This software is released under the MIT License, see LICENSE.txt.
