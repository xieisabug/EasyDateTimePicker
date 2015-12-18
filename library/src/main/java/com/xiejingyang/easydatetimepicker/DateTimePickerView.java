package com.xiejingyang.easydatetimepicker;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DateTimePickerView extends RelativeLayout implements DatePicker.OnDateChangedListener, TimePicker.OnTimeChangedListener {

    DateTimePickerController dateTimePickerController;
    DialogOperator dialogOperator;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TextView tip;
    private TextView startTime;
    private Button confirm;
    private Button cancel;

    public DateTimePickerView(Context context, int controllerType) {
        this(context, null, controllerType);
    }

    public DateTimePickerView(Context context, AttributeSet attrs, int controllerType) {
        this(context, attrs, 0, controllerType);
    }

    public DateTimePickerView(Context context, AttributeSet attrs, int defStyleAttr, int controllerType) {
        super(context, attrs, defStyleAttr);

        //初始化界面
        LayoutInflater from = LayoutInflater.from(context);
        View inflate = from.inflate(R.layout.datetime_dialog, this, true);
        tip = (TextView) inflate.findViewById(R.id.tip);
        startTime = (TextView) inflate.findViewById(R.id.start_time);
        tabLayout = (TabLayout) inflate.findViewById(R.id.tab);
        viewPager = (ViewPager) inflate.findViewById(R.id.view_pager);
        confirm = (Button) inflate.findViewById(R.id.confirm);
        cancel = (Button) inflate.findViewById(R.id.cancel);

        View datePicker = from.inflate(R.layout.date_picker, viewPager, false);

        //根据情况，初始化控制器
        switch (controllerType) {
            case 1:
                dateTimePickerController = new SingleDateTimePickerController();
                break;
            case 2:
                dateTimePickerController = new DoubleDateTimePickerController();
                break;
        }
        Calendar initCalendar = dateTimePickerController.getInitCalendar();
        ((DatePicker) datePicker.findViewById(R.id.date_picker))
                .init(initCalendar.get(Calendar.YEAR), initCalendar.get(Calendar.MONTH), initCalendar.get(Calendar.DAY_OF_MONTH), this);
        TimePicker timePicker = new TimePicker(context);
        timePicker.setOnTimeChangedListener(this);

        final List<View> viewList = Arrays.asList(datePicker, timePicker);

        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public void destroyItem(ViewGroup view, int position, Object object) {
                view.removeView(viewList.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup view, int position) {
                view.addView(viewList.get(position), 0);

                return viewList.get(position);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return dateTimePickerController.getPageTitle(position);
            }
        });

        tabLayout.setupWithViewPager(viewPager);
        tip.setText("请选择日期");



        confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dateTimePickerController.onPositiveButtonClick();
            }
        });
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dateTimePickerController.onNegativeButtonClick();
            }
        });
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        dateTimePickerController.onDateChoose(year, monthOfYear, dayOfMonth);
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        dateTimePickerController.onTimeChoose(hourOfDay, minute);
    }

    public interface DialogOperator {
        void dialogDismiss();
        void chooseSuccess(DateTimePickerController dateTimePickerController);
    }

    public void setDialogOperator(DialogOperator dialogOperator) {
        this.dialogOperator = dialogOperator;
    }

    /**
     * 日期时间控制器的抽象类
     */
    public abstract class DateTimePickerController {
        //初始化tab的标题
        abstract String getPageTitle(int position);
        //选择时间触发事件
        abstract void onDateChoose(int year, int monthOfYear, int dayOfMonth);
        //选择时间触发事件
        abstract void onTimeChoose(int hourOfDay, int minute);
        //获取初始化的calendar
        abstract Calendar getInitCalendar();
        //确定按钮被点击事件
        public abstract void onPositiveButtonClick();
        //取消按钮被点击事件
        public abstract void onNegativeButtonClick();
    }

    /**
     * 仅选择一个日期时间的控制器
     */
    public class SingleDateTimePickerController extends DateTimePickerController {

        public Calendar calendar;
        private final SimpleDateFormat dateFormat;
        private final SimpleDateFormat timeFormat;

        public SingleDateTimePickerController() {
            this.calendar = Calendar.getInstance();
            dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
        }

        @Override
        Calendar getInitCalendar() {
            return calendar;
        }

        @Override
        public void onPositiveButtonClick() {
            if (viewPager.getCurrentItem() == 0) {
                viewPager.setCurrentItem(1, true);
            } else {
                dialogOperator.dialogDismiss();
                dialogOperator.chooseSuccess(dateTimePickerController);
            }
        }

        @Override
        public void onNegativeButtonClick() {
            dialogOperator.dialogDismiss();
        }

        @Override
        String getPageTitle(int position) {
            if (position == 0) {
                return dateFormat.format(calendar.getTime());
            } else {
                return timeFormat.format(calendar.getTime());
            }
        }

        @Override
        void onDateChoose(int year, int monthOfYear, int dayOfMonth) {
            calendar.set(year, monthOfYear, dayOfMonth);
            TabLayout.Tab tabAt = tabLayout.getTabAt(0);
            if (tabAt != null) {
                tabAt.setText(dateFormat.format(calendar.getTime()));
            }
        }

        @Override
        void onTimeChoose(int hourOfDay, int minute) {
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
            TabLayout.Tab tabAt = tabLayout.getTabAt(1);
            if (tabAt != null) {
                tabAt.setText(timeFormat.format(calendar.getTime()));
            }
        }


    }

    public class DoubleDateTimePickerController extends DateTimePickerController {

        public Calendar startCalendar;
        public Calendar endCalendar;
        private final SimpleDateFormat dateFormat;
        private final SimpleDateFormat timeFormat;

        public static final int START_STATE = 0;
        public static final int END_STATE = 1;

        public int state;

        public DoubleDateTimePickerController() {
            this.startCalendar = Calendar.getInstance();
            this.endCalendar = Calendar.getInstance();
            dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
            confirm.setText("下一步");
            startTime.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    state = START_STATE;
                    viewPager.setCurrentItem(0, true);
                    confirm.setText("下一步");
                    startTime.setVisibility(GONE);
                }
            });
        }

        @Override
        String getPageTitle(int position) {
            if (position == 0) {
                return dateFormat.format(startCalendar.getTime());
            } else {
                return timeFormat.format(startCalendar.getTime());
            }
        }

        @Override
        void onDateChoose(int year, int monthOfYear, int dayOfMonth) {
            if (state == START_STATE) {
                startCalendar.set(year, monthOfYear, dayOfMonth);
                TabLayout.Tab tabAt = tabLayout.getTabAt(0);
                if (tabAt != null) {
                    tabAt.setText(dateFormat.format(startCalendar.getTime()));
                }
            } else {
                endCalendar.set(year, monthOfYear, dayOfMonth);
                TabLayout.Tab tabAt = tabLayout.getTabAt(0);
                if (tabAt != null) {
                    tabAt.setText(dateFormat.format(endCalendar.getTime()));
                }
            }
        }

        @Override
        void onTimeChoose(int hourOfDay, int minute) {
            if (state == START_STATE) {
                startCalendar.set(startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
                TabLayout.Tab tabAt = tabLayout.getTabAt(1);
                if (tabAt != null) {
                    tabAt.setText(timeFormat.format(startCalendar.getTime()));
                }
            } else {
                endCalendar.set(endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
                TabLayout.Tab tabAt = tabLayout.getTabAt(1);
                if (tabAt != null) {
                    tabAt.setText(timeFormat.format(endCalendar.getTime()));
                }
            }
        }

        @Override
        Calendar getInitCalendar() {
            return startCalendar;
        }

        @Override
        public void onPositiveButtonClick() {
            if (state == START_STATE) {
                if (viewPager.getCurrentItem() == 0) {
                    viewPager.setCurrentItem(1, true);
                } else {
                    state = END_STATE;
                    viewPager.setCurrentItem(0, true);
                    tip.setText("选择结束时间");
                    startTime.setText("开始时间：" + dateFormat.format(startCalendar.getTime()) + "  " + timeFormat.format(startCalendar.getTime()));
                    startTime.setVisibility(VISIBLE);
                }
            } else if (state == END_STATE) {
                if (viewPager.getCurrentItem() == 0) {
                    viewPager.setCurrentItem(1, true);
                    confirm.setText("确定");
                } else {
                    dialogOperator.dialogDismiss();
                    dialogOperator.chooseSuccess(dateTimePickerController);
                }
            }
        }

        @Override
        public void onNegativeButtonClick() {
            dialogOperator.dialogDismiss();
        }
    }
}
