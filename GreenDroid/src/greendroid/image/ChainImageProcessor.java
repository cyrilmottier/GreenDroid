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

/**
 * Allows multiple image processors to be chained
 * 
 * @author Cyril Mottier
 * @author kennydude
 */
public class ChainImageProcessor implements ImageProcessor {

    ImageProcessor[] mProcessors;

    /**
     * Create a new ChainImageProcessor.
     * 
     * @param processors An array of {@link ImageProcessor} that will be
     *            sequentially applied
     */
    public ChainImageProcessor(ImageProcessor... processors) {
        mProcessors = processors;
    }

    public Bitmap processImage(Bitmap bitmap) {
        for (ImageProcessor processor : mProcessors) {
            bitmap = processor.processImage(bitmap);
        }
        return bitmap;
    }

}
