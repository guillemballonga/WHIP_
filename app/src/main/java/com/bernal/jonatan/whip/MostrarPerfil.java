package com.bernal.jonatan.whip;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.master.glideimageview.GlideImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MostrarPerfil extends AppCompatActivity {

    Button goToEditarPerfil, goToMisPosts;
    static String nomBack;
    static String cognomBack;
    static String userBack;
    static String cpBack;
    static String correuBack;
    static String urlFoto;
    TextView nom,cognom,user,cp, correu;
    //ImageView imatge;
    GlideImageView imatge;

    //Objectes per JSONGet
    private String URL;
    private RequestQueue requestqueue;
    private JSONArray resultat;
    private JSONObject result;

    private UserLoggedIn ul = UserLoggedIn.getUsuariLogejat("","");
    private String api = ul.getAPI_KEY();




    @TargetApi(Build.VERSION_CODES.KITKAT)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar_perfil);

        goToEditarPerfil = findViewById(R.id.boto_editar_perfil);
        goToMisPosts = findViewById(R.id.boto_mis_posts);

        //GET PER CONNEXIÓ AMB BACK
        //Coneixón con la API

        URL = "https://whip-api.herokuapp.com/users/profile";
        requestqueue = Volley.newRequestQueue(this);

        //Llamada a la API
        JsonObjectRequest arrayJsonrequest = new JsonObjectRequest(
                JsonRequest.Method.GET,
                URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            result = response;

                            MostrarParametresPerfil(response);

                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", api); //valor de V ha de ser el de la var global
                return params;
            }
        };
        requestqueue.add(arrayJsonrequest);
        //FINAL GET

        //Gestión de la toolbar
        Toolbar tool = findViewById(R.id.toolbar_mostrarPerfil);
        setSupportActionBar(tool);
        Objects.requireNonNull(getSupportActionBar()).setTitle("PERFIL");

        goToEditarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MostrarPerfil.this, EditProfile.class));
                finish();
            }
        });

        goToMisPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MostrarPerfil.this, MyPosts.class));
                //finish();
            }
        });
    }
    //Funció per assignar els parametres rebuts de back
    private void MostrarParametresPerfil(JSONObject response) throws JSONException {

        nomBack = result.getString("first_name");
        nom = findViewById(R.id.escr_nom);
        nom.setText(nomBack);

        cognomBack = result.getString("family_name");
        cognom = findViewById(R.id.escr_cognom);
        cognom.setText(cognomBack);

        userBack = result.getString("username");
        user = findViewById(R.id.escr_username);
        user.setText(userBack);

        cpBack = result.getString("post_code");
        cp = findViewById(R.id.escr_CP);
        cp.setText(cpBack);

        correuBack = result.getString("email");
        correu = findViewById(R.id.escr_correu);
        correu.setText(correuBack);
        correu.setTextSize(12);

        urlFoto = result.getString("photo_url");
        imatge = findViewById(R.id.imagen_perfil);

        //CARREGAR IMATGE FIREBASE
        if(urlFoto.substring(1, 7).equals("images")){
            retrieveImage(urlFoto);
        }
        else{ //CARREGAR IMATGE DE GOOGLE
            imatge.loadImageUrl(urlFoto);
        }





    }
    public static String getCorreu() {
        return correuBack;
    }
    public static String getNom() {
        return nomBack;
    }
    public static String getCognom() {
        return cognomBack;
    }
    public static String getUsername() {
        return userBack;
    }
    public static String getCP() {
        return cpBack;
    }
    public static String getFoto() { return urlFoto; }

    public void retrieveImage(String idImageFirebase) {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        //Tot: necessito recuperar l objecte desde el json. a child posarhi l indetificador guardat
        StorageReference storageReference = storage.getReferenceFromUrl("gs://whip-1553341713756.appspot.com/").child(idImageFirebase);

        //foto_post = (ImageView) findViewById(R.id.foto_postPerd);
        try {
            final File localFile = File.createTempFile("images", "jpg");
            storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    imatge.setImageBitmap(bitmap);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                }
            });
        } catch (IOException e ) {}
    }


}