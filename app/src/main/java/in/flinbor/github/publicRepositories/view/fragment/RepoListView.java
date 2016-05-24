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

package in.flinbor.github.publicRepositories.view.fragment;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.List;

import in.flinbor.github.publicRepositories.presenter.RepoListPresenter;
import in.flinbor.github.publicRepositories.presenter.vo.Repository;

/**
 * Passive interface that displays data and routes user commands to the presenter to act upon that data
 */
public interface RepoListView {

    /**
     * set presenter interface to View layer
     *
     * @param presenter {@link RepoListPresenter}
     */
    void setPresenter(@NonNull RepoListPresenter presenter);

    /**
     * show repositories on UI
     *
     * @param list with repositories to show
     */
    void showRepoList(@NonNull List<Repository> list);

    /**
     * show error on UI
     *
     * @param error text of error
     */
    void showError(@NonNull String error);

    /**
     * show singe-choice dialog
     *
     * @param title title of dialog
     * @param actions items of list
     */
    void showOpenWebsiteDialog(String title, HashMap<String, String> actions);
}
