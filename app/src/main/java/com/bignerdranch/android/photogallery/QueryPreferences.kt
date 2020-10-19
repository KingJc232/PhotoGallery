package com.bignerdranch.android.photogallery

import android.content.Context
import android.preference.PreferenceManager
import android.provider.Settings.Global.putString
import androidx.core.content.edit

//Used as the key for the query preference
//you will use this key any time you read or write the query value
private const val PREF_SEARCH_QUERY = "searchQuery"

private const val PREF_LAST_RESULT_ID = "lastResultId"

//Used to determine if the Worker Is Enabled
private const val PREF_IS_POLLING = "isPolling"

/**Will Serve As a convenient interface for reading and writing the query in the shared preferences
 * Updating the QueryPreferences to Store and Retrieve the Latest PhotoID From shared preferences
 * */
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


    //Function Used to Retrieve the last Result ID from the Default SharedPreferences
    fun getLastResultId(context: Context) : String
    {
        //String Returned Should Never Be Null therefore Asserting this with the Double !!
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(PREF_LAST_RESULT_ID, "")!!
    }

    //Function Used to Set the Last Result ID based on the given parameter (lastResultId)
    fun setLastResultId(context: Context, lastResultId: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {putString(PREF_LAST_RESULT_ID, lastResultId)}

    }

    /**Methods Used to determine if the Worker is Currently Running*/

    fun isPolling(context: Context) : Boolean
    {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(PREF_IS_POLLING, false)
    }

    fun setPolling(context: Context, isOn : Boolean)
    {
        PreferenceManager.getDefaultSharedPreferences(context).edit { putBoolean(PREF_IS_POLLING, isOn) }
    }
}















