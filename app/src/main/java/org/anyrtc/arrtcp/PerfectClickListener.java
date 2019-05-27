package org.anyrtc.arrtcp;

import android.view.View;

import java.util.Calendar;

public abstract class PerfectClickListener implements View.OnClickListener {

    private int id = -1;
    private long lastClickTime = 0;
    private static final int MIN_CLICK_DELAY_TIME = 1000;

    @Override
    public void onClick(View v) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        int mId = v.getId();
        if (id != mId) {
            id = mId;
            lastClickTime = currentTime;
            onNoDoubleClick(v);
            return;
        }
        if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
            lastClickTime = currentTime;
            onNoDoubleClick(v);
        }
    }

    protected abstract void onNoDoubleClick(View v);
}
