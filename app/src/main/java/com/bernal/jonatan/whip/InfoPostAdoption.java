package com.bernal.jonatan.whip;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class InfoPostAdoption extends AppCompatActivity {

    TextView titulo, fecha, especie,raza, contenido;
    ImageView foto_post;
    String Identificador;

    private String URL, URL_favs, URL_like;
    private RequestQueue requestqueue;

    private Usuari_Logejat ul = Usuari_Logejat.getUsuariLogejat("");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_post_adoption);

        //Obtengo el ID del post
        Identificador = getIntent().getStringExtra("identificadorPost");

        titulo = (TextView) findViewById(R.id.titulo_postAdoption);
        fecha = (TextView) findViewById(R.id.fecha_postAdoption);
        especie = (TextView) findViewById(R.id.especie_postAdoption);
        raza = (TextView) findViewById(R.id.raza_postAdoption);
        contenido = (TextView) findViewById(R.id.contenido_postAdoption);

        foto_post = (ImageView) findViewById(R.id.foto_postAdoption);

        //Gestión toolbar
        Toolbar tool = (Toolbar) findViewById(R.id.toolbar_infoPostAdoption);
        setSupportActionBar(tool);
        getSupportActionBar().setTitle("ADOPCIÓN");


        //Recoger los datos de Back y cargarlos en la vista
        URL = "https://whip-api.herokuapp.com/contributions/adoptionposts/" + Identificador;
        URL_favs = "https://whip-api.herokuapp.com/contributions/adoptionposts/" + Identificador + "/like";
        URL_like = "https://whip-api.herokuapp.com/contributions/adoptionposts/" + Identificador + "/like";
        requestqueue = Volley.newRequestQueue(this);


        JsonObjectRequest objectJsonrequest = new JsonObjectRequest(
                JsonRequest.Method.GET,
                URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONObject lostpost = response.getJSONObject("postInfo");
                            titulo.setText(lostpost.getString("title"));
                            String[] data = (lostpost.getString("createdAt")).split("T");
                            fecha.setText(data[0]);
                            especie.setText(lostpost.getString("specie"));

                            raza.setText(lostpost.getString("race"));
                            contenido.setText(lostpost.getString("text"));
                            //Fotografías con IMGUR
                            foto_post.setBackgroundResource(R.drawable.perfilperro);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "ERROOOOOOOR", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", ul.getAPI_KEY()); //valor de V ha de ser el de la var global
                return params;
            }
        };
        requestqueue.add(objectJsonrequest);
    }


    public boolean onCreateOptionsMenu(final Menu menu) {
        JsonObjectRequest objectJsonrequest3 = new JsonObjectRequest(
                JsonRequest.Method.GET,
                URL_favs,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            boolean fav = response.getBoolean("isFavorite");

                            if (fav) {
                                Toast.makeText(getApplicationContext(), "MENU FAVORITO", Toast.LENGTH_SHORT).show();
                                getMenuInflater().inflate(R.menu.menu_infopostperdlike, menu);
                            } else {
                                Toast.makeText(getApplicationContext(), "MENU NO FAVORITO", Toast.LENGTH_SHORT).show();
                                getMenuInflater().inflate(R.menu.menu_infopostperd, menu);
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "ERROOOOOOOR EN MOSTRAR MENU", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", ul.getAPI_KEY());
                return params;
            }
        };
        requestqueue.add(objectJsonrequest3);
        return true;
    }


    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.icono_fav:
                //comunicacion con back + cambiar color de la estrella

                BackFavs_like();

                break;
            case R.id.icono_fav_rell:

                BackFavs_dislike();

                break;
        }
        return true;
    }

    private void BackFavs_dislike() {
        JsonObjectRequest objectJsonrequest = new JsonObjectRequest(
                JsonRequest.Method.DELETE,
                URL_like,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getApplicationContext(), "DISLIKE", Toast.LENGTH_SHORT).show();
                        recreate();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "ERROOOOOOOR DISLIKE", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", ul.getAPI_KEY());
                return params;
            }
        };
        requestqueue.add(objectJsonrequest);
    }


    public void BackFavs_like() {

        JsonObjectRequest objectJsonrequest = new JsonObjectRequest(
                JsonRequest.Method.POST,
                URL_like,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getApplicationContext(), "LIKE", Toast.LENGTH_SHORT).show();
                        recreate();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "ERROOOOOOOR LIKE", Toast.LENGTH_SHORT).show();
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
    }



}