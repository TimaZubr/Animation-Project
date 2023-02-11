package com.example.animationproject;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private EditText email, password, password2;
    private DatabaseReference mDataBase;
    private FirebaseAuth mAuth;
    Dialog regQuestion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); //скрывается строка состояния телефона
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);//приложение всегда портретное
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        email = findViewById(R.id.emailReg);
        password = findViewById(R.id.passwordReg);
        password2 = findViewById(R.id.passwordReg2);
        String USER_KEY = "User";
        mDataBase = FirebaseDatabase.getInstance().getReference(USER_KEY);
        mAuth = FirebaseAuth.getInstance();
        regQuestion = new Dialog(this);
        regQuestion.setContentView(R.layout.reg_question);

    }
    // кнопка регистрация
    public void onClickCreateUser(View view) {
        String passwordDB = password.getText().toString();
        String emailDB = email.getText().toString();
        String passwordRepeat = password2.getText().toString();
        if ( !passwordRepeat.isEmpty() && !passwordDB.isEmpty() && !emailDB.isEmpty()) {
            if (passwordDB.equals(passwordRepeat)) {
                mAuth.createUserWithEmailAndPassword(emailDB,passwordDB).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Toast toast = Toast.makeText(getApplicationContext(), "Подтвердите почту для завершения!", Toast.LENGTH_LONG);
                        toast.show();
                        sendEmailVer();
                        String id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                            User newUser = new User(id, emailDB, passwordDB);
                            mDataBase.child(id).setValue(newUser);
                            Intent next = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(next);
                            finish();

                    }else {
                        Toast.makeText(view.getContext(),"Ошибка регистрации", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast toast = Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_LONG);
                toast.show();
            }
        } else {
            Toast toast = Toast.makeText(this, "Введите корректно данные", Toast.LENGTH_LONG);
            toast.show();
        }
    }
    // объяснение ошибки регистрации
    public void onClickRegQuestion(View view){
        regQuestion.show();
    }
    // отправка верификации
    private void sendEmailVer(){
        FirebaseUser user =  mAuth.getCurrentUser();
        assert user!=null;
        user.sendEmailVerification().addOnCompleteListener(Task::isSuccessful);
    }
}