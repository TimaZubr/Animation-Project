package com.example.animationproject;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText email, password;
    private ImageButton login, reg, bStart,  bOut;
    private TextView tvStart, senVer;
    private String emailStr;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); //скрывается строка состояния телефона
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);//приложение всегда портретное
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        init();
    }
    // инициализация компонентов
    private void init(){
        senVer = findViewById(R.id.senVer);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.LogIn);
        reg = findViewById(R.id.reg);
        bStart = findViewById(R.id.bStart);
        bOut = findViewById(R.id.bOut);
        tvStart = findViewById(R.id.tvStart);
        mAuth = FirebaseAuth.getInstance();
        user =  mAuth.getCurrentUser();
    }
    // кнопки
    public void onClickRegister(View view){
        Intent next = new Intent(this.getApplicationContext(),RegisterActivity.class);
        startActivity(next);
    }
    public void onClickLogin(View view) {
        emailStr = email.getText().toString();
        String passwordStr = password.getText().toString();
        if (!passwordStr.isEmpty() && !emailStr.isEmpty()) {
            mAuth.signInWithEmailAndPassword(emailStr,passwordStr).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    user =  mAuth.getCurrentUser();
                    assert user != null;
                    if(user.isEmailVerified()) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Добро Пожаловать!", Toast.LENGTH_LONG);
                        toast.show();
                        Intent next = new Intent(getApplicationContext(), MainActivity.class);
                        next.putExtra("email",emailStr);
                        startActivity(next);
                    }else{
                        Toast toast = Toast.makeText(getApplicationContext(), "Проверьте почту!", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Неверный логин или пароль", Toast.LENGTH_LONG).show());
        } else {
            Toast toast = Toast.makeText(this, "Введите корректно данные!", Toast.LENGTH_LONG);
            toast.show();
        }
    }
    public void onClickStart(View view){
        if(user.isEmailVerified()) {
            recreate();
            Intent next = new Intent(getApplicationContext(), MainActivity.class);
            next.putExtra("email",emailStr);
            startActivity(next);
        }else{
            Toast toast = Toast.makeText(getApplicationContext(), "Проверьте почту!", Toast.LENGTH_LONG);
            toast.show();
        }
    }
    public void onClickSignOut(View view){
        FirebaseAuth.getInstance().signOut();
        recreate();
    }
    public void onClickSendVerification(View view){
        emailStr = email.getText().toString();
        String passwordStr = password.getText().toString();
        if (!passwordStr.isEmpty() && !emailStr.isEmpty()) {
            mAuth.signInWithEmailAndPassword(emailStr,passwordStr).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    user =  mAuth.getCurrentUser();
                    assert user != null;
                    if(user.isEmailVerified()) {
                        Toast.makeText(getApplicationContext(), "Ваша запись подтверждена!", Toast.LENGTH_LONG).show();
                    }else{
                        sendEmailVer();
                        Toast toast = Toast.makeText(getApplicationContext(), "Проверьте почту!", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Неверный логин или пароль", Toast.LENGTH_LONG).show());
        } else {
            Toast toast = Toast.makeText(this, "Введите корректно данные!", Toast.LENGTH_LONG);
            toast.show();
        }
    }
    // укрывание компонентов, в зависимости от инициализации пользователя
    @Override
    protected void onStart() {
        super.onStart();
        if(user!=null){
            bOut.setVisibility(View.VISIBLE);
            bStart.setVisibility(View.VISIBLE);
            tvStart.setVisibility(View.VISIBLE);
            login.setVisibility(View.GONE);
            reg.setVisibility(View.GONE);
            email.setVisibility(View.GONE);
            password.setVisibility(View.GONE);
            senVer.setVisibility(View.GONE);
            String userName = "Вы вошли как : " +user.getEmail();
            tvStart.setText(userName);
        }else{
            bOut.setVisibility(View.GONE);
            bStart.setVisibility(View.GONE);
            tvStart.setVisibility(View.GONE);
            login.setVisibility(View.VISIBLE);
            reg.setVisibility(View.VISIBLE);
            email.setVisibility(View.VISIBLE);
            password.setVisibility(View.VISIBLE);

        }
    }
    // отправка письма с верификацией
    private void sendEmailVer(){
        FirebaseUser user =  mAuth.getCurrentUser();
        assert user!=null;
        user.sendEmailVerification().addOnCompleteListener(Task::isSuccessful);
    }
}