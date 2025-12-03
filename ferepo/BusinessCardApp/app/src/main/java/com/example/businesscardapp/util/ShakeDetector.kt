package com.example.businesscardapp.util

import android.content.Context
import android.hardware.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.sqrt

class ShakeConfig(
    val gForceThreshold: Float = 2.7f,
    val minIntervalMs: Long = 600L
)

fun shakeEvents(context: Context, config: ShakeConfig = ShakeConfig()): Flow<Unit> = callbackFlow {
    val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    if (accel == null) {
        android.util.Log.w("Shake", "No accelerometer: emulator/device doesn't expose it")
        close()    // 수집해도 콜백 안 올 상황이므로 종료
        return@callbackFlow
    }

    var lastShake = 0L
    val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val gX = event.values[0] / SensorManager.GRAVITY_EARTH
            val gY = event.values[1] / SensorManager.GRAVITY_EARTH
            val gZ = event.values[2] / SensorManager.GRAVITY_EARTH
            val gForce = kotlin.math.sqrt(gX * gX + gY * gY + gZ * gZ)

            android.util.Log.d("Shake", "gForce=$gForce (x=$gX y=$gY z=$gZ)")

            val now = System.currentTimeMillis()
            if (gForce > config.gForceThreshold && now - lastShake > config.minIntervalMs) {
                lastShake = now
                trySend(Unit)
                android.util.Log.d("Shake", ">>> SHAKE TRIGGERED!")
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    val registered = sm.registerListener(listener, accel, SensorManager.SENSOR_DELAY_UI)
    android.util.Log.d("Shake", "registerListener(accel!=null=${accel!=null}) -> $registered")

    awaitClose {
        sm.unregisterListener(listener)
        android.util.Log.d("Shake", "listener unregistered")
    }
}
