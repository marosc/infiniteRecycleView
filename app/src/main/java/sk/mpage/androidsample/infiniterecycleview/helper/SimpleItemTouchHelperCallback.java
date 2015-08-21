/*
 * Copyright (C) 2015 Maros Cavojsky, (mpage.sk)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sk.mpage.androidsample.infiniterecycleview.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import sk.mpage.androidsample.infiniterecycleview.R;

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final ItemTouchHelperAdapter mAdapter;
    private final Context context;

    public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter, Context context) {
        mAdapter = adapter;
        this.context = context;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final int dragFlags = 0;
        final int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mAdapter.onItemDismiss(viewHolder.getAdapterPosition(), direction);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            // Get RecyclerView item from the ViewHolder
            View itemView = viewHolder.itemView;
            Paint paint = new Paint();

            Bitmap like_bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_thumb_up_outline);
            Bitmap dislike_bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_thumb_down_outline);
            float width = like_bitmap.getWidth();

            if (dX > 0) { // swiping right
                paint.setColor(context.getResources().getColor(R.color.swipeDislike));
                c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX + dX, (float) itemView.getBottom(), paint);

                if (dX > 96f + width) {//when slided are is more than icon width show icon
                    float height = (itemView.getHeight() / 2) - (dislike_bitmap.getHeight() / 2);
                    c.drawBitmap(dislike_bitmap, 96f, (float) itemView.getTop() + height, null);
                }

            } else { // swiping left
                paint.setColor(context.getResources().getColor(R.color.swipeLike));
                c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), paint);

                if (dX < -1 * (96 + width)) {//when slided are is more than icon width show icon
                    float height = (itemView.getHeight() / 2) - (dislike_bitmap.getHeight() / 2);
                    c.drawBitmap(like_bitmap, ((float) itemView.getRight() - like_bitmap.getWidth()) - 96f, (float) itemView.getTop() + height, null);
                }

            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);

        if (viewHolder instanceof ItemTouchHelperViewHolder) {
            // Tell the view holder it's time to restore the idle state
            ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
            itemViewHolder.onItemClear();
        }
    }
}
