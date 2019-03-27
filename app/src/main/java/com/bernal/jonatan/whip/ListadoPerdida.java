package com.bernal.jonatan.whip;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListadoPerdida extends AppCompatActivity {


    private String URL;
    private RequestQueue requestqueue;
    private JSONArray resultat;
    private ArrayList<Fuente> Posts_perdidos;
    private Adaptador adapt;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_perdida);


        //Coneixón con la API
        URL = "https://whip-api.herokuapp.com/contributions/lostposts";
        requestqueue = Volley.newRequestQueue(this);

        Toolbar tool = (Toolbar) findViewById(R.id.toolbar_listadoPerd);
        setSupportActionBar(tool);
        getSupportActionBar().setTitle("ABANDONO O PÉRDIDA");



        //Llamada a la API

        JsonArrayRequest arrayJsonrequest = new JsonArrayRequest(
                JsonRequest.Method.GET,
                URL,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            Toast.makeText(getApplicationContext(),"Listado mostrado correctamente",Toast.LENGTH_SHORT).show();
                            resultat = response;
                            Posts_perdidos = new ArrayList<Fuente>();
                            for (int i = 0; i < resultat.length();i++) {
                                JSONObject postite = resultat.getJSONObject(i);
                                Posts_perdidos.add(new Fuente(postite.getString("title"),R.drawable.perro,postite.getString("text"),0));
                                RecyclerView contenedor = (RecyclerView) findViewById(R.id.contenedor);
                                LinearLayoutManager layout = new LinearLayoutManager(getApplicationContext());
                                layout.setOrientation(LinearLayoutManager.VERTICAL);
                                adapt = new Adaptador(Posts_perdidos);
                                contenedor.setAdapter(adapt);
                                contenedor.setLayoutManager(layout);
                            }
                            adapt.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {
                                    startActivity(new Intent(ListadoPerdida.this, InfoPost.class));
                                }
                            });
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
                params.put("Authorization", "2C4T55N-4SY40G3-JBG7QMB-4PYNJ9P"); //valor de V ha de ser el de la var global
                return params;
            }
        };
        requestqueue.add(arrayJsonrequest);

    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menus,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){
            case R.id.icono_añadir:
                startActivity(new Intent(ListadoPerdida.this, NuevoPostPerdido.class));
                break;
        }
        return true;
    }



}
