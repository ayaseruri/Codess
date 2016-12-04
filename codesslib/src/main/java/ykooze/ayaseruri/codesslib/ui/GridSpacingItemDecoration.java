package ykooze.ayaseruri.codesslib.ui;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by zhangxl on 16/1/6.
 */
public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private int spanCount;
    private int spacing;
    private boolean includeEdge, hasHeader, hasFooter;

    //spanCount 列数  spacing 间距 includeEdge 是否包含边框
    public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge, boolean hasHeader, boolean hasFooter) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.includeEdge = includeEdge;
        this.hasHeader = hasHeader;
        this.hasFooter = hasFooter;
    }

    public void setSpanCount(int spanCount) {
        this.spanCount = spanCount;
    }

    public void setSpacing(int spacing) {
        this.spacing = spacing;
    }

    public void setIncludeEdge(boolean includeEdge) {
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if(spanCount == 0){
            return;
        }

        int position = parent.getChildAdapterPosition(view); // item position
        if(hasHeader){
            if(0 == position){
                return;
            }else {
                position --;
            }
        }

        if((hasFooter && parent.getAdapter().getItemCount() == position + 1)){
            return;
        }

        int column = position % spanCount; // item column

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
            outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

            if (position < spanCount) { // top edge
                outRect.top = spacing;
            }
            outRect.bottom = spacing; // item bottom
        } else {
            outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
            outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
            if (position >= spanCount) {
                outRect.top = spacing; // item top
            }
        }
    }
}
