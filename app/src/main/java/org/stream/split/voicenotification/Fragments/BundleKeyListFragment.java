package org.stream.split.voicenotification.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.stream.split.voicenotification.Adapters.BundleKeysAdapter;
import org.stream.split.voicenotification.Enities.BundleKeyEntity;
import org.stream.split.voicenotification.Helpers.SimpleItemTouchHelperCallback;
import org.stream.split.voicenotification.Interfaces.OnStartDragListener;
import org.stream.split.voicenotification.R;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by split on 2016-02-12.
 */
public class BundleKeyListFragment<T extends BundleKeyEntity & Serializable> extends BaseFragment implements OnStartDragListener {

    private static final String TAG = "BundleKeyListFragment";
    private static String ARG_BUNDLEKEY_LIST = "argBundlekeyList";
    private static String ARG_BUNDLEKEY_LIST_TYPE = "argBundlekeyListType";


    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private BundleKeysAdapter mAdapter;
    private ItemTouchHelper mItemTouchHelper;


    public BundleKeysAdapter getAdapter() {
        return mAdapter;
    }

    public static <T extends BundleKeyEntity & Serializable> BundleKeyListFragment newInstance(ArrayList<T> bundleKeyEntityList)
    {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_BUNDLEKEY_LIST, bundleKeyEntityList);
        BundleKeyListFragment fragment = new BundleKeyListFragment();
        fragment.setArguments(bundle);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List entities = new ArrayList();
        if (getArguments() != null) {
            entities = (List<T>) getArguments().getSerializable(ARG_BUNDLEKEY_LIST);
        }
        mAdapter = new BundleKeysAdapter(entities,this,getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bundlekey_list, container, false);


        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_budlekeys);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        SimpleItemTouchHelperCallback callback = new SimpleItemTouchHelperCallback(mAdapter);
        callback.setIsItemViewSwipeEnabled(false);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);


        return view;
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

}
