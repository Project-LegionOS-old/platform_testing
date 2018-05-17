/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.server.wm.flicker;

import static android.view.Surface.rotationToString;

import static com.android.server.wm.flicker.CommonTransitions.changeAppRotation;
import static com.android.server.wm.flicker.WindowUtils.getAppPosition;
import static com.android.server.wm.flicker.WindowUtils.getNavigationBarPosition;
import static com.android.server.wm.flicker.WindowUtils.getStatusBarPosition;
import static com.android.server.wm.flicker.WmTraceSubject.assertThat;

import android.graphics.Rect;
import android.support.test.InstrumentationRegistry;
import android.util.Log;
import android.view.Surface;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Cycle through supported app rotations.
 * To run this test: {@code atest FlickerTest:ChangeAppRotationTest}
 */
@RunWith(Parameterized.class)
public class ChangeAppRotationTest extends FlickerTestBase {
    private static final String NAVIGATION_BAR_WINDOW_TITLE = "NavigationBar";
    private static final String STATUS_BAR_WINDOW_TITLE = "StatusBar";

    private int beginRotation;
    private int endRotation;

    public ChangeAppRotationTest(String beginRotationName, String endRotationName,
            int beginRotation, int endRotation) {
        this.testApp = new StandardAppHelper(InstrumentationRegistry.getInstrumentation(),
                "com.android.server.wm.flicker.testapp", "SimpleApp");
        this.beginRotation = beginRotation;
        this.endRotation = endRotation;
    }

    @Parameters(name = "{0}-{1}")
    public static Collection<Object[]> getParams() {
        int[] supportedRotations =
                {Surface.ROTATION_0, Surface.ROTATION_90, Surface.ROTATION_270};
        Collection<Object[]> params = new ArrayList<>();
        for (int begin : supportedRotations) {
            for (int end : supportedRotations) {
                if (begin != end) {
                    params.add(new Object[]{rotationToString(begin), rotationToString(end), begin,
                            end});
                }
            }
        }
        return params;
    }

    @Before
    public void runTransition() {
        super.runTransition(
                changeAppRotation(testApp, uiDevice, beginRotation, endRotation).build());
    }

    @Test
    public void checkVisibility_navBarIsAlwaysVisible() {
        checkResults(result -> assertThat(result)
                .showsAboveAppWindow(NAVIGATION_BAR_WINDOW_TITLE).forAllEntries());
    }

    @Test
    public void checkVisibility_statusBarIsAlwaysVisible() {
        checkResults(result -> assertThat(result)
                .showsAboveAppWindow(STATUS_BAR_WINDOW_TITLE).forAllEntries());
    }

    @Test
    public void checkPosition_navBarRotatesAndScales() {
        Rect startingPos = getNavigationBarPosition(beginRotation);
        Rect endingPos = getNavigationBarPosition(endRotation);
        checkResults(result -> {
                    LayersTraceSubject.assertThat(result)
                            .hasVisibleRegion(NAVIGATION_BAR_WINDOW_TITLE, startingPos)
                            .inTheBeginning();
                    LayersTraceSubject.assertThat(result)
                            .hasVisibleRegion(NAVIGATION_BAR_WINDOW_TITLE, endingPos).atTheEnd();
                }
        );
    }

    @Test
    public void checkPosition_appRotates() {
        Rect startingPos = getAppPosition(beginRotation);
        Rect endingPos = getAppPosition(endRotation);
        Log.e(TAG, "startingPos=" + startingPos + " endingPos=" + endingPos);
        checkResults(result -> {
                    LayersTraceSubject.assertThat(result)
                            .hasVisibleRegion(testApp.getPackage(), startingPos).inTheBeginning();
                    LayersTraceSubject.assertThat(result)
                            .hasVisibleRegion(testApp.getPackage(), endingPos).atTheEnd();
                }
        );
    }

    @Test
    public void checkPosition_statusBarScales() {
        Rect startingPos = getStatusBarPosition(beginRotation);
        Rect endingPos = getStatusBarPosition(endRotation);
        checkResults(result -> {
                    LayersTraceSubject.assertThat(result)
                            .hasVisibleRegion(STATUS_BAR_WINDOW_TITLE, startingPos)
                            .inTheBeginning();
                    LayersTraceSubject.assertThat(result)
                            .hasVisibleRegion(STATUS_BAR_WINDOW_TITLE, endingPos).atTheEnd();
                }
        );
    }
}
