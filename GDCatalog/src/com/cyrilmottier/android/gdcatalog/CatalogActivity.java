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

import greendroid.widget.ItemAdapter;
import greendroid.widget.item.TextItem;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class CatalogActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ItemAdapter adapter = new ItemAdapter(this);
        adapter.add(new TextItem("Basic items"));
        adapter.add(new TextItem("XML items"));
        adapter.add(new TextItem("Tweaked item cell"));
        
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        
        Class<?> klass = null;
        
        switch (position) {
            case 0:
                klass = BasicItemActivity.class;
                break;
                
            case 1:
                klass = XmlItemActivity.class;
                break;
                
            case 2:
                klass = TweakedItemViewActivity.class;
                break;

        }
        
        if (klass != null) {
            Intent intent = new Intent(this, klass);
            startActivity(intent);
        }
        
    }
    
}
