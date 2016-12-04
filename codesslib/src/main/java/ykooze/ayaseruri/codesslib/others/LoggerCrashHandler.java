package ykooze.ayaseruri.codesslib.others;

import android.content.Context;
import android.content.Intent;

import com.orhanobut.logger.Logger;

import ykooze.ayaseruri.codesslib.BuildConfig;
import ykooze.ayaseruri.codesslib.ui.CrashActivity;

/**
 * Created by wufeiyang on 16/7/22.
 */
public class LoggerCrashHandler implements Thread.UncaughtExceptionHandler {

    private Context mContext;

    public LoggerCrashHandler(){

    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Logger.e(throwable.getMessage());

        Intent intent = new Intent(mContext, CrashActivity.class);
        intent.putExtra(CrashActivity.STACK_MSG_TAG, throwable.getMessage());
        mContext.startActivity(intent);
    }

    public void init(Context context){
        if(BuildConfig.DEBUG){
            mContext = context.getApplicationContext();
            Thread.setDefaultUncaughtExceptionHandler(this);
        }
    }
}
