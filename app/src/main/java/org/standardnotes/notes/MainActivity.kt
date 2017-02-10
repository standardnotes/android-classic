package org.standardnotes.notes

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_navigation_header.view.*
import org.standardnotes.notes.frag.NoteListFragment

class MainActivity : AppCompatActivity() {

    private var account: Account? = null

    private var mDrawerToggle: ActionBarDrawerToggle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        account = SApplication.instance!!.account(intent)
        if (null == account) {
            finish()
            return
        }
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        mDrawerToggle = ActionBarDrawerToggle(this, drawer_layout,  R.string.app_name, R.string.app_name)
        drawer_layout.addDrawerListener(mDrawerToggle!!)
        mDrawerToggle!!.isDrawerIndicatorEnabled = true
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        val header = drawer.inflateHeaderView(R.layout.view_navigation_header)
        val values = SApplication.instance!!.valueStore(account!!)
        header.main_account_server.text = values.server
        header.main_account_email.text = values.email
        enableNavigationMenu(header)

        title = getString(R.string.app_name)

        fab.setOnClickListener { view ->
            (supportFragmentManager.findFragmentById(R.id.noteListFrag) as NoteListFragment).startNewNote()
        }

        drawer.setNavigationItemSelectedListener {
            drawerMenuHandler(it)
        }
    }

    fun drawerMenuHandler(it: MenuItem): Boolean {
        when (it.itemId) {
            R.id.menu_account_add -> {
                finish()
                addNewAccount(this)
            }
            R.id.menu_account_logout -> {
                finish()
                SApplication.instance!!.removeAccount(account!!, this)
            }
        }
        return true
    }

    fun enableNavigationMenu(header: View) {
        var accountMenu = false
        fun changeMenu() {
            fun accountMenuItem(it: MenuItem, acc: Account) {
                it.setIcon(R.drawable.ic_account)
                if (acc.equals(account)) {
                    it.isChecked = true
                }
                it.setOnMenuItemClickListener {
                    SApplication.instance!!.changeAccount(acc)
                    finish()
                    startActivity(Intent(this, MainActivity::class.java))
                    return@setOnMenuItemClickListener true
                }

            }
            fun tagMenuItem(it: MenuItem, uuid: String?) {
                it.setIcon(R.drawable.ic_tag)
            }
            val icon = if (accountMenu) R.drawable.ic_menu_up else R.drawable.ic_menu_down
            header.main_account_menu_icon.setImageResource(icon)
            drawer.menu.clear()
            drawer.inflateMenu(if (accountMenu) R.menu.account_accounts else R.menu.account_normal)
            if (accountMenu) {
                // Add sub-items
                val mitem = drawer.menu.findItem(R.id.menu_account_accounts)
                val accounts = AccountManager.get(this).getAccountsByType(getString(R.string.account_type))
                for (acc in accounts) {
                    val values = SApplication.instance!!.valueStore(acc)
                    val menuItem = mitem.subMenu.add(values.email)
                    accountMenuItem(menuItem, acc)
                }
            } else {
                val tags = SApplication.instance!!.noteStore(account!!).getAllTags()
                val mitem = drawer.menu.findItem(R.id.menu_account_tags)
                val item = mitem.subMenu.add("All notes")
                tagMenuItem(item, null)
                for (tag in tags) {
                    val item = mitem.subMenu.add(tag.title)
                    tagMenuItem(item, tag.uuid)
                }
            }
        }
        header.main_account_title.setOnClickListener {
            accountMenu = !accountMenu
            changeMenu()
        }
        changeMenu()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        val item = menu.add(getString(R.string.action_logout))
        item.setIcon(android.R.drawable.ic_menu_search)
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (mDrawerToggle!!.onOptionsItemSelected(item)) {
            return true
        }
        // TODO: Remove account
        return true
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mDrawerToggle?.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        mDrawerToggle?.onConfigurationChanged(newConfig)
    }
}
