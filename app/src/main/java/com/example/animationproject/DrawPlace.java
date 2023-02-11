package com.example.animationproject;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import yuku.ambilwarna.AmbilWarnaDialog;

public class DrawPlace extends AppCompatActivity {
    @SuppressLint("StaticFieldLeak")
    private static ImageView canvasPreview;
    private static FrameAdapter  adapter;
    private static PaintView paintView;
    private  ImageView forStart;
    private String prName;
    private int mDefaultColor,bgColor,deletingElement;
    private int currentFrame = -1;
    private Dialog dialog, exitDialog,saveDialog;
    private float fps = 0;
    private ImageButton saveAsGif,saveProjectToDb,cancelSave, mColorPreview;
    private EditText fpsEditText;
    private StorageReference storageReference,httpsRef;
    private Uri uploadUri;
    private DatabaseReference mDataBase;
    private FirebaseUser user;
    private final ArrayList<Element> elements = new ArrayList<>();

    @SuppressLint({"CutPasteId", "NotifyDataSetChanged"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_place);
        // настройки окна приложения
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); //скрывается строка состояния телефона
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// приложение всегда альбомное
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // тема всегда светлая

        init();
        isDBNeeded();
    }
    // инициализация компонентов
    @SuppressLint("NotifyDataSetChanged")
    private void init(){
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    user =  mAuth.getCurrentUser();
    mDataBase = FirebaseDatabase.getInstance().getReference("User").child(user.getUid());
    mColorPreview = findViewById(R.id.color_preview);
    paintView = findViewById(R.id.paint_view2);
    initNewPaint();
    mDefaultColor = Color.BLACK;
    forStart = findViewById(R.id.forStart);
    canvasPreview = findViewById(R.id.canvas_preview);
    canvasPreview.setBackgroundColor(paintView.getDefaultBgColor());
    initRecyclerView();
    adapter.notifyDataSetChanged();
    prName = getIntent().getStringExtra("ProjectName");
    elements.clear();
    paintView.clear();
    adapter.notifyDataSetChanged();
    final SeekBar seekBar = findViewById(R.id.brush_size);
    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            paintView.setBrushSize(seekBar.getProgress());
        }
    });
}
    @SuppressLint("NotifyDataSetChanged")
    private void isDBNeeded(){
    boolean isNewProject = getIntent().getBooleanExtra("IsNewProject", true);
    if(!isNewProject){
        loaddata();
        adapter.notifyDataSetChanged();
    }
}
    private void initNewPaint(){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        bgColor = getIntent().getIntExtra("BG_Color",mDefaultColor);
        paintView.init(metrics);
        paintView.setColorEraser(bgColor);
        paintView.setDefaultBgColor(bgColor);
    }
    // инициализация RecycleView
    private void initRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.list_of_frames);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        FrameAdapter.OnFrameClickListener onFrameClickListener = new FrameAdapter.OnFrameClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onFrameClick(Element element) {
                if(currentFrame>-1 && currentFrame<elements.size()){
                    elements.get(currentFrame).setBmp(viewToBitmap(paintView));
                    adapter.notifyDataSetChanged();
                }
              paintView.clear();
              currentFrame = element.getPosition();
               paintView.setOldFrame(element.getBmp());
               paintView.pen();
               paintView.invalidate();
               elements.get(element.getPosition()).setBmp(viewToBitmap(paintView));
            }

            @Override
            public void onLongFrameClick(Element element) {
                initDialogDelete();
                deletingElement = element.getPosition();
                System.out.println("______deletingElement = "+deletingElement);
                dialog.show();
            }
        };

        adapter = new FrameAdapter(this, elements, onFrameClickListener);
        recyclerView.setAdapter(adapter);
    }
    // обработчики Диалоговых окон
    @SuppressLint("NotifyDataSetChanged")
    private void initDialogDelete(){
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
            if(elements.size()==0) forStart.setVisibility(View.VISIBLE);
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

        saveAsGif =  saveDialog.findViewById(R.id.saveAsGIF);
        saveProjectToDb =  saveDialog.findViewById(R.id.saveAnimToDb);
        cancelSave =  saveDialog.findViewById(R.id.cancelSave);
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
                                DrawPlace.this,
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
    // окно выбора цвета
    public void openColorPickerDialogue() {
        final AmbilWarnaDialog colorPickerDialogue = new AmbilWarnaDialog(this, mDefaultColor,
                new AmbilWarnaDialog.OnAmbilWarnaListener(){

                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {

                    }

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        paintView.colorPen = color;
                        mDefaultColor = color;
                        mColorPreview.setBackgroundColor(mDefaultColor);
                        paintView.pen();
                    }
                } );
        colorPickerDialogue.show();
    }
    // загрузка данных в бд
    @SuppressLint("NotifyDataSetChanged")
    public boolean saveToDbElements(){
        if(elements!=null && elements.size()!=0 ) {
            if(currentFrame>-1 && currentFrame<elements.size()){
                elements.get(currentFrame).setBmp(viewToBitmap(paintView));
                adapter.notifyDataSetChanged();
            }
            storageReference = FirebaseStorage.getInstance().getReference("User").child(user.getUid()).child(prName);
            mDataBase.child(prName).removeValue();
            for (int i = 0; i < elements.size(); i++) {
                if (elements.get(i).getBmp() != null && elements.get(i).getUriImg() == null) {
                    uploadImage(elements.get(i).getBmp(),  i);
                }
            }
            Toast.makeText(this,"Сохранено!",Toast.LENGTH_SHORT).show();
            return true;
        }else{
            Toast.makeText(this, "Пусто!", Toast.LENGTH_LONG).show();
            return  false;
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
            element.setBgColor(bgColor);
            element.setActivityType(1);
            mDataBase.child(prName).child(""+element.getPosition()).setValue(element);
            turnOnnButtons();
        });
    }
    // выгрузка данных из бд
    @SuppressLint("NotifyDataSetChanged")
    private void loaddata(){
        elements.clear();
        DatabaseReference ref = mDataBase.child(prName);
        ref.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Log.d("firebase", String.valueOf(task.getResult().getValue()));
                for(DataSnapshot ds : task.getResult().getChildren()) {
                    Element element = ds.getValue(Element.class);
                    Log.e("Element","START");
                    if (element != null) {
                        Log.e("Element",String.valueOf(element.getUserEmail()));
                        Log.e("Element",String.valueOf(element.getUriImg()));
                        Log.e("Element",String.valueOf(element.getNameOfAnimation()));
                        if (element.getUriImg()!=null && element.getUserEmail().equals(user.getEmail()) && element.getNameOfAnimation().equals(prName)) {
                            Element e = new Element();
                            httpsRef = FirebaseStorage.getInstance().getReferenceFromUrl(element.getUriImg());
                            httpsRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                                e.setBmp(getBitmapfromByteArray(bytes));
                                e.setPosition(element.getPosition());
                            });
                            Log.e("ELMENT BITMAP",String.valueOf(e.getBmp()));
                            System.out.println("POSITION: "+element.getPosition());
                            elements.add(e);
                            adapter.notifyDataSetChanged();
                            Log.e("Element","LOADED");
                        }
                    }else{
                        Log.e("Element","null");
                    }
                }
            }
        });

    }
    // обработчики кнопок
    public void onClickBrush(View view){
       paintView.pen();
    }
    public void onClickEraser(View view){
      paintView.eraser();
    }
    public void onClickBack(View view){
       paintView.back();
        Bitmap b3 = viewToBitmap(paintView);
        canvasPreview.setImageBitmap(Bitmap.createScaledBitmap(b3,240,120,false));
    }
    public void onClickForward(View view){
        paintView.forward();
        Bitmap b3 = viewToBitmap(paintView);
        canvasPreview.setImageBitmap(Bitmap.createScaledBitmap(b3,240,120,false));
    }
    public void onClickDelete(View view){
        paintView.clear();
        Bitmap b3 = viewToBitmap(paintView);
        canvasPreview.setImageBitmap(Bitmap.createScaledBitmap(b3,240,120,false));

    }
    @SuppressLint("NotifyDataSetChanged")
    public void onClickAddFrame(View view){
        forStart.setVisibility(View.GONE);
        if(currentFrame>-1 && currentFrame<elements.size()){
            elements.get(currentFrame).setBmp(viewToBitmap(paintView));
            adapter.notifyDataSetChanged();
        }
            paintView.clear();
            Element e = new Element(viewToBitmap(paintView));
            e.setPosition(elements.size());
            elements.add(e);
            currentFrame =  e.getPosition();
            Bitmap bm = viewToBitmap(paintView);
            paintView.setOldFrame(BitmapFactory.decodeResource(getResources(), R.drawable.empty_pic));
            adapter.notifyDataSetChanged();
            canvasPreview.setImageBitmap(Bitmap.createScaledBitmap(bm, bm.getWidth() / 5, bm.getHeight() / 5, false));

    }
    public void onClickSaveGIF(View view){
   initDialogSave();
   saveDialog.show();
    }
    public void onClickExit(View view){
        initDialogExit();
        exitDialog.show();
    }
    public void onClickPickColor(View view){
        openColorPickerDialogue();
    }
    // генерация и сохранение GIF память устройства
    public byte[] generateGIF() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        encoder.start(bos);
        for (int i = 0;i<elements.size();i++) {
            Bitmap bitmap = elements.get(i).getBmp();
            encoder.setFrameRate(fps);
            encoder.addFrame(bitmap);
        }
        encoder.finish();
        return bos.toByteArray();
    }
    public File saveGif() {
        if (fps > 0) {
            System.out.println(prName);
            String path = prName + System.currentTimeMillis() + ".gif";
            try {
                File file = new File(create("AnimationProject"), path);
                FileOutputStream outStream = new FileOutputStream(file);
                outStream.write(generateGIF());
                outStream.close();
                return file;
            } catch (Exception e) {
                File file = new File(create("AnimationProject"), path);
                file.delete();
                return null;
            }
        } else {
            Toast.makeText(this, "Укажите FPS верно", Toast.LENGTH_LONG).show();
            return null;
        }
    }
    // создаем папку в директории downloads
    public static File create(String name) {
        File baseDir;

        baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        if (baseDir == null)
            return Environment.getExternalStorageDirectory();

        File folder = new File(baseDir, name);

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
    // преобразование View в Bitmap
    public static Bitmap viewToBitmap(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
    // преобразование массива байтов в Bitmap
    public Bitmap getBitmapfromByteArray(byte[] bitmap) {
        return BitmapFactory.decodeByteArray(bitmap , 0, bitmap.length);
    }
    // отключение/включение кнопок
    private void turnOnnButtons(){
        cancelSave.setClickable(true);
        saveProjectToDb.setClickable(true);
        saveAsGif.setClickable(true);
    }
    private void turnOffButtons(){
        cancelSave.setClickable(false);
        saveProjectToDb.setClickable(false);
        saveAsGif.setClickable(false);
    }
    @SuppressLint("NotifyDataSetChanged")

    public static ImageView getCanvasPreview() {
        return canvasPreview;
    }
}

