package com.example.ct_001;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LearningNextPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning_1); // 첫 번째 레이아웃

        // 버튼 클릭 이벤트 설정
        Button choiceButton1 = findViewById(R.id.choiceButton1);
        Button choiceButton2 = findViewById(R.id.choiceButton2);
        Button choiceButton3 = findViewById(R.id.choiceButton3);

        // 버튼 클릭 시 두 번째 페이지로 이동
        View.OnClickListener listener = v -> {
            Intent intent = new Intent(LearningNextPage.this, LearningActivity_2.class); // 두 번째 Activity로 이동
            startActivity(intent); // Activity 시작
        };

        choiceButton1.setOnClickListener(listener);
        choiceButton2.setOnClickListener(listener);
        choiceButton3.setOnClickListener(listener);
    }
}
