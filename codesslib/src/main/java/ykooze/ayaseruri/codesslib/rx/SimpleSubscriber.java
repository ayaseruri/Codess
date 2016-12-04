package ykooze.ayaseruri.codesslib.rx;

import rx.Subscriber;

/**
 * Created by wufeiyang on 16/8/2.
 */
public class SimpleSubscriber<T> extends Subscriber<T> {
    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void onNext(T t) {

    }

    @Override
    public void onStart() {

    }
}
