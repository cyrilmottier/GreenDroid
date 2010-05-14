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
package greendroid.util;

public class Config {
    
    /**
     * This flag must be used when debugging only. It enables logs and
     * everything that is CPU consuming (debugging features, etc.) but may help
     * the developer to debug his/her application.
     */
    public static final int DEBUG = 1;

    /**
     * Flag used when releasing the application
     */
    public static final int RELEASE = 2;

    private Config() {
    }

    /**
     * Set this flag to Config.RELEASE when releasing your application.
     */
    public static final int CONFIGURATION = RELEASE;

    /**
     * Enable/disable debugging logs in the application. You may set it to false
     * when releasing this application.
     */
    public static final boolean DEBUG_LOGS_GREENDROID_ENABLED = (CONFIGURATION == DEBUG);

}
