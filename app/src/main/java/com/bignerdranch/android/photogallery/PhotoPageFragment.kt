package com.bignerdranch.android.photogallery

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

private const val ARG_URI = "photo_page_url"


/**This will be the fragment that will Display the Web page (Picture) In our App When a User Selects and image */
class PhotoPageFragment : VisibleFragment()
{
    private lateinit var uri: Uri
    private lateinit var webView: WebView

    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uri = arguments?.getParcelable(ARG_URI) ?: Uri.EMPTY
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        //Getting the View for this fragment
        val view = inflater.inflate(R.layout.fragment_photo_page, container, false  )

        progressBar = view.findViewById(R.id.progress_bar)
        progressBar.max = 100

        //Finding the WebView From the View
        webView = view.findViewById(R.id.web_view)

        //Enabling JavaScript
        webView.settings.javaScriptEnabled = true

        /**Creating a webChromeClient Using Aynonyoumous Classes
         * To Control the Visibility of the ProgressBar
         * */
        webView.webChromeClient = object : WebChromeClient()
        {
            /**Progress Updates and title updates each have thier own call back functions
             *
             * The progress you receive from onProgressChanged is an integer from 0 to 100
             * */
            override fun onProgressChanged(view: WebView?, newProgress: Int) {

                if(newProgress == 100)
                {
                    progressBar.visibility = View.GONE
                }
                else
                {
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = newProgress
                }

            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                (activity as AppCompatActivity).supportActionBar?.subtitle = title
            }

        }






        //Creating our WebView Client to respond to rendering events on a WebView
        webView.webViewClient = WebViewClient()

        //Getting our URL
        webView.loadUrl(uri.toString())


        //Returning the View
        return view
    }

    companion object
    {
        fun newInstance(uri: Uri) : PhotoPageFragment
        {
            return PhotoPageFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_URI, uri)
                }
            }
        }

    }


}
