package ykooze.ayaseruri.codesslib.rx;

import com.orhanobut.logger.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import ykooze.ayaseruri.codesslib.io.SerializeUtils;
import ykooze.ayaseruri.codesslib.others.Utils;

/**
 * Created by wufeiyang on 16/7/7.
 */
public class RxUtils {

    private volatile static ExecutorService sExecutors;
    private volatile static Scheduler sChedulers;

    public static <T> Observable.Transformer<T, T> applySchedulers(){
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable.subscribeOn(getSchedulers())
                        .unsubscribeOn(getSchedulers())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    public static <T> Observable.Transformer<T, T> applyCache(){
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(final Observable<T> observable) {
                return observable.subscribeOn(getSchedulers())
                        .unsubscribeOn(getSchedulers())
                        .map(new Func1<T, T>() {
                            @Override
                            public T call(T t) {
                                try {
                                    SerializeUtils.serializationSync(t.getClass().getName(), t);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return t;
                            }
                        });
            }
        };
    }

    public static <T> Observable.Transformer<T, T> useCache(final Class<? super T> t, final boolean delete){
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(final Observable<T> observable) {
                return Observable.concat(Observable.create(new Observable.OnSubscribe<T>() {
                    @Override
                    public void call(Subscriber<? super T> subscriber) {
                        Object object = null;
                        try {
                            object = SerializeUtils.deserializationSync(t.getClass().getName(), delete);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if(null != object){
                            subscriber.onNext((T) object);
                        }
                        subscriber.onCompleted();
                    }
                }).subscribeOn(getSchedulers()).unsubscribeOn(getSchedulers()), observable);
            }
        };
    }

    public static ExecutorService getExecutors() {
        if(null == sExecutors){
            synchronized (RxUtils.class){
                if(null == sExecutors){
                    int workNum = Math.min(Math.max(4, Utils.getNumberOfCores()) * 2, 8);
//                    If the thread pool has not reached the core size, it creates new threads.
//                    If the core size has been reached and there is no idle threads, it queues tasks.
//                    If the core size has been reached, there is no idle threads, and the queue becomes full, it creates new threads (until it reaches the max size).
//                    If the max size has been reached, there is no idle threads, and the queue becomes full, the rejection policy kicks in.
                    sExecutors = new ThreadPoolExecutor(2, // core size
                            workNum, // max size
                            10*60, // idle timeout
                            TimeUnit.SECONDS,
                            new ArrayBlockingQueue<Runnable>(20));
                    Logger.d("后台线程池的最大线程数量为: " + workNum);
                }
            }
        }

        return sExecutors;
    }

    public static Scheduler getSchedulers() {
        if(null == sChedulers){
            synchronized (RxUtils.class){
                if(null == sChedulers){
                    sChedulers = Schedulers.from(getExecutors());
                }
            }
        }

        return sChedulers;
    }
}
