package ykooze.ayaseruri.codesslib.ui;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import ykooze.ayaseruri.codesslib.R;
import ykooze.ayaseruri.codesslib.net.NetUtils;
import ykooze.ayaseruri.codesslib.others.ToastUtils;
import ykooze.ayaseruri.codesslib.others.Utils;
import ykooze.ayaseruri.codesslib.rx.RxUtils;

/**
 * Created by wufeiyang on 16/5/29.
 */
public class UpdateManager {
    private static final String UPDATE_SETTINGS_PREFER = "update_settings_prefer";
    private static final String INGNORE_VER_KEY = "ingnore_ver";
    private static final int DOWNLOAD_NOTIFICATION_ID = 10086;

    private Context mContext;
    private Dialog mDialog;
    private TextView mDetailTV;

    private Button mCancel, mOk, mIngnore;
    private NotificationManager mNotifyMgr;
    private SharedPreferences mUpdateSettingsSharePrefer;

    public UpdateManager(Context mContext) {
        this.mContext = mContext;
        this.mNotifyMgr = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        this.mUpdateSettingsSharePrefer = mContext.getSharedPreferences(UPDATE_SETTINGS_PREFER, Context.MODE_PRIVATE);
    }

    public void checkUpdate(final boolean isSilent, final OkHttpClient okHttpClient, final String url){
        Observable.create(new Observable.OnSubscribe<UpdateInfo>() {
            @Override
            public void call(Subscriber<? super UpdateInfo> subscriber) {
                subscriber.onStart();
                Request request = new Request.Builder().url(url).build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    if(response.isSuccessful()){
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        int ingnoreVer = mUpdateSettingsSharePrefer.getInt(INGNORE_VER_KEY, -1);
                        int ver = jsonObject.getInt("ver_code");
                        if(ingnoreVer == ver || Utils.getVerCode(mContext) == ver){
                            subscriber.onNext(null);
                        }else {
                            UpdateInfo updateInfo = new UpdateInfo();
                            updateInfo.setVer_code(ver);
                            updateInfo.setDetail(jsonObject.getString("detail"));
                            updateInfo.setDownload_url(jsonObject.getString("download_url"));
                            updateInfo.setIs_fouce_update(jsonObject.getBoolean("is_fouce_update"));

                            subscriber.onNext(updateInfo);
                        }
                        subscriber.onCompleted();
                    }else {
                        String errorMsg = mContext.getString(R.string.can_not_connect_to_update_server);
                        ToastUtils.showTost(ToastUtils.TOAST_ALERT, errorMsg);
                        subscriber.onError(new Exception(errorMsg));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                } catch (JSONException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        }).compose(RxUtils.<UpdateInfo>applySchedulers()).subscribe(new Subscriber<UpdateInfo>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                if(!isSilent){
                    ToastUtils.showTost(ToastUtils.TOAST_ALERT, mContext.getString(R.string.update_failed));
                }
            }

            @Override
            public void onNext(final UpdateInfo updateInfo) {
                if(null != updateInfo){
                    mDialog = new Dialog(mContext);
                    mDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                    mDialog.setCancelable(false);
                    mDialog.setContentView(R.layout.codess_dialog_update);

                    mDetailTV = (TextView) mDialog.findViewById(R.id.update_detail);
                    mCancel = (Button) mDialog.findViewById(R.id.cancel);
                    mIngnore = (Button) mDialog.findViewById(R.id.ingnore);
                    mOk = (Button) mDialog.findViewById(R.id.ok);

                    mIngnore.setVisibility(updateInfo.is_fouce_update() ? View.GONE : View.VISIBLE);

                    mCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDialog.dismiss();
                        }
                    });

                    mIngnore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mUpdateSettingsSharePrefer.edit().putInt(INGNORE_VER_KEY, updateInfo.getVer_code()).apply();
                            mDialog.dismiss();
                        }
                    });

                    mOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            downloadUpdate(mContext
                                    , okHttpClient
                                    , updateInfo.getDownload_url()
                                    , "update_" + System.currentTimeMillis() + ".apk"
                                    , mContext.getCacheDir().getPath());

                            mDialog.dismiss();
                        }
                    });

                    mDetailTV.setText(updateInfo.getDetail());

                    mDialog.show();
                }
            }

            @Override
            public void onStart() {
                if(!isSilent){
                    ToastUtils.showTost(ToastUtils.TOAST_INFO, mContext.getString(R.string.update_begin));
                }
            }
        });
    }

    private void downloadUpdate(Context context, OkHttpClient okHttpClient,
                                final String url, final String saveName, final String savePath){

        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext);

        NetUtils.downloadFile(context, okHttpClient, url, saveName, savePath, new NetUtils.IDownLoad() {
            @Override
            public void onStart() {
                notificationBuilder.setContentTitle(mContext.getString(R.string.update_ing));
                mNotifyMgr.notify(DOWNLOAD_NOTIFICATION_ID, notificationBuilder.build());
            }

            @Override
            public void onDownloading(int percent) {
                notificationBuilder.setProgress(100, percent, false);
                mNotifyMgr.notify(DOWNLOAD_NOTIFICATION_ID, notificationBuilder.build());
                ToastUtils.showTost(ToastUtils.TOAST_ALERT, mContext.getString(R.string.update_failed));

            }

            @Override
            public void onComplete() {
                notificationBuilder.setProgress(100, 100, false);
                notificationBuilder.setContentTitle(mContext.getString(R.string.click_to_update));
                mNotifyMgr.notify(DOWNLOAD_NOTIFICATION_ID, notificationBuilder.build());

                File apkFile = new File(savePath + "/" + saveName);
                if(apkFile.exists()){
                    Uri uri = Uri.fromFile(apkFile); //这里是APK路径
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "application/vnd.android.package-archive");
                    mContext.startActivity(intent);
                }
            }

            @Override
            public void onError(Throwable e) {
                notificationBuilder.setContentTitle(mContext.getString(R.string.update_failed));
                mNotifyMgr.notify(DOWNLOAD_NOTIFICATION_ID, notificationBuilder.build());
            }
        });
    }

    static class UpdateInfo {
        private String detail;
        private int ver_code;
        private boolean is_fouce_update;
        private String download_url;

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public int getVer_code() {
            return ver_code;
        }

        public void setVer_code(int ver_code) {
            this.ver_code = ver_code;
        }

        public boolean is_fouce_update() {
            return is_fouce_update;
        }

        public void setIs_fouce_update(boolean is_fouce_update) {
            this.is_fouce_update = is_fouce_update;
        }

        public String getDownload_url() {
            return download_url;
        }

        public void setDownload_url(String download_url) {
            this.download_url = download_url;
        }
    }
}
