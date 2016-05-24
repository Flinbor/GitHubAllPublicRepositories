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

package in.flinbor.github.publicRepositories.model;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import in.flinbor.github.publicRepositories.presenter.vo.Repository;
import in.flinbor.github.publicRepositories.app.App;

/**
 * implementation of Model layer for work with database
 */
public class ModelImpl implements Model {


    public ModelImpl() {
    }


    @Override
    public List<Repository> getRepositories() {
        List<Repository> list = new ArrayList<>();
        Cursor cursor = App.getApp().getContentResolver().query(RepoProvider.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.moveToFirst()) {
                do {
                    Repository repository = new Repository();
                    repository.setRepoName(cursor.getString(cursor.getColumnIndex(RepoProvider.repoName)));
                    repository.setDescription(cursor.getString(cursor.getColumnIndex(RepoProvider.description)));
                    repository.setLoginOfTheOwner(cursor.getString(cursor.getColumnIndex(RepoProvider.loginOfTheOwner)));
                    repository.setLinkToOwner(cursor.getString(cursor.getColumnIndex(RepoProvider.linkToOwner)));
                    repository.setLinkToRepo(cursor.getString(cursor.getColumnIndex(RepoProvider.linkToRepo)));
                    repository.setServerRepoId(cursor.getInt(cursor.getColumnIndex(RepoProvider.serverRepoId)));
                    int isFork = cursor.getInt(cursor.getColumnIndex(RepoProvider.isFork));
                    if (isFork == 1) {
                        repository.setFork(true);
                    } else {
                        repository.setFork(false);
                    }
                    list.add(repository);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return list;
    }
}
