/*
 * Copyright 2020 wshunli
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
package com.wshunli.map.tianditu.sample;

import android.app.Application;
import android.os.Environment;
import com.wshunli.map.tianditu.TianDiTuInitialer;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化
        String cachePath = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/TianDiTu100Cache";
        TianDiTuInitialer.getInstance().init(this, "471bc0a0e0e2027b388a4d7db8e38cc7", cachePath);
    }
}
