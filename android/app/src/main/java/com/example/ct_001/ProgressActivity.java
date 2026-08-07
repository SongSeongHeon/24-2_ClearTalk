package com.example.ct_001;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProgressActivity extends AppCompatActivity {

    private Toast currentToast; // Toast 중복 방지 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress); // 성취도 화면 레이아웃 연결

        // 네비게이션 버튼 설정
        setupNavigationButtons();
    }

    private void setupNavigationButtons() {
        setupNavigationButton(R.id.learnButton, MainActivity.class, false);
        setupNavigationButton(R.id.reviewButton, ReviewActivity.class, false);
        setupNavigationButton(R.id.performanceButton, null, true);
    }

    private void setupNavigationButton(int buttonId, Class<?> targetActivity, boolean isCurrent) {
        Button button = findViewById(buttonId);
        if (button != null) {
            button.setOnClickListener(v -> {
                if (isCurrent) {
                    showToast("현재 화면입니다.");
                } else {
                    if (targetActivity != null) {
                        Intent intent = new Intent(ProgressActivity.this, targetActivity);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // 페이드 인/아웃 효과
                    }
                }
            });
        }
    }

    // Toast 메시지 표시 메서드
    private void showToast(String message) {
        if (currentToast != null) {
            currentToast.cancel(); // 기존 Toast 취소
        }
        currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        currentToast.show();
    }
}
