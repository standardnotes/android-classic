package org.standardnotes.notes

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import org.standardnotes.notes.frag.NoteFragment


class NoteActivity : BaseActivity() {

    val REVEAL_ANIM_DURATION = 200L
    var backPressed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)

        if (savedInstanceState == null) {
            val frag: NoteFragment = NoteFragment()
            frag.arguments = intent.extras

            supportFragmentManager.beginTransaction().replace(android.R.id.content, frag).commit()

            if (frag.arguments == null) {
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
        if (!backPressed) {
            overridePendingTransition(0, 0)
            if (intent.extras == null) {
                circularHide()
                findViewById(android.R.id.content).postDelayed({
                    super.onBackPressed()
                }, REVEAL_ANIM_DURATION)
            } else {
                super.onBackPressed()
            }
        }
        backPressed = true
    }

    private fun circularReveal() {
        val rootView = findViewById(android.R.id.content)
        val halfFab = 56.dpToPixels() / 2
        val cx = rootView.width - resources.getDimension(R.dimen.activity_horizontal_margin).toInt() - halfFab
        val cy = rootView.height -  resources.getDimension(R.dimen.activity_vertical_margin).toInt() - halfFab
        val circularReveal = ViewAnimationUtils.createCircularReveal(rootView, cx, cy, 0f, Math.max(rootView.width, rootView.height).toFloat())
        circularReveal.duration = REVEAL_ANIM_DURATION
        rootView.visibility = View.VISIBLE
        circularReveal.start()
    }

    private fun circularHide() {
        val rootView = findViewById(android.R.id.content)
        val halfFab = 56.dpToPixels() / 2
        val cx = rootView.width - resources.getDimension(R.dimen.activity_horizontal_margin).toInt() - halfFab
        val cy = rootView.height -  resources.getDimension(R.dimen.activity_vertical_margin).toInt() - halfFab
        val circularHide = ViewAnimationUtils.createCircularReveal(rootView, cx, cy, Math.max(rootView.width, rootView.height).toFloat(), 0f)
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