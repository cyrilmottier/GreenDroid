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

import greendroid.app.GDMapActivity;
import greendroid.graphics.drawable.DrawableStateSet;
import greendroid.graphics.drawable.MapPinDrawable;

import java.util.ArrayList;
import java.util.Random;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MapPinMapActivity extends GDMapActivity {

    private static Random sRandom = new Random();

    private static final int[] PRESSED_STATE = {
        android.R.attr.state_pressed
    };

    //@formatter:off
    private static final OverlayItem[] sFrance = {
        new OverlayItem(new GeoPoint(48635600, -1510600), "Mont Saint Michel", null),
        new OverlayItem(new GeoPoint(48856700, 2351000), "Paris", null),
        new OverlayItem(new GeoPoint(44837400, -576100), "Bordeaux", null),
        new OverlayItem(new GeoPoint(48593100, -647500), "Domfront", null)
    };
    
    private static final OverlayItem[] sEurope = {
        new OverlayItem(new GeoPoint(55755800, 37617600), "Moscow", null),
        new OverlayItem(new GeoPoint(59332800, 18064500), "Stockholm", null),
        new OverlayItem(new GeoPoint(59939000, 30315800), "Saint Petersburg", null),
        new OverlayItem(new GeoPoint(60169800, 24938200), "Helsinki", null),
        new OverlayItem(new GeoPoint(60451400, 22268700), "Turku", null),
        new OverlayItem(new GeoPoint(65584200, 22154700), "Luleå", null),
        new OverlayItem(new GeoPoint(59438900, 24754500), "Talinn", null),
        new OverlayItem(new GeoPoint(66498700, 25721100), "Rovaniemi", null)
    };
    
    private static final OverlayItem[] sUSAEastCoast = {
        new OverlayItem(new GeoPoint(40714400, -74006000), "New York City", null),
        new OverlayItem(new GeoPoint(39952300, -75163800), "Philadelphia", null),
        new OverlayItem(new GeoPoint(38895100, -77036400), "Washington", null),
        new OverlayItem(new GeoPoint(41374800, -83651300), "Bowling Green", null),
        new OverlayItem(new GeoPoint(42331400, -83045800), "Detroit", null)
    };
    
    private static final OverlayItem[] sUSAWestCoast = {
        new OverlayItem(new GeoPoint(37774900, -122419400), "San Francisco", null),
        new OverlayItem(new GeoPoint(37770600, -119510800), "Yosemite National Park", null),
        new OverlayItem(new GeoPoint(36878200, -121947300), "Monteray Bay", null),
        new OverlayItem(new GeoPoint(35365800, -120849900), "Morro Bay", null),
        new OverlayItem(new GeoPoint(34420800, -119698200), "Santa Barbara", null),
        new OverlayItem(new GeoPoint(34052200, -118243700), "Los Angeles", null),
        new OverlayItem(new GeoPoint(32715300, -117157300), "San Diego", null),
        new OverlayItem(new GeoPoint(36114600, -115172800), "Las Vegas", null),
        new OverlayItem(new GeoPoint(36220100, -116881700), "Death Valley", null),
        new OverlayItem(new GeoPoint(36355200, -112661200), "Grand Canyon", null),
        new OverlayItem(new GeoPoint(37289900, -113048900), "Zion National Park", null),
        new OverlayItem(new GeoPoint(37628300, -112167700), "Bryce Canyon", null),
        new OverlayItem(new GeoPoint(36936900, -111483800), "Lake Powell", null),
    };
    
    private static final OverlayItem[][] sAreas = {
        sFrance, sEurope, sUSAEastCoast, sUSAWestCoast
    };
    //@formatter:on

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setActionBarContentView(R.layout.map_pin);

        final MapView mapView = (MapView) findViewById(R.id.map_view);
        mapView.setBuiltInZoomControls(true);

        final Resources r = getResources();

        for (int i = 0; i < sAreas.length; i++) {
            final OverlayItem[] items = sAreas[i];

            ColorStateList pinCsl = createRandomColorStateList();
            ColorStateList dotCsl = createRandomColorStateList();
            BasicItemizedOverlay itemizedOverlay = new BasicItemizedOverlay(new MapPinDrawable(r, pinCsl, dotCsl));

            for (int j = 0; j < items.length; j++) {
                itemizedOverlay.addOverlay(items[j]);
            }

            mapView.getOverlays().add(itemizedOverlay);
        }
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    private class BasicItemizedOverlay extends ItemizedOverlay<OverlayItem> {

        private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

        public BasicItemizedOverlay(Drawable defaultMarker) {
            super(boundCenterBottom(defaultMarker));
        }

        public void addOverlay(OverlayItem overlay) {
            mOverlays.add(overlay);
            populate();
        }

        @Override
        protected OverlayItem createItem(int i) {
            return mOverlays.get(i);
        }

        @Override
        public int size() {
            return mOverlays.size();
        }

        @Override
        protected boolean onTap(int index) {
            return true;
        }

    }

    private ColorStateList createRandomColorStateList() {

        int[][] states = new int[2][];
        int[] colors = new int[2];

        final int randomColor = randomColor();

        states[0] = PRESSED_STATE;
        colors[0] = addRGB(randomColor, -50);

        states[1] = DrawableStateSet.EMPTY_STATE_SET;
        colors[1] = randomColor;

        return new ColorStateList(states, colors);
    }

    private static final int randomColor() {
        int r = sRandom.nextInt(256);
        int g = sRandom.nextInt(256);
        int b = sRandom.nextInt(256);
        return Color.rgb(r, g, b);
    }

    private static int addRGB(int color, int amount) {
        int r = constrain(Color.red(color) + amount, 0, 255);
        int g = constrain(Color.green(color) + amount, 0, 255);
        int b = constrain(Color.blue(color) + amount, 0, 255);
        return Color.rgb(r, g, b);
    }

    public static int constrain(int amount, int low, int high) {
        return amount < low ? low : (amount > high ? high : amount);
    }

}
