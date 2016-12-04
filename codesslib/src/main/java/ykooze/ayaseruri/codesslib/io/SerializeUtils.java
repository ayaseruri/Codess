package ykooze.ayaseruri.codesslib.io;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import ykooze.ayaseruri.codesslib.rx.RxUtils;

/**
 * @author zhangxl
 * @className SerializeUtils
 * @create 2014年4月16日 上午11:31:07
 * @description 序列化工具类，可用于序列化对象到文件或从文件反序列化对象
 */
public class SerializeUtils {
    /**
     * 从文件反序列化对象
     *
     * @param tag
     * @return
     * @throws RuntimeException if an error occurs
     */

    private static Context mContext;

    public static void init(@NonNull Context context){
        mContext = context.getApplicationContext();
    }

    public static Object deserializationSync(String tag, boolean delete) throws Exception {
        if(null == mContext){
            throw new Exception("you should call init(Context context) at last once before");
        }

        FileInputStream fileInputStream = mContext.openFileInput(tag);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        Object object = objectInputStream.readObject();
        objectInputStream.close();
        if (delete) {
            mContext.deleteFile(tag);
        }
        return object;
    }

    public static <T> T deserializationSync(Class<? extends T> className, boolean delete) throws Exception{
        return (T) deserializationSync(className.getName(), delete);
    }
    /**
     * 序列化对象到文件，默认异步
     *
     * @param tag
     * @param obj
     * @return
     * @throws RuntimeException if an error occurs
     */
    public static void serialization(final String tag, final Object obj) {
        Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    serializationSync(tag, obj);
                    subscriber.onNext(true);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(RxUtils.getSchedulers()).subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean b) {

            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {

            }
        });
    }

    public static void serialization(final Object obj){
        serialization(obj.getClass().getName(), obj);
    }

    //同步序列化
    public static void serializationSync(final String tag, final Object obj) throws Exception {
        if(null == mContext){
            throw new Exception("you should call init(Context context) at last once before");
        }

        FileOutputStream fileOutputStream = mContext.openFileOutput(tag, Context.MODE_PRIVATE);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(obj);
        objectOutputStream.close();
    }

    public static void serializationSync(final Object obj) throws Exception{
        serializationSync(obj.getClass().getName(), obj);
    }
}
