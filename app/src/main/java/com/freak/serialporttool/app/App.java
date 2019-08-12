package com.freak.serialporttool.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.freak.serialporttool.R;
import com.freak.serialporttool.base.IActivityStatusBar;
import com.freak.serialporttool.utils.LogUtil;
import com.freak.serialporttool.utils.PrefHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;


/**
 * @author Freak
 * @date 2019/8/12.
 */


public class App extends MultiDexApplication {
    public static final int DESIGN_WIDTH = 375;
    private static App instance;
    private Set<Activity> allActivities;
    private Handler mUiHandler;

    public Set<Activity> getAllActivities() {
        return allActivities;
    }

    public void setAllActivities(Set<Activity> allActivities) {
        this.allActivities = allActivities;
    }

    public static synchronized App getInstance() {
        return instance;
    }

    public static void setInstance(App instance) {
        App.instance = instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mUiHandler = new Handler();
        initUtils();
        LogUtil.init("SerialPortTool", true);
        registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                //限制竖屏
                //8.0.0版本系统，同时设置竖屏和设置全屏透明冲突，
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                resetDensity(getApplicationContext(), DESIGN_WIDTH);
                resetDensity(activity, DESIGN_WIDTH);
                setImmersiveStatusBar(activity);

            }

            @Override
            public void onActivityStarted(Activity activity) {
                setToolBar(activity);
                resetDensity(getApplicationContext(), DESIGN_WIDTH);
                resetDensity(activity, DESIGN_WIDTH);
            }

            @Override
            public void onActivityResumed(Activity activity) {
                resetDensity(getApplicationContext(), DESIGN_WIDTH);
                resetDensity(activity, DESIGN_WIDTH);
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                resetDensity(getApplicationContext(), DESIGN_WIDTH);
                resetDensity(activity, DESIGN_WIDTH);
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });


    }

    private void initUtils() {
        PrefHelper.initDefault(this);
    }

    public static Handler getUiHandler() {
        return getInstance().mUiHandler;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // you must install multiDex whatever tinker is installed!
        MultiDex.install(base);
    }

    /**
     * 设置ToolBar
     *
     * @param activity
     */
    private void setToolBar(final Activity activity) {
        if (activity.findViewById(R.id.tool_bar) != null && ((AppCompatActivity) activity).getSupportActionBar() == null) {
            Toolbar toolbar = activity.findViewById(R.id.tool_bar);
            if (!TextUtils.isEmpty(activity.getTitle())) {
                toolbar.setTitle(activity.getTitle());
            } else {
                toolbar.setTitle("");
            }

            if (((IActivityStatusBar) activity).getStatusBarColor() != 0) {
                toolbar.setBackgroundColor(((IActivityStatusBar) activity).getStatusBarColor());
            } else {
                toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.color_white));
            }

            ((AppCompatActivity) activity).setSupportActionBar(toolbar);
            ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setHomeButtonEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.onBackPressed();
                }
            });
        }
    }

    /**
     * 设置状态栏
     *
     * @param activity
     */
    private void setImmersiveStatusBar(Activity activity) {
        if (activity instanceof IActivityStatusBar) {
            if (((IActivityStatusBar) activity).getStatusBarColor() != 0) {
                setTranslucentStatus(activity);
                addImmersiveStatusBar(activity, ((IActivityStatusBar) activity).getStatusBarColor());
            } else {
                if (((IActivityStatusBar) activity).getDrawableStatusBar() != 0) {
                    setTranslucentStatus(activity);
                    addImmersiveShadeStatusBar(activity, ((IActivityStatusBar) activity).getDrawableStatusBar());
                }
            }
        }
    }

    /**
     * 添加自定义状态栏
     *
     * @param activity
     */
    private void addImmersiveStatusBar(Activity activity, int color) {
        ViewGroup contentFrameLayout = activity.findViewById(android.R.id.content);
        View contentView = contentFrameLayout.getChildAt(0);
        if (contentView != null && Build.VERSION.SDK_INT >= 14) {
            contentView.setFitsSystemWindows(true);
        }

        View statusBar = new View(activity);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (hasNotchInScreen(activity)) {
            params.height = getNotchSize(activity)[1];
        } else {
            params.height = getStatusBarHeight();
        }
        params.height = getStatusBarHeight();
        statusBar.setLayoutParams(params);
        statusBar.setBackgroundColor(color);
        contentFrameLayout.addView(statusBar);
    }

    /**
     * 设置状态栏渐变色
     *
     * @param activity activity
     * @param drawable drawable资源文件
     */
    private void addImmersiveShadeStatusBar(Activity activity, @DrawableRes int drawable) {
        ViewGroup contentFrameLayout = activity.findViewById(android.R.id.content);
        View contentView = contentFrameLayout.getChildAt(0);
        if (contentView != null && Build.VERSION.SDK_INT >= 14) {
            contentView.setFitsSystemWindows(true);
        }

        View statusBar = new View(activity);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (hasNotchInScreen(activity)) {
            params.height = getNotchSize(activity)[1];
        } else {
            params.height = getStatusBarHeight();
        }
//        params.height = getStatusBarHeight();
        statusBar.setLayoutParams(params);
        statusBar.setBackgroundResource(drawable);
        contentFrameLayout.addView(statusBar);
    }

    /**
     * 获取状态栏高度
     *
     * @return
     */
    private int getStatusBarHeight() {
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    /**
     * 设置状态栏为透明
     *
     * @param activity
     */
    private void setTranslucentStatus(Activity activity) {
        //******** 5.0以上系统状态栏透明 ********

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 以pt为单位重新计算大小
     */
    public static void resetDensity(Context context, float designWidth) {
        if (context == null) {
            return;
        }
        Point size = new Point();
        ((WindowManager) context.getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getSize(size);
        Resources resources = context.getResources();
        resources.getDisplayMetrics().xdpi = size.x / designWidth * 72f;
        DisplayMetrics metrics = getMetricsOnMIUI(context.getResources());
        if (metrics != null) {
            metrics.xdpi = size.x / designWidth * 72f;
        }
    }

    /**
     * 解决MIUI屏幕适配问题
     *
     * @param resources
     * @return
     */
    private static DisplayMetrics getMetricsOnMIUI(Resources resources) {
        if ("MiuiResources".equals(resources.getClass().getSimpleName()) || "XResources".equals(resources.getClass().getSimpleName())) {
            try {
                Field field = Resources.class.getDeclaredField("mTmpMetrics");
                field.setAccessible(true);
                return (DisplayMetrics) field.get(resources);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public void addActivity(Activity act) {
        if (allActivities == null) {
            allActivities = new HashSet<>();
        }
        allActivities.add(act);
    }

    public void removeActivity(Activity act) {
        if (allActivities != null) {
            allActivities.remove(act);
        }
    }

    public void finishActivity() {
        for (Activity activity : allActivities) {
            if (null != activity) {
                activity.finish();
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * 判断是否是刘海屏  华为手机
     *
     * @param context
     * @return
     */
    public static boolean hasNotchInScreen(Context context) {

        boolean ret = false;

        try {

            ClassLoader cl = context.getClassLoader();

            Class hwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");

            Method get = hwNotchSizeUtil.getMethod("hasNotchInScreen");

            ret = (boolean) get.invoke(hwNotchSizeUtil);

        } catch (ClassNotFoundException e) {

            Log.e("test", "hasNotchInScreen ClassNotFoundException");

        } catch (NoSuchMethodException e) {

            Log.e("test", "hasNotchInScreen NoSuchMethodException");

        } catch (Exception e) {

            Log.e("test", "hasNotchInScreen Exception");

        }
        LogUtil.e("是否刘海屏-->" + ret);
        return ret;

    }

    /**
     * 获取刘海屏尺寸 华为手机
     *
     * @param context
     * @return
     */
    public static int[] getNotchSize(Context context) {

        int[] ret = new int[]{0, 0};

        try {

            ClassLoader cl = context.getClassLoader();

            Class hwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");

            Method get = hwNotchSizeUtil.getMethod("getNotchSize");

            ret = (int[]) get.invoke(hwNotchSizeUtil);

        } catch (ClassNotFoundException e) {

            LogUtil.e("getNotchSize ClassNotFoundException");

        } catch (NoSuchMethodException e) {

            LogUtil.e("getNotchSize NoSuchMethodException");

        } catch (Exception e) {

            LogUtil.e("getNotchSize Exception");

        }
        LogUtil.e("刘海屏尺寸-->" + ret.toString());
        return ret;

    }
}

