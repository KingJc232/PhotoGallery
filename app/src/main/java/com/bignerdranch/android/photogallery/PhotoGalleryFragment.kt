package com.bignerdranch.android.photogallery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


private const val TAG = "PhotoGalleryFragment"

class PhotoGalleryFragment : Fragment() {

    //Will be used to reference the recycler view in the layout
    private lateinit var photoRecyclerView: RecyclerView


    //Context is the Activity that is hosting the fragment remember can do Ctrl + Click to see implementation
    //Method Used to initialize Reference To the layout since we will inflate it in this method
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {

        /**Using a FlcikrFetchr To request a photo from the network */
        val flickrLiveData: LiveData<String> = FlickrFetchr().fetchPhotos()

        //Using a Observer so when the LiveData is Recieved it will
        flickrLiveData.observe(
            this,
            Observer {responseString ->
                Log.d(TAG, "Response Received: $responseString")
            }
        )

        //Inflating the Fragment Layout
        val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)

        //Initializing the Reference Variables to the widgets of the layout since it is now inflated
        this.photoRecyclerView = view.findViewById(R.id.photo_recycler_view)

        //Every Recycler View Needs A Layout Manager Because it is responsible for measuring and positioning all item views within a recycler view
        // as well as determining the policy for when to recycle item views that are no longer visible to the user
        //By Changing The Layout Manager a recycler view can be used to implement a std vertically scrolling list, uniform grid, etc ...

        this.photoRecyclerView.layoutManager = GridLayoutManager(context, 3) //Hard coding it initially to 3 columns as a Grid Layout
        return view
    }

    //Static Method Used to Create a Instance of This class
    companion object
    {
        fun newInstance() = PhotoGalleryFragment()
    }


}























