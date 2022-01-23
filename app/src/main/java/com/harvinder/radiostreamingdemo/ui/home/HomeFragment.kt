package com.harvinder.radiostreamingdemo.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import com.harvinder.radiostreamingdemo.R
import com.harvinder.radiostreamingdemo.databinding.FragmentHomeBinding
import com.harvinder.radiostreamingdemo.models.GetPlayNowDataItem
import com.harvinder.radiostreamingdemo.others.Status
import com.harvinder.radiostreamingdemo.utils.Common
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val homeViewModel by viewModels<HomeViewModel>()
    private var _binding: FragmentHomeBinding? = null
    var getPlayNowDataItem = ArrayList<GetPlayNowDataItem>()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        // call to the view model
        if (Common.isNetworkAvailable(requireContext())) {
            _binding?.progress?.visibility = View.VISIBLE
            callApi()
        } else {
            getUserDetails()
        }
        //addAds()
        return root
    }

    private fun callApi() {

        homeViewModel.res.observe(viewLifecycleOwner, Observer {
            when (it.status) {

                Status.SUCCESS -> {
                    try {
                        _binding?.progress?.visibility = View.GONE
                        if(getPlayNowDataItem.isNotEmpty()){
                            homeViewModel.deleteAll()
                        }
                        it.data.let { res ->
                            getPlayNowDataItem = res as ArrayList<GetPlayNowDataItem>
                            Log.d("array", "" + getPlayNowDataItem.size)
                            for (i in 0 until getPlayNowDataItem.size) {
                                insertPlayDetails(
                                    GetPlayNowDataItem(
                                        getPlayNowDataItem[i].album,
                                        getPlayNowDataItem[i].artist,
                                        getPlayNowDataItem[i].image_url,
                                        getPlayNowDataItem[i].link_url,
                                        getPlayNowDataItem[i].name,
                                        getPlayNowDataItem[i].played_at,
                                        getPlayNowDataItem[i].preview_url,
                                        getPlayNowDataItem[i].sid

                                    )
                                )
                            }
                            if (getPlayNowDataItem.isNotEmpty()) {
                                _binding?.tvHeader?.setText(getPlayNowDataItem[0].name)
                                _binding?.tvTitle?.setText(getPlayNowDataItem[0].artist)
                                Glide.with(requireContext())
                                    .load(getPlayNowDataItem?.get(0)?.image_url)
                                    .placeholder(R.drawable.loader)
                                    .error(R.mipmap.bg_image)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .into(_binding!!.imageView)
                            }


                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _binding?.progress?.visibility = View.GONE
                    }
                }
                Status.LOADING -> {
                    _binding?.progress?.visibility = View.GONE
                }
                Status.ERROR -> {
                    _binding?.progress?.visibility = View.GONE
                    Snackbar.make(_binding!!.root, "Something went wrong", Snackbar.LENGTH_SHORT)
                        .show()
                }
            }


        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun insertPlayDetails(getPlayNowDataItem: GetPlayNowDataItem) {
        GlobalScope.launch {
            homeViewModel.insertUserDetails(getPlayNowDataItem)
        }

    }

   private fun getUserDetails() {
        this.lifecycleScope.launch {
            homeViewModel.doGetUserDetails()
            homeViewModel.userDetails.collect {
                if (it.isNotEmpty()) {
                    Glide.with(requireContext())
                        .load(it?.get(1).image_url)
                        .placeholder(R.drawable.loader)
                        .error(R.drawable.loader)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(_binding!!.imageView)
                }


            }
        }
    }
}