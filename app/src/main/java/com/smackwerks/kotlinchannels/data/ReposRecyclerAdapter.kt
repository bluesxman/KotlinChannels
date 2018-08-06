package com.smackwerks.kotlinchannels.data

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.smackwerks.kotlinchannels.R
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch

class ReposRecyclerAdapter(
    private val reposChan: Channel<Repository>,
    private val onWait: () -> Unit,
    private val onReady: () -> Unit
) : RecyclerView.Adapter<ReposRecyclerAdapter.ReposViewholder>() {

    private val reposCache = mutableListOf<Repository>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReposViewholder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler_repos, parent, false) as TextView
        return ReposViewholder(view)
    }

    override fun getItemCount() = Int.MAX_VALUE

    override fun onBindViewHolder(holder: ReposViewholder, position: Int) {
        if (reposCache.size > position) {
            holder.display(reposCache[position])
        } else {
            launch(UI) {
                while (reposCache.size <= position) {
                    if (reposChan.isEmpty) {
                        onWait()
                        reposCache.add(reposChan.receive())
                        onReady()
                    } else {
                        reposCache.add(reposChan.receive())
                    }
                }
                holder.display(reposCache[position])
            }
        }
    }

    class ReposViewholder(private val view: TextView) : RecyclerView.ViewHolder(view) {
        fun display(repo: Repository) {
            view.text = "Name: ${repo.name}"
        }
    }
}