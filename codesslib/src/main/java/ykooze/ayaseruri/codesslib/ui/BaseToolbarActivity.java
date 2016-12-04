package ykooze.ayaseruri.codesslib.ui;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ykooze.ayaseruri.codesslib.R;
import ykooze.ayaseruri.codesslib.others.Utils;

/**
 * Created by wufeiyang on 16/7/12.
 */
public class BaseToolbarActivity extends BaseActivity {

    protected LinearLayout mRootView;
    private ImageView mStatusBarView;

    protected Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isSystemReady = false;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            isSystemReady = true;
        }

        if (isSystemReady) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            mStatusBarView = new ImageView(this);
            mStatusBarView.setBackgroundColor(getPlaceholderColor());
            mStatusBarView.setVisibility((isTranslucent() && isAddPlaceHolder()) ? View.VISIBLE : View.GONE);

            mRootView = new LinearLayout(this);
            mRootView.setOrientation(LinearLayout.VERTICAL);

            mRootView.addView(mStatusBarView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , Utils.getStatusBarHeight(this)));
            super.setContentView(mRootView);
        }

        if(!isCustomToolbar()){
            View.inflate(this, R.layout.codess_base_activity_toolbar, mRootView);
            mToolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);
            mToolbar.setBackgroundColor(getToolBarBgColor());
            mToolbar.setTitle("");

            setSupportActionBar(mToolbar);
            if(isShowHomeAsUpEnabled()){

                mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onNavigationClick();
                    }
                });

                ActionBar actionBar = getSupportActionBar();

                if(null != actionBar){
                    actionBar.setDisplayHomeAsUpEnabled(true);
                }
            }
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        if(null == mRootView){
            super.setContentView(layoutResID);
        }else {
            View.inflate(this, layoutResID, mRootView);
        }
    }

    @Override
    public void setContentView(View view) {
        if(null == mRootView){
            super.setContentView(view);
        }else {
            mRootView.addView(view);
        }
    }

    /**
     * 是否采用浸入模式
     */
    protected boolean isTranslucent() {
        return true;
    }

    protected boolean isAddPlaceHolder() {
        return true;
    }

    protected void setAddPlaceHolder(boolean isAdd){
        if(null != mStatusBarView){
            mStatusBarView.setVisibility(isAdd ? View.VISIBLE : View.GONE);
        }
    }

    protected int getPlaceholderColor() {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        return typedValue.data;
    }

    private void setPlaceholderColor(int color){
        if(null != mStatusBarView){
            mStatusBarView.setBackgroundColor(Utils.getColor(this, color));
        }
    }

    protected boolean isCustomToolbar() {
        return false;
    }

    protected boolean isShowHomeAsUpEnabled(){
        return true;
    }

    protected void onNavigationClick() {
        onBackPressed();
    }

    protected int getToolBarBgColor(){
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        return typedValue.data;
    }

    protected void setToolBarBgColor(int color){
        if(null != mToolbar){
            mToolbar.setBackgroundColor(color);
        }
    }

    protected void setTitle(String title){
        mToolbar.setTitle(title);
    }
}
