package com.panda.keyboards.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.panda.keyboards.R
import com.panda.keyboards.ui.MainActivity

/**
 * [AppWidgetProvider] implementing the "Quick Font Copy" home-screen widget.
 *
 * Displays a styled Panda Keyboards shortcut card with one-tap access
 * to open the main app.
 */
class QuickFontWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val views = RemoteViews(context.packageName, R.layout.widget_quick_font).apply {
                setOnClickPendingIntent(R.id.widget_container, pendingIntent)
                setTextViewText(R.id.widget_title, "🐼 Panda Keyboards")
                setTextViewText(R.id.widget_subtitle, "𝕱𝖆𝖓𝖈𝖞 𝕱𝖔𝖓𝖙𝖘 & 𝕿𝖍𝖊𝖒𝖊𝖘")
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
