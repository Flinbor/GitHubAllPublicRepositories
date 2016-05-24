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

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import in.flinbor.github.publicRepositories.R;


/**
 * base adapter for repositories, general methods implemented
 */
public abstract class BaseAdapter <T> extends RecyclerView.Adapter<BaseAdapter.ViewHolder> {

    protected List<T> list;

    public BaseAdapter(List<T> list) {
        this.list = list;
    }

    public List<T> getList() {
        return list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_repo_list, viewGroup, false);
        return new ViewHolder(v);
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textDescription;
        final TextView textOwnerLogin;
        final TextView textRepoName;
        final CardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            textRepoName = (TextView) itemView.findViewById(R.id.item_repo_text_view_repo_name);
            textDescription = (TextView) itemView.findViewById(R.id.item_repo_text_view_description);
            textOwnerLogin = (TextView) itemView.findViewById(R.id.item_repo_text_view_owner_login);
            cardView = (CardView) itemView.findViewById(R.id.item_repo_card_view);
        }
    }

}

