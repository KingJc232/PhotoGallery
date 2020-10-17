package com.bignerdranch.android.photogallery

import android.content.Context
import android.preference.PreferenceManager

//Used as the key for the query preference
//you will use this key any time you read or write the query value
private const val PREF_SEARCH_QUERY = "searchQuery"

/**Will Serve As a convenient interface for reading and writing the query in the shared preferences*/
class QueryPreferences
{
    //Returns the query value stored in shared preferences
    //It does so by first acquiring the default SharedPreferences for the given context
    fun getStoredQuery(context: Context) : String
    {
        //returns an instance with a default name and private permissions
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        //!! is non null assertion operator use when you know wont be null to avoid try / catch block
        return prefs.getString(PREF_SEARCH_QUERY, "")!!
    }

    //Writes the input query to the default shared Preferences for the given context
    fun setStoredQuery(context: Context, query: String)
    {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(PREF_SEARCH_QUERY, query)
                //Apply() function makes change in memory immediately and does the actual file writing on a background thread
            .apply()
    }

}