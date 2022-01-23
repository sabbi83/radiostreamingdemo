package com.harvinder.radiostreamingdemo.ui.recent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.harvinder.radiostreamingdemo.R
import com.harvinder.radiostreamingdemo.models.GetPlayNowDataItem
import kotlinx.android.synthetic.main.recent_item.view.*

class RecentAdapter : RecyclerView.Adapter<RecentAdapter.EmployeeViewHolder>() {

    inner class EmployeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val diffCallback = object : DiffUtil.ItemCallback<GetPlayNowDataItem>() {
        override fun areItemsTheSame(oldItem: GetPlayNowDataItem, newItem: GetPlayNowDataItem): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: GetPlayNowDataItem, newItem: GetPlayNowDataItem): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<GetPlayNowDataItem>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        return EmployeeViewHolder(
            LayoutInflater.from(
                parent.context
            ).inflate(
                R.layout.recent_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {

        val item = differ.currentList[position]

        holder.itemView.apply {
            tv_album.text = "${item.album}"
            tv_header.text = "${item.name}"
            tv_title.text = "${item.artist}"

            Glide.with(context)
                .load(item.image_url)
                .placeholder(R.drawable.loader)
                .error(R.mipmap.bg_image)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imageView)
        }

    }
}