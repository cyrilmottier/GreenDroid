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
package greendroid.graphics.drawable;

/**
 * <p>
 * A class defining some constants that may be used when working with stateful
 * Drawable.
 * </p>
 * <p>
 * <em><strong>Note</strong>: Those constants are already defined in the View 
 * class. Unfortunately, they are not easily accessible as their scope is 
 * <code>protected</code>.</em>
 * </p>
 * 
 * @author Cyril Mottier
 */
public class DrawableStateSet {

    /**
     * The empty state
     */
    public static final int[] EMPTY_STATE_SET = {};

    /**
     * The state representing a pressed and enabled entity
     */
    public static final int[] ENABLED_PRESSED_STATE_SET = {
            android.R.attr.state_enabled, android.R.attr.state_pressed
    };

    /**
     * The state representing a focused and enabled entity
     */
    public static final int[] ENABLED_FOCUSED_STATE_SET = {
            android.R.attr.state_enabled, android.R.attr.state_focused
    };

    private DrawableStateSet() {
    }

}
