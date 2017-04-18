package org.standardnotes.notes.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.joda.time.format.DateTimeFormat;
import org.standardnotes.notes.R;
import org.standardnotes.notes.comms.data.Note;
import org.standardnotes.notes.frag.NoteListFragment;
import org.standardnotes.notes.store.NoteStore;

import java.util.ArrayList;
import java.util.List;

import static org.standardnotes.notes.frag.NoteListFragment.EXTRA_X_COOR;
import static org.standardnotes.notes.frag.NoteListFragment.EXTRA_Y_COOR;

public class AppWidgetRefreshService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        return (new ListProvider(this.getApplicationContext(), intent));
    }


    public class ListProvider implements RemoteViewsFactory {
        private ArrayList<Note> noteList = new ArrayList();
        private Context context = null;
        private int appWidgetId;

        public ListProvider(Context context, Intent intent) {
            this.context = context;
            appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            loadNotes();
        }

        private void loadNotes() {
            noteList.clear();
            NoteStore ns = new NoteStore();
            List<Note> allNotes = ns.getAllNotes();

            int limit = 0;
            //number of notes to display. should be changeable in settings.
            int maxlimit = 10;
            for (Note n : allNotes) {
                noteList.add(n);
                if (++limit >= maxlimit)
                    return;
            }
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            loadNotes();
        }

        @Override
        public void onDestroy() {
        }

        @Override
        public int getCount() {
            return noteList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            final RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.item_note);
            Note listItem = noteList.get(position);

            //sett all values for the note
            String title = listItem.getTitle();
            String Text = abbreviate(listItem.getText(), 256);
            String UUID = listItem.getUuid();
            remoteView.setTextViewText(R.id.title, title);
            remoteView.setTextViewText(R.id.text, Text);
            remoteView.setTextViewText(R.id.date, DateTimeFormat.shortDateTime().print(listItem.getUpdatedAt()));
            remoteView.setViewVisibility(R.id.synced, (listItem.getDirty() ? View.VISIBLE : View.INVISIBLE));

            //start App with clicked Note
            Intent fillInIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putString(NoteListFragment.EXTRA_NOTE_ID, UUID);
            bundle.putInt(NoteListFragment.EXTRA_X_COOR, 0);
            bundle.putInt(NoteListFragment.EXTRA_Y_COOR, 0);
            fillInIntent.putExtras(bundle);
            remoteView.setOnClickFillInIntent(R.id.item_note_row, fillInIntent);

            return remoteView;
        }

        private String abbreviate(String str, int lenght) {
            //reduce lenght of string
            str = str.substring(0, Math.min(lenght, str.length()));
            str.replace('\n', ' ');
            return str;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }
    }
}
