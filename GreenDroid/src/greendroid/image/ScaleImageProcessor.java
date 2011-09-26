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
package greendroid.image;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.widget.ImageView.ScaleType;

/**
 * Scales Bitmaps according to a given width and height. The scaling method may
 * be one of the ScaleType values
 * 
 * @author Cyril Mottier
 * @author kennydude
 */
public class ScaleImageProcessor implements ImageProcessor {

    private int mWidth;
    private int mHeight;
    private ScaleType mScaleType;
    private final Matrix mMatrix = new Matrix();

    private final RectF mTempSrc = new RectF();
    private final RectF mTempDst = new RectF();

    /**
     * Create a new ScaleImageProcessor.
     * 
     * @param width The width of the final surrounding box
     * @param height The height of the final surrounding box
     * @param scaleType The ScaleType method used to scale the original Bitmap
     */
    public ScaleImageProcessor(int width, int height, ScaleType scaleType) {
        mWidth = width;
        mHeight = height;
        mScaleType = scaleType;
    }

    public Bitmap processImage(Bitmap bitmap) {

        if (bitmap == null) {
            return null;
        }

        mMatrix.reset();

        final int bWidth = bitmap.getWidth();
        final int bHeight = bitmap.getHeight();

        switch (mScaleType) {

            case CENTER_CROP: {
                // Center and scale the bitmap so that it entirely fills the
                // given space. The bitmap ratio remains unchanged
                float scale;
                float dx = 0, dy = 0;

                if (bWidth * mHeight > mWidth * bHeight) {
                    scale = (float) mHeight / (float) bHeight;
                    dx = (mWidth - bWidth * scale) * 0.5f;
                } else {
                    scale = (float) mWidth / (float) bWidth;
                    dy = (mHeight - bHeight * scale) * 0.5f;
                }

                mMatrix.setScale(scale, scale);
                mMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
                break;
            }

            case CENTER: {
                // Center bitmap without scaling
                final int dx = (int) ((mWidth - bWidth) * 0.5f + 0.5f);
                final int dy = (int) ((mHeight - bHeight) * 0.5f + 0.5f);
                mMatrix.setTranslate(dx, dy);
                break;
            }

            case CENTER_INSIDE: {
                // Center and scale the bitmap so that it entirely fits into the
                // given space.
                float scale;
                float dx;
                float dy;

                if (bWidth <= mWidth && bHeight <= mHeight) {
                    scale = 1.0f;
                } else {
                    scale = Math.min((float) mWidth / (float) bWidth, (float) mHeight / (float) bHeight);
                }

                dx = (int) ((mWidth - bWidth * scale) * 0.5f + 0.5f);
                dy = (int) ((mHeight - bHeight * scale) * 0.5f + 0.5f);

                mMatrix.setScale(scale, scale);
                mMatrix.postTranslate(dx, dy);
                break;
            }

            case FIT_XY:
            default:
                // Entirely fills the space without respecting bitmap's ratio.
                mTempSrc.set(0, 0, bWidth, bHeight);
                mTempDst.set(0, 0, mWidth, mHeight);

                mMatrix.setRectToRect(mTempSrc, mTempDst, Matrix.ScaleToFit.FILL);
                break;
        }

        Bitmap result = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(bitmap, mMatrix, null);

        return result;
    }
}
