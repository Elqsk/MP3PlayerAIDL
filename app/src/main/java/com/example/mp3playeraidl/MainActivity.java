package com.example.mp3playeraidl;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.mp3playeraidlsecondmodule.IMyAidlInterface;

/*
 * 서비스 모듈(MP3 Player AIDL Second Module)을 먼저 설치하고 본 앱을 실행시켜야 한다.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView titleView;

    ImageView playBtn;
    ImageView stopBtn;
    ProgressBar mProgressBar;

    // 재생할 음악 파일 경로
    String fileDir;

    boolean isPlaying;

    // 브로드캐스트 리시버를 구현하지 않고 데이터를 공유하려면(AIDL을 이용하려면) 서버 역할을 하는 보조
    // 앱--여기서는, MP3 Player AIDL Second Module--과 클라이언트 역할을 하는 본 앱이 같은 AIDL 파일을
    // 가지고 있어야 한다. 서버 역할의 앱에서 먼저 AIDL 폴더와 패키지, 파일을 생성한 뒤, 그대로 복사해서 본
    // 앱에 붙여넣기 하면 된다. 그리고 양쪽 모두 Make Module을 해서 자바 코드에서 AIDL 파일들을 인식하게
    // 해야 한다. AIDL 파일에는 흔히 아는 인터페이스 처럼 함수만 정의되어 있다.
    IMyAidlInterface mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titleView = findViewById(R.id.lab2_title);

        playBtn = findViewById(R.id.lab2_play);
        stopBtn = findViewById(R.id.lab2_stop);
        mProgressBar = findViewById(R.id.lab2_progress);

        playBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);

        stopBtn.setEnabled(false);
        playBtn.setEnabled(false);

        /*
         * 브로드캐스트 리시버 예제에서는 이 부분에서 권한을 요청하고 브로드캐스트 리시버를 등록했다.
         */

        // 재생할 음악 파일 경로(내장 메모리)
        fileDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Samsung/Music/Over_the_Horizon.mp3";

        Log.d("kkang", "Main Activity / onCreate(...) / 파일 경로: " + fileDir);

        // 해당 앱을 A라고 하면, 앱 B를 실행시키기 위해서는, 앱 B 매니페스트의 intent-filter에 동일한
        // 액션이 포함되어 있어야 하고 ...
        Intent intent = new Intent("com.example.mp3playeraidl.ACTION_PLAY");
        intent.putExtra("file_dir", fileDir);
        // 앱 B의 패키지명을 인텐트에 담아야 한다.
        intent.setPackage("com.example.mp3playeraidlsecondmodule");
        // 브로드캐스트 리시버 예제에서는 브로드캐스트 리시버를 등록할 때 인텐트 필터를 같이 담아서 줬지만
        // 여기서는 그러지 않기 때문에 서비스 모듈의 매니페스트에 인텐트 필터를 별도로 명시해 주어야 한다.

        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 서비스가 정상적으로 연결되면 서비스 모듈의 패키지명을 반환받을 수 있다. Content Name을
            // 로그로 찍어보면 다음과 같이 나온다: com.example.mp3playeraidlsecondmodule
            Log.d("kkang", "Main Activity / ServiceConnection / ComponentName name: " + name);
            Log.d("kkang", "Main Activity / ServiceConnection / IBinder service: " + service);
            Log.d("kkang", " ");

            // 서비스 모듈의 서비스(onBind)에서 반환한 IMyAidlInterface.Stub()에 작성된 메소드를 그대로
            // 사용하면 된다. 즉, IMyAidlInterface.start()로 음악을 재생할 수 있다. 브로드캐스트 리시버
            // 예제에서는 브로드캐스트 리시버에게 인텐트로 시작 신호를 전달했었다. Stub는 어느 모듈(앱)에도
            // 소속되지 않고 중간에서 두 앱을 이어주는 인터페이스다.
            mPlayer = IMyAidlInterface.Stub.asInterface(service);

            playBtn.setEnabled(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPlayer = null;
        }
    };

    @Override
    public void onClick(View v) {
        if (v == playBtn) {
            if (mPlayer != null) {
                try {
                    // 서비스 모듈의 서비스(onBind)에서 반환한 IMyAidlInterface.Stub()에 작성된 메소드를
                    // 그대로 사용하면 된다. 즉, IMyAidlInterface.start()로 음악을 재생할 수 있다.
                    // 브로드캐스트 리시버 예제에서는 브로드캐스트 리시버에게 인텐트로 시작 신호를
                    // 전달했었다. Stub는 어느 모듈(앱)에도 소속되지 않고 중간에서 두 앱을 이어주는
                    // 인터페이스다.
                    mPlayer.start();

                    mProgressBar.setMax(mPlayer.getMaxDuration());

                    Log.d("kkang", "Main Activity / onClick(...) / 재생 / mAidl.getMaxDuration(): " + mPlayer.getMaxDuration());

                } catch (RemoteException e) {
                    e.printStackTrace();
                    Log.e("kkang", "Main Activity / onClick(...) / 재생 / RemoteException e: " + e);
                }
                isPlaying = true;

                ProgressThread mThread = new ProgressThread();
                mThread.start();

                playBtn.setEnabled(false);
                stopBtn.setEnabled(true);
            }
        } else if (v == stopBtn) {
            if (mPlayer != null) {
                try {
                    mPlayer.stop();

                } catch (RemoteException e) {
                    e.printStackTrace();
                    Log.e("kkang", "Main Activity / onClick(...) / 중지 / RemoteException e: " + e);
                }
                isPlaying = false;

                mProgressBar.setProgress(0);

                playBtn.setEnabled(true);
                stopBtn.setEnabled(false);
            }
        }
        Log.d("kkang", " ");
    }

    class ProgressThread extends Thread {
        @Override
        public void run() {
            while (isPlaying) {
                Log.d("kkang", "Main Activity / 프로그래스 바 이전 진행도: " + mProgressBar.getProgress());
                // 음악 파일의 전체 길이를 얻으면 191242(191.242초) 처럼 나온다. 1000이 1초 즉, 전체
                // 길이는 191.242고, 1씩 오르는 것이다.
                mProgressBar.incrementProgressBy(1000);

                Log.d("kkang", "Main Activity / 프로그래스 바 현재 진행도: " + mProgressBar.getProgress());
                Log.d("kkang", " ");
                // 1초 마다 게이지가 찬다.
                SystemClock.sleep(1000);

                if (mProgressBar.getProgress() == mProgressBar.getMax()) {
                    isPlaying = false;
                }
            }
        }
    }
}