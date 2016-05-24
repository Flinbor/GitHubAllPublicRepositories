/*
 * Copyright 2016 Flinbor Bogdanov Oleksandr
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package in.flinbor.github.publicRepositories.app;

import android.os.Handler;

import in.flinbor.github.publicRepositories.model.Model;
import in.flinbor.github.publicRepositories.model.ModelImpl;
import in.flinbor.github.publicRepositories.executorservice.ServiceHelper;

/**
 * Custom Application for global initialization
 */
public class App extends android.app.Application {
    private static App app;
    private Model model;
    private ServiceHelper serviceHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        model = new ModelImpl();
        serviceHelper = new ServiceHelper(this, new Handler());
    }

    /**
     * @return static instance of {@link App}
     */
    public static App getApp() {
        return app;
    }

    /**
     * @return instance of {@link Model}
     */
    public Model getModelInstance() {
        return model;
    }

    /**
     * @return instance of {@link ServiceHelper}
     */
    public ServiceHelper getServiceHelper() {
        return serviceHelper;
    }
}
