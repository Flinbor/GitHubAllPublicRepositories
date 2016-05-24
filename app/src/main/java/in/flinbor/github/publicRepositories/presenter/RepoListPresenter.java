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

package in.flinbor.github.publicRepositories.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import in.flinbor.github.publicRepositories.model.Model;
import in.flinbor.github.publicRepositories.presenter.vo.Repository;
import in.flinbor.github.publicRepositories.view.fragment.RepoListView;

/**
 * The presenter acts upon the model and the view.
 * Retrieves data from Model, and formats it for display in the View.
 */
public interface RepoListPresenter {

    /**
     * Set View interface to Presenter layer
     *
     * @param view {@link RepoListView}
     */
    void setView(@NonNull RepoListView view);

    /**
     * Set model interface to Presenter layer
     *
     * @param model {@link Model}
     */
    void setModel(Model model);

    /**
     * View created, Restore state
     *
     * @param savedInstanceState The bundle with stored state
     */
    void onCreateView(@Nullable Bundle savedInstanceState);

    /**
     * On a long click on a list item
     * @param repository that was clicked
     */
    void longClickRepo(Repository repository);

    /**
     * List scrolled to optimal position for request new data
     *
     * @param lastReportId The integer ID of the last Repository that you've seen.
     */
    void loadPointReached(int lastReportId);

    /**
     * Lifecycle of view - in fragment onResume called
     */
    void onResume();

    /**
     * Lifecycle of view - in fragment onPause called
     */
    void onPause();

    /**
     * Store data before view destroyed
     *
     * @param outState The bundle to store into
     */
    void storeState(Bundle outState);
}
