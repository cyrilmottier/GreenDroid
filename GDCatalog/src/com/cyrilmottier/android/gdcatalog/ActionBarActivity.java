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
package com.cyrilmottier.android.gdcatalog;

import greendroid.app.GDActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class ActionBarActivity extends GDActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setActionBarContentView(R.layout.text);
        ((TextView) findViewById(R.id.text)).setText("Screen 1");

        getActionBar().addItem(R.drawable.ic_title_export);
        getActionBar().addItem(R.drawable.ic_title_search);
    }

    @Override
    public boolean onHandleActionBarItemClick(int position) {

        switch (position) {
            case 0:
                Intent intent = new Intent(this, TabbedActionBarActivity.class);
                startActivity(intent);
                return true;

            case 1:
                Toast.makeText(this, "Fake feature. Click on the other item instead", Toast.LENGTH_SHORT).show();
                return true;

            default:
                return super.onHandleActionBarItemClick(position);
        }
    }
}
