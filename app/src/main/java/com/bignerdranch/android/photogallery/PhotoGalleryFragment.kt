package com.bignerdranch.android.photogallery

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.Gallery
import android.widget.ImageView

import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import java.util.concurrent.TimeUnit


private const val TAG = "PhotoGalleryFragment"

private const val POLL_WORK = "POLL_WORK"

class PhotoGalleryFragment : VisibleFragment() {

    //Will be used to reference the recycler view in the layout
    private lateinit var photoRecyclerView: RecyclerView

    //Will be used to reference the ViewModel Object that  Stores the Gallery Item Data
    private lateinit var photoGalleryViewModel: PhotoGalleryViewModel

    //Will Be used to reference our Background Thread Which downloads the Pictures From Flicker While Our Main Thread is Handling UI Stuff
    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoHolder>

    private var queryPreferences = QueryPreferences()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**So that the PhotoGalleryFragment Class Matches the Users Perceived life with this fragment
         * We are going to retain the Fragment Instance
         * */
        retainInstance = true

        /**Tells the Fragment that it has an app bar menu */
        setHasOptionsMenu(true)

        /**Using a ViewModel To request and store a photo from the network */
        this.photoGalleryViewModel = ViewModelProviders.of(this).get(PhotoGalleryViewModel::class.java)

        /**Initializing our Background thread and Associating it with this Fragment as its lifeCycle owner*/

        //Creating a Handler and associating it with the main Thread So that  we can pass it to the Background Thread
        //Since by default a Handler will attach itself to the Looper for the current Thread therefore the main threads looper
        val responseHandler =  Handler()
        //Initializing Our Background thread and with the Main Thread Handler
        //And using a Lambda Function does the UI work with the Returning BitMaps
        thumbnailDownloader = ThumbnailDownloader(responseHandler){
            photoHolder, bitmap ->
            val drawable = BitmapDrawable(resources, bitmap)
            photoHolder.bindDrawable(drawable)
        }


        /**
         * Since we added thumbnailDownloader it receives the Fragments lifecycle callbacks SO
         * When onCreate(..) is Called ThumbnailDownloader.setup() is Called
         * When onDestroy(..) is Called ThumbnailDownloader.tearDown() gets called
         * */
        lifecycle.addObserver(thumbnailDownloader.fragmentLifeCycleObserver)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId)
        {
            R.id.menu_item_clear -> {
                photoGalleryViewModel.fetchPhotos("")
                true
            }
            /**Responds to the Poll toggling by the user
             * If the worker is not running , Creates a new PeriodicWorkRequest and Schedule it with the Work Manger
             * If the Worker is Running Stops it
             * */
            R.id.menu_item_toggle_polling -> {
                val isPolling = this.queryPreferences.isPolling(requireContext())
                if(isPolling)
                {
                    WorkManager.getInstance().cancelUniqueWork(POLL_WORK)
                    this.queryPreferences.setPolling(requireContext(), false)
                }
                else
                {
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.UNMETERED)
                        .build()
                    val periodicRequest = PeriodicWorkRequest.Builder(PollWorker::class.java, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build()
                    WorkManager.getInstance().enqueueUniquePeriodicWork(POLL_WORK, ExistingPeriodicWorkPolicy.KEEP, periodicRequest)
                    this.queryPreferences.setPolling(requireContext(), true)

                }
                activity?.invalidateOptionsMenu()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        /**Inflating the Search View Of the Menu In the App Bar */
        inflater.inflate(R.menu.fragment_photo_gallery, menu)

        /**Using SearchView.onQueryTextListener Interface to receive a call back when a  query is submitted*/

        //Pulling the MenuItem representing the Search Box from the menu and storing it in SearchItem
        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        //Pulling the SearchView object from the searchItem using getActionView()
        val searchView = searchItem.actionView as SearchView

        /**
         * Now that we have a reference to the SearchView We are able to Set a SearchView.OnQueryTextListener using setOnQueryTextListener(...)
         * */

        searchView.apply {
            setOnQueryTextListener(object :
                SearchView.OnQueryTextListener {
                //Is Executed when the user submits a query
                //The Query The User submitted is passed as input
                override fun onQueryTextSubmit(queryText: String):
                        Boolean {
                    Log.d(TAG, "QueryTextSubmit: $queryText")

                    //Making a Call to the viewModel Function which will request from Flickr what to Search And Update the Recycler View with the result
                    photoGalleryViewModel.fetchPhotos(queryText)
                    return true
                }
                //This is called anytime text in the SearchView text box changes
                override fun onQueryTextChange(queryText: String):
                        Boolean {
                    Log.d(TAG, "QueryTextChange: $queryText")
                    return false
                }
            })

            setOnSearchClickListener {
                searchView.setQuery(photoGalleryViewModel.searchTerm, false)
            }
        }

        /**Setting the Correct Title For the Item Menu in the App Bar that allows you to disable or enable The Worker Poller */
        val toggleItem = menu.findItem(R.id.menu_item_toggle_polling)
        val isPolling = this.queryPreferences.isPolling(requireContext())
        val toggleItemTitle = if(isPolling)
        {
            R.string.stop_polling
        }
        else
        {
            R.string.start_polling
        }
        toggleItem.setTitle(toggleItemTitle)

    }

    override fun onDestroy() {
        super.onDestroy()

        //Removing This fragment as the lifecycle owner of our background thread
        lifecycle.removeObserver(
            this.thumbnailDownloader.fragmentLifeCycleObserver
        )

    }

    override fun onDestroyView() {
        super.onDestroyView()

        //Removing This Background Thread from observing this Fragments View Life Cycle
        viewLifecycleOwner.lifecycle.removeObserver(thumbnailDownloader.viewLifecycleObserver)
    }


    //Context is the Activity that is hosting the fragment remember can do Ctrl + Click to see implementation
    //Method Used to initialize Reference To the layout since we will inflate it in this method
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {

        /**Adding the Background Thread As an Observer to the Fragments View Since The Fragment Retains Instance the view will destroy on Rotation while the Fragment wont*/
        viewLifecycleOwner.lifecycle.addObserver(thumbnailDownloader.viewLifecycleObserver)

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

    /**Updating PhotoHolder to use a Image View Instead of a Text View So that we can display our pictures downloaded from Flickr*/
    private inner class PhotoHolder(private val itemImageView: ImageView) : RecyclerView.ViewHolder(itemImageView), View.OnClickListener
    {
        private lateinit var galleryItem: GalleryItem

        init {
            itemView.setOnClickListener(this)
        }

        val bindDrawable: (Drawable) -> Unit = itemImageView::setImageDrawable

        fun bindGalleryItem(item: GalleryItem)
        {
            galleryItem = item
        }

        override fun onClick(view: View)
        {
            val intent = PhotoPageActivity
                .newIntent(requireContext(), galleryItem.photoPageUri)
            startActivity(intent)
        }

    }

    /**Adapter used to provide PhotoHolders as needed based on a list of GalleryItems
     *
     * Now Since PhotoHolder uses a ImageView Updating onCreateViewHolder to inflate the list_item_gallery.xml file when we create
     * A PhotoHolder
     *
     * Marking it as an inner class so that we can access the members of the outer class
     * */
    private inner class PhotoAdapter(private val galleryItems: List<GalleryItem>) : RecyclerView.Adapter<PhotoHolder>()
    {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {

            //Inflating the view used for the PhotoHolder Item and passing it to it when we create a ViewHolder object (PhotoHolder)
            val view = layoutInflater.inflate(R.layout.list_item_gallery, parent, false) as ImageView
            return PhotoHolder(view)
        }

        override fun getItemCount(): Int = galleryItems.size

        /**Now we need a placeholder image for each ImageView To display until we can download an image to replace it
         * Using bill_up_close.png as the Place Holder Image as the ImageView's Drawable
         * */
        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {

            val galleryItem = galleryItems[position]

            holder.bindGalleryItem(galleryItem)

            //Using a Temp Picture until we download the pictures
            val placeholder: Drawable = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.bill_up_close
            ) ?: ColorDrawable()
            holder.bindDrawable(placeholder) //Updating the ImageView When We bind it

            //Passing the target (PhotoHolder) Where the image will be placed and
            // The GalleryItem's URL to download from
            thumbnailDownloader.queueThumbnail(holder,galleryItem.url)
        }
    }
}




