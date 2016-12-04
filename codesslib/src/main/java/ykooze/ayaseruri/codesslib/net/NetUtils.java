package ykooze.ayaseruri.codesslib.net;

import android.Manifest;
import android.content.Context;

import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import ykooze.ayaseruri.codesslib.others.ToastUtils;
import ykooze.ayaseruri.codesslib.rx.RxUtils;

/**
 * Created by wufeiyang on 16/7/25.
 */
public class NetUtils {
    //savePath最后不要加上"/"
    public static void downloadFile(final Context context
            , final OkHttpClient okHttpClient
            , final String url
            , final String saveName
            , final String savePath
            , final IDownLoad iDownLoad){
        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    Request request = new Request.Builder().url(url).build();
                    BufferedSink output = null;
                    BufferedSource input = null;

                    subscriber.onStart();
                    Response response = okHttpClient.newCall(request).execute();
                    long totalByteLength = response.body().contentLength();

                    if(response.isSuccessful() && 0 != totalByteLength){
                        File dir = new File(savePath);
                        if(!dir.exists()){
                            dir.createNewFile();
                        }

                        File file = new File(dir, saveName);
                        if(!file.exists()){
                            file.createNewFile();
                        }

                        output = Okio.buffer(Okio.sink(file));
                        input = Okio.buffer(Okio.source(response.body().byteStream()));
                        byte data[] = new byte[1024];

                        long total = 0;
                        int count;
                        while ((count = input.read(data)) != -1) {
                            total += count;
                            output.write(data, 0, count);
                            subscriber.onNext((int) (total * 100 / totalByteLength));
                        }

                        output.flush();
                        output.close();
                        input.close();
                        subscriber.onCompleted();
                    }else {
                        ToastUtils.showTost(ToastUtils.TOAST_ALERT, "网络出现问题");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        }).throttleFirst(1000, TimeUnit.MILLISECONDS)
                .compose(RxUtils.<Integer>applySchedulers())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                        iDownLoad.onComplete();
                    }

                    @Override
                    public void onError(Throwable e) {
                        iDownLoad.onError(e);
                    }

                    @Override
                    public void onNext(Integer percent) {
                        iDownLoad.onDownloading(percent);
                    }

                    @Override
                    public void onStart() {
                        iDownLoad.onStart();
                    }
                });
    }

    public interface IDownLoad{
        void onStart();
        void onDownloading(int percent); //最大为100 最小为0
        void onComplete();
        void onError(Throwable e);
    }
}
