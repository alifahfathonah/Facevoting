package com.berkatfaatulohalawa1711010164.facevoting.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.berkatfaatulohalawa1711010164.facevoting.API.APIService;
import com.berkatfaatulohalawa1711010164.facevoting.MainActivity;
import com.berkatfaatulohalawa1711010164.facevoting.R;
import com.berkatfaatulohalawa1711010164.facevoting.helper.ErrorHelper;
import com.berkatfaatulohalawa1711010164.facevoting.helper.MyFirebaseMessagingService;
import com.berkatfaatulohalawa1711010164.facevoting.helper.SessionHelper;
import com.berkatfaatulohalawa1711010164.facevoting.model.ErrorModel;
import com.berkatfaatulohalawa1711010164.facevoting.model.UserModel;

import java.util.Random;
import java.util.regex.Pattern;

import okhttp3.Interceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Daftar extends AppCompatActivity {
    private TextView btnLogin;
    private ImageView btnDaftar;
    private EditText txtNama,txtIdentitas,txtEmail,txtPassword;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar);
        init();
//        if(SessionHelper.isLoggedIn(this)){
//            Intent intent = new Intent(Daftar.this, MainActivity.class);
//            startActivity(intent);
//            finish();
//        }
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Daftar.this, Login.class);
                startActivity(intent);
            }
        });
        btnDaftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proses();
            }
        });
    }

    private void proses(){
        tampilLoading();
        String nama_lengkap = txtNama.getText().toString();
        String identitas = txtIdentitas.getText().toString();
        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();
        String token_firebase = MyFirebaseMessagingService.getToken(getApplicationContext());
        if(nama_lengkap.length()== 0 || identitas.length() == 0 || email.length() == 0 || password.length() == 0) {
            hideLoading();
            tampilPesan("Semua kolom harus diisi !");
        } else {
            if(cekEmail(email)){
                try{
                    Call<UserModel> call = APIService.Factory.create(getApplicationContext()).daftarUser(nama_lengkap,identitas,email,password,token_firebase);
                    call.enqueue(new Callback<UserModel>() {
                        @Override
                        public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                            hideLoading();
                            if(response.isSuccessful()){
                                String id_user = response.body().getId_user();
                                String token_user = random_token();
                                if (SessionHelper.login(Daftar.this, id_user,token_user)){
                                    Intent intent = new Intent(Daftar.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                                    startActivity(intent);
                                    finish();
                                    tampilPesan("Anda berhasil mendaftar !");
                                }

                            } else {
                                ErrorModel error = ErrorHelper.parseError(response);
                                tampilPesan(error.message());
                            }
                        }

                        @Override
                        public void onFailure(Call<UserModel> call, Throwable t) {
                            hideLoading();
                            tampilPesan(t.getMessage());
                        }
                    });
                }catch (Exception e){
                    hideLoading();
                    e.printStackTrace();
                    tampilPesan(e.getMessage());
                }
            } else {
                hideLoading();
                tampilPesan("Email tidak valid !");
            }
        }
    }

    private static String random_token() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(5);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    private boolean cekEmail(String email){
        return Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$").matcher(email).matches();
    }

    private void tampilLoading(){
        if(!progressDialog.isShowing()){
            progressDialog.show();
        }
    }

    private void hideLoading(){
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }

    private void init()
    {
        btnLogin = findViewById(R.id.tbl_login);
        btnDaftar = findViewById(R.id.tombol_daftar);
        txtNama =  findViewById(R.id.ed_nama);
        txtIdentitas = findViewById(R.id.ed_identitas);
        txtEmail = findViewById(R.id.ed_email);
        txtPassword = findViewById(R.id.ed_password);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading.....");
    }

    public void tampilPesan(String pesan)
    {
        Toast.makeText(getApplicationContext(), pesan, Toast.LENGTH_LONG).show();
    }
}