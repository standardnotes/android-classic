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

    val REVEAL_ANIM_DURATION = 400L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.transition_none, R.anim.transition_none)

        if (savedInstanceState == null) {
            val frag: NoteFragment = NoteFragment()
            frag.arguments = intent.extras

            supportFragmentManager.beginTransaction().replace(android.R.id.content, frag).commit()

            if (frag.arguments == null) {
                val rootView = findViewById(android.R.id.content)
                rootView.visibility = View.INVISIBLE
                val viewTreeObserver = rootView.viewTreeObserver
                if (viewTreeObserver.isAlive) {
                    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {

                            circularReveal()

                            // TODO this will be required if we ever backport the app...
                            // if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            //  rootView.viewTreeObserver.removeGlobalOnLayoutListener(this)
                            // } else {
                            rootView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                            // }
                        }
                    })
                }
            }
        }
    }

    override fun onBackPressed() {
        overridePendingTransition(R.anim.transition_none, R.anim.transition_none)
        if (intent.extras == null) {
            circularHide()
            findViewById(android.R.id.content).postDelayed({
                super.onBackPressed()
            }, REVEAL_ANIM_DURATION)
        } else {
            super.onBackPressed()
        }
    }

    private fun circularReveal() {
        val rootView = findViewById(android.R.id.content)
        val halfFab = 56.dpToPixels() / 2
        val cx = rootView.width - resources.getDimension(R.dimen.activity_horizontal_margin).toInt() - halfFab
        val cy = rootView.height -  resources.getDimension(R.dimen.activity_vertical_margin).toInt() - halfFab
        val circularReveal = ViewAnimationUtils.createCircularReveal(rootView, cx, cy, 0f, Math.max(rootView.width, rootView.height).toFloat())
        circularReveal.duration = REVEAL_ANIM_DURATION
        rootView.visibility = View.VISIBLE

//        val fromcolor = ContextCompat.getColor(this, R.color.colorPrimary)
//        val tocolor = ContextCompat.getColor(this, android.R.color.white)
//
//        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), fromcolor, tocolor)
//        colorAnimation.duration = 1000
//        colorAnimation.addUpdateListener { animator ->
//            val i = animator.animatedValue as Int
//            rootView.background.setColorFilter(i, PorterDuff.Mode.SRC_IN)
//        }
//        colorAnimation.start()

        circularReveal.start()
    }

    private fun circularHide() {
        val rootView = findViewById(android.R.id.content)
        val halfFab = 56.dpToPixels() / 2
        val cx = rootView.width - resources.getDimension(R.dimen.activity_horizontal_margin).toInt() - halfFab
        val cy = rootView.height -  resources.getDimension(R.dimen.activity_vertical_margin).toInt() - halfFab
        val circularReveal = ViewAnimationUtils.createCircularReveal(rootView, cx, cy, Math.max(rootView.width, rootView.height).toFloat(), 0f)
        circularReveal.duration = REVEAL_ANIM_DURATION
        circularReveal.addListener(object : Animator.AnimatorListener {
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
        circularReveal.start()
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