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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import sk.mpage.androidsample.infiniterecycleview.helper.InfiniteScrollListener;
import sk.mpage.androidsample.infiniterecycleview.helper.SimpleItemTouchHelperCallback;
import sk.mpage.androidsample.infiniterecycleview.helper.UndoButtonListener;

public class RecyclerListFragment extends Fragment
        implements UndoButtonListener, InfiniteScrollListener, SearchView.OnQueryTextListener {

    private ItemTouchHelper mItemTouchHelper;
    private FloatingActionButton myFab;
    private RecyclerListAdapter adapter;
    private final LinearLayoutManager mLayoutManager;
    private RecyclerView recyclerView;


    private int loading = InfiniteScrollListener.LoadingIdle;
    private int visibleThreshold = 0;
    int firstVisibleItem, visibleItemCount, totalItemCount;

    private Runnable fabHideHandler = null;

    public RecyclerListFragment() {
        mLayoutManager = new LinearLayoutManager(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        myFab = (FloatingActionButton) view.findViewById(R.id.fab);
        myFab.hide();

        adapter = new RecyclerListAdapter(getActivity(), this, this);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(mLayoutManager);


        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter, getActivity().getApplicationContext());
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //check data count if we are not at the end
                checkDataToAdd();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);

    }


    /*-----------------Interface methods -----------------------*/

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        adapter.setFilterQuery(query);
        return true;
    }

    @Override
    public void checkDataToAdd() {
        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = mLayoutManager.getItemCount();
        firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

        Log.d("infiniteScroll", "Visible " + visibleItemCount + ", total " + totalItemCount + ", first " + firstVisibleItem);

        if (loading != InfiniteScrollListener.LoadingIdle) {
            Log.d("infiniteScroll", "waiting for data to fetch");
        } else if ((totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            //remaining less then visible + threshold, load next
            Log.d("infiniteScroll", "adding new data");
            adapter.generateItems();
        }
    }

    @Override
    public void setLoadingStart() {
        Log.d("infiniteScroll", "started");
        loading = InfiniteScrollListener.LoadingRunning;
    }

    @Override
    public void setLoadingEnd() {
        Log.d("infiniteScroll", "end");
        loading = InfiniteScrollListener.LoadingIdle;
    }

    @Override
    public void setLoadingError() {
        loading = InfiniteScrollListener.LoadingError;
    }

    @Override
    public void show(final String item, final int position) {

        // set undo button event to undo dismiss
        myFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.undoDismiss(item, position);
            }
        });

        Log.d("UndoButton", "showing");
        if (!myFab.isShown())
            myFab.show();

        //remove previous postDelayed message
        if (fabHideHandler != null)
            myFab.removeCallbacks(fabHideHandler);

        //hide undo button after 5 seconds
        myFab.postDelayed(fabHideHandler = new Runnable() {
            @Override
            public void run() {
                hide();
            }
        }, 5000);

    }

    @Override
    public void hide() {
        Log.d("UndoButton", "hidding");
        if (myFab.isShown())
            myFab.hide();
    }
}
