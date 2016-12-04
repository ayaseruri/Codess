package ykooze.ayaseruri.codesslib.adapter;

import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wufeiyang on 16/5/7.
 */
public abstract class RecyclerAdapter<T, V extends View & RecyclerAdapter.IRecyclerItem<T>>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = -100044;
    private static final int TYPE_NORMAL = -100045;
    private static final int TYPE_FOOTER = -100046;

    protected List<T> mItemDatas;
    protected View mHeaderView;
    protected View mFooterView;

    public RecyclerAdapter() {
        mItemDatas = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case TYPE_HEADER:
                return new BaseHeaderAndFooterViewHolder<>(mHeaderView);
            case TYPE_NORMAL:
                return new BaseItemViewHolder<>(onCreateItemView(parent, viewType));
            case TYPE_FOOTER:
                return new BaseHeaderAndFooterViewHolder<>(mFooterView);
            default:
                break;
        }

        return new BaseItemViewHolder<>(onCreateItemView(parent, viewType));
    }

    @Override
    public int getItemCount() {
        return (mHeaderView == null ? 0 : 1) + (mFooterView == null ? 0 : 1) + mItemDatas.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(null != mHeaderView && 0 == position){
            return TYPE_HEADER;
        }

        if(null != mFooterView && getItemCount() - 1 == position){
            return TYPE_FOOTER;
        }

        return getViewType(position);
    }

    @UiThread
    public void refresh(List<T> datas){
        if(null != datas){
            mItemDatas.clear();
            mItemDatas.addAll(datas);
            notifyDataSetChanged();
        }
    }

    @UiThread
    public void add(T data){
        if(null != data){
            mItemDatas.add(data);
            notifyItemInserted(mItemDatas.hashCode());
        }
    }

    @UiThread
    public void add(List<T> datas){
        if(null != datas){
            mItemDatas.addAll(datas);
            notifyItemRangeChanged(mItemDatas.size() - datas.size(), datas.size());
        }
    }

    @UiThread
    public void addAll(int index, List<T> datas){
        if(null != datas){
            mItemDatas.addAll(index, datas);
            notifyItemRangeChanged(index, datas.size());
        }
    }

    @UiThread
    public void remove(int i){
        if(i >= 0 && i < mItemDatas.size()){
            mItemDatas.remove(i);
            notifyItemRemoved(i);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if(viewType != TYPE_HEADER && viewType != TYPE_FOOTER){
            int paddingStart = (null == mHeaderView ? 0 : 1);
            position -= paddingStart;
            V v = (V) holder.itemView;
            v.onBindData(mItemDatas.get(position), position);
        }
    }

    protected int getViewType(int itemPostion){
        return TYPE_NORMAL;
    }

    protected abstract V onCreateItemView(ViewGroup parent, int viewType);

    public View getHeaderView() {
        return mHeaderView;
    }

    @UiThread
    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyDataSetChanged();
    }

    public View getFooterView() {
        return mFooterView;
    }

    @UiThread
    public void setFooterView(View footerView) {
        mFooterView = footerView;
        notifyDataSetChanged();
    }

    public T getData(int pos){
        if(pos >= 0 && pos < mItemDatas.size()){
            return mItemDatas.get(pos);
        }
        return null;
    }

    static class BaseItemViewHolder<V extends View> extends RecyclerView.ViewHolder{
        public BaseItemViewHolder(V itemView) {
            super(itemView);
        }
    }

    static class BaseHeaderAndFooterViewHolder<V extends View> extends RecyclerView.ViewHolder{
        public BaseHeaderAndFooterViewHolder(V itemView) {
            super(itemView);
        }
    }

    public interface IRecyclerItem<T>{
        void onBindData(T t, int postion);
    }
}
