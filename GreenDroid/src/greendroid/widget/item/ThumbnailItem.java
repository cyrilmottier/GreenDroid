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
package greendroid.widget.item;

import greendroid.widget.AsyncImageView;
import greendroid.widget.itemview.ItemView;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.cyrilmottier.android.greendroid.R;

/**
 * <p>
 * A ThumbnailItem item is a complex item that wraps a Drawable and two Strings:
 * a title and a subtitle. The representation of that {@link Item} is quite
 * common to Android users: The Drawable is on the left of the item view and on
 * the right the title and the subtitle are displayed like a
 * {@link SubtitleItem}.
 * </p>
 * <p>
 * ThumbnailItem also support the {@link AsyncImageView} widget which enables
 * asynchronous loading of images. When using ThumbnailItem in an asynchronous
 * manner, use {@link ThumbnailItem#drawableId} as the default image (the one
 * displayed while loading) and {@link ThumbnailItem#drawableURL} as the URL of
 * the to-be-loaded image.
 * </p>
 * 
 * @author Cyril Mottier
 */
public class ThumbnailItem extends SubtitleItem {

    /**
     * The resource ID for the Drawable.
     */
    public int drawableId;

    /**
     * An optional URL that may be used to retrieve an image
     */
    public String drawableURL;

    /**
     * @hide
     */
    public ThumbnailItem() {
    }

    /**
     * Create a new ThumbnailItem.
     * 
     * @param text The text to draw
     * @param subtitle The subtitle to use
     * @param drawableId The resource identifier to the Drawable
     */
    public ThumbnailItem(String text, String subtitle, int drawableId) {
        this(text, subtitle, drawableId, null);
    }

    /**
     * Create a new ThumbnailItem which will asynchronously load the image at
     * the given URL.
     * 
     * @param text The text to draw
     * @param subtitle The subtitle to use
     * @param drawableId The default image used when loading the image at the
     *            given <em>drawableURL</em>
     * @param drawableURL The URL pointing to the image to load.
     */
    public ThumbnailItem(String text, String subtitle, int drawableId, String drawableURL) {
        super(text, subtitle);
        this.drawableId = drawableId;
        this.drawableURL = drawableURL;
    }

    @Override
    public ItemView newView(Context context, ViewGroup parent) {
        return createCellFromXml(context, R.layout.gd_thumbnail_item_view, parent);
    }

    @Override
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs);

        TypedArray a = r.obtainAttributes(attrs, R.styleable.ThumbnailItem);
        drawableId = a.getResourceId(R.styleable.ThumbnailItem_thumbnail, drawableId);
        drawableURL = a.getString(R.styleable.ThumbnailItem_thumbnailURL);
        a.recycle();
    }

}
