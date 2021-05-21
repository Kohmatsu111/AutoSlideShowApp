package jp.techacademy.masahiro.kohmatsu.autoslideshowapp

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.provider.MediaStore
import android.content.ContentUris
import android.database.Cursor
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

    // null許容のCursor型のメンバ変数 cursor を定義
    // これにより、以下の関数どこででも、cursorが利用できるようになった　
    var cursor: Cursor? = null

    // null許容のTimer型のメンバ変数 mTimer を定義
    private var mTimer: Timer? = null

    //タイマー用の時間の為の変数
    private var mTimerSec = 0.0

    //ハンドラクラスのインスタンスを mHandlerを定義
    private var mHandler = Handler()

    //許可された際にやり取りするコードを以下の定数で定義して、100:Int とする
    private val PERMISSIONS_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // アプリ起動時にOSのverで許可の処理を分ける
        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されていれば、getContentInfo()を返す
                getContentsInfo()
            } else {
                // 許可されていない場合、許可ダイアログを表示する
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE
                )
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }

        //各種ボタンにクリックリスナを設定
            forward_button.setOnClickListener(this)
            back_button.setOnClickListener(this)
            play_and_stop_button.setOnClickListener(this)

    }

    // ユーザーの選択結果を受け取る
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // PERMISSION_REQUEST_CODE が等しければ getContentsInfo()を返す
                    getContentsInfo()
                }
        }
    }

    //getContentsInfo()メソッドで、ContentProvider を使って、端末内の画像の情報を取得する
    private fun getContentsInfo() {

        val resolver = contentResolver//ContentResolverプロパティを持つ定数　val resolverを定義

        // getContentsInfoメソッドで，カーソルの情報を取得したものを，メンバ変数のcursorへ代入するという形に変更。
        cursor = resolver.query( //ContentResolver クラスの queryメソッドで条件を指定・検索、情報を取得

            // 画像の情報を取得するが条件は絞らない　->　全件取得する
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )

        if (cursor!!.moveToFirst()) {// cursor に「最初」の画像があった場合は、その位置のデータに対し、以下の処理を実行

            // cursorが指しているデータの中から画像のIDがセットされている位置を取得
            val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
            // 画像のIDを取得する
            val id = cursor!!.getLong(fieldIndex)
            // 実際の画像のURIを取得してimageUriに格納
            val imageUri =
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            // imageViewのsetImageURI メソッドを使い、URIが指している画像ファイルをImageViewに表示させる
            imageView.setImageURI(imageUri)
        }
        //検索結果がなければ、falseを返し、close()メソッドを呼ぶ
        //ここで、close()メソッドを使ってしまうと、cursor変数が使えなくなるので、一旦ここでコメントアウト
        //cursor.close()
    }

    //イベントハンドラで、ボタンをクリックした際の処理を記述
    override fun onClick(v: View) {
        if (v.id == R.id.forward_button) {// forward_button　がクリックされたときに、以下の処理を実行

            if (cursor!!.moveToNext()) {//cursor が「次」に移動できたら、その位置のデータに対し、以下の処理を実行

                val fieldIndex =
                    cursor!!.getColumnIndex(MediaStore.Images.Media._ID)// cursor.getColumnIndex()で現在cursorが指しているデータの中から、画像のIDがセットされている位置を取得

                val id = cursor!!.getLong(fieldIndex)// cursor.getLong()でその位置の画像のIDを取得

                val imageUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                ) // ContentUris.withAppendidId()で、画像を取得

                imageView.setImageURI(imageUri) //imvageViewのsetImageURIメソッドを使って、URIが指している画像を表示させる

            } else {//cursor を「次」に移動できなかったら場合(->"else")は、以下の処理を実行
                cursor!!.moveToFirst() // cursor を「初め」に移動させる

                val fieldIndex =
                    cursor!!.getColumnIndex(MediaStore.Images.Media._ID) // 画像IDの位置を取得する

                val id = cursor!!.getLong(fieldIndex) // その位置の画像IDを取得

                val imageUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                ) // そのIDの画像を取得

                imageView.setImageURI(imageUri)//画像を画面に表示させる
            }

        } else if (v.id == R.id.back_button) {// back_button がクリックされたときに、以下の処理を実行

            if (cursor!!.moveToPrevious()) {//cursor が「前」に移動できたら、その位置のデータに対し、以下の処理を実行

                val fieldIndex =
                    cursor!!.getColumnIndex(MediaStore.Images.Media._ID) // 画像IDの位置を取得する

                val id = cursor!!.getLong(fieldIndex) // その位置の画像IDを取得

                val imageUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                ) // そのIDの画像を取得

                imageView.setImageURI(imageUri)//画像を画面に表示させる

            } else { //cursorを「前」に移動できなかったら、以下の処理を実行
                cursor!!.moveToLast() //cursor　を「最後」に移動させる

                val fieldIndex =
                    cursor!!.getColumnIndex(MediaStore.Images.Media._ID) // 画像IDの位置を取得する

                val id = cursor!!.getLong(fieldIndex) // その位置の画像IDを取得

                val imageUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                ) // そのIDの画像を取得

                imageView.setImageURI(imageUri)//画像を画面に表示させる

            }

        } else if(v.id == R.id.play_and_stop_button){ // plan_and_stop_button がクリックされたときに、以下の処理を実行
            if(mTimer == null) {//mTimerがnullで、mTimerが開始していない時
                mTimer = Timer()//Timer()コンストラクタをmTimerに代入して、非nullにする。
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        mTimerSec += 2.0
                        mHandler.post {
                            if (cursor!!.moveToNext()) {//cursor が「次」に移動できたら、その位置のデータに対し、以下の処理を実行

                                val fieldIndex =
                                    cursor!!.getColumnIndex(MediaStore.Images.Media._ID)// cursor.getColumnIndex()で現在cursorが指しているデータの中から、画像のIDがセットされている位置を取得

                                val id =
                                    cursor!!.getLong(fieldIndex)// cursor.getLong()でその位置の画像のIDを取得

                                val imageUri = ContentUris.withAppendedId(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    id
                                ) // ContentUris.withAppendidId()で、画像を取得

                                imageView.setImageURI(imageUri) //imvageViewのsetImageURIメソッドを使って、URIが指している画像を表示させる

                            } else {//cursor を「次」に移動できなかったら場合(->"else")は、以下の処理を実行
                                cursor!!.moveToFirst() // cursor を「初め」に移動させる

                                val fieldIndex =
                                    cursor!!.getColumnIndex(MediaStore.Images.Media._ID) // 画像IDの位置を取得する

                                val id = cursor!!.getLong(fieldIndex) // その位置の画像IDを取得

                                val imageUri = ContentUris.withAppendedId(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    id
                                ) // そのIDの画像を取得

                                imageView.setImageURI(imageUri)//画像を画面に表示させる
                            }
                        }
                    }
                }, 2000, 2000)
                //ここに起動中に制約したいコードを記述
                //1. 自動送りの際は進むボタンと戻るボタンはタップできない
                forward_button.isClickable = false
                back_button.isClickable = false

                //2. 再生ボタンを押して再生中は、ボタンの表示が「停止」になる
                play_and_stop_button.text = "停止"

            //mTimer == null 出ない場合
            }else{
                mTimer!!.cancel()//mTimerを
                mTimer = null

                play_and_stop_button.text = "再生"

                forward_button.isClickable = true
                back_button.isClickable = true

            }

        }

    }

}


