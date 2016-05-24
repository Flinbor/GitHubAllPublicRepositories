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

package in.flinbor.github.publicRepositories.view.adapter;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import in.flinbor.github.publicRepositories.R;
import in.flinbor.github.publicRepositories.presenter.RepoListPresenter;
import in.flinbor.github.publicRepositories.presenter.vo.Repository;

/**
 * RecyclerView adapter for Repositories
 */
public class RepoListAdapter extends BaseAdapter<Repository>{

    private final RepoListPresenter presenter;

    public RepoListAdapter(List<Repository> list, RepoListPresenter presenter) {
        super(list);
        this.presenter = presenter;
    }

    @Override
    public void onBindViewHolder(BaseAdapter.ViewHolder holder, int position) {
        Repository repo = list.get(position);

        setTextOrClearView(holder.textRepoName, repo.getRepoName());
        setTextOrClearView(holder.textDescription, repo.getDescription());
        setTextOrClearView(holder.textOwnerLogin, repo.getLoginOfTheOwner());
        setBackgroundColor(holder.cardView, repo.isFork());

        /* set on long click listener, dispatch resutl to presenter */
        holder.cardView.setTag(position);
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int position = (int) view.getTag();
                presenter.longClickRepo(list.get(position));
                return true;
            }
        });
    }

    /**
     * set new data to recyclingView
     * @param list to insert into recyclingView
     */
    public void setRepoList(List<Repository> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    private void setTextOrClearView(TextView textView, String text) {
        if (text != null) {
            textView.setText(text);
        } else {
            textView.setText("");
        }
    }

    /**
     * Show a light green background if the fork flag is false or missing, a white one otherwise.
     * @param cardView background of this view will be changed
     * @param isFork   is repo fork
     */
    @SuppressWarnings("deprecation")
    public void setBackgroundColor(CardView cardView, boolean isFork) {
        if (isFork) {
            cardView.setCardBackgroundColor(getColor(cardView.getContext(), R.color.green_light));
        } else {
            cardView.setCardBackgroundColor(getColor(cardView.getContext(), R.color.white));
        }
    }

    @SuppressWarnings("deprecation")
    private int getColor(Context context, int resourceColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getResources().getColor(resourceColor, null);
        } else {
            return context.getResources().getColor(resourceColor);
        }
    }
}
