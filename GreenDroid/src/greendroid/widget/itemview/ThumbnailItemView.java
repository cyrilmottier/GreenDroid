/*
 * Copyright (C) 2010 Cyril Mottier (http://www.cyrilmottier.com)
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
package greendroid.widget.itemview;

import greendroid.widget.AsyncImageView;
import greendroid.widget.item.Item;
import greendroid.widget.item.TextItem;
import greendroid.widget.item.ThumbnailItem;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;

/**
 * View representation of the {@link ThumbnailItem}.
 * 
 * @author Cyril Mottier
 */
public class ThumbnailItemView extends RelativeLayout implements ItemView {

    private TextView mTextView;
    private TextView mSubtitleView;
    private AsyncImageView mThumbnailView;

    public ThumbnailItemView(Context context) {
        this(context, null);
    }

    public ThumbnailItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThumbnailItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
	public void prepareItemView() {
        mTextView = (TextView) findViewById(R.id.gd_text);
        mSubtitleView = (TextView) findViewById(R.id.gd_subtitle);
        mThumbnailView = (AsyncImageView) findViewById(R.id.gd_thumbnail);
    }

    @Override
	public void setObject(Item object) {
        final ThumbnailItem item = (ThumbnailItem) object;
        mTextView.setText(item.text);
        mSubtitleView.setText(item.subtitle);
        mThumbnailView.setDefaultImageResource(item.drawableId);
        mThumbnailView.setUrl(item.drawableURL);
    }

	@Override
	public Class<? extends Item> getItemClass() {
		return ThumbnailItem.class;
	}
}
