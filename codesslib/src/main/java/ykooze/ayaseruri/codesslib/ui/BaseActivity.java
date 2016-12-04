package ykooze.ayaseruri.codesslib.ui;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.orhanobut.logger.Logger;
import com.trello.rxlifecycle.LifecycleProvider;
import com.trello.rxlifecycle.LifecycleTransformer;
import com.trello.rxlifecycle.RxLifecycle;
import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.android.RxLifecycleAndroid;

import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Created by wufeiyang on 16/6/16.
 */
class BaseActivity extends AppCompatActivity implements LifecycleProvider<ActivityEvent> {

    private final BehaviorSubject<ActivityEvent> lifecycleSubject = BehaviorSubject.create();

    @Override
    @NonNull
    @CheckResult
    public final Observable<ActivityEvent> lifecycle() {
        return lifecycleSubject.asObservable();
    }

    @Override
    @NonNull
    @CheckResult
    public final <T> LifecycleTransformer<T> bindUntilEvent(@NonNull ActivityEvent event) {
        return RxLifecycle.bindUntilEvent(lifecycleSubject, event);
    }

    @Override
    @NonNull
    @CheckResult
    public final <T> LifecycleTransformer<T> bindToLifecycle() {
        return RxLifecycleAndroid.bindActivity(lifecycleSubject);
    }

    @Override
    @CallSuper
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onEvent(ActivityEvent.CREATE);
        lifecycleSubject.onNext(ActivityEvent.CREATE);
        Logger.d("Codess BaseActivity: onCreate");
    }

    @Override
    @CallSuper
    protected void onStart() {
        super.onStart();
        onEvent(ActivityEvent.START);
        lifecycleSubject.onNext(ActivityEvent.START);
        Logger.d("Codess BaseActivity: onStart");
    }

    @Override
    @CallSuper
    protected void onResume() {
        super.onResume();
        onEvent(ActivityEvent.RESUME);
        lifecycleSubject.onNext(ActivityEvent.RESUME);
        Logger.d("Codess BaseActivity: onResume");
    }

    @Override
    @CallSuper
    protected void onPause() {
        onEvent(ActivityEvent.PAUSE);
        lifecycleSubject.onNext(ActivityEvent.PAUSE);
        Logger.d("Codess BaseActivity: onPause");
        super.onPause();
    }

    @Override
    @CallSuper
    protected void onStop() {
        onEvent(ActivityEvent.STOP);
        lifecycleSubject.onNext(ActivityEvent.STOP);
        Logger.d("Codess BaseActivity: onStop");
        super.onStop();
    }

    @Override
    @CallSuper
    protected void onDestroy() {
        onEvent(ActivityEvent.DESTROY);
        lifecycleSubject.onNext(ActivityEvent.DESTROY);
        Logger.d("Codess BaseActivity: onDestroy");
        super.onDestroy();
    }

    //用于处理必要的回调，例如统计等
    protected void onEvent(ActivityEvent activityEvent){

    }
}
