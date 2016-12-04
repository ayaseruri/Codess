package ykooze.ayaseruri.codesslib.others;

import android.content.Context;
import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.github.johnpersano.supertoasts.library.SuperToast;
import com.github.johnpersano.supertoasts.library.utils.PaletteUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.github.johnpersano.supertoasts.library.Style.DURATION_LONG;

/**
 * Created by wufeiyang on 16/7/20.
 */
public class ToastUtils {

    public static final int TOAST_ALERT = 0;
    public static final int TOAST_INFO = 1;
    public static final int TOAST_CONFIRM = 2;

    private static Context mContext;
    private static SuperToast mSuperToast;

    public static void init(Context context){
        if(null == mContext){
            mContext = context.getApplicationContext();
        }
    }


    public static void showTost(@ToastType int type, String content){
        if(TextUtils.isEmpty(content)){
            return;
        }

        if(null != mContext && null != mSuperToast && mSuperToast.isShowing() && mSuperToast.getText().equals(content)){
            return;
        }

        Style style = Style.green();
        switch (type){
            case TOAST_ALERT:
                style = Style.red();
                break;
            case TOAST_CONFIRM:
                style = Style.green();
                break;
            case TOAST_INFO:
                style = Style.grey();
                break;
            default:
                break;
        }

        mSuperToast = SuperToast.create(mContext, content, DURATION_LONG, style)
                .setFrame(Style.FRAME_KITKAT)
                .setText(content)
                .setAnimations(Style.ANIMATIONS_FADE);
        mSuperToast.show();
    }


    @IntDef({TOAST_INFO, TOAST_ALERT, TOAST_CONFIRM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ToastType{

    }
}
