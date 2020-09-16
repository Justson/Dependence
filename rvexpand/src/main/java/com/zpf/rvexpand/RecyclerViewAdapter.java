package com.zpf.rvexpand;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.zpf.api.ItemListAdapter;
import com.zpf.api.ItemTypeManager;
import com.zpf.api.ItemViewCreator;
import com.zpf.api.OnItemClickListener;
import com.zpf.api.OnItemViewClickListener;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemListAdapter<T> {

    private final ArrayList<T> dataList = new ArrayList<>();
    private ItemClickHelper clickHelper = new ItemClickHelper();
    private boolean holderRecyclable = true;
    private ItemViewCreator itemViewCreator;
    private ItemTypeManager itemTypeManager;
    public LoadMoreHelper loadMoreHelper;

    public RecyclerViewAdapter() {
        setHasStableIds(true);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if (loadMoreHelper != null) {
            loadMoreHelper.attachedToRecyclerView(recyclerView);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (loadMoreHelper != null) {
            loadMoreHelper.detachedFromRecyclerView(recyclerView);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        if (itemViewCreator != null) {
            View item = itemViewCreator.onCreateView(parent.getContext(), viewType);
            holder = new RecyclerView.ViewHolder(item) {
            };
        }
        if (holder == null) {
            holder = new EmptyHolder(parent.getContext());
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (itemViewCreator != null) {
            itemViewCreator.onBindView(holder.itemView, position);
        }
        holder.setIsRecyclable(holderRecyclable);
        clickHelper.bindItemClick(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        int size = dataList.size();
        if (size > 0 && loadMoreHelper != null) {
            return size + 1;
        } else {
            return size;
        }
    }

    public RecyclerViewAdapter<T> setItemClickListener(@Nullable OnItemClickListener itemClickListener) {
        clickHelper.itemClickListener = itemClickListener;
        return this;
    }

    public RecyclerViewAdapter<T> setItemViewClickListener(@Nullable OnItemViewClickListener itemViewClickListener) {
        clickHelper.itemViewClickListener = itemViewClickListener;
        return this;
    }

    public RecyclerViewAdapter<T> addData(@Nullable T data) {
        if (data != null) {
            dataList.add(data);
        }
        return this;
    }

    public RecyclerViewAdapter<T> addDataList(@Nullable List<T> list) {
        if (list != null) {
            dataList.addAll(list);
        }
        return this;
    }

    public RecyclerViewAdapter<T> setDataList(@Nullable List<T> list) {
        dataList.clear();
        if (list != null) {
            dataList.addAll(list);
        }
        return this;
    }

    public RecyclerViewAdapter<T> setItemViewCreator(@Nullable ItemViewCreator creator) {
        itemViewCreator = creator;
        return this;
    }

    @Nullable
    public T getPositionData(int position) {
        if (position < 0 || position > dataList.size() - 1) {
            return null;
        }
        return dataList.get(position);
    }

    @NonNull
    public List<T> getDataList() {
        return dataList;
    }

    @Override
    public RecyclerViewAdapter<T> setItemTypeManager(ItemTypeManager manager) {
        itemTypeManager = manager;
        return this;
    }

    @Override
    public int getItemViewType(int position) {
        if (itemTypeManager != null) {
            return itemTypeManager.getItemViewType(position);
        }
        return super.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        if (itemTypeManager != null) {
            return itemTypeManager.getItemViewId(position);
        }
        return super.getItemId(position);
    }

    @Override
    public int getSize() {
        return getItemCount();
    }

    public int getDataIndex(int position) {
        return position;
    }

    public RecyclerViewAdapter<T> setHolderRecyclable(boolean recyclable) {
        holderRecyclable = recyclable;
        return this;
    }

    protected boolean isHolderRecyclable() {
        return holderRecyclable;
    }

}