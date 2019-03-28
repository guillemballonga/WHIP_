package com.bernal.jonatan.whip;

import android.app.VoiceInteractor;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;

public class NuevoPostPerdido extends AppCompatActivity {

    ImageView foto;
    Spinner especie,tipo;
    Button create,cancel;
    EditText titulo,cp,raza,contenido;

    //variables para comucicación back
    private String URL;
    private RequestQueue requestqueue;
    private Usuari_Logejat ul = Usuari_Logejat.getUsuariLogejat("");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_post_perdido);

        //Coneixón con la API
        URL = "https://whip-api.herokuapp.com/contributions/lostposts/new";
        requestqueue = Volley.newRequestQueue(this);

        //Gestión de toolbar
        Toolbar tool = (Toolbar) findViewById(R.id.toolbar_nuevoPostPerd);
        setSupportActionBar(tool);
        getSupportActionBar().setTitle("ABANDONO O PÉRDIDA");

        foto = (ImageView) findViewById(R.id.perfil_perroPerd);

        especie = (Spinner) findViewById(R.id.especie_postPerd);
        tipo = (Spinner) findViewById(R.id.tipo_postPerd);

        titulo = (EditText) findViewById(R.id.titulo_postPerd);
        cp = (EditText) findViewById(R.id.cp_postPerd);
        raza = (EditText) findViewById(R.id.raza_postPerd);
        contenido = (EditText) findViewById(R.id.descripcion_postPerd);


        // Spinner per a seleccionar els items
        String[] itemsEspecie = new String[]{"Perro", "Gato", "Otro"};
        String[] itemsTipo = new String[]{"Encontrado", "Perdido"};

        ArrayAdapter<String> adapterEspecie = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsEspecie);
        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemsTipo);

        especie.setAdapter(adapterEspecie);
        tipo.setAdapter(adapterTipo);


        //Obrir la galeria d'imatges
        foto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                openGallery();
            }
        });






        //Botons

        create = (Button) findViewById(R.id.boton_create);
        cancel = (Button) findViewById(R.id.boton_cancelNewPostPerd);

        create.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

                //jason para comunicación con back
                JSONObject post = new JSONObject();
                JSONArray k = new JSONArray();

                //falta afegir imatge IMGUR
                k.put("");
                k.put("");
                k.put("");
                k.put("");
//JASON
                try {
                    post.put("specie", especie.getSelectedItem().toString());
                    post.put("urls", k );
                    post.put("race", raza.getText().toString());
                    post.put("post_code", cp.getText().toString());
                    post.put("text", contenido.getText().toString());
                    post.put("title", titulo.getText().toString());
                    if (tipo.getSelectedItem().toString().equals("Encontrado"))
                        post.put("type", "T");
                    else post.put("type", "L");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(titulo.getText().toString().equals("") || cp.getText().toString().equals("") || raza.getText().toString().equals("") || especie.getSelectedItem().toString().equals("") || tipo.getSelectedItem().toString().equals("")) {
                    Toast.makeText(getApplicationContext(),"Los campos con * son obligatorios",Toast.LENGTH_SHORT).show();

                }
                else {
                    //Guardar los datos del formulario en BACK. NOTA: No olvidar guardar la fecha de creación del Post
                    JsonObjectRequest objectJsonrequest = new JsonObjectRequest(
                            JsonRequest.Method.POST,
                            URL,
                            post,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Toast.makeText(getApplicationContext(),"Post guardado correctamente",Toast.LENGTH_SHORT).show();
                                    try {
                                        Intent i = new Intent(NuevoPostPerdido.this, InfoPost.class);
                                        i.putExtra("identificadorPost",response.getString("id"));
                                        startActivity(i);
                                        finish();

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(getApplicationContext(),"ERROOOOOOOR",Toast.LENGTH_SHORT).show();

                                }
                            }
                    ) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("Content-Type", "application/json");
                            params.put("Authorization", ul.getAPI_KEY()); //valor de V ha de ser el de la var global
                            return params;
                        }
                    };
                    requestqueue.add(objectJsonrequest);
//JASON

                    //Ir a ver el post en concreto


                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                finish();
            }
        });






    }

    public void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        gallery.setType("image/");
        startActivityForResult(gallery.createChooser(gallery,"Seleccione la Aplicación"),10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri path = data.getData();
            foto.setImageURI(path);

            //Guardar el path de la foto en IMGUR


        }
    }

}
