package com.example.bao.calendarlist;

import android.content.Context;

/**
 * @author Jiang
 * @date 2019-09-16
 */
public class CommonUtil {
    /**
     * 根据手机分辨率从dp转成px
     *
     * @param context
     * @param dpValue
     * @return
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
