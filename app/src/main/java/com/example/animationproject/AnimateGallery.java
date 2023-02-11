package com.example.animationproject;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class AnimateGallery extends AppCompatActivity {
    private TextView name;
    private EditText projectName, fpsEditText;
    private String prName="" ;
    private final ArrayList<Element> elements = new ArrayList<>();
    private final ArrayList<String> names = new ArrayList<>();
    private ElementAdapter adapter;
    private ImageButton add, cancel,save;
    private StorageReference storageReference, httpsRef;
    private Uri uploadUri;
    private DatabaseReference mDataBase;
    private FirebaseUser user;
    private int deletingElement;
    private Dialog dialog, exitDialog, saveDialog;
    private float fps = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animate_gallery);
        //настройки окна
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); //скрывается строка состояния телефона
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//приложение всегда альбомное
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //тема всегда светлая
        init();
        isDBNeeded();
        initRecyclerView();
    }
    // инициализация компонентов
    private void init(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user =  mAuth.getCurrentUser();
        mDataBase = FirebaseDatabase.getInstance().getReference("User").child(user.getUid());
        add = findViewById(R.id.add);
        save = findViewById(R.id.saveToDB);
        cancel = findViewById(R.id.cancelGallery);
        projectName = findViewById(R.id.projectNameGallery);
        projectName.setVisibility(View.VISIBLE);
        name = findViewById(R.id.nameGalleryTV);
        name.setVisibility(View.GONE);
    }
    // проверка необходимости выгружать данные из бд
    private void isDBNeeded(){
        boolean isNewProject = getIntent().getBooleanExtra("IsNewProject",true);
        if(isNewProject)
            getDataFromDB();
        if(!isNewProject) {
            projectName.setVisibility(View.GONE);
            prName = getIntent().getStringExtra("ProjectName");
            name.setText(prName);
            name.setVisibility(View.VISIBLE);
            loaddata();
        }
    }
    // слушатели нажатий кнопок
    @SuppressLint("NotifyDataSetChanged")
    public void onRefreshRV(View view){
        adapter.notifyDataSetChanged();
    }
    public void onCancelGallery(View view){
        initDialogExit();
        exitDialog.show();
    }
    public void onAddPhoto(View view){
        getImage();
    }
    public void onSaveToDB (View view){
        initDialogSave();
        saveDialog.show();
    }
    // инициализация списка
    private void initRecyclerView(){
        RecyclerView list = findViewById(R.id.image_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        list.setLayoutManager(layoutManager);
        ElementAdapter.OnElementClickListener oecl = element -> {
            initDialog();
            deletingElement = element.getPosition();
            System.out.println("______deletingElement = "+deletingElement);
            Log.d("RECYCLERVIEW",String.valueOf(element.getBmp()));
            dialog.show();
        };
        adapter = new ElementAdapter(this,elements, oecl);
        list.setAdapter(adapter);
    }
    @SuppressLint("NotifyDataSetChanged")
    // инициализация диалоговых окон
    private void initDialog(){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.delete_dialog);
        ImageButton delete = dialog.findViewById(R.id.deleteFrameDialog);
        ImageButton cancel = dialog.findViewById(R.id.cancelDialog);
        delete.setOnClickListener(view -> {
            elements.remove(deletingElement);
            for (int i=0;i<elements.size(); i++){
                elements.get(i).setPosition(i);
            }
            adapter.notifyDataSetChanged();
            dialog.cancel();
        });
        cancel.setOnClickListener(view -> dialog.cancel());
    }
    private void initDialogExit(){
        exitDialog = new Dialog(this);
        exitDialog.setContentView(R.layout.exit_dialog);
        ImageButton exit = exitDialog.findViewById(R.id.exitToMenuDialog);
        ImageButton cancel = exitDialog.findViewById(R.id.cancelDialogExit);
        exit.setOnClickListener(view -> {
            Intent back = new Intent(view.getContext(),MainActivity.class);
            startActivity(back);
            finish();
        });
        cancel.setOnClickListener(view -> exitDialog.cancel());
    }
    private void initDialogSave(){
        saveDialog = new Dialog(this);
        saveDialog.setContentView(R.layout.saving_animation_dialog);

        ImageButton saveAsGif = saveDialog.findViewById(R.id.saveAsGIF);
        ImageButton saveProjectToDb = saveDialog.findViewById(R.id.saveAnimToDb);
        ImageButton cancelSave = saveDialog.findViewById(R.id.cancelSave);
        ImageButton share = saveDialog.findViewById(R.id.share);
        fpsEditText = saveDialog.findViewById(R.id.fpsEditText);
        cancelSave.setOnClickListener(view -> saveDialog.cancel());
        saveAsGif.setOnClickListener(view -> {
            if (!fpsEditText.getText().toString().equals("")) {
                if(saveToDbElements()) {
                    fps = Float.parseFloat(fpsEditText.getText().toString());
                    saveGif();
                }
            }else{
                Toast.makeText(view.getContext(), "Укажите FPS верно", Toast.LENGTH_LONG).show();
            }
        });
        saveProjectToDb.setOnClickListener(view -> saveToDbElements());
        share.setOnClickListener(view -> {
            if(!fpsEditText.getText().toString().equals("")) {
                if (saveToDbElements()) {
                    fps = Float.parseFloat(fpsEditText.getText().toString());
                    File file = saveGif();
                    if (file != null) {
                        Uri imageUri = FileProvider.getUriForFile(
                                AnimateGallery.this,
                                "com.example.animationproject.provider",
                                file);
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("file/gif");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                        startActivity(shareIntent);
                    }
                }
            }else{
                Toast.makeText(view.getContext(), "Укажите FPS верно", Toast.LENGTH_LONG).show();
            }
        });
    }
    // генерация и сохранение GIF память устройства
    public byte[] generateGIF(){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        encoder.start(bos);
        ArrayList<Bitmap> picks = new ArrayList<>();

        for (int i = 0;i<elements.size();i++){
            picks.add(elements.get(i).getBmp());
        }
        for (Bitmap bitmap : picks) {
            encoder.setFrameRate(fps);
            encoder.addFrame(bitmap);
        }
        encoder.finish();
        Toast.makeText(this, "Сохранено! 1", Toast.LENGTH_LONG).show();
        turnOnnButtons();
        return bos.toByteArray();
    }
    public File saveGif() {
        if (fps > 0) {
            System.out.println(prName);
            String path = prName + System.currentTimeMillis() + ".gif";
            try {
                File file = new File(create(), path);
                FileOutputStream outStream = new FileOutputStream(file);
                outStream.write(generateGIF());
                outStream.close();
                return file;
            } catch (Exception e) {
                File file = new File(create(), path);
                file.delete();
                return null;
            }
        } else {
            Toast.makeText(this, "Укажите FPS верно", Toast.LENGTH_LONG).show();
            return null;
        }
    }
    private File create() {
        File baseDir;

        baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        if (baseDir == null)
            return Environment.getExternalStorageDirectory();

        File folder = new File(baseDir, "AnimationProject");

        if (folder.exists()) {
            return folder;
        }
        if (folder.isFile()) {
            folder.delete();
        }
        if (folder.mkdirs()) {
            return folder;
        }

        return Environment.getExternalStorageDirectory();
    }
    // загрузка данных в бд
    public boolean saveToDbElements(){
        boolean same = false;
        if(projectName.getVisibility()== View.VISIBLE)
            prName = projectName.getText().toString();
        if (!prName.equals("")) {
            if(!prName.contains(".") &&!prName.contains("#") &&!prName.contains("$")&&!prName.contains("[") &&!prName.contains("]") ){
                for (int i = 0; i < names.size(); i++) {
                    if (prName.equals(names.get(i)) && projectName.getVisibility() == View.VISIBLE) {
                        same = true;
                        break;
                    }
                }
                if(!same){
                    if(elements!=null && elements.size()!=0 ) {
                        name.setVisibility(View.VISIBLE);
                        name.setText(prName);
                        projectName.setVisibility(View.GONE);
                        storageReference = FirebaseStorage.getInstance().getReference("User").child(user.getUid()).child(prName);
                        mDataBase.child(prName).removeValue();
                        for (int i = 0; i < elements.size(); i++) {
                            if (elements.get(i).getBmp() != null && elements.get(i).getUriImg() == null) {
                                turnOffButtons();
                                uploadImage(elements.get(i).getBmp(), i);
                            }
                        }
                        Toast.makeText(this,"Сохранено!",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast toast = Toast.makeText(this, "Пусто!", Toast.LENGTH_LONG);
                        toast.show();
                        turnOnnButtons();
                        return false;
                    }
                }else{
                    Toast.makeText(this,"Проект с таким именем уже есть",Toast.LENGTH_LONG).show();
                    turnOnnButtons();
                    return false;
                }
                return true;
            }else{
                Toast.makeText(this,"Запрещены символы: #  .  $  [  ] ",Toast.LENGTH_LONG).show();
                return false;
            }
        }else{
            Toast.makeText(this,"Введите название",Toast.LENGTH_SHORT).show();
            return false;
        }

    }
    private void uploadImage(Bitmap bitmap,int i){
        turnOffButtons();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100,baos);
        byte[] byteArray = baos.toByteArray();
        final StorageReference myRef = storageReference.child(prName+i);
        UploadTask up = myRef.putBytes(byteArray);
        Task<Uri> task = up.continueWithTask(task1 -> myRef.getDownloadUrl()).addOnCompleteListener(task12 -> {
            uploadUri = task12.getResult();
            Element element = new Element(prName,
                    user.getEmail(),uploadUri.toString());
            element.setPosition(i);
            element.setBgColor(Color.WHITE);
            element.setActivityType(2);
            mDataBase.child(prName).child(""+element.getPosition()).setValue(element);
            turnOnnButtons();

        });
    }
    // выгрузка данных из бд
    private void getDataFromDB(){
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                for(DataSnapshot ds : snapshot.getChildren()) {
                    Element element = ds.getValue(Element.class);
                    if (element != null) {
                        if (element.getUriImg()!=null && element.getUserEmail().equals(user.getEmail()) && element.getPosition()==0) {
                            names.add(element.getNameOfAnimation());
                            System.out.println(names.get(names.size()-1));
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        mDataBase.addChildEventListener(childEventListener);
    }
    @SuppressLint("NotifyDataSetChanged")
    private void loaddata() {
        elements.clear();

        DatabaseReference ref = mDataBase.child(prName);
        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("firebase", String.valueOf(task.getResult().getValue()));
                for (DataSnapshot ds : task.getResult().getChildren()) {
                    Element element = ds.getValue(Element.class);
                    Log.e("Element", "START");
                    if (element != null) {
                        Log.e("Element", String.valueOf(element.getUserEmail()));
                        Log.e("Element", String.valueOf(element.getUriImg()));
                        Log.e("Element", String.valueOf(element.getNameOfAnimation()));
                        if (element.getUriImg() != null && element.getUserEmail().equals(user.getEmail()) && element.getNameOfAnimation().equals(prName)) {
                            Element e = new Element();
                            httpsRef = FirebaseStorage.getInstance().getReferenceFromUrl(element.getUriImg());
                            httpsRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> e.setBmp(getBitmapfromByteArray(bytes)));
                            Log.e("ELMENT BITMAP", String.valueOf(e.getBmp()));
                            elements.add(e);
                            adapter.notifyDataSetChanged();
                            Log.e("Element", "LOADED");
                        }
                    } else {
                        Log.e("Element", "null");
                    }
                    assert element != null;
                    if (element.getUriImg() != null && element.getUserEmail().equals(user.getEmail()) && element.getPosition() == 0) {
                        names.add(element.getNameOfAnimation());
                        System.out.println(names.get(names.size() - 1));
                    }
                }

            }
        });

    }
    // получение изображений из галереи
    private void getImage(){
        Intent intentChooser = new Intent();
        intentChooser.setType("image/*");
        intentChooser.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intentChooser, 1);
    }
    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && data!=null &&data.getData()!=null){
            if(resultCode == -1){
                Uri uri = data.getData();
                final InputStream imageStream;
                try {
                    imageStream =this.getContentResolver().openInputStream(uri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    Element e = new Element(selectedImage);
                    e.setPosition(elements.size());  //!!
                    elements.add(e);
                    System.out.println("getImage!!");
                    adapter.notifyDataSetChanged();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }
    }
    //отключение/включение кнопок во время и после загрузки
    public void turnOffButtons(){
        save.setClickable(false);
        add.setClickable(false);
        cancel.setClickable(false);
    }
    public void turnOnnButtons(){
        save.setClickable(true);
        add.setClickable(true);
        cancel.setClickable(true);
    }
    // получение Bitmap из массива байтов
    public Bitmap getBitmapfromByteArray(byte[] bitmap) {
        return BitmapFactory.decodeByteArray(bitmap , 0, bitmap.length);
    }
}