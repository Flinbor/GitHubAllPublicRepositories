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

package in.flinbor.github.publicRepositories.executorservice;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import in.flinbor.github.publicRepositories.BuildConfig;
import in.flinbor.github.publicRepositories.R;
import in.flinbor.github.publicRepositories.app.App;
import in.flinbor.github.publicRepositories.dto.RepositoryDTO;
import in.flinbor.github.publicRepositories.model.RepoProvider;

/**
 * command for communication with webservice and store data to database
 */
public class GetRepositoriesCommand extends Command {

    public static final String SINCE                  = "SINCE"; //The integer ID of the last Repository that you've seen.
    public static final int READ_TIMEOUT_MILLIS       = 10000;
    public static final int CONNECTION_TIMEOUT_MILLIS = 15000;
    public static final String TAG                    = GetRepositoriesCommand.class.getSimpleName();
    private final int since;

    public GetRepositoriesCommand(Context context, Intent intent) {
        super(context, intent);
        since = intent.getIntExtra(SINCE, 0);
    }

    @Override
    public void doWork() {
        if (checkConnection(getContext())) {
            loadNewReports(since);
        } else {
            Bundle b = new Bundle();
            b.putString("failure", getContext().getString(R.string.warn_internet_no_connection));
            postResult(RESULT_FAILURE, b);
        }
    }

    private void loadNewReports(int since) {
        Uri.Builder builtUri = new Uri.Builder()
                .scheme("https")
                .authority("api.github.com")
                .path("repositories")
                .appendQueryParameter("since", String.valueOf(since));

        if (BuildConfig.ACCESS_TOKEN != null) {
            builtUri.appendQueryParameter("access_token", BuildConfig.ACCESS_TOKEN);
        }
        String stringUrl = builtUri.build().toString();
        Log.d(TAG, "GET: "+ builtUri);

        URL url;
        HttpURLConnection connection = null;
        try {
            // Create connection
            url = new URL(stringUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setReadTimeout(READ_TIMEOUT_MILLIS);
            connection.setConnectTimeout(CONNECTION_TIMEOUT_MILLIS);
            connection.connect();

            BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream()));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line+"\n");
            }
            br.close();

            String jsonString = sb.toString();
            Log.d(TAG, jsonString);
            Gson gson = new Gson();
            Type token = new TypeToken<List<RepositoryDTO>>() {}.getType();
            List<RepositoryDTO> repoList = gson.fromJson(jsonString, token);

            insertRepoToDB(repoList);
            postResult(RESULT_SUCCESSFUL, null);
        } catch (Exception e) {
            e.printStackTrace();
            Bundle b = new Bundle();
            b.putString(EXTRA_NESTED_COMMAND_EXTRAS, e.toString());
            postResult(RESULT_FAILURE, b);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void insertRepoToDB(List<RepositoryDTO> repoList) {
        for (int i = 0; i < repoList.size(); i++) {
            RepositoryDTO repoDTO = repoList.get(i);
            ContentValues values = new ContentValues();
            values.put(RepoProvider.repoName, repoDTO.getName());
            values.put(RepoProvider.description, repoDTO.getDescription());
            values.put(RepoProvider.loginOfTheOwner, repoDTO.getOwner().getLogin());
            values.put(RepoProvider.linkToOwner, repoDTO.getOwner().getHtmlUrl());
            values.put(RepoProvider.linkToRepo, repoDTO.getHtmlUrl());
            values.put(RepoProvider.serverRepoId, repoDTO.getId());
            values.put(RepoProvider.isFork, repoDTO.isFork()? 1 : 0);
            Uri uri = App.getApp().getContentResolver().insert(RepoProvider.CONTENT_URI, values);
        }
    }

    /**
     *  Check whether internet connection is available or not
     */
    public static boolean checkConnection(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();

        if (activeNetworkInfo != null) { // connected to the internet
            if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                return true;
            } else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                return true;
            }
        }
        return false;
    }

}
