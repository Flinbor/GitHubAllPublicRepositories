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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.List;

import in.flinbor.github.publicRepositories.R;
import in.flinbor.github.publicRepositories.app.App;
import in.flinbor.github.publicRepositories.executorservice.Command;
import in.flinbor.github.publicRepositories.executorservice.GetRepositoriesCommand;
import in.flinbor.github.publicRepositories.executorservice.ServiceHelper;
import in.flinbor.github.publicRepositories.model.Model;
import in.flinbor.github.publicRepositories.presenter.vo.Repository;
import in.flinbor.github.publicRepositories.view.fragment.RepoListView;

/**
 * Concrete implementation of RepoListPresenter
 * Start loading new Repositories and triger Update UI
 */
public class RepoListPresenterImpl implements RepoListPresenter, ServiceHelper.OnServiceResultListener {

    private RepoListView view;
    private Model model;
    private final String REQUEST_ID = "REQUEST_ID";
    private long         requestId = ServiceHelper.DEFAULT_REQUEST_ID;

    @Override
    public void setView(@NonNull RepoListView view) {
        this.view = view;
    }

    @Override
    public void setModel(Model model) {
        this.model = model;
    }

    @Override
    public void onCreateView(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            requestId = savedInstanceState.getLong(REQUEST_ID, ServiceHelper.DEFAULT_REQUEST_ID);
        }
        List<Repository> list = model.getRepositories();
        view.showRepoList(list);
    }

    @Override
    public void longClickRepo(Repository repository) {
        HashMap<String, String> actions = new HashMap<>();
        actions.put(repository.getLoginOfTheOwner(), repository.getLinkToOwner());
        actions.put(repository.getRepoName(), repository.getLinkToRepo());
        String title = App.getApp().getString(R.string.dialog_open_website_title);
        view.showOpenWebsiteDialog(title, actions);
    }

    @Override
    public void loadPointReached(int lastReportId) {
        loadNewRepos(lastReportId);
    }

    @Override
    public void onResume() {
        App.getApp().getServiceHelper().addListener(this);
        List<Repository> list = model.getRepositories();
        view.showRepoList(list);
        if (list.size() == 0) {
            loadNewRepos(0);
        }
    }

    @Override
    public void onPause() {
        App.getApp().getServiceHelper().removeListener(this);
    }

    @Override
    public void storeState(Bundle outState) {
        outState.putLong(REQUEST_ID, requestId);
    }


    @Override
    public void onServiceResult(long requestId, Intent requestIntent, int resultCode, Bundle resultData, long nestedRequestId) {
        Log.d("service", "onResult");
        if (requestId == this.requestId && resultCode == Command.RESULT_SUCCESSFUL) {
            List<Repository> list = model.getRepositories();
            view.showRepoList(list);
        } else if (requestId == this.requestId && resultCode == Command.RESULT_FAILURE) {
            if (resultData != null) {
                String string = resultData.getString(Command.EXTRA_NESTED_COMMAND_EXTRAS);
                if (string != null) {
                    view.showError(string);
                }

            }

        }
    }

    /**
     * Reques loading public reposytories
     *
     * @param lastReportId Id of last visible repo, for pagination
     */
    private void loadNewRepos(int lastReportId) {
        if (!App.getApp().getServiceHelper().isPending(requestId)) {
            Bundle bundle = new Bundle();
            bundle.putInt(GetRepositoriesCommand.SINCE, lastReportId);
            requestId = App.getApp().getServiceHelper().getRequest(GetRepositoriesCommand.class.getName(), bundle, true, false);
        }
    }
}
