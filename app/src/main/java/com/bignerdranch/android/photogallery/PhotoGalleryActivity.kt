package com.bignerdranch.android.photogallery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class PhotoGalleryActivity : AppCompatActivity() {
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