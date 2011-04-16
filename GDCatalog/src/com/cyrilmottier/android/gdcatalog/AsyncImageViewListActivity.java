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

import greendroid.app.GDListActivity;
import greendroid.image.ChainImageProcessor;
import greendroid.image.ImageProcessor;
import greendroid.image.MaskImageProcessor;
import greendroid.image.ScaleImageProcessor;
import greendroid.widget.AsyncImageView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

public class AsyncImageViewListActivity extends GDListActivity implements OnScrollListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListAdapter(new MyAdapter(this));
        getListView().setOnScrollListener(this);
    }

    private static class MyAdapter extends BaseAdapter {

        private static final String BASE_URL_PREFIX = "http://www.cyrilmottier.com/files/greendroid/images/image";
        private static final String BASE_URL_SUFFIX = ".png";
        private static final StringBuilder BUILDER = new StringBuilder();

        private final String mImageForPosition;

        static class ViewHolder {
            public AsyncImageView imageView;
            public TextView textView;
            public StringBuilder textBuilder = new StringBuilder();
        }

        private LayoutInflater mInflater;
        private ImageProcessor mImageProcessor;

        public MyAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            mImageForPosition = context.getString(R.string.image_for_position);

            prepareImageProcessor(context);
        }

        private void prepareImageProcessor(Context context) {
            
            final int thumbnailSize = context.getResources().getDimensionPixelSize(R.dimen.thumbnail_size);
            final int thumbnailRadius = context.getResources().getDimensionPixelSize(R.dimen.thumbnail_radius);

            if (Math.random() >= 0.5f) {
                //@formatter:off
                mImageProcessor = new ChainImageProcessor(
                        new ScaleImageProcessor(thumbnailSize, thumbnailSize, ScaleType.FIT_XY),
                        new MaskImageProcessor(thumbnailRadius));
                //@formatter:on
            } else {
                
                Path path = new Path();
                path.moveTo(thumbnailRadius, 0);
                
                path.lineTo(thumbnailSize - thumbnailRadius, 0);
                path.lineTo(thumbnailSize, thumbnailRadius);
                path.lineTo(thumbnailSize, thumbnailSize - thumbnailRadius);
                path.lineTo(thumbnailSize - thumbnailRadius, thumbnailSize);
                path.lineTo(thumbnailRadius, thumbnailSize);
                path.lineTo(0, thumbnailSize - thumbnailRadius);
                path.lineTo(0, thumbnailRadius);
                
                path.close();
                
                Bitmap mask = Bitmap.createBitmap(thumbnailSize, thumbnailSize, Config.ARGB_8888);
                Canvas canvas = new Canvas(mask);
                
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setStyle(Style.FILL_AND_STROKE);
                paint.setColor(Color.RED);
                
                canvas.drawPath(path, paint);
                
                //@formatter:off
                mImageProcessor = new ChainImageProcessor(
                        new ScaleImageProcessor(thumbnailSize, thumbnailSize, ScaleType.FIT_XY),
                        new MaskImageProcessor(mask));
                //@formatter:on
            }
        }

        public int getCount() {
            return 100;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.image_item_view, parent, false);
                holder = new ViewHolder();
                holder.imageView = (AsyncImageView) convertView.findViewById(R.id.async_image);
                holder.imageView.setImageProcessor(mImageProcessor);
                holder.textView = (TextView) convertView.findViewById(R.id.text);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            BUILDER.setLength(0);
            BUILDER.append(BASE_URL_PREFIX);
            BUILDER.append(position);
            BUILDER.append(BASE_URL_SUFFIX);
            holder.imageView.setUrl(BUILDER.toString());

            final StringBuilder textBuilder = holder.textBuilder;
            textBuilder.setLength(0);
            textBuilder.append(mImageForPosition);
            textBuilder.append(position);
            holder.textView.setText(textBuilder);

            return convertView;
        }
    }

    public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
    }

    public void onScrollStateChanged(AbsListView listView, int scrollState) {
        if (getListView() == listView) {
            searchAsyncImageViews(listView, scrollState == OnScrollListener.SCROLL_STATE_FLING);
        }
    }

    private void searchAsyncImageViews(ViewGroup viewGroup, boolean pause) {
        final int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AsyncImageView image = (AsyncImageView) viewGroup.getChildAt(i).findViewById(R.id.async_image);
            if (image != null) {
                image.setPaused(pause);
            }
        }
    }

}
