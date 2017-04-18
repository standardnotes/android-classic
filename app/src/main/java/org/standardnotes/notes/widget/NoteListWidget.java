package org.standardnotes.notes.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;

import org.standardnotes.notes.NoteActivity;
import org.standardnotes.notes.R;
import org.standardnotes.notes.StarterActivity;


public class NoteListWidget extends AppWidgetProvider {

    static RemoteViews updateAppWidget(Context context,
                                int appWidgetId) {

        //which layout to show on widget
        RemoteViews remoteViews = new RemoteViews(
                context.getPackageName(),R.layout.widget_note_list);

        //RemoteViews Service needed to provide adapter for ListView
        Intent svcIntent = new Intent(context.getApplicationContext(), AppWidgetRefreshService.class);
        //passing app widget id to that RemoteViews Service
        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        //setting a unique Uri to the intent
        //don't know its purpose to me right now
        svcIntent.setData(Uri.parse(
                svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
        //setting adapter to listview of the widget
        remoteViews.setRemoteAdapter( R.id.noteListView,
                svcIntent);
        //setting an empty view in case of no data
        remoteViews.setEmptyView(R.id.noteListView, R.id.empty_view);

        //intent for App Start Button
        Intent intentstart = new Intent(context, StarterActivity.class);
        PendingIntent pendingIntentstart = PendingIntent.getActivity(context, 0, intentstart, 0);
        remoteViews.setOnClickPendingIntent(R.id.start_app, pendingIntentstart);

        //Intent for new Note Button
        Intent intentnew = new Intent(context, NoteActivity.class);
        intentnew.setAction(Intent.ACTION_SEND);
        intentnew.setType("text/plain");
        Bundle bundle = new Bundle();
        intentnew.putExtras(bundle);
        int iUniqueId = (int) (System.currentTimeMillis() & 0xfffffff);
        PendingIntent pendingIntentnew = PendingIntent.getActivity(context,iUniqueId, intentnew, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.new_note, pendingIntentnew);

        //Template Intent for each entry in list
        Intent startActivityIntent = new Intent(context, NoteActivity.class);
        startActivityIntent.setAction(Intent.ACTION_SEND);
        startActivityIntent.setType("text/plain");
        startActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Close previous note activity if already open
        PendingIntent startActivityPendingIntent = PendingIntent.getActivity(context, iUniqueId+10, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(R.id.noteListView, startActivityPendingIntent);

        return remoteViews;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; ++i) {
            RemoteViews remoteViews = updateAppWidget(context, appWidgetIds[i]);
            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
        }
        //update entrys
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,  R.id.noteListView);
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

