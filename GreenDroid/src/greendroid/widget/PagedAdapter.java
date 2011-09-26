/*
 * Copyright (C) 2011 Cyril Mottier (http://www.cyrilmottier.com)
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
package greendroid.widget;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

/**
 * <p>
 * The base implementation of an Adapter to use with a {@link PagedView}.
 * Clients may create classes that extends from this base implementation. The
 * work consists on overriding the {@link PagedAdapter#getCount()} and
 * {@link PagedAdapter#getView(int, View, ViewGroup)} methods.
 * </p>
 * 
 * @author Cyril Mottier
 */
public abstract class PagedAdapter implements Adapter {

    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    public abstract int getCount();

    public abstract Object getItem(int position);

    public abstract long getItemId(int position);

    public boolean hasStableIds() {
        throw new UnsupportedOperationException("hasStableIds(int) is not supported in the context of a SwipeAdapter");
    }

    public abstract View getView(int position, View convertView, ViewGroup parent);

    public final int getItemViewType(int position) {
        throw new UnsupportedOperationException("getItemViewType(int) is not supported in the context of a SwipeAdapter");
    }

    public final int getViewTypeCount() {
        throw new UnsupportedOperationException("getViewTypeCount() is not supported in the context of a SwipeAdapter");
    }

    public final boolean isEmpty() {
        throw new UnsupportedOperationException("isEmpty() is not supported in the context of a SwipeAdapter");
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }

    public void notifyDataSetChanged() {
        mDataSetObservable.notifyChanged();
    }

}
