using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class HVCCPlugin : MonoBehaviour {

	#if UNITY_ANDROID
	// Unity Androidプラグイン関係
	private AndroidJavaObject cureent_activty = null;
	#endif

	//HVC-Cから取得する各値を格納する変数
	private string expression = "";
	private int face_score = 0;
	private int face_degree = 0;

	private int body_detect = 0;
	private int body_size = 0;
	private int body_posX = 0;
	private int body_posY = 0;
	private int body_confidence = 0;

	private int face_detect = 0;
	private int face_size = 0;
	private int face_posX = 0;
	private int face_posY = 0;
	private int face_confidence = 0;

	//uGUI関係
	private Text hvcclog;

	// Use this for initialization
	void Start () {

		#if UNITY_ANDROID
		
		// unityPlayerインスタンスを取得
		AndroidJavaClass unityPlayer = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
		
		//現在使用中のアクティビティを取得
		cureent_activty = unityPlayer.GetStatic<AndroidJavaObject>("currentActivity");
		
		#endif

		hvcclog = GameObject.Find ("Log").GetComponent<Text>();

	}
	
	// Update is called once per frame
	void Update () {
	
	}

	public void StartButton(){

		Debug.Log("--- START ---");

		#if UNITY_ANDROID
		cureent_activty.Call("EXECuteChange", "OMRON_HVC.*|omron_hvc.*");
		#endif

	}

	//ネイティブプラグインからの値を受け取るメソッド
	public void onCallBack(string str){
		switch (str) {
		
		case "onConnected":
			Debug.Log("onCallBack:onConnected");
			break;

		case "onPostExecute":

			Debug.Log("onCallBack:onPostExecute");

			string logdata = "";

			//プラグイン側から人体検出関係の値を取得
			body_detect = cureent_activty.Get<int>("body_detect");
			body_size = cureent_activty.Get<int>("body_size");
			body_posX = cureent_activty.Get<int>("body_posX");
			body_posY = cureent_activty.Get<int>("body_posY");
			body_confidence = cureent_activty.Get<int>("body_confidence");

			//プラグイン側から顔検出関係の値を取得
			face_detect = cureent_activty.Get<int>("face_detect");
			face_size = cureent_activty.Get<int>("face_size");
			face_posX = cureent_activty.Get<int>("face_posX");
			face_posY = cureent_activty.Get<int>("face_posY");
			face_confidence = cureent_activty.Get<int>("face_confidence");

			//プラグイン側から表情推定の値を取得
			expression = cureent_activty.Get<string>("expression");
			face_score = cureent_activty.Get<int>("face_score");
			face_degree = cureent_activty.Get<int>("face_degree");

			logdata = "body_detect:" + body_detect.ToString() + "\n" +
				"body_size:" + body_size.ToString() + "\n" +
				"body_pos:" + body_posX.ToString() + "," + body_posY.ToString() + "\n" + 
				"body_confidence:" + body_confidence.ToString() + "\n" +
				"face_detect:" + face_detect.ToString() + "\n" +
				"face_size:" + face_size.ToString() + "\n" +
				"face_pos:" + face_posX.ToString() + "," + face_posY.ToString() + "\n" +
				"face_confidence:" + face_confidence + "\n" +
				"expression:" + expression.ToString() + "\n" +
				"face_score:" + face_score.ToString() + "\n" +
				"face_degree:" + face_degree.ToString() + "\n";

			//画面上に取得したデータを表示
			hvcclog.text = logdata;

			break;

		}
	}
}
