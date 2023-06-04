package app.steptracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedImageDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private TextView stepsCount;
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int totalSteps = 0;
    private ImageView steps_image;


    private ImageView gifImageView;
    private AnimatedImageDrawable animatedGif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_main);

        gifImageView = findViewById(R.id.gif);
        animatedGif = (AnimatedImageDrawable) gifImageView.getDrawable();



        stepsCount = findViewById(R.id.steps);
        steps_image = findViewById(R.id.steps_image);

        // Получаем экземпляр SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Получаем шагомер (STEP_COUNTER)
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        // Проверяем, поддерживается ли шагомер на устройстве
        if (stepSensor == null) {
            stepsCount.setText("Шагомер недоступен!");
            // steps_image.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        animatedGif.start();

        // Запуск службы StepTrackerService
        Intent serviceIntent = new Intent(this, StepTrackerService.class);
        startService(serviceIntent);

        // Загрузка сохраненного количества шагов
        totalSteps = loadTotalSteps();
        int todaySteps = calculateTodaySteps(totalSteps);
        stepsCount.setText("Шагов сегодня: " + todaySteps);
    }

    private int calculateTodaySteps(int totalSteps) {
        // Получаем текущую дату
        Calendar calendar = Calendar.getInstance();
        int currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

        // Получаем сохраненную дату последнего обновления шагов
        SharedPreferences preferences = getSharedPreferences("StepTrackerPrefs", MODE_PRIVATE);
        int lastUpdatedDayOfYear = preferences.getInt("lastUpdatedDayOfYear", -1);

        // Если это первый запуск или день изменился, обнуляем сохраненные шаги и обновляем дату
        if (lastUpdatedDayOfYear == -1 || lastUpdatedDayOfYear != currentDayOfYear) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("lastUpdatedDayOfYear", currentDayOfYear);
            editor.putInt("todaySteps", 0);
            editor.apply();

            return 0; // Возвращаем 0 шагов для нового дня
        }

        // Получаем сохраненное количество шагов за сегодня
        int todaySteps = preferences.getInt("todaySteps", 0);

        // Возвращаем количество шагов за сегодня (разницу между общим количеством шагов и сохраненным количеством шагов за сегодня)
        return totalSteps - todaySteps;
    }


    @Override
    protected void onPause() {
        super.onPause();
        animatedGif.stop();

        // Остановка службы StepTrackerService
        Intent serviceIntent = new Intent(this, StepTrackerService.class);
        stopService(serviceIntent);

        // Сохранение количества шагов
        saveTotalSteps(totalSteps);
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
        // Предполагается, что у вас есть ссылка на TextView для отображения количества шагов
        TextView stepsCountTextView = findViewById(R.id.steps);

        // Обновляем текстовое поле с количеством шагов
        stepsCountTextView.setText("Шагов сегодня: " + steps);
    }


}
