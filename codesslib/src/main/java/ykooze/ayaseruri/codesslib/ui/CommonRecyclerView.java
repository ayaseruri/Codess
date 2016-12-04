package ykooze.ayaseruri.codesslib.ui;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;

import com.trello.rxlifecycle.LifecycleTransformer;
import com.trello.rxlifecycle.RxLifecycle;
import com.trello.rxlifecycle.android.RxLifecycleAndroid;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func0;
import rx.observers.Subscribers;
import ykooze.ayaseruri.codesslib.adapter.RecyclerAdapter;
import ykooze.ayaseruri.codesslib.rx.RxUtils;

/**
 * Created by wufeiyang on 16/6/17.
 */
public class CommonRecyclerView extends RecyclerView {

    public static final int TYPE_REFRESH = 0;
    public static final int TYPE_LOADMORE = 1;
    public static final int TYPE_FIRSTIN = 2;
    public static final int TYPE_END = 3;

    private int mPreLoadPadding = 20;
    private boolean isLoading, isRefreshing;
    private Runnable mRefreshTask;


    public CommonRecyclerView(Context context) {
        super(context);
    }

    public CommonRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public <T, V extends View & RecyclerAdapter.IRecyclerItem<T>> void init
            (@NonNull final ICommonRecyclerView<T> iCommonRecyclerView
                    , final RecyclerAdapter<T, V> recyclerAdapter){
        setAdapter(new SlideInBottomAnimationAdapter(recyclerAdapter)) ;

        //执行firstin任务
        Observable.defer(new Func0<Observable<List<T>>>() {
            @Override
            public Observable<List<T>> call() {
                return Observable.create(new Observable.OnSubscribe<List<T>>() {
                    @Override
                    public void call(Subscriber<? super List<T>> subscriber) {
                        subscriber.onStart();
                        subscriber.onNext(iCommonRecyclerView.getFirstInData());
                        subscriber.onCompleted();
                    }
                });
            }
        })
        .compose(RxUtils.<List<T>>applySchedulers())
                .compose(RxLifecycleAndroid.<List<T>>bindView(this))
                .subscribe(new Subscriber<List<T>>() {
                    @Override
                    public void onCompleted() {
                        iCommonRecyclerView.uiLoadingComplete(TYPE_FIRSTIN);
                    }

                    @Override
                    public void onError(Throwable e) {
                        iCommonRecyclerView.uiLoadingError(TYPE_FIRSTIN, e);
                    }

                    @Override
                    public void onNext(List<T> list) {
                        recyclerAdapter.refresh(list);
                    }

                    @Override
                    public void onStart() {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                iCommonRecyclerView.uiLoadingStart(TYPE_FIRSTIN);
                            }
                        });
                    }
                });

        //构建refresh任务
        mRefreshTask = new Runnable() {
            @Override
            public void run() {
                Observable.defer(new Func0<Observable<List<T>>>() {
                    @Override
                    public Observable<List<T>> call() {
                        return Observable.create(new Observable.OnSubscribe<List<T>>() {
                            @Override
                            public void call(Subscriber<? super List<T>> subscriber) {
                                subscriber.onStart();
                                subscriber.onNext(iCommonRecyclerView.getRefreshData());
                                subscriber.onCompleted();
                            }
                        });
                    }
                })
                .compose(RxUtils.<List<T>>applySchedulers())
                        .compose(RxLifecycleAndroid.<List<T>>bindView(CommonRecyclerView.this))
                        .subscribe(new Subscriber<List<T>>() {
                            @Override
                            public void onCompleted() {
                                iCommonRecyclerView.uiLoadingComplete(TYPE_REFRESH);
                                isRefreshing = false;
                            }

                            @Override
                            public void onError(Throwable e) {
                                iCommonRecyclerView.uiLoadingError(TYPE_REFRESH, e);
                                isRefreshing = false;
                            }

                            @Override
                            public void onNext(List<T> list) {
                                recyclerAdapter.refresh(list);
                            }

                            @Override
                            public void onStart() {
                                iCommonRecyclerView.uiLoadingStart(TYPE_REFRESH);
                            }
                        });
            }
        };

        //新增滑动listener
        clearOnScrollListeners();
        addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                    int totalItemCount = layoutManager.getItemCount();
                    if(totalItemCount > 0 && iCommonRecyclerView.isEnd() && getLastVisibleItemPosition(recyclerView) >= totalItemCount - 1 ){
                        iCommonRecyclerView.uiLoadingComplete(TYPE_END);
                    }else if(!iCommonRecyclerView.isEnd() && !isLoading){
                        if(getLastVisibleItemPosition(recyclerView) >= totalItemCount - 1 - mPreLoadPadding){
                            isLoading = true;
                            Observable.defer(new Func0<Observable<List<T>>>() {
                                @Override
                                public Observable<List<T>> call() {
                                    return Observable.create(new Observable.OnSubscribe<List<T>>() {
                                        @Override
                                        public void call(Subscriber<? super List<T>> subscriber) {
                                            subscriber.onNext(iCommonRecyclerView.getLoadMoreData());
                                            subscriber.onCompleted();
                                        }
                                    });
                                }
                            })
                            .compose(RxUtils.<List<T>>applySchedulers())
                                    .compose(RxLifecycleAndroid.<List<T>>bindView(CommonRecyclerView.this))
                                    .subscribe(new Subscriber<List<T>>() {
                                        @Override
                                        public void onCompleted() {
                                            iCommonRecyclerView.uiLoadingComplete(TYPE_LOADMORE);
                                            isLoading = false;
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            iCommonRecyclerView.uiLoadingError(TYPE_LOADMORE, e);
                                            isLoading = false;
                                        }

                                        @Override
                                        public void onNext(List<T> list) {
                                            recyclerAdapter.add(list);
                                        }

                                        @Override
                                        public void onStart() {
                                            iCommonRecyclerView.uiLoadingStart(TYPE_LOADMORE);
                                        }
                                    });
                        }
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });


    }

    public int getLastVisibleItemPosition(RecyclerView recyclerView){
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager)
                    .findLastVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredGridLayoutManager
                    = (StaggeredGridLayoutManager) layoutManager;
            int[] lastPositions = new int[staggeredGridLayoutManager.getSpanCount()];
            staggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions);
            return findMax(lastPositions);
        }
        return -1;
    }

    public void refreshData(){
        if(!isRefreshing){
            synchronized (CommonRecyclerView.class){
                if(!isRefreshing){
                    isRefreshing = true;
                    post(mRefreshTask);
                }
            }
        }
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    public int getPreLoadPadding() {
        return mPreLoadPadding;
    }

    public void setPreLoadPadding(int preLoadPadding) {
        mPreLoadPadding = preLoadPadding;
    }

    public interface ICommonRecyclerView<T>{
        List<T> getFirstInData();
        List<T> getRefreshData();
        List<T> getLoadMoreData();
        boolean isEnd();
        void uiLoadingStart(@LoadingType int loadingType);
        void uiLoadingError(@LoadingType int loadingType, Throwable e);
        void uiLoadingComplete(@LoadingType int loadingType);
    }

    public static class SimpleICommonRecyclerView<T> implements ICommonRecyclerView<T>{

        @Override
        public List<T> getFirstInData() {
            return null;
        }

        @Override
        public List<T> getRefreshData() {
            return null;
        }

        @Override
        public List<T> getLoadMoreData() {
            return null;
        }

        @Override
        public boolean isEnd() {
            return false;
        }

        @Override
        public void uiLoadingStart(int loadingType) {

        }

        @Override
        public void uiLoadingError(int loadingType, Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void uiLoadingComplete(int loadingType) {

        }
    }

    @IntDef({TYPE_FIRSTIN, TYPE_LOADMORE, TYPE_REFRESH, TYPE_END})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LoadingType{

    }
}
