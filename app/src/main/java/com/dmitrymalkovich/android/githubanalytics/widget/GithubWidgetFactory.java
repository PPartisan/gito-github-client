/*
 * Copyright 2017.  Dmitry Malkovich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dmitrymalkovich.android.githubanalytics.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.dmitrymalkovich.android.githubanalytics.R;
import com.dmitrymalkovich.android.githubanalytics.data.source.local.contract.RepositoryContract;
import com.dmitrymalkovich.android.githubanalytics.traffic.TrafficActivity;

class GithubWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private Cursor mCursor;
    private Context mContext;

    @SuppressWarnings("unused")
    GithubWidgetFactory(Context context, Intent intent) {
        mContext = context;
    }

    @Override
    public void onCreate() {
        // Nothing to do
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.list_item_github_widget);
        if (mCursor.moveToPosition(position)) {
            String title = mCursor.getString(RepositoryContract.RepositoryEntry.COL_REPOSITORY_NAME);
            String stars = mCursor.getString(RepositoryContract.RepositoryEntry.COL_REPOSITORY_WATCHERS);
            String starsToday = mCursor.getString(RepositoryContract.RepositoryEntry.COL_STARGAZERS_STARS);
            String subtitle = mContext.getResources().getString(R.string.widget_github_subtitle,
                    stars, starsToday != null ? starsToday : "0");
            rv.setTextViewText(R.id.title, title);
            rv.setTextViewText(R.id.subtitle, subtitle);

            Intent intent = new Intent();
            long repositoryId = mCursor.getLong(RepositoryContract.RepositoryEntry.COL_REPOSITORY_ID);
            intent.putExtra(TrafficActivity.EXTRA_REPOSITORY_ID, repositoryId);
            rv.setOnClickFillInIntent(R.id.list_item, intent);

        }
        return rv;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onDataSetChanged() {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = mContext.getContentResolver().query(
                RepositoryContract.RepositoryEntry.CONTENT_URI_REPOSITORY_STARGAZERS,
                RepositoryContract.RepositoryEntry.REPOSITORY_COLUMNS_WITH_ADDITIONAL_INFO,
                RepositoryContract.RepositoryEntry.COLUMN_REPOSITORY_FORK + " = ?",
                new String[]{"0"},
                RepositoryContract.RepositoryEntry.COLUMN_REPOSITORY_WATCHERS + " DESC");
    }

    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }
}
