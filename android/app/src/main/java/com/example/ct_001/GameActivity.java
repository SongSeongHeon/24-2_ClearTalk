package com.example.ct_001;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    private Toast currentToast; // Toast 중복 방지 변수
    private TextView timerText; // 타이머 텍스트뷰
    private CountDownTimer countDownTimer; // 카운트다운 타이머 변수
    private SpeechRecognizer speechRecognizer; // 음성 인식기 변수
    private TextView resultText; // 음성 인식 결과 텍스트뷰
    private Button startSpeechButton; // 음성 입력 시작 버튼
    private String targetText = "발음할 문장이 여기에 표시됩니다."; // 목표 문장

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game); // activity_game.xml 연결

        // 타이머 텍스트뷰 초기화
        timerText = findViewById(R.id.timerText);

        // 결과 텍스트뷰 초기화
        resultText = findViewById(R.id.resultText);

        // 음성 입력 시작 버튼 초기화
        startSpeechButton = findViewById(R.id.startSpeechButton);

        // 30초 카운트다운 타이머 설정
        startCountdownTimer();

        // 음성 인식 권한 요청
        checkAudioPermission();

        // 음성 입력 시작 버튼 클릭 리스너 설정
        startSpeechButton.setOnClickListener(v -> startSpeechRecognition());

        // 하단 네비게이션 버튼 설정
        setupNavigationButtons();
    }

    private void checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            initializeSpeechRecognizer();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeSpeechRecognizer();
            } else {
                showToast("음성 인식 권한이 필요합니다.");
                finish();
            }
        }
    }

    private void initializeSpeechRecognizer() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                showToast("음성을 입력하세요...");
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String resultTextValue = matches.get(0);
                    resultText.setText("결과: " + resultTextValue);
                    calculateSimilarity(targetText, resultTextValue);
                } else {
                    resultText.setText("음성 인식 결과 없음");
                }
            }

            @Override
            public void onError(int error) {
                showToast("음성 인식 오류: " + getErrorText(error));
            }

            @Override
            public void onBeginningOfSpeech() {}
            @Override
            public void onRmsChanged(float rmsdB) {}
            @Override
            public void onBufferReceived(byte[] buffer) {}
            @Override
            public void onEndOfSpeech() {}
            @Override
            public void onPartialResults(Bundle partialResults) {}
            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        speechRecognizer.startListening(intent);
    }

    private String getErrorText(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_NETWORK:
                return "네트워크 오류";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "일치하는 결과가 없습니다";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "시간 초과";
            default:
                return "알 수 없는 오류";
        }
    }

    private void calculateSimilarity(String target, String input) {
        int editDistance = calculateEditDistance(target, input);
        int maxLength = Math.max(target.length(), input.length());
        int similarity = (int) ((1 - (double) editDistance / maxLength) * 100);
        showToast("일치율: " + similarity + "%");
    }

    private int calculateEditDistance(String str1, String str2) {
        int[][] dp = new int[str1.length() + 1][str2.length() + 1];
        for (int i = 0; i <= str1.length(); i++) {
            for (int j = 0; j <= str2.length(); j++) {
                if (i == 0) dp[i][j] = j;
                else if (j == 0) dp[i][j] = i;
                else if (str1.charAt(i - 1) == str2.charAt(j - 1)) dp[i][j] = dp[i - 1][j - 1];
                else dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
            }
        }
        return dp[str1.length()][str2.length()];
    }

    private void startCountdownTimer() {
        countDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) millisUntilFinished / 1000;
                timerText.setText("남은 시간: " + secondsRemaining + "초");
            }

            @Override
            public void onFinish() {
                timerText.setText("시간 초과!");
                showToast("시간이 초과되었습니다.");
                // 여기서 추가 동작을 수행할 수 있습니다.
            }
        }.start();
    }

    private void setupNavigationButtons() {
        // 버튼 초기화
        Button learnButton = findViewById(R.id.learnButton);
        Button reviewButton = findViewById(R.id.reviewButton);
        Button performanceButton = findViewById(R.id.performanceButton);

        // NullPointerException 방지
        if (learnButton != null && reviewButton != null && performanceButton != null) {
            // 학습 버튼 클릭 이벤트
            learnButton.setOnClickListener(v -> navigateToActivity(MainActivity.class, false));

            // 복습 버튼 클릭 이벤트
            reviewButton.setOnClickListener(v -> navigateToActivity(ReviewActivity.class, true));

            // 성취도 버튼 클릭 이벤트
            performanceButton.setOnClickListener(v -> navigateToActivity(ProgressActivity.class, true));
        } else {
            showToast("네비게이션 버튼 초기화 실패");
        }
    }

    private void navigateToActivity(Class<?> targetActivity, boolean shouldFinish) {
        if (targetActivity.equals(GameActivity.class)) {
            showToast("현재 화면입니다.");
        } else {
            Intent intent = new Intent(GameActivity.this, targetActivity);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // 페이드 인/아웃 효과
            if (shouldFinish) {
                finish(); // 필요 시 현재 Activity 종료
            }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel(); // Activity 종료 시 타이머 취소
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy(); // Activity 종료 시 음성 인식기 해제
        }
    }
}
