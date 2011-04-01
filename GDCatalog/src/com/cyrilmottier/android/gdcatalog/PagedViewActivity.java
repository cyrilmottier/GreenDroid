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
package com.cyrilmottier.android.gdcatalog;

import greendroid.app.GDActivity;
import greendroid.widget.PageIndicator;
import greendroid.widget.PagedAdapter;
import greendroid.widget.PagedView;
import greendroid.widget.PagedView.OnPagedViewChangeListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class PagedViewActivity extends GDActivity {

    private static final int PAGE_COUNT = 7;
    private static final int PAGE_MAX_INDEX = PAGE_COUNT - 1;

    private PageIndicator mPageIndicatorNext;
    private PageIndicator mPageIndicatorPrev;
    private PageIndicator mPageIndicatorOther;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActionBarContentView(R.layout.paged_view);

        final PagedView pagedView = (PagedView) findViewById(R.id.paged_view);
        pagedView.setOnPageChangeListener(mOnPagedViewChangedListener);
        pagedView.setAdapter(new PhotoSwipeAdapter());

        mPageIndicatorNext = (PageIndicator) findViewById(R.id.page_indicator_next);
        mPageIndicatorNext.setDotCount(PAGE_MAX_INDEX);
        mPageIndicatorNext.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                pagedView.smoothScrollToNext();
            }
        });

        mPageIndicatorPrev = (PageIndicator) findViewById(R.id.page_indicator_prev);
        mPageIndicatorPrev.setDotCount(PAGE_MAX_INDEX);
        mPageIndicatorPrev.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                pagedView.smoothScrollToPrevious();
            }
        });
        
        mPageIndicatorOther = (PageIndicator) findViewById(R.id.page_indicator_other);
        mPageIndicatorOther.setDotCount(PAGE_COUNT);
        
        setActivePage(pagedView.getCurrentPage());
    }
    
    private void setActivePage(int page) {
        mPageIndicatorOther.setActiveDot(page);
        mPageIndicatorNext.setActiveDot(PAGE_MAX_INDEX - page);
        mPageIndicatorPrev.setActiveDot(page);
    }
    
    private OnPagedViewChangeListener mOnPagedViewChangedListener = new OnPagedViewChangeListener() {

        @Override
        public void onStopTracking(PagedView pagedView) {
        }

        @Override
        public void onStartTracking(PagedView pagedView) {
        }

        @Override
        public void onPageChanged(PagedView pagedView, int previousPage, int newPage) {
            setActivePage(newPage);
        }
    };
    
    private class PhotoSwipeAdapter extends PagedAdapter {
        
        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.paged_view_item, parent, false);
            }

            ((TextView) convertView).setText(Integer.toString(position));

            return convertView;
        }

    }
}
