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

package in.flinbor.github.publicRepositories.view.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import java.util.HashMap;
import java.util.Map;

/**
 * in Dialog shown list of items
 * on item click open associated with item url in web browser
 */
public class OpenWebsiteDialog extends DialogFragment {

    private static final String ITEMS = "ITEMS";
    private static final String TITLE = "TITLE";
    private String              title;
    private Map<String, String> items;

    /**
     * create Dialog, add parameters to bundle
     * @param title title of dialog
     * @param items items shown in dialog
     * @return new instance of Dialog
     */
    public static OpenWebsiteDialog getInstance(@NonNull String title, @NonNull HashMap<String, String> items) {
        OpenWebsiteDialog dialog = new OpenWebsiteDialog();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ITEMS, items);
        bundle.putString(TITLE, title);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        items = (Map<String, String>) arguments.getSerializable(ITEMS);
        title = arguments.getString(TITLE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String[] names = getItemsName(this.items);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setItems(names, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String name = names[item];
                String url = items.get(name);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });
        AlertDialog alert = builder.create();
        return alert;
    }

    private String[] getItemsName(Map<String, String> items) {
        String[] names = items.keySet().toArray(new String[items.size()]);
        return names;
    }
}
