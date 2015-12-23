package fr.bde_eseo.eseomega.listeners;

import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;

/**
 * Created by Rascafr on 22/12/2015.
 */
// Scroll listener to prevent issue 77846
public class RecyclerViewDisabler implements RecyclerView.OnItemTouchListener {

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        return true;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}