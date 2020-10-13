package com.bignerdranch.android.photogallery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


private const val TAG = "PhotoGalleryFragment"

class PhotoGalleryFragment : Fragment() {

    //Will be used to reference the recycler view in the layout
    private lateinit var photoRecyclerView: RecyclerView

    //Will be used to reference the ViewModel Object that  Stores the Gallery Item Data
    private lateinit var photoGalleryViewModel: PhotoGalleryViewModel

    //Context is the Activity that is hosting the fragment remember can do Ctrl + Click to see implementation
    //Method Used to initialize Reference To the layout since we will inflate it in this method
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {

        /**Using a ViewModel To request and store a photo from the network */
        this.photoGalleryViewModel = ViewModelProviders.of(this).get(PhotoGalleryViewModel::class.java)


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


    /**
     * Updating PhotoGalleryFragment to observe PhotoGalleryViewModel's Live Data
     * Once the Fragments view is created
     * Eventually We will use these results to update our recycler view contents in Response to Data Changes
     * */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        /**
         * Passing viewLifeCycleOwner as the LifecycleOwner parameter to LiveData.observe(..) Ensures that the LiveData
         * Object will remove your observer when the fragment's view is destroyed
         * */
        this.photoGalleryViewModel.galleryItemLiveData.observe(
            viewLifecycleOwner,
            Observer {galleryItems ->

                /**Adding Code to Attach The RecyclerViews Adapter with updated gallery item data when the live data observer callback fires*/
                this.photoRecyclerView.adapter = PhotoAdapter(galleryItems)
            })

    }


    //Static Method Used to Create a Instance of This class
    companion object
    {
        fun newInstance() = PhotoGalleryFragment()
    }


    /**Every Recycler View Needs a Holder and a Adapter Class*/

    private class PhotoHolder(itemTextView: TextView) : RecyclerView.ViewHolder(itemTextView)
    {
        val bindTitle: (CharSequence) -> Unit = itemTextView::setText
    }

    /**Adapter used to provide PhotoHolders as needed based on a list of GalleryItems*/
    private class PhotoAdapter(private val galleryItems: List<GalleryItem>) : RecyclerView.Adapter<PhotoHolder>()
    {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {

            val textView = TextView(parent.context)
            return PhotoHolder(textView)
        }

        override fun getItemCount(): Int = galleryItems.size

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {

            val galleryItem = galleryItems[position]
            holder.bindTitle(galleryItem.title)
        }
    }

}























