package com.example.ct_001;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class LearningActivity_2 extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    private TextView resultTextView;
    private TextView targetTextView; // 문제 텍스트 표시용 TextView
    private Button startButton;
    private SpeechRecognizer speechRecognizer;
    private String targetText = ""; // 초기값 비워둠

    private FirebaseFirestore firestore;
    private Toast currentToast; // Toast 중복 방지 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning_2); // 학습 화면 레이아웃 연결

        // Firestore 초기화
        firestore = FirebaseFirestore.getInstance();

        // 학습 관련 View 초기화
        resultTextView = findViewById(R.id.result_text);
        targetTextView = findViewById(R.id.target_text); // TextView에 문제 텍스트 표시
        startButton = findViewById(R.id.start_button);

        if (resultTextView == null || targetTextView == null || startButton == null) {
            showToast("필수 요소가 초기화되지 않았습니다.");
            finish();
            return;
        }

        // LearningActivity_1에서 전달된 keyword 가져오기
        String keyword = getIntent().getStringExtra("keyword");
        if (keyword != null && !keyword.isEmpty()) {
            targetText = keyword; // targetText를 전달받은 keyword로 설정
        } else {
            targetText = "기본 문제 텍스트"; // 기본 텍스트 설정
        }

        // targetText를 TextView에 표시
        targetTextView.setText(targetText);

        checkAudioPermission();

        initializeSpeechRecognizer();

        startButton.setOnClickListener(v -> startSpeechRecognition());

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

    private void showToast(String message) {
        if (currentToast != null) {
            currentToast.cancel();
        }
        currentToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        currentToast.show();
    }

    private void setupNavigationButtons() {
        Button learnButton = findViewById(R.id.learnButton);
        Button reviewButton = findViewById(R.id.reviewButton);
        Button performanceButton = findViewById(R.id.performanceButton);

        // 학습 버튼 클릭 이벤트
        learnButton.setOnClickListener(v -> {
            Intent intent = new Intent(LearningActivity_2.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // 페이드 인/아웃 효과
            finish();
        });

        // 복습 버튼 클릭 이벤트
        reviewButton.setOnClickListener(v -> {
            Intent intent = new Intent(LearningActivity_2.this, ReviewActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // 페이드 인/아웃 효과
            finish();
        });

        // 성취도 버튼 클릭 이벤트
        performanceButton.setOnClickListener(v -> {
            Intent intent = new Intent(LearningActivity_2.this, ProgressActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // 페이드 인/아웃 효과
            finish();
        });
    }

    private void initializeSpeechRecognizer() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Toast.makeText(LearningActivity_2.this, "음성을 입력하세요...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String resultText = matches.get(0);
                    resultTextView.setText(resultText);
                    compareResults(resultText);
                } else {
                    resultTextView.setText("음성 인식 결과 없음");
                }
            }

            @Override
            public void onError(int error) {
                Toast.makeText(LearningActivity_2.this, "음성 인식 오류: " + error, Toast.LENGTH_SHORT).show();
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

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        speechRecognizer.startListening(intent);
    }

    private void compareResults(String resultText) {
        int similarity = calculateSimilarity(targetText, resultText);
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        saveLearningData("user123", currentDate, similarity); // 사용자 데이터 저장

        // Keywords 컬렉션의 rate 업데이트
        updateRateInFirestore(targetText, similarity);

        Toast.makeText(this, "일치율: " + similarity + "%", Toast.LENGTH_LONG).show();
    }

    private void updateRateInFirestore(String keyword, int similarity) {
        firestore.collection("Keywords")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        List<Map<String, Object>> subcategories = (List<Map<String, Object>>) document.get("subcategories");

                        if (subcategories != null) {
                            for (Map<String, Object> subcategory : subcategories) {
                                if (subcategory.containsKey("name") && keyword.equals(subcategory.get("name"))) {
                                    subcategory.put("rate", similarity); // rate 필드 업데이트

                                    // Firestore에 서브카테고리 업데이트
                                    firestore.collection("Keywords")
                                            .document(document.getId())
                                            .update("subcategories", subcategories)
                                            .addOnSuccessListener(aVoid ->
                                                    Toast.makeText(this, "Firestore에 일치율 저장 성공", Toast.LENGTH_SHORT).show())
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(this, "Firestore에 일치율 저장 실패", Toast.LENGTH_SHORT).show());
                                    return; // 작업 완료 후 메서드 종료
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Firestore에서 데이터 가져오기 실패", Toast.LENGTH_SHORT).show());
    }

    private int calculateSimilarity(String target, String input) {
        int editDistance = calculateEditDistance(target, input);
        int maxLength = Math.max(target.length(), input.length());
        return (int) ((1 - (double) editDistance / maxLength) * 100);
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

    private void saveLearningData(String userId, String timestamp, int accuracy) {
        Map<String, Object> learningData = new HashMap<>();
        learningData.put("timestamp", timestamp);
        learningData.put("accuracy", accuracy);

        firestore.collection("users")
                .document(userId)
                .collection("learning_data")
                .add(learningData)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(this, "데이터 저장 성공", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "데이터 저장 실패", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}
