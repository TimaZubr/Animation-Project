package com.example.animationproject;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Locale;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity {
    public static final int IDM_LOGOUT = 101;
    public static final int IDM_EXIT = 102;
    private int mDefaultColor = Color.WHITE;
    View  viewColor;
    private Dialog dialog, socialMediaDialog,addingProjectDialog;
    private String deletingElement;
    private EditText search;
    private String filter="";
    private MainListAdapter adapter;
    private final ArrayList<Element> elements = new ArrayList<>();
    private final ArrayList<String> names = new ArrayList<>();
    private StorageReference storageReference;
    private DatabaseReference mDataBase;
    private FirebaseUser user;
    public MainActivity() {
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); //скрывается строка состояния телефона
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//приложение всегда альбомное
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        init();
        // обработка поиска по проектам
        search = findViewById(R.id.searchProjects);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                elements.clear();
                filter = search.getText().toString().toLowerCase(Locale.ROOT);
                getDataFromDB();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

    }
private void init(){
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    user =  mAuth.getCurrentUser();
    mDataBase = FirebaseDatabase.getInstance().getReference("User").child(user.getUid());
    ImageButton exitApp = findViewById(R.id.exitApp);
    registerForContextMenu(exitApp);
    initRecyclerView();
}
    // обработка RecycleView
    private void initRecyclerView(){
        RecyclerView list = findViewById(R.id.allProjects);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        list.setLayoutManager(layoutManager);
       MainListAdapter.OnMainItemClickListener oecl = new MainListAdapter.OnMainItemClickListener() {
           @Override
           public void onItemClick(Element item)
           {
               switch (item.getActivityType()){
                   case 1:
                       Intent next = new Intent(getApplicationContext(), DrawPlace.class);
                       next.putExtra("BG_Color",item.getBgColor());
                       next.putExtra("IsNewProject", false);
                       next.putExtra("ProjectName",item.getNameOfAnimation());
                       startActivity(next);
                       finish();
                       break;
                   case 2:
                       Intent gal = new Intent(getApplicationContext(), AnimateGallery.class);
                       gal.putExtra("ProjectName",item.getNameOfAnimation());
                       gal.putExtra("IsNewProject", false);
                       startActivity(gal);
                       finish();
                       break;
               }

           }

           @Override
           public void onLongItemClick(Element item) {
               initDialogDelete();
                deletingElement = item.getNameOfAnimation();
                dialog.show();
           }
       };
        adapter = new MainListAdapter(this,elements, oecl);
        list.setAdapter(adapter);
    }
    // обработка всех Dialog
    @SuppressLint("NotifyDataSetChanged")
    private void initDialogDelete(){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.delete_dialog);
        TextView tv = dialog.findViewById(R.id.textDialog);
        tv.setText("Удалить проект?");
        ImageButton delete = dialog.findViewById(R.id.deleteFrameDialog);
        ImageButton cancel = dialog.findViewById(R.id.cancelDialog);
        delete.setOnClickListener(view -> {
            storageReference = FirebaseStorage.getInstance().getReference("User").child(user.getUid()).child(deletingElement);
            storageReference.listAll().addOnSuccessListener(listResult -> {
                for (StorageReference item : listResult.getItems()) {
                   item.delete();
                }
            });
            for (int i =0;i<elements.size();i++){
                if(elements.get(i).getNameOfAnimation().equals(deletingElement)) {
                    elements.remove(i);
                }
            }
            adapter.notifyDataSetChanged();
            mDataBase.child(deletingElement).removeValue();
            names.clear();
            dialog.cancel();
        });
        cancel.setOnClickListener(view -> dialog.cancel());
    }
    private void initDialogoSocMedia(){
        socialMediaDialog = new Dialog(this);
        socialMediaDialog.setContentView(R.layout.author_dialog);
        ImageButton github = socialMediaDialog.findViewById(R.id.github);
        ImageButton vk = socialMediaDialog.findViewById(R.id.vk);
        ImageButton inst = socialMediaDialog.findViewById(R.id.inst);
        vk.setOnClickListener(view -> {
            Intent browserIntent = new
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/timabbbb"));
            startActivity(browserIntent);
        });
        inst.setOnClickListener(view -> {
            Intent browserIntent = new
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/timabbbb/?igshid=YmMyMTA2M2Y%3D"));
            startActivity(browserIntent);
        });
    }
    private void initDialogAdd(){
        addingProjectDialog = new Dialog(this);
        addingProjectDialog.setContentView(R.layout.adding_project_dialog);
        ImageButton cancelAdding, startFromGallery, chooseBg, startDraw;
        EditText projectName =addingProjectDialog.findViewById(R.id.projectName);
        viewColor = addingProjectDialog.findViewById(R.id.view);
        cancelAdding = addingProjectDialog.findViewById(R.id.cancelAdding);
        startFromGallery = addingProjectDialog.findViewById(R.id.startFromGallery);
        startDraw = addingProjectDialog.findViewById(R.id.startDraw);
        chooseBg = addingProjectDialog.findViewById(R.id.chooseBg);
        cancelAdding.setOnClickListener(view -> addingProjectDialog.cancel());
        startDraw.setOnClickListener(view -> {
            boolean same = false;
            Intent next = new Intent(view.getContext(), DrawPlace.class);
            next.putExtra("BG_Color",mDefaultColor);
            next.putExtra("IsNewProject", true);
            String prName = projectName.getText().toString();
            if(!prName.equals("")) {
                if(!prName.contains(".") &&!prName.contains("#") &&!prName.contains("$")&&!prName.contains("[") &&!prName.contains("]") ){
                for (int i = 0; i < names.size(); i++) {
                    if (prName.equals(names.get(i))) {
                        same = true;
                        break;
                    }
                }
                if(!same) {
                    next.putExtra("ProjectName", prName);
                    startActivity(next);
                    finish();
                }else{
                    Toast.makeText(view.getContext(),"Проект с таким именем уже есть",Toast.LENGTH_LONG).show();
                }
                }else{
                    Toast.makeText(view.getContext(),"Запрещены символы: #  .  $  [  ] ",Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(view.getContext(),"Введите название",Toast.LENGTH_SHORT).show();
            }
        });
        chooseBg.setOnClickListener(view -> openColorPickerDialogue());
        startFromGallery.setOnClickListener(view -> {
            Intent next = new Intent(view.getContext(), AnimateGallery.class);
            startActivity(next);
            finish();
        });
    }
    public void openColorPickerDialogue() {
        final AmbilWarnaDialog colorPickerDialogue = new AmbilWarnaDialog(this, mDefaultColor,
                new AmbilWarnaDialog.OnAmbilWarnaListener(){

                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {

                    }

                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        mDefaultColor = color;
                        viewColor.setBackgroundColor(color);
                    }
                } );
        colorPickerDialogue.show();
    }
    // обработка меню (выходы)
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(Menu.NONE, IDM_LOGOUT, Menu.NONE, "Выйти в LogIn Меню");
        menu.add(Menu.NONE, IDM_EXIT, Menu.NONE, "Выйти из приложения");
    }
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case IDM_LOGOUT:
                Intent back = new Intent(this, LoginActivity.class);
                startActivity(back);
                finish();
                break;
            case IDM_EXIT:
                this.finishAffinity();
                break;
            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }
    // кнопки
    public void onExit(View v) {
        openContextMenu(v);
    }
    public void onAddNewProject(View view){
        initDialogAdd();
        addingProjectDialog.show();
}
    public void onClickInfo(View view){
        initDialogoSocMedia();
        socialMediaDialog.show();
}
    public void onRefresh(View view){
        search.setText("");
        getDataFromDB();
    }
    // получение данных из бд
    private void getDataFromDB(){
        ChildEventListener childEventListener = new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                for(DataSnapshot ds : snapshot.getChildren()) {
                    Element element = ds.getValue(Element.class);
                    if (element != null) {
                        if (element.getUriImg()!=null && element.getUserEmail().equals(user.getEmail()) && element.getPosition()==0) {
                            if(element.getNameOfAnimation().toLowerCase(Locale.ROOT).contains(filter)){
                                Element e = new Element(element.getNameOfAnimation(), element.getUserEmail(), element.getUriImg());
                                e.setBgColor(element.getBgColor());
                                e.setActivityType(element.getActivityType());
                                elements.add(e);
                                names.add(e.getNameOfAnimation());
                            }
                        }
                    }
                }
                for (int i=0;i<elements.size();i++){
                    for (int j=0;j<elements.size();j++){
                        if(elements.get(i).getNameOfAnimation().equals(elements.get(j).getNameOfAnimation())&& i!=j){
                            elements.remove(j);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
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


}