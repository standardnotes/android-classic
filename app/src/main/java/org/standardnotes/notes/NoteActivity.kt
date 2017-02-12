package org.standardnotes.notes

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import org.standardnotes.notes.frag.NoteFragment
import org.standardnotes.notes.frag.NoteListFragment.Companion.EXTRA_X_COOR
import org.standardnotes.notes.frag.NoteListFragment.Companion.EXTRA_Y_COOR

class NoteActivity : BaseActivity() {

    val REVEAL_ANIM_DURATION = 200L
    var revealX: Int? = null
    var revealY: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)

        if (savedInstanceState == null) {
            val frag: NoteFragment = NoteFragment()
            frag.arguments = intent.extras

            supportFragmentManager.beginTransaction().replace(android.R.id.content, frag).commit()

            if (frag.arguments != null && frag.arguments.containsKey(EXTRA_X_COOR)) {

                revealX = frag.arguments.getInt(EXTRA_X_COOR)
                revealY = frag.arguments.getInt(EXTRA_Y_COOR)

                val rootView = findViewById(android.R.id.content)
                val viewTreeObserver = rootView.viewTreeObserver
                if (viewTreeObserver.isAlive) {
                    rootView.visibility = View.INVISIBLE
                    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            circularReveal()
                            rootView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        }
                    })
                }
            }
        }
    }

    override fun onBackPressed() {
        overridePendingTransition(0, 0)
        if (intent.extras != null && intent.extras.containsKey(EXTRA_X_COOR)) {
            circularHide()
        } else {
            super.onBackPressed()
        }
    }

    private fun circularReveal() {
        Log.d("zzz", "x: " + revealX + ", y:" + revealY)
        val rootView = findViewById(android.R.id.content)
        val circularReveal = ViewAnimationUtils.createCircularReveal(rootView, revealX!!, revealY!!, 0f, Math.max(rootView.width, rootView.height).toFloat())
        circularReveal.duration = REVEAL_ANIM_DURATION
        rootView.visibility = View.VISIBLE
        circularReveal.start()
    }

    private fun circularHide() {
        val rootView = findViewById(android.R.id.content)
        val circularHide = ViewAnimationUtils.createCircularReveal(rootView, revealX!!, revealY!!, Math.max(rootView.width, rootView.height).toFloat(), 0f)
        circularHide.duration = REVEAL_ANIM_DURATION
        circularHide.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                //
            }

            override fun onAnimationRepeat(animation: Animator) {
                //
            }

            override fun onAnimationEnd(animation: Animator) {
                rootView.visibility = View.GONE
                overridePendingTransition(0, 0)
                finish()
            }

            override fun onAnimationCancel(animation: Animator) {
                //
            }
        })
        circularHide.start()
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
}