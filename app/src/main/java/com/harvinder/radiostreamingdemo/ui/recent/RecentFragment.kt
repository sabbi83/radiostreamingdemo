package com.harvinder.radiostreamingdemo.ui.recent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.harvinder.radiostreamingdemo.R
import com.harvinder.radiostreamingdemo.databinding.FragmentDashboardBinding
import com.harvinder.radiostreamingdemo.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
@AndroidEntryPoint
class RecentFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val recentViewModel by viewModels<RecentViewModel>()
    private lateinit var adapter: RecentAdapter
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        adapter = RecentAdapter()
        _binding?.rvRecent?.layoutManager=LinearLayoutManager(requireContext())
        _binding?.rvRecent?.adapter=adapter
        getUserDetails()
        return root
    }
    private fun getUserDetails() {
        this.lifecycleScope.launch {
            recentViewModel.doGetUserDetails()
            recentViewModel.userDetails.collect {
                if(it.isNotEmpty()){
                    adapter.submitList(it)
                }

            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}