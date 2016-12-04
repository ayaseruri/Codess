package ykooze.ayaseruri.codesslib.ui;

import android.Manifest;
import android.content.Intent;
import android.database.Observable;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.io.FileNotFoundException;

import rx.Observer;
import rx.Subscriber;
import rx.functions.Action1;
import ykooze.ayaseruri.codesslib.R;
import ykooze.ayaseruri.codesslib.bitmap.BitmapUtils;
import ykooze.ayaseruri.codesslib.others.ToastUtils;
import ykooze.ayaseruri.codesslib.others.Utils;
import ykooze.ayaseruri.codesslib.rx.RxUtils;

/**
 * Created by wufeiyang on 16/7/22.
 */
public class CrashActivity extends BaseToolbarActivity {

    public static final String STACK_MSG_TAG = "stack_msg";

    private TextView mStack;
    private int mTotalHeight;
    private Bitmap mScreenShotBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.codess_crash_activity);

        mStack = (TextView) findViewById(R.id.stack_msg);

        String stackMsg = getIntent().getStringExtra(STACK_MSG_TAG);
        if(null != stackMsg){
            mStack.setText(stackMsg);
        }

        ToastUtils.init(this);
        initBtns();
    }

    private void initBtns(){
        findViewById(R.id.screen_short_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RxPermissions.getInstance(CrashActivity.this)
                        .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean granted) {
                                if(granted){
                                    screenShortAction();
                                }else {
                                    ToastUtils.showTost(ToastUtils.TOAST_ALERT, "暂无写入权限");
                                }
                            }
                        });
            }
        });

        findViewById(R.id.restart_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.doRestart(CrashActivity.this);
            }
        });
    }

    private void screenShortAction(){

        final ScrollView scrollView = (ScrollView) findViewById(R.id.root_scroll);
        final int scrollViewChildCount = scrollView.getChildCount();

        mTotalHeight = 0;

        for(int i=0; i < scrollViewChildCount; i ++){
            mTotalHeight += scrollView.getChildAt(i).getHeight();
        }

        rx.Observable.create(new rx.Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                mScreenShotBitmap = Bitmap.createBitmap(scrollView.getWidth(), mTotalHeight
                        , Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(mScreenShotBitmap);
                scrollView.draw(canvas);

                if(null != mScreenShotBitmap){
                    File dir = new File(Environment.getExternalStorageDirectory(), "CodessDebug");
                    if(!dir.exists()){
                        dir.mkdir();
                    }

                    String fileName = "CodessDebug_" + System.currentTimeMillis() + ".jpg";

                    BitmapUtils.storeBitmap(mScreenShotBitmap, Bitmap.CompressFormat.JPEG
                            , 80, dir.getAbsolutePath(), fileName);

                    // 其次把文件插入到系统图库
                    try {
                        MediaStore.Images.Media.insertImage(getContentResolver()
                                , dir.getAbsolutePath(), fileName, null);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    // 最后通知图库更新
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
                            , Uri.parse("file://" + dir.getAbsolutePath() + "/" + fileName)));

                    subscriber.onNext(true);
                }else {
                    subscriber.onNext(false);
                }
                subscriber.onCompleted();
            }
        }).compose(RxUtils.<Boolean>applySchedulers()).subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean success) {
                ToastUtils.showTost(success ? ToastUtils.TOAST_CONFIRM : ToastUtils.TOAST_ALERT
                        , success ? "截图已经成功保存到系统相册XD" : "截图失败,请手动截图XD");
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                ToastUtils.showTost(ToastUtils.TOAST_ALERT, "保存截图失败,请手动截图XD");
            }
        });
    }

    @Override
    protected boolean isCustomToolbar() {
        return true;
    }

    @Override
    protected boolean isAddPlaceHolder() {
        return false;
    }

}
