package com.example.ct_001;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView; // TextView 클래스 추가

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.HashMap;
import java.util.Locale;
import android.util.Log;
import java.util.ArrayList;  // ArrayList를 사용하려면 임포트 필요
import java.util.List;
import java.util.Map;


public class LearningActivity_1 extends AppCompatActivity {

    private Toast currentToast; // Toast 중복 방지 변수
    private TextView feedbackText; // 피드백 텍스트뷰 변수 선언

    private FirebaseFirestore firestore; // Firestore 초기화
    private Button choiceButton1, choiceButton2, choiceButton3; // 정답 버튼
    private ImageView saveImageButton; // 즐겨찾기 이미지 버튼
    private String keyword; // MainActivity에서 전달된 키워드
    private String selectedCategory; // selectedCategory 변수를 클래스 수준에서 선언
    private boolean isFavorite = false; // 즐겨찾기 상태
    private TextToSpeech textToSpeech; // TextToSpeech 객체
    private boolean isTextToSpeechInitialized = false; // TextToSpeech 초기화 상태

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning_1);

        // 하단 네비게이션 버튼 설정
        setupNavigationButtons();

        // 정답 버튼 클릭 이벤트 설정
        setupAnswerButtons();

        // Firestore 인스턴스 생성
        firestore = FirebaseFirestore.getInstance();  // Firestore 인스턴스 가져오기
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        if (firestore != null) {
            Log.d("Firestore", "Firestore 인스턴스 초기화 성공");
        } else {
            Log.e("Firestore", "Firestore 인스턴스 초기화 실패");
        }

        if (firestore == null) {
            Log.e("Firestore", "Firestore 인스턴스가 null입니다.");
        } else {
            Log.d("Firestore", "Firestore 인스턴스가 정상적으로 초기화되었습니다.");
        }

        // 버튼 초기화
        choiceButton1 = findViewById(R.id.choiceButton1);
        choiceButton2 = findViewById(R.id.choiceButton2);
        choiceButton3 = findViewById(R.id.choiceButton3);

        // TextView 초기화
        feedbackText = findViewById(R.id.feedbackText);

        // ** TextView 초기 상태 설정 (빈칸) **
        if (feedbackText != null) {
            feedbackText.setText(""); // 빈 문자열로 초기화
        }

        // 정답 버튼 클릭 이벤트 설정
        setupAnswerButtons();

        // 이미지 버튼 초기화
        saveImageButton = findViewById(R.id.favoriteButton);

        // TextToSpeech 객체 초기화
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                // 한국어 설정
                int langResult = textToSpeech.setLanguage(Locale.KOREAN);
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "TextToSpeech 언어가 지원되지 않습니다.", Toast.LENGTH_SHORT).show();
                    // TTS 데이터 설치 요청
                    Intent installIntent = new Intent();
                    installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivityForResult(installIntent, 1);
                } else {
                    isTextToSpeechInitialized = true;
                    Toast.makeText(this, "TextToSpeech 초기화 성공", Toast.LENGTH_SHORT).show();
                    // 화면이 열릴 때 자동으로 키워드 발음
                    speakKeyword(keyword);
                }
            } else {
                Toast.makeText(this, "TextToSpeech 초기화 실패", Toast.LENGTH_SHORT).show();
            }
        });

        // UtteranceProgressListener 설정
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                runOnUiThread(() -> Toast.makeText(LearningActivity_1.this, "발음 시작", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onDone(String utteranceId) {
                runOnUiThread(() -> Toast.makeText(LearningActivity_1.this, "발음 완료", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(String utteranceId) {
                runOnUiThread(() -> Toast.makeText(LearningActivity_1.this, "발음 오류 발생", Toast.LENGTH_SHORT).show());
            }
        });

        // 이미지 버튼 초기 상태 설정 (색칠되지 않은 별)
        saveImageButton.setImageResource(android.R.drawable.btn_star_big_off);

        // MainActivity에서 전달된 키워드 가져오기
        keyword = getIntent().getStringExtra("keyword");

        // 키워드에 맞는 데이터를 Firestore에서 가져와 버튼 텍스트 설정
        if (keyword != null) {
            setupAnswerButtonsFromFirestore(keyword);

            // Firestore에서 초기 상태 확인 후 즐겨찾기 아이콘 설정
            checkFavoriteStatus(keyword);

            // 즐겨찾기 버튼 클릭 이벤트 설정
            setupImageButtonListener(keyword);
        } else {
            Toast.makeText(this, "키워드가 전달되지 않았습니다.", Toast.LENGTH_SHORT).show();
        }

        // Intent로 전달된 데이터 받기
        Intent intent = getIntent();
        selectedCategory = intent.getStringExtra("category");  // 'category' 값 받기

    }

    private void setupAnswerButtons() {
        // 정답 버튼 초기화
        choiceButton1 = findViewById(R.id.choiceButton1);
        choiceButton2 = findViewById(R.id.choiceButton2);
        choiceButton3 = findViewById(R.id.choiceButton3);

        // NullPointerException 방지
        if (choiceButton1 != null && choiceButton2 != null && choiceButton3 != null) {
            // 버튼별 클릭 이벤트 리스너 설정
            choiceButton1.setOnClickListener(v -> navigateToLearningActivity2(choiceButton1.getText().toString()));
            choiceButton2.setOnClickListener(v -> navigateToLearningActivity2(choiceButton2.getText().toString()));
            choiceButton3.setOnClickListener(v -> navigateToLearningActivity2(choiceButton3.getText().toString()));
        } else {
            showToast("정답 버튼 초기화 실패");
        }
    }


    private void navigateToLearningActivity2(String keyword) {
        Intent intent = new Intent(LearningActivity_1.this, LearningActivity_2.class);
        intent.putExtra("keyword", keyword); // keyword를 Intent에 추가
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // 화면 전환 애니메이션
    }

    private void checkAnswerAndProceed(String buttonText) {
        // 탐색할 도큐먼트 리스트
        String[] documentNames = {"finance", "lifestyle", "food", "work"};

        // 정답 여부를 확인하기 위한 변수
        boolean[] isCorrect = {false};

        for (String documentName : documentNames) {
            firestore.collection("Keywords")
                    .document(documentName)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<Map<String, Object>> subcategories = (List<Map<String, Object>>) documentSnapshot.get("subcategories");

                            if (subcategories != null && !subcategories.isEmpty()) {
                                for (Map<String, Object> subcategory : subcategories) {
                                    if (subcategory.containsKey("name") && subcategory.get("name").equals(buttonText)) {
                                        // 정답 확인
                                        isCorrect[0] = true;
                                        navigateToActivity(LearningActivity_2.class, true); // 다음 페이지로 이동
                                        return;
                                    }
                                }
                            }
                        }

                        // 마지막 도큐먼트까지 확인한 후에도 정답이 아닌 경우
                        if (!isCorrect[0]) {
                            showFeedback("오답입니다."); // 오답 메시지 표시
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "데이터 가져오기 실패: " + e.getMessage());
                        showFeedback("오류 발생. 다시 시도하세요."); // 오류 메시지 표시
                    });
        }
    }

    // 동적 피드백 업데이트 메서드
    private void showFeedback(String message) {
        if (feedbackText != null) {
            feedbackText.setText(message); // TextView에 메시지 설정
            feedbackText.setTextColor(getResources().getColor(android.R.color.holo_red_dark)); // 텍스트 색상 변경
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

    private void navigateToActivity(Class<?> targetActivity, boolean shouldFinish) {
        Intent intent = new Intent(LearningActivity_1.this, targetActivity);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // 페이드 인/아웃 효과
        if (shouldFinish) {
            finish(); // 필요 시 현재 Activity 종료
        }
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

    private void setupAnswerButtonsFromFirestore(String keyword) {
        // 탐색할 도큐먼트 리스트
        String[] documentNames = {"finance", "lifestyle", "food", "work"};

        // 데이터를 저장할 리스트
        List<String> collectedData = new ArrayList<>();

        // 비동기 작업 완료 추적 변수
        int[] tasksCompleted = {0};

        for (String documentName : documentNames) {
            firestore.collection("Keywords")
                    .document(documentName)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Log.d("Firestore", documentName + " 문서가 존재합니다.");
                            List<Map<String, Object>> subcategories = (List<Map<String, Object>>) documentSnapshot.get("subcategories");

                            if (subcategories != null && !subcategories.isEmpty()) {
                                for (Map<String, Object> subcategory : subcategories) {
                                    if (subcategory.containsKey("name") && subcategory.get("name").equals(keyword)) {
                                        List<String> categoryPronunciations = (List<String>) subcategory.get("pronunciations");

                                        if (categoryPronunciations != null && !categoryPronunciations.isEmpty()) {
                                            collectedData.addAll(categoryPronunciations.subList(0, Math.min(3, categoryPronunciations.size())));
                                        } else {
                                            Log.e("Firestore", "pronunciations 필드가 비어있거나 없음: " + subcategory);
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.e("Firestore", documentName + " 문서가 존재하지 않습니다.");
                        }

                        // 작업 완료 추적
                        tasksCompleted[0]++;
                        if (tasksCompleted[0] == documentNames.length) {
                            // 모든 문서의 데이터를 가져온 후에 버튼 업데이트
                            while (collectedData.size() < 3) {
                                collectedData.add("데이터 부족");
                            }
                            updateAnswerButtons(collectedData);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "데이터 가져오기 실패: " + e.getMessage());
                        Toast.makeText(this, "Firestore 데이터 가져오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                        // 작업 완료 추적
                        tasksCompleted[0]++;
                        if (tasksCompleted[0] == documentNames.length) {
                            // 실패한 경우에도 버튼 업데이트
                            while (collectedData.size() < 3) {
                                collectedData.add("데이터 부족");
                            }
                            updateAnswerButtons(collectedData);
                        }
                    });
        }
    }

    private void updateAnswerButtons(List<String> collectedData) {
        // 버튼 텍스트 업데이트
        choiceButton1.setText(collectedData.get(0));
        choiceButton2.setText(collectedData.get(1));
        choiceButton3.setText(collectedData.get(2));
    }

    private void setupImageButtonListener(String keyword) {
        saveImageButton.setOnClickListener(v -> {
            // 탐색할 도큐먼트 리스트
            String[] documentNames = {"finance", "lifestyle", "food", "work"};

            // 비동기 작업 완료 추적 변수
            int[] tasksCompleted = {0};
            boolean[] isUpdated = {false}; // 업데이트 여부 확인

            for (String documentName : documentNames) {
                firestore.collection("Keywords")
                        .document(documentName)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                List<Map<String, Object>> subcategories = (List<Map<String, Object>>) documentSnapshot.get("subcategories");

                                if (subcategories != null && !subcategories.isEmpty()) {
                                    for (Map<String, Object> subcategory : subcategories) {
                                        if (subcategory.containsKey("name") && subcategory.get("name").equals(keyword)) {
                                            // "isFavorite" 필드 가져오기
                                            Boolean currentFavorite = (Boolean) subcategory.get("isFavorite");
                                            boolean newFavorite = (currentFavorite != null) && !currentFavorite;

                                            // Firestore에서 "isFavorite" 값 업데이트
                                            firestore.collection("Keywords")
                                                    .document(documentName)
                                                    .update("subcategories",
                                                            FieldValue.arrayRemove(subcategory)) // 기존 서브카테고리 제거
                                                    .addOnSuccessListener(aVoid -> {
                                                        subcategory.put("isFavorite", newFavorite); // 값 변경
                                                        firestore.collection("Keywords")
                                                                .document(documentName)
                                                                .update("subcategories",
                                                                        FieldValue.arrayUnion(subcategory)) // 수정된 서브카테고리 추가
                                                                .addOnSuccessListener(updateVoid -> {
                                                                    isUpdated[0] = true;
                                                                    Toast.makeText(this, "즐겨찾기 상태 변경 성공!", Toast.LENGTH_SHORT).show();
                                                                    updateFavoriteIcon(newFavorite); // 아이콘 업데이트
                                                                })
                                                                .addOnFailureListener(err -> {
                                                                    Log.e("Firestore", "즐겨찾기 상태 변경 실패: " + err.getMessage());
                                                                    Toast.makeText(this, "즐겨찾기 상태 변경 실패!", Toast.LENGTH_SHORT).show();
                                                                });
                                                    })
                                                    .addOnFailureListener(err -> {
                                                        Log.e("Firestore", "서브카테고리 제거 실패: " + err.getMessage());
                                                        Toast.makeText(this, "즐겨찾기 상태 변경 실패!", Toast.LENGTH_SHORT).show();
                                                    });
                                            break; // 반복 종료
                                        }
                                    }
                                }
                            }

                            // 작업 완료 추적
                            tasksCompleted[0]++;
                            if (tasksCompleted[0] == documentNames.length && !isUpdated[0]) {
                                // 모든 도큐먼트를 탐색했으나 변경되지 않은 경우
                                Toast.makeText(this, "키워드가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firestore", "데이터 가져오기 실패: " + e.getMessage());
                            tasksCompleted[0]++;
                            if (tasksCompleted[0] == documentNames.length && !isUpdated[0]) {
                                Toast.makeText(this, "Firestore 데이터 가져오기 실패!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void updateFavoriteIcon(boolean isFavorite) {
        if (isFavorite) {
            saveImageButton.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            saveImageButton.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }


    private void checkFavoriteStatus(String keyword) {
        // Firestore에서 키워드 상태 확인
        firestore.collection("savedKeywords").document("userSelectedKeywords")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains(keyword)) {
                        // 키워드가 존재하면 즐겨찾기 활성화
                        isFavorite = true;
                        updateFavoriteIcon();
                    } else {
                        // 키워드가 존재하지 않으면 즐겨찾기 비활성화
                        isFavorite = false;
                        updateFavoriteIcon();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "상태 확인 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateFavoriteIcon() {
        if (isFavorite) {
            // 즐겨찾기된 상태: 색칠된 별
            saveImageButton.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            // 즐겨찾기되지 않은 상태: 색칠되지 않은 별
            saveImageButton.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }

    private void speakKeyword(String keyword) {
        if (textToSpeech != null) {
            Bundle params = new Bundle();
            params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f);
            textToSpeech.speak(keyword, TextToSpeech.QUEUE_FLUSH, params, "utteranceId");
        }
    }

    @Override
    protected void onDestroy() {
        // TextToSpeech 객체 해제
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == TextToSpeech.SUCCESS) {
                Toast.makeText(this, "TTS 데이터가 설치되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "TTS 데이터 설치 필요", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}