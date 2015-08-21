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

package sk.mpage.androidsample.infiniterecycleview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sk.mpage.androidsample.infiniterecycleview.helper.InfiniteScrollListener;
import sk.mpage.androidsample.infiniterecycleview.helper.ItemTouchHelperAdapter;
import sk.mpage.androidsample.infiniterecycleview.helper.ItemTouchHelperViewHolder;
import sk.mpage.androidsample.infiniterecycleview.helper.UndoButtonListener;


public class RecyclerListAdapter extends RecyclerView.Adapter<RecyclerListAdapter.ItemViewHolder>
        implements ItemTouchHelperAdapter {

    private final Context context;
    private final List<String> mItems = new ArrayList<>();
    private final UndoButtonListener undoButtonListener;
    private final InfiniteScrollListener infiniteScrollListener;

    public RecyclerListAdapter(Context context, UndoButtonListener undoButtonListener, InfiniteScrollListener infiniteScrollListener) {
        this.context = context;
        this.undoButtonListener = undoButtonListener;
        this.infiniteScrollListener = infiniteScrollListener;
        generateItems();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent, false);
        ItemViewHolder itemViewHolder = new ItemViewHolder(
                view,
                parent.getContext().getResources().getColor(R.color.item_row_bg_selected),
                parent.getContext().getResources().getColor(R.color.item_row_bg));
        return itemViewHolder;
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        holder.textView.setText(mItems.get(position));

    }

    public void undoDismiss(String item, int position) {
        mItems.add(position, item);
        notifyItemInserted(position);
        undoButtonListener.hide();
        itemUndoDismissed(item);
    }

    public void generateItems() {
        infiniteScrollListener.setLoadingStart();

        List<String> data = Arrays.asList(context.getResources().getStringArray(R.array.dummy_items));
        int last_pos = mItems.size();
        mItems.addAll(data);
        notifyItemRangeInserted(last_pos, last_pos + data.size());
        Log.d("infiniteScroll", "data fetched to " + last_pos + " ( " + data.size() + "), total " + mItems.size());

        infiniteScrollListener.setLoadingEnd();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    /* ------------------- Interface methods ------------------*/

    @Override
    public void onItemDismiss(int position, int direction) {
        if (direction == ItemTouchHelper.LEFT || direction == ItemTouchHelper.RIGHT) {
            String content = mItems.get(position);
            mItems.remove(position);
            notifyItemRemoved(position);

            if (direction == ItemTouchHelper.LEFT) {
                itemDismissedLeft(content);
            } else if (direction == ItemTouchHelper.RIGHT) {
                itemDismissedRight(content);
            }

            undoButtonListener.show(content, position);
            infiniteScrollListener.checkDataToAdd();
        }
    }

    @Override
    public void itemDismissedLeft(String item) {
        Log.d("SwipeDismissedItem", "swipe to left");
    }

    @Override
    public void itemDismissedRight(String item) {
        Log.d("SwipeDismissedItem", "swipe to right");
    }

    @Override
    public void itemUndoDismissed(String item) {
        Log.d("SwipeDismissedItem", "undo swipe");

    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return false;
    }

    /* ------------------------------------ */

    public static class ItemViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder {

        public final TextView textView;
        public final int selected_item_color;
        public final int deselected_item_color;

        public ItemViewHolder(View itemView, int selectColor, int deselectColor) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text);
            selected_item_color = selectColor;
            deselected_item_color = deselectColor;
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(selected_item_color);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(deselected_item_color);
        }

    }
}
