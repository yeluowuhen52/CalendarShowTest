package com.example.bao.calendarlist;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarList extends FrameLayout {
    private static final String TAG = "_LOG";
    private RecyclerView recyclerView;
    private CalendarAdapter adapter;
    private DateBean startDate;//开始时间
    private DateBean endDate;//结束时间
    private OnDateSelected onDateSelected;//选中监听
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static Context mContext;
    //日期格式化
    public SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    public SimpleDateFormat formatYYYYMM = new SimpleDateFormat("yyyy-MM");
    //日期集合
    private List<DateBean> allDays;
    //当天
    private DateBean todayBean;

    public CalendarList(Context context) {
        super(context);
        init(context);
    }

    public CalendarList(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CalendarList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        addView(LayoutInflater.from(context).inflate(R.layout.item_calendar, this, false));

        recyclerView = findViewById(R.id.recyclerView);
        adapter = new CalendarAdapter();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 7);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int i) {
                if (DateBean.item_type_month == adapter.data.get(i).getItemType()) {
                    return 7;
                } else {
                    return 1;
                }
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        initMyDays(context);
    }

    /**
     * 重新初始化布局
     *
     * @param context
     */
    public void initMyDays(Context context) {
        if (allDays != null) {
            allDays.clear();
        }
        if (adapter.data != null) {
            adapter.data.clear();
        }
        allDays = days(new Date(), -1, 2);
        adapter.data.addAll(allDays);

//        DividerItemDecoration dividerItemDecoration=new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
//        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this,R.drawable.shape));
//        recyclerView.addItemDecoration(dividerItemDecoration);

        MyItemD myItemD = new MyItemD(context);
        recyclerView.addItemDecoration(myItemD);

        adapter.setOnRecyclerviewItemClick(new CalendarAdapter.OnRecyclerviewItemClick() {
            @Override
            public void onItemClick(View v, int position) {
                onClick(adapter.data.get(position));
            }
        });
        if (todayBean != null) {
            recyclerView.smoothScrollBy(0, 200);
        }
    }

    private void onClick(DateBean dateBean) {

        if (dateBean.getItemType() == DateBean.item_type_month || TextUtils.isEmpty(dateBean.getDay())) {
            return;
        }

        //如果没有选中开始日期则此次操作选中开始日期
        if (startDate == null) {
            startDate = dateBean;
            dateBean.setItemState(DateBean.ITEM_STATE_BEGIN_DATE);
        } else if (endDate == null) {
            //如果选中了开始日期但没有选中结束日期，本次操作选中结束日期

            //如果当前点击的结束日期跟开始日期一致 则不做操作
            if (startDate == dateBean) {

            } else if (dateBean.getDate().getTime() < startDate.getDate().getTime()) {
                //当前点选的日期小于当前选中的开始日期 则本次操作重新选中开始日期
                startDate.setItemState(DateBean.ITEM_STATE_NORMAL);
                startDate = dateBean;
                startDate.setItemState(DateBean.ITEM_STATE_BEGIN_DATE);
            } else {
                //选中结束日期
                endDate = dateBean;
                endDate.setItemState(DateBean.ITEM_STATE_END_DATE);
                setState();

                if (onDateSelected != null) {
                    onDateSelected.selected(simpleDateFormat.format(startDate.getDate()), simpleDateFormat.format(endDate.getDate()));
                }
            }

        } else if (startDate != null && endDate != null) {
            //结束日期和开始日期都已选中
            clearState();

            if (endDate == dateBean) {
                endDate.setItemState(DateBean.ITEM_STATE_NORMAL);
                endDate = null;

                //重新选择开始日期,结束日期清除
                startDate.setItemState(DateBean.ITEM_STATE_NORMAL);

                dateBean.setItemState(DateBean.ITEM_STATE_BEGIN_DATE);
                startDate = dateBean;

                ArrayList<DateBean> datas = adapter.data;
            } else {
                //重新选择开始日期,结束日期清除
                startDate.setItemState(DateBean.ITEM_STATE_NORMAL);
                //这里有问题
                startDate = dateBean;
                startDate.setItemState(DateBean.ITEM_STATE_BEGIN_DATE);
                ArrayList<DateBean> datas = adapter.data;
                endDate.setItemState(DateBean.ITEM_STATE_NORMAL);
                endDate = null;
            }
        }

        adapter.notifyDataSetChanged();
    }

    //选中中间的日期
    private void setState() {
        if (endDate != null && startDate != null) {
            int start = adapter.data.indexOf(startDate);
            start += 1;
            int end = adapter.data.indexOf(endDate);
            for (; start < end; start++) {

                DateBean dateBean = adapter.data.get(start);
                if (!TextUtils.isEmpty(dateBean.getDay())) {
                    dateBean.setItemState(DateBean.ITEM_STATE_SELECTED);
                }
            }
        }
    }

    //取消选中状态
    private void clearState() {
        if (endDate != null && startDate != null) {
            int start = adapter.data.indexOf(startDate);
            start += 1;
            int end = adapter.data.indexOf(endDate);
            for (; start < end; start++) {
                DateBean dateBean = adapter.data.get(start);
                dateBean.setItemState(DateBean.ITEM_STATE_NORMAL);
            }
        }
    }

    //日历adapter
    public static class CalendarAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        ArrayList<DateBean> data = new ArrayList<>();
        private CalendarAdapter.OnRecyclerviewItemClick onRecyclerviewItemClick;

        public CalendarAdapter.OnRecyclerviewItemClick getOnRecyclerviewItemClick() {
            return onRecyclerviewItemClick;
        }

        public void setOnRecyclerviewItemClick(CalendarAdapter.OnRecyclerviewItemClick onRecyclerviewItemClick) {
            this.onRecyclerviewItemClick = onRecyclerviewItemClick;
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            if (i == DateBean.item_type_day) {
                View rootView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_day, viewGroup, false);

                final CalendarAdapter.DayViewHolder dayViewHolder = new CalendarAdapter.DayViewHolder(rootView);
                dayViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onRecyclerviewItemClick != null) {
                            onRecyclerviewItemClick.onItemClick(v, dayViewHolder.getLayoutPosition());
                        }
                    }
                });
                return dayViewHolder;
            } else if (i == DateBean.item_type_month) {
                View rootView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_month, viewGroup, false);
                final CalendarAdapter.MonthViewHolder monthViewHolder = new CalendarAdapter.MonthViewHolder(rootView);
                monthViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onRecyclerviewItemClick != null) {
                            onRecyclerviewItemClick.onItemClick(v, monthViewHolder.getLayoutPosition());
                        }
                    }
                });
                return monthViewHolder;
            }
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            if (viewHolder instanceof CalendarAdapter.MonthViewHolder) {
                ((CalendarAdapter.MonthViewHolder) viewHolder).tv_month.setText(data.get(i).getMonthStr());
            } else {
                CalendarAdapter.DayViewHolder vh = ((CalendarAdapter.DayViewHolder) viewHolder);
                vh.tv_day.setText(data.get(i).getDay());
                vh.tv_check_in_check_out.setVisibility(View.GONE);
                DateBean dateBean = data.get(i);

                //设置item状态
                if (dateBean.getItemState() == DateBean.ITEM_STATE_BEGIN_DATE || dateBean.getItemState() == DateBean.ITEM_STATE_END_DATE) {
                    //开始日期或结束日期
                    vh.itemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.dateStartBlue));
                    vh.tv_day.setTextColor(Color.WHITE);
                    vh.tv_check_in_check_out.setVisibility(View.VISIBLE);
                    if (dateBean.getItemState() == DateBean.ITEM_STATE_END_DATE) {
                        vh.tv_check_in_check_out.setText("终止");
                    } else {
                        vh.tv_check_in_check_out.setText("起始");
                    }

                } else if (dateBean.getItemState() == DateBean.ITEM_STATE_SELECTED) {
                    //选中状态
                    vh.itemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.dateSelectBlue));
                    vh.tv_day.setTextColor(Color.WHITE);
                } else if (dateBean.isToday) {
                    vh.itemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.dateStartBlue));
                    vh.tv_day.setTextColor(Color.BLACK);
                    vh.tvToday.setVisibility(View.VISIBLE);
                } else {
                    //正常状态
                    vh.itemView.setBackgroundColor(Color.WHITE);
                    vh.tv_day.setTextColor(Color.BLACK);
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            return data.get(position).getItemType();
        }

        public class DayViewHolder extends RecyclerView.ViewHolder {
            public TextView tv_day;
            public TextView tv_check_in_check_out;
            public TextView tvToday;

            public DayViewHolder(@NonNull View itemView) {
                super(itemView);
                tv_day = itemView.findViewById(R.id.tv_day);
                tv_check_in_check_out = itemView.findViewById(R.id.tv_check_in_check_out);
                tvToday = itemView.findViewById(R.id.tvToday);
            }
        }

        public class MonthViewHolder extends RecyclerView.ViewHolder {
            public TextView tv_month;

            public MonthViewHolder(@NonNull View itemView) {
                super(itemView);
                tv_month = itemView.findViewById(R.id.tv_month);
            }
        }

        public interface OnRecyclerviewItemClick {
            void onItemClick(View v, int position);
        }
    }

    /**
     * 生成日历数据
     *
     * @param baseDate    基准日期
     * @param deforeMonth 往前几个月
     * @param afterMonth  往后几个月
     * @return
     */
    private List<DateBean> days(Date baseDate, int deforeMonth, int afterMonth) {
        List<DateBean> dateBeans = new ArrayList<>();
        try {
            Calendar calendar = Calendar.getInstance();


            //起始日期（前一个月）
            Date startDate = baseDate;
            calendar.setTime(startDate);
            calendar.add(Calendar.MONTH, deforeMonth);
            startDate = calendar.getTime();

            //结束日期(后一个月)
            calendar.add(Calendar.MONTH, afterMonth);
            Date endDate = new Date(calendar.getTimeInMillis());

            Log.d(TAG, "startDate:" + format.format(startDate) + "----------endDate:" + format.format(endDate));

            //格式化开始日期和结束日期为 yyyy-mm-dd格式
            String endDateStr = format.format(endDate);
            endDate = format.parse(endDateStr);

            String startDateStr = format.format(startDate);
            startDate = format.parse(startDateStr);

            calendar.setTime(startDate);

            Log.d(TAG, "startDateStr:" + startDateStr + "---------endDate:" + format.format(endDate));
            Log.d(TAG, "endDateStr:" + endDateStr + "---------endDate:" + format.format(endDate));

            calendar.set(Calendar.DAY_OF_MONTH, 1);
            Calendar monthCalendar = Calendar.getInstance();


            //按月生成日历 每行7个 最多6行 42个
            //每一行有七个日期  日 一 二 三 四 五 六 的顺序
            for (; calendar.getTimeInMillis() <= endDate.getTime(); ) {

                //月份item
                DateBean monthDateBean = new DateBean();
                monthDateBean.setDate(calendar.getTime());
                monthDateBean.setMonthStr(formatYYYYMM.format(monthDateBean.getDate()));
                monthDateBean.setItemType(DateBean.item_type_month);
                dateBeans.add(monthDateBean);

                //获取一个月结束的日期和开始日期
                monthCalendar.setTime(calendar.getTime());
                monthCalendar.set(Calendar.DAY_OF_MONTH, 1);
                Date startMonthDay = calendar.getTime();

                monthCalendar.add(Calendar.MONTH, 1);
                monthCalendar.add(Calendar.DAY_OF_MONTH, -1);
                Date endMonthDay = monthCalendar.getTime();

                //重置为本月开始
                monthCalendar.set(Calendar.DAY_OF_MONTH, 1);

                Log.d(TAG, "月份的开始日期:" + format.format(startMonthDay) + "---------结束日期:" + format.format(endMonthDay));
                for (; monthCalendar.getTimeInMillis() <= endMonthDay.getTime(); ) {
                    //生成单个月的日历

                    //处理一个月开始的第一天
                    if (monthCalendar.get(Calendar.DAY_OF_MONTH) == 1) {
                        //看某个月第一天是周几
                        int weekDay = monthCalendar.get(Calendar.DAY_OF_WEEK);
                        switch (weekDay) {
                            case 1:
                                //周日
                                break;
                            case 2:
                                //周一
                                addDatePlaceholder(dateBeans, 1, monthDateBean.getMonthStr());
                                break;
                            case 3:
                                //周二
                                addDatePlaceholder(dateBeans, 2, monthDateBean.getMonthStr());
                                break;
                            case 4:
                                //周三
                                addDatePlaceholder(dateBeans, 3, monthDateBean.getMonthStr());
                                break;
                            case 5:
                                //周四
                                addDatePlaceholder(dateBeans, 4, monthDateBean.getMonthStr());
                                break;
                            case 6:
                                addDatePlaceholder(dateBeans, 5, monthDateBean.getMonthStr());
                                //周五
                                break;
                            case 7:
                                addDatePlaceholder(dateBeans, 6, monthDateBean.getMonthStr());
                                //周六
                                break;
                        }
                    }

                    //生成某一天日期实体 日item
                    DateBean dateBean = new DateBean();
                    dateBean.setDate(monthCalendar.getTime());
                    dateBean.setDay(monthCalendar.get(Calendar.DAY_OF_MONTH) + "");
                    dateBean.setMonthStr(monthDateBean.getMonthStr());
                    Date date = new Date();

                    String dayTemp = dateBean.getDay();
                    if (Integer.valueOf(dayTemp) > 0 && Integer.valueOf(dayTemp) < 10) {
                        dayTemp = "0" + dayTemp;
                    }
                    String dateCurrent = dateBean.getMonthStr() == null ? "" :
                            dateBean.getMonthStr() + "-" + String.valueOf(dayTemp);

                    //格式化开始日期和结束日期为 yyyy-mm-dd格式
                    String dateStr = format.format(date);
                    if (dateCurrent.equals(dateStr)) {
                        dateBean.setToday(true);
                        todayBean = dateBean;
                    }

                    dateBean.setDateString(dateCurrent);
                    dateBeans.add(dateBean);


                    //处理一个月的最后一天
                    if (monthCalendar.getTimeInMillis() == endMonthDay.getTime()) {
                        //看某个月第一天是周几
                        int weekDay = monthCalendar.get(Calendar.DAY_OF_WEEK);
                        switch (weekDay) {
                            case 1:
                                //周日
                                addDatePlaceholder(dateBeans, 6, monthDateBean.getMonthStr());
                                break;
                            case 2:
                                //周一
                                addDatePlaceholder(dateBeans, 5, monthDateBean.getMonthStr());
                                break;
                            case 3:
                                //周二
                                addDatePlaceholder(dateBeans, 4, monthDateBean.getMonthStr());
                                break;
                            case 4:
                                //周三
                                addDatePlaceholder(dateBeans, 3, monthDateBean.getMonthStr());
                                break;
                            case 5:
                                //周四
                                addDatePlaceholder(dateBeans, 2, monthDateBean.getMonthStr());
                                break;
                            case 6:
                                addDatePlaceholder(dateBeans, 1, monthDateBean.getMonthStr());
                                //周5
                                break;
                        }
                    }

                    //天数加1
                    monthCalendar.add(Calendar.DAY_OF_MONTH, 1);
                }

                Log.d(TAG, "日期" + format.format(calendar.getTime()) + "----周几" + getWeekStr(calendar.get(Calendar.DAY_OF_WEEK) + ""));
                //月份加1
                calendar.add(Calendar.MONTH, 1);
            }

        } catch (Exception ex) {

        }

        return dateBeans;
    }

    //添加空的日期占位
    private void addDatePlaceholder(List<DateBean> dateBeans, int count, String monthStr) {
        for (int i = 0; i < count; i++) {
            DateBean dateBean = new DateBean();
            dateBean.setMonthStr(monthStr);
            dateBeans.add(dateBean);
        }
    }

    private String getWeekStr(String mWay) {
        if ("1".equals(mWay)) {
            mWay = "天";
        } else if ("2".equals(mWay)) {
            mWay = "一";
        } else if ("3".equals(mWay)) {
            mWay = "二";
        } else if ("4".equals(mWay)) {
            mWay = "三";
        } else if ("5".equals(mWay)) {
            mWay = "四";
        } else if ("6".equals(mWay)) {
            mWay = "五";
        } else if ("7".equals(mWay)) {
            mWay = "六";
        }
        return mWay;
    }


    /**
     * 设置选中时间
     * @param startMyDate
     * @param endMyDate
     * @return -1 日期为空 2：设置成功 1：日期过期 0：未设置日期
     */
    public int setMySelectDate(String startMyDate, String endMyDate) {
        if (TextUtils.isEmpty(startMyDate) || TextUtils.isEmpty(endMyDate)) {
            return -1;
        }
        if (startDate != null) {
            startDate.setItemState(DateBean.ITEM_STATE_NORMAL);
        }

        if (endDate != null) {
            endDate.setItemState(DateBean.ITEM_STATE_NORMAL);
        }
        if (startDate != null && endDate != null) {
            clearAllState();
        }
        int isAllOk = 0;
        for (DateBean dateBean : allDays) {
            if (startMyDate.equals(dateBean.getDateString())) {
                startDate = dateBean;
                startDate.setItemState(DateBean.ITEM_STATE_BEGIN_DATE);
                isAllOk++;
            } else if (endMyDate.equals(dateBean.getDateString())) {
                endDate = dateBean;
                endDate.setItemState(DateBean.ITEM_STATE_END_DATE);
                isAllOk++;
            }
            if (isAllOk == 2) {
                break;
            }
        }
        if (isAllOk == 2) {
            setState();
            adapter.notifyDataSetChanged();
            return 2;
        } else if (isAllOk == 1 || isAllOk == 0) {
            //随着时间推移，选择的时间已经失效
            initMyDays(mContext);
            return 1;
        } else {
            //用户未设置时间
            return 0;
        }
    }

    /**
     * 清空状态
     */
    public void clearAllState() {
        if (startDate != null) {
            startDate.setItemState(DateBean.ITEM_STATE_NORMAL);
        }
        if (endDate != null) {
            endDate.setItemState(DateBean.ITEM_STATE_NORMAL);
        }
        clearState();
        adapter.notifyDataSetChanged();
    }

    public interface OnDateSelected {
        void selected(String startDate, String endDate);
    }

    public void setOnDateSelected(OnDateSelected onDateSelected) {
        this.onDateSelected = onDateSelected;
    }
}
