package com.spc.nutricoach.workout

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.spc.nutricoach.MainActivity
import com.spc.nutricoach.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

class WorkoutService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    companion object {
        const val CHANNEL_ID = "workout_channel"
        const val NOTIFICATION_ID = 1

        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_FINISH_SET = "ACTION_FINISH_SET"
        const val ACTION_SKIP_REST = "ACTION_SKIP_REST"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        combine(
            WorkoutManager.diaActual,
            WorkoutManager.currentExerciseIndex,
            WorkoutManager.currentSet,
            WorkoutManager.isResting,
            WorkoutManager.restTimeRemaining,
            WorkoutManager.isFinished
        ) { args ->
            val dia = args[0] as com.spc.nutricoach.model.Dia?
            val exerciseIndex = args[1] as Int
            val currentSet = args[2] as Int
            val isResting = args[3] as Boolean
            val restTimeRemaining = args[4] as Int
            val isFinished = args[5] as Boolean

            if (dia == null || isFinished) {
                stopSelf()
                return@combine
            }

            dia.ejercicios.getOrNull(exerciseIndex) ?: return@combine
            
            val notification = buildNotification(
                dia = dia,
                exerciseIndex = exerciseIndex,
                currentSet = currentSet,
                isResting = isResting,
                restTimeRemaining = restTimeRemaining
            )
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)

        }.launchIn(serviceScope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val notification = buildNotification(
                    dia = WorkoutManager.diaActual.value,
                    exerciseIndex = WorkoutManager.currentExerciseIndex.value,
                    currentSet = WorkoutManager.currentSet.value,
                    isResting = WorkoutManager.isResting.value,
                    restTimeRemaining = WorkoutManager.restTimeRemaining.value
                )
                startForeground(NOTIFICATION_ID, notification)
            }
            ACTION_FINISH_SET -> WorkoutManager.finishSet()
            ACTION_SKIP_REST -> WorkoutManager.skipRest()
            ACTION_STOP -> {
                WorkoutManager.stop()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun buildNotification(
        dia: com.spc.nutricoach.model.Dia?,
        exerciseIndex: Int,
        currentSet: Int,
        isResting: Boolean,
        restTimeRemaining: Int
    ): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = "ACTION_NAVIGATE_TO_WORKOUT"
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val currentExercise = dia?.ejercicios?.getOrNull(exerciseIndex)
        val exerciseName = currentExercise?.nombreSnapshot ?: "NutriCoach Workout"

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setColor(0xFF00E676.toInt())
            .setContentTitle(exerciseName)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setShowWhen(false)

        if (isResting) {
            builder.setContentText("Descansando: ${formatTime(restTimeRemaining)}")
            val totalRest = currentExercise?.descanso?.toIntOrNull() ?: 60
            builder.setProgress(100, if (totalRest > 0) ((totalRest - restTimeRemaining) * 100 / totalRest) else 0, false)
            
            val skipIntent = Intent(this, WorkoutService::class.java).apply { action = ACTION_SKIP_REST }
            val skipPI = PendingIntent.getService(this, 1, skipIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(R.drawable.ic_notification_next, "Saltar Descanso", skipPI)
        } else {
            val totalSets = currentExercise?.series ?: 1
            builder.setContentText("Serie $currentSet de $totalSets")
            builder.setProgress(100, if (totalSets > 0) (currentSet * 100 / totalSets) else 0, false)
            
            val finishIntent = Intent(this, WorkoutService::class.java).apply { action = ACTION_FINISH_SET }
            val finishPI = PendingIntent.getService(this, 2, finishIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(R.drawable.ic_notification_finish, "Terminar Serie", finishPI)
        }

        val stopIntent = Intent(this, WorkoutService::class.java).apply { action = ACTION_STOP }
        val stopPI = PendingIntent.getService(this, 3, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        builder.addAction(R.drawable.ic_notification_stop, "Detener", stopPI)

        return builder.build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Entrenamiento Activo",
            NotificationManager.IMPORTANCE_LOW 
        ).apply {
            description = "Muestra el progreso del entrenamiento y temporizador de descanso"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    private fun formatTime(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format(java.util.Locale.getDefault(), "%02d:%02d", mins, secs)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
