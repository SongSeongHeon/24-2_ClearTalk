package com.example.ct_001;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign); // 회원가입 화면 표시

        // 가입 취소 버튼 설정
        setupCancelButton();

        // 가입하기 버튼 설정
        setupSignupButton();
    }

    private void setupCancelButton() {
        Button cancelButton = findViewById(R.id.button_container).findViewById(R.id.cancel_signup_button);
        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // 페이드 인/아웃 효과
                finish();
            });
        }
    }

    private void setupSignupButton() {
        Button signupButton = findViewById(R.id.button_container).findViewById(R.id.submit_signup_button);
        if (signupButton != null) {
            signupButton.setOnClickListener(v -> {
                // 여기에 추가적인 회원가입 처리 로직을 넣을 수 있습니다 (e.g., 유효성 검사, 데이터베이스에 사용자 정보 저장 등).
                // 현재는 회원가입 성공 Toast만 표시하고 로그인 화면으로 돌아갑니다.
                showToast("회원가입 성공!");
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // 페이드 인/아웃 효과
                finish();
            });
        }
    }

    // Toast 메시지 표시 메서드 (중복 방지)
    private void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }
}