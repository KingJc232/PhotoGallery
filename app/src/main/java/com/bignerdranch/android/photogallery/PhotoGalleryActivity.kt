package com.bignerdranch.android.photogallery

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class PhotoGalleryActivity : AppCompatActivity() {

    companion object
    {
        /**Static Method Used to Return an Intent Instance that can be used to start PhotoGalleryActivity*/
        fun newIntent(context: Context) : Intent{
            return Intent(context, PhotoGalleryActivity::class.java)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Inflating the Activity Layout Setting it
        setContentView(R.layout.activity_photo_gallery)

        /**Checking if this class is hosting a fragment already if not it will create one */

        val isFragmentContainerEmpty = savedInstanceState == null

        if (isFragmentContainerEmpty)
        {
            //Creating a Fragment if its Empty
            supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, PhotoGalleryFragment.newInstance()).commit()
        }

    }
}