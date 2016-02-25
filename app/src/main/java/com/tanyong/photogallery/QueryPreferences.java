package com.tanyong.photogallery;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by baidu on 16/2/25.
 * 存储用户的搜索状态
 */
public class QueryPreferences {
    private static final String PREF_SEARCH_QUERY = "searchQuery";

    public static String getStoredQuery(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_SEARCH_QUERY, null);
    }

    public static void setStoredQuery(Context context, String query) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putString(PREF_SEARCH_QUERY, query)
                .apply();
    }
}
