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

package in.flinbor.github.publicRepositories.view.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import in.flinbor.github.publicRepositories.R;
import in.flinbor.github.publicRepositories.app.App;
import in.flinbor.github.publicRepositories.model.Model;
import in.flinbor.github.publicRepositories.presenter.RepoListPresenter;
import in.flinbor.github.publicRepositories.presenter.RepoListPresenterImpl;
import in.flinbor.github.publicRepositories.view.fragment.RepoListFragment;
import in.flinbor.github.publicRepositories.view.fragment.RepoListView;

/**
 * Main activity of application
 * Creats fragment and binds MVP
 */
public class GitHubActivity extends AppCompatActivity {
    private static final String DEFAULT_FRAGMENT = RepoListFragment.class.getSimpleName();
    private FragmentManager     fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_git_gub);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(DEFAULT_FRAGMENT);

        if (fragment == null) {
            fragment = new RepoListFragment();
            replaceFragment(fragment, false);
        }
        if (fragment instanceof RepoListView) {
            bindMVP((RepoListView) fragment, new RepoListPresenterImpl(), App.getApp().getModelInstance());
        }

    }

    /**
     * creating links between model-view-presenter
     */
    private void bindMVP(@NonNull RepoListView view, @NonNull RepoListPresenter presenter, @NonNull Model model) {
        view.setPresenter(presenter);
        presenter.setView(view);
        presenter.setModel(model);
    }


    /**
     * set fragment to default activity container
     *
     * @param fragment fragment to insert
     * @param addBackStack if true -> fragment will be added to backStack
     */
    private void replaceFragment(@NonNull Fragment fragment, boolean addBackStack) {
        Log.d("fragment", "replace");
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, DEFAULT_FRAGMENT);
        if (addBackStack) transaction.addToBackStack(null);
        transaction.commit();
    }

}
