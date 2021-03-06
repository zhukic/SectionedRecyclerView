package com.zhukic.sectionedrecyclerview;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class SectionedRecyclerViewAdapter<SH extends RecyclerView.ViewHolder, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements SectionProvider {

    protected static final int DEFAULT_TYPE_HEADER = -1;

    private SectionManager sectionManager;

    public SectionedRecyclerViewAdapter() {
        this.sectionManager = new SectionManager(this);
    }

    public abstract VH onCreateItemViewHolder(ViewGroup parent, int viewType);

    public abstract SH onCreateSubheaderViewHolder(ViewGroup parent, int viewType);

    public abstract void onBindItemViewHolder(VH holder, int itemPosition);

    /**
     * Called to display the data for the subheader at the specified position.
     *
     * @param nextItemPosition position of the first item in the section to which
     *                         {@code subheaderHolder} belongs.
     */
    public abstract void onBindSubheaderViewHolder(SH subheaderHolder, int nextItemPosition);

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        sectionManager.init();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        sectionManager.clear();
    }

    /**
     * Don't return {@link SectionedRecyclerViewAdapter#DEFAULT_TYPE_HEADER}.
     * It's reserved for subheader view type.
     */
    public int getViewType(int position) {
        return 0;
    }

    public int getSubheaderViewType(int position) {
        return DEFAULT_TYPE_HEADER;
    }

    @Override
    public final int getItemViewType(int position) {
        if (sectionManager.isSectionSubheaderAtPosition(position)) {
            return getSubheaderViewType(position);
        } else {
            final int viewType = getViewType(position);
            if (isSubheaderViewType(viewType)) {
                throw new IllegalStateException("wrong view type = " + viewType + " at position = " +
                        position + " . It's reserved for subheader view type");
            }
            return viewType;
        }
    }

    public boolean isSubheaderViewType(int viewType) {
        return viewType == DEFAULT_TYPE_HEADER;
    }

    @NonNull
    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (isSubheaderViewType(viewType)) {
            return onCreateSubheaderViewHolder(parent, viewType);
        } else {
            return onCreateItemViewHolder(parent, viewType);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (sectionManager.isSectionSubheaderAtPosition(position)) {
            onBindSubheaderViewHolder((SH)holder, sectionManager.getItemPositionForSubheaderViewHolder(position));
        } else {
            onBindItemViewHolder((VH)holder, sectionManager.getItemPositionForItemViewHolder(position));
        }
    }

    @Override
    public final int getItemCount() {
        return sectionManager.getItemCount();
    }

    public final void notifyDataChanged() {
        final NotifyResult result = sectionManager.onDataChanged();
        applyResult(result);
    }

    /**
     * Notify that item at {@code itemPosition} has been changed.
     *
     * @param itemPosition position of the item that has changed in your data set.
     */
    public final void notifyItemChangedAtPosition(int itemPosition) {
        final NotifyResult result = sectionManager.onItemChanged(itemPosition);
        applyResult(result);
    }

    /**
     * Notify that item at {@code itemPosition} has been inserted.
     *
     * @param itemPosition position of the new item in your data set.
     */
    public final void notifyItemInsertedAtPosition(int itemPosition) {
        final NotifyResult result = sectionManager.onItemInserted(itemPosition);
        applyResult(result);
    }

    /**
     * Notify that item at {@code itemPosition} has been removed.
     *
     * @param itemPosition position of the item that has removed in your data set.
     */
    public final void notifyItemRemovedAtPosition(int itemPosition) {
        final NotifyResult result = sectionManager.onItemRemoved(itemPosition);
        applyResult(result);
    }

    public final void setGridLayoutManager(final GridLayoutManager gridLayoutManager) {
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if(sectionManager.isSectionSubheaderAtPosition(position)) {
                    return gridLayoutManager.getSpanCount();
                } else {
                    return 1;
                }
            }
        });
    }

    /**
     * Returns true if section subheader is placed at the specified adapter position.
     *
     * @param adapterPosition adapter position of the item in adapter's data set.
     * @return true if section subheader is placed at the specified adapter position.
     * @throws IndexOutOfBoundsException if the adapter position is out of range.
     */
    public final boolean isSubheaderAtPosition(int adapterPosition) {
        if (adapterPosition < 0 || adapterPosition >= getItemCount()) {
            throw new IndexOutOfBoundsException("adapterPosition: " + adapterPosition + ", itemCount: " + getItemCount());
        }
        return sectionManager.isSectionSubheaderAtPosition(adapterPosition);
    }

    /**
     * Expand the section at the specified index.
     *
     * @param sectionIndex index of the section to be expanded.
     */
    public final void expandSection(int sectionIndex) {
        if (sectionIndex < 0 || sectionIndex >= getSectionsCount()) {
            throw new IndexOutOfBoundsException("sectionIndex: " + sectionIndex + ", sectionCount: " + getSectionsCount());
        }

        final NotifyResult result = sectionManager.expandSection(sectionIndex);
        applyResult(result);
    }

    /**
     * Expand all sections.
     */
    public final void expandAllSections() {
        final NotifyResult result = sectionManager.expandAllSections();
        applyResult(result);
    }

    /**
     * Collapse the section at the specified index.
     *
     * @param sectionIndex index of the section to be collapsed.
     */
    public final void collapseSection(int sectionIndex) {
        if (sectionIndex < 0 || sectionIndex >= getSectionsCount()) {
            throw new IndexOutOfBoundsException("sectionIndex: " + sectionIndex + ", sectionCount: " + getSectionsCount());
        }

        final NotifyResult result = sectionManager.collapseSection(sectionIndex);
        applyResult(result);
    }

    /**
     * Collapse all sections.
     */
    public final void collapseAllSections() {
        final NotifyResult result = sectionManager.collapseAllSections();
        applyResult(result);
    }

    /**
     * Returns true if section at the specified position is expanded.
     *
     * @param sectionIndex index of section whose expansion to be tested.
     * @return true if section at the specified position is expanded.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    public final boolean isSectionExpanded(int sectionIndex) {
        if (sectionIndex < 0 || sectionIndex >= getSectionsCount()) {
            throw new IndexOutOfBoundsException("sectionIndex: " + sectionIndex + ", sectionCount: " + getSectionsCount());
        }
        return sectionManager.isSectionExpanded(sectionIndex);
    }

    /**
     * Returns the index of the section which contains the item at the specified adapterPosition.
     *
     * @param adapterPosition adapter position of the item in adapter's data set.
     * @return the index of the section which contains the item at the specified adapterPosition.
     * @throws IndexOutOfBoundsException if the adapter position is out of range.
     */
    public final int getSectionIndex(int adapterPosition) {
        if (adapterPosition < 0 || adapterPosition >= getItemCount()) {
            throw new IndexOutOfBoundsException("adapterPosition: " + adapterPosition + ", itemCount: " + getItemCount());
        }
        return sectionManager.sectionIndex(adapterPosition);
    }

    /**
     * Returns the position of the item in the section this item belongs,
     * or -1 if subheader is placed at the specified adapter position.
     *
     * @param adapterPosition adapter position of the item in adapter's data set.
     * @return the position of the item in the section this item belongs,
     *         or -1 if subheader is placed at the specified adapter position.
     * @throws IndexOutOfBoundsException if the adapter position is out of range.
     */
    public final int getItemPositionInSection(int adapterPosition) {
        if (adapterPosition < 0 || adapterPosition >= getItemCount()) {
            throw new IndexOutOfBoundsException("adapterPosition: " + adapterPosition + ", itemCount: " + getItemCount());
        }
        if (sectionManager.isSectionSubheaderAtPosition(adapterPosition)) {
            return -1;
        }
        return sectionManager.positionInSection(adapterPosition);
    }

    /**
     * Returns true if the item at the specified adapter position
     * is the first in the section this item belongs.
     *
     * @param adapterPosition adapter position of the item in adapter's data set.
     * @return true if the item at the specified adapter position
     *         is the first in the section this item belongs.
     * @throws IndexOutOfBoundsException if the adapter position is out of range.
     */
    public final boolean isFirstItemInSection(int adapterPosition) {
        if (adapterPosition < 0 || adapterPosition >= getItemCount()) {
            throw new IndexOutOfBoundsException("adapterPosition: " + adapterPosition + ", itemCount: " + getItemCount());
        }
        return sectionManager.isFirstItemInSection(adapterPosition);
    }

    /**
     * Returns true if the item at the specified adapter position
     * is the last in the section this item belongs.
     *
     * @param adapterPosition adapter position of the item in adapter's data set.
     * @return true if the item at the specified adapter position
     *         is the last in the section this item belongs.
     * @throws IndexOutOfBoundsException if the adapter position is out of range.
     */
    public final boolean isLastItemInSection(int adapterPosition) {
        if (adapterPosition < 0 || adapterPosition >= getItemCount()) {
            throw new IndexOutOfBoundsException("adapterPosition: " + adapterPosition + ", itemCount: " + getItemCount());
        }
        return sectionManager.isLastItemInSection(adapterPosition);
    }

    /**
     * Returns the number of items(not including subheader) in the section at the specified position.
     *
     * @param sectionIndex index of the section.
     * @return the number of items(not including subheader) in the section at the specified position.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    public final int getSectionSize(int sectionIndex) {
        if (sectionIndex < 0 || sectionIndex >= getSectionsCount()) {
            throw new IndexOutOfBoundsException("sectionIndex: " + sectionIndex + ", sectionCount: " + getSectionsCount());
        }
        return sectionManager.sectionSize(sectionIndex);
    }

    /**
     * Returns the subheader adapter position of the section at the specified index.
     *
     * @param sectionIndex index of the section.
     * @return the subheader adapter position of the section at the specified index.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    public final int getSectionSubheaderPosition(int sectionIndex) {
        if (sectionIndex < 0 || sectionIndex >= getSectionsCount()) {
            throw new IndexOutOfBoundsException("sectionIndex: " + sectionIndex + ", sectionCount: " + getSectionsCount());
        }
        return sectionManager.getSectionSubheaderPosition(sectionIndex);
    }

    /**
     * Returns the number of sections in this adapter.
     *
     * @return the number of sections in this adapter
     */
    public final int getSectionsCount() {
        return sectionManager.getSectionsCount();
    }

    private void applyResult(NotifyResult notifyResult) {
        for (Notifier notifier : notifyResult.getNotifiers()) {
            applyNotifier(notifier);
        }
    }

    private void applyNotifier(Notifier notifier) {
        switch (notifier.getType()) {
            case ALL_DATA_CHANGED:
                notifyDataSetChanged();
                break;
            case CHANGED:
                notifyItemRangeChanged(notifier.getPositionStart(), notifier.getItemCount());
                break;
            case INSERTED:
                notifyItemRangeInserted(notifier.getPositionStart(), notifier.getItemCount());
                break;
            case REMOVED:
                notifyItemRangeRemoved(notifier.getPositionStart(), notifier.getItemCount());
                break;
        }
    }

    void setSectionManager(SectionManager sectionManager) {
        this.sectionManager = sectionManager;
    }
}
