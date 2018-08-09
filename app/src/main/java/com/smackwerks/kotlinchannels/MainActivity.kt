package com.smackwerks.kotlinchannels

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.smackwerks.kotlinchannels.data.RepoModel
import com.smackwerks.kotlinchannels.data.StockModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        setupTicker()
        setupRepos()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRepos() {
        repos_recycler.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = ReposRecyclerAdapter(
                RepoModel.getRepos(),
                { main_progress_bar.visibility = View.VISIBLE },
                { main_progress_bar.visibility = View.GONE }
            )
        }
    }

    private fun setupTicker() {
        launch(UI) {
            val chan = StockModel.getStockPrice("GOOGL")

            while (!chan.isClosedForReceive) {
                chan.receiveOrNull()?.apply {
                    stock_ticker.text = "${symbol}: $${price}"
                }
            }
        }
    }
}
