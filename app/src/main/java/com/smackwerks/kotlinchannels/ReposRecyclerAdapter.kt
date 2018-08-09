package com.smackwerks.kotlinchannels

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.smackwerks.kotlinchannels.data.Repository
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

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
                    val notReady = reposChan.isEmpty
                    if (notReady) onWait()
                    reposChan.receiveOrNull()
                        ?.let { reposCache.add(it) }
                        ?: run {
                            Timber.e("Repo channel closed unexpectedly.")
                            return@launch
                        }
                    if (notReady) onReady()
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