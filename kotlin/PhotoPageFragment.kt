package com.bignerdranch.android.photogallery

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bignerdranch.android.photogallery.databinding.FragmentPhotoPageBinding

//Listing 23.5: Setting up your web browser
//All the work that this fragment does will occur in the onCreateView() function, so you do not need to hold on to a reference to the binding this time.
class PhotoPageFragment : Fragment() {
    //Listing 23.10 Loading the URL into WebView
    private val args: PhotoPageFragmentArgs by navArgs()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPhotoPageBinding.inflate(
            inflater,
            container,
            false
        )

        //23.10
        binding.apply {

            //Listing 23.11 Using WebChromeClient
            //In order to hook up ProgressBar, need to use 2nd callback on WebView, which is WebChromeClient
            //WebViewClient is interface for responding to rendering events, so...
            //WebChromeClient is interface for reacting to events that should change (Chrome or chrome?) Elements around the browser
            //Includes JavaScript alerts, favicons, and updates for loading progress and title of current page

            progressBar.max = 100

            webView.apply {
                settings.javaScriptEnabled = true
                webViewClient = WebViewClient()
                loadUrl(args.photoPageUri.toString())

                //Listing 23.11
                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(
                        webView: WebView,
                        newProgress: Int
                    ) {
                        if (newProgress == 100) {
                            progressBar.visibility = View.GONE
                        } else {
                            progressBar.visibility = View.VISIBLE
                            progressBar.progress = newProgress
                        }
                    }

                    override fun onReceivedTitle(
                        view: WebView?,
                        title: String?
                    ) {
                        val parent = requireActivity() as AppCompatActivity
                        parent.supportActionBar?.subtitle = title
                    }
                }
            }
        }

        return binding.root
    }
}