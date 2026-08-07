package com.example.ct_001;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameInput, passwordInput;
    private ImageView passwordToggle;
    private Button loginButton;
    private TextView signupLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // 로그인 화면 표시

        // UI 초기화
        initializeUI();

        // 비밀번호 표시/숨김 토글 설정
        setupPasswordToggle();

        // 로그인 버튼 클릭 이벤트 설정
        setupLoginButton();

        // 회원가입 레이블 클릭 이벤트 설정
        setupSignupLabel();
    }

    private void initializeUI() {
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        passwordToggle = findViewById(R.id.password_toggle);
        loginButton = findViewById(R.id.login_button);
        signupLabel = findViewById(R.id.signup_label);

        // 소프트 키보드가 나타날 때 성능 향상을 위해 입력 방식 최적화
        passwordInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    private void setupPasswordToggle() {
        passwordToggle.setOnClickListener(v -> {
            if (passwordInput.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                passwordInput.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordToggle.setImageResource(android.R.drawable.ic_menu_view); // 비밀번호 표시 아이콘
            } else {
                passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordToggle.setImageResource(android.R.drawable.ic_menu_close_clear_cancel); // 비밀번호 숨김 아이콘
            }
            passwordInput.setSelection(passwordInput.getText().length()); // 커서 위치 유지
        });
    }

    private void setupLoginButton() {
        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                showToast("모든 필드를 입력하세요.");
            } else {
                handleLogin(username, password);
            }
        });
    }

    private void handleLogin(String username, String password) {
        if (username.equals("admin") && password.equals("1234")) {
            showToast("로그인 성공!");
            Intent intent = new Intent(this, MainActivity.class); // 로그인 성공 시 MainActivity로 이동
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // 애니메이션 효과
            finish();
        } else {
            showToast("로그인 실패: 잘못된 아이디 또는 비밀번호");
        }
    }

    private void setupSignupLabel() {
        signupLabel.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class); // 회원가입 페이지로 이동
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // 애니메이션 효과
        });
    }

    // Toast 메시지 표시 메서드 (중복 방지)
    private void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }
}
