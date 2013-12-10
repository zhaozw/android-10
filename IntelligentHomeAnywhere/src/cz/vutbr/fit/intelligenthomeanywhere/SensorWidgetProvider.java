package cz.vutbr.fit.intelligenthomeanywhere;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.device.BaseDevice;

public class SensorWidgetProvider extends AppWidgetProvider {
	
	private static final String TAG = SensorWidgetProvider.class.getSimpleName();

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// new widget has been instantiated, the requested update interval having lapsed, or the system booting
		Log.d(TAG, "onUpdate()");

		WidgetUpdateService.startUpdating(context, appWidgetIds);
	}

	@Override
    public void onDeleted(Context context, int[] appWidgetIds) {
		// some widget is deleted
		Log.d(TAG, "onDeleted()");
		super.onDeleted(context, appWidgetIds);
		
		// TODO: delete widget settings?
    }

    @Override
    public void onDisabled(Context context) {
    	// last widget is deleted
    	Log.d(TAG, "onDisabled()");
    	super.onDisabled(context);

    	WidgetUpdateService.stopUpdating(context);
    }

    @Override
    public void onEnabled(Context context) {
    	// first widget is created
    	Log.d(TAG, "onEnabled()");
        super.onEnabled(context);
    }
    
    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onAppWidgetOptionsChanged(Context context,
    		AppWidgetManager appWidgetManager, int appWidgetId,
    		Bundle newOptions) {
    	// widget has changed size
    	Log.d(TAG, "onAppWidgetOptionsChanged()");
    	super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

    	int min_width = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
    	int max_width = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
    	int min_height = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
    	int max_height = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
    	Log.d(TAG, "[" + min_width + "-" + max_width + "] x [" + min_height + "-" + max_height + "]");

    	// workaround for Galaxy S3 and similar on 4.1.2
    	// http://stackoverflow.com/questions/17396045/how-to-catch-widget-size-changes-on-devices-where-onappwidgetoptionschanged-not
    	
    	// TODO: determine proper layout based on widget size and keep his name in widget settings

    	// force update widget
    	context.startService(WidgetUpdateService.getForceUpdateIntent(context, appWidgetId));
    } 
    
    public void updateWidget(Context context, int widgetId, BaseDevice device) {
    	//Log.d(TAG, "updateWidget()");
    	AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_sensor);
        
        remoteViews.setImageViewResource(R.id.icon, device.getTypeIconResource());
		remoteViews.setTextViewText(R.id.name, device.getName());
		remoteViews.setTextViewText(R.id.value, device.getStringValueUnit(context));
		
		// register an onClickListener
		PendingIntent pendingIntent;
		
		// TODO: remove when block below will be working
		pendingIntent = WidgetUpdateService.getForceUpdatePendingIntent(context, widgetId);

		/* TODO: this will crash as application is not ready for this
		Intent intent = new Intent(context, SensorDetailActivity.class);
		intent.putExtra(Constants.DEVICE_CLICKED, device.getName());
		pendingIntent = PendingIntent.getActivity(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		*/
		
		remoteViews.setOnClickPendingIntent(R.id.layout, pendingIntent);

		// request widget redraw
		appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }
    
}