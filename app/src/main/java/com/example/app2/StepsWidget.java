package com.example.app2;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class StepsWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Itérer sur tous les widgets actifs
        for (int appWidgetId : appWidgetIds) {
            // Créer une RemoteViews pour le layout du widget
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.steps_widget);

            // Mettre à jour la valeur des pas (exemple)
            views.setTextViewText(R.id.steps_value, "1500");

            // Ajouter un PendingIntent pour ouvrir MainActivity en cliquant sur le widget
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.steps_value, pendingIntent);

            // Mettre à jour le widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Mettre à jour le widget après redémarrage
        }
    }

}
