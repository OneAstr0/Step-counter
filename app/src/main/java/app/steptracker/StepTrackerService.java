package app.steptracker;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

public class StepTrackerService extends Service {
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int totalSteps = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        // Получаем экземпляр SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Получаем шагомер (STEP_COUNTER)
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Регистрируем слушателя для шагомера
        if (stepSensor != null) {
            sensorManager.registerListener(stepListener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        // Установите флаг START_STICKY, чтобы служба была автоматически перезапущена, если она будет остановлена системой
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Отменяем регистрацию слушателя при остановке службы
        if (stepSensor != null) {
            sensorManager.unregisterListener(stepListener);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Не требуется реализация
        return null;
    }

    private SensorEventListener stepListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                int steps = (int) event.values[0];
                if (totalSteps == 0) {
                    // Если это первое значение, запоминаем его как начальное
                    totalSteps = steps;
                    saveTotalSteps(totalSteps);
                }
                int todaySteps = steps - totalSteps;
                // Здесь вы можете обновить пользовательский интерфейс или сохранить шаги в базу данных, если необходимо
                updateStepsCount(todaySteps);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Не требуется реализация
        }
    };

    private void saveTotalSteps(int steps) {
        SharedPreferences preferences = getSharedPreferences("StepTrackerPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("totalSteps", steps);
        editor.apply();
    }

    private int loadTotalSteps() {
        SharedPreferences preferences = getSharedPreferences("StepTrackerPrefs", MODE_PRIVATE);
        return preferences.getInt("totalSteps", 0);
    }

    private void updateStepsCount(int steps) {
        // Обновление количества шагов в пользовательском интерфейсе или базе данных
    }
}

