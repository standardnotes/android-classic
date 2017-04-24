package org.standardnotes.notes

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import org.standardnotes.notes.frag.NoteListFragment


class SearchActivity : AppCompatActivity() {

    var query: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        query = savedInstanceState?.getString("query", "") ?: ""
        noteListFragment().refreshNotesForSearch(query)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("query", query)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.search, menu)
        val searchView = menu.findItem(R.id.search).actionView as SearchView
        searchView.setQuery(query, false)
        searchView.queryHint = getText(R.string.search)
        searchView.maxWidth = Int.MAX_VALUE
        searchView.setIconifiedByDefault(false)
        searchView.requestFocus()
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(newText: String): Boolean {
                query = newText
                noteListFragment().refreshNotesForSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                query = newText
                noteListFragment().refreshNotesForSearch(newText)
                return true
            }

        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (NavUtils.getParentActivityIntent(this) != null) {
                NavUtils.navigateUpTo(this, NavUtils.getParentActivityIntent(this).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            } else {
                onBackPressed()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun noteListFragment(): NoteListFragment {
        return supportFragmentManager.findFragmentById(R.id.noteListFrag) as NoteListFragment
    }
}
