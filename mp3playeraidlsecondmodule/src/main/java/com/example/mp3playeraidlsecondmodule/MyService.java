package com.example.mp3playeraidlsecondmodule;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;

public class MyService extends Service {

    public static final int MEDIA_STATUS_STOP = 0;
    public static final int MEDIA_STATUS_RUNNING = 1;
    public static final int MEDIA_STATUS_COMPLETED = 2;

    int status = MEDIA_STATUS_STOP;

    MediaPlayer mPlayer;

    // 재생할 음악 파일 경로(내장 메모리)
    String fileDir;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");

        fileDir = intent.getStringExtra("file_dir");

        Log.d("kkang", "(Returned from Second Module) My Service / IBinder / 파일 경로: " + fileDir);
        Log.d("kkang", " ");

        return new IMyAidlInterface.Stub() {
            @Override
            public int currentPosition() throws RemoteException {
                if (mPlayer.isPlaying()) {
                    Log.d("kkang", "(Returned from Second Module) My Service / IMyAidlInterface.Stub() / currentPosition() / 플레이어 현재 위치: " + mPlayer.getCurrentPosition());
                    Log.d("kkang", " ");

                    return mPlayer.getCurrentPosition();
                } else {
                    return 0;
                }
            }

            @Override
            public int getMaxDuration() throws RemoteException {
                if (mPlayer.isPlaying()) {
                    Log.d("kkang", "(Returned from Second Module) My Service / IMyAidlInterface.Stub() / 플레이어 길이: " + mPlayer.getDuration());
                    Log.d("kkang", " ");

                    return mPlayer.getDuration();
                } else {
                    return 0;
                }
            }

            @Override
            public void start() throws RemoteException {
                Log.d("kkang", "(Returned from Second Module) My Service / IMyAidlInterface.Stub() / start()");
                Log.d("kkang", " ");

                mPlayer = new MediaPlayer();
                try {
                    mPlayer.setDataSource(fileDir);
                    mPlayer.prepare();

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("kkang", "(Returned from Second Module) My Service / IMyAidlInterface.Stub() / start() / IOException e: " + e);
                }
                mPlayer.start();
                mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        status = MEDIA_STATUS_COMPLETED;
                    }
                });
                status = MEDIA_STATUS_RUNNING;


            }

            @Override
            public void stop() throws RemoteException {
                Log.d("kkang", "(Returned from Second Module) My Service / IMyAidlInterface.Stub() / stop()");
                Log.d("kkang", " ");

                if (mPlayer.isPlaying()) {
                    mPlayer.stop();
                }
                status = MEDIA_STATUS_STOP;
            }

            @Override
            public int getMediaStatus() throws RemoteException {
                return status;
            }
        };
    }

    public MyService() {
    }

    @Override
    public void onDestroy() {
        mPlayer.release();

        super.onDestroy();
    }
}