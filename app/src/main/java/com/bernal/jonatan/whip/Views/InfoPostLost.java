package com.bernal.jonatan.whip.Views;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.bernal.jonatan.whip.Models.Comment;
import com.bernal.jonatan.whip.R;
import com.bernal.jonatan.whip.RecyclerViews.CommentAdapter;
import com.bernal.jonatan.whip.RecyclerViews.OnCommentListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InfoPostLost extends AppCompatActivity {

    //private static final String  = ;
    TextView titulo, fecha, especie, tipo, raza, contenido,num_comments;
    ImageView foto_post, foto_user, compartirRRSS, organ_quedada;
    EditText box_comment;
    String Identificador;
    Button cerrar_post,crear_comment,borrar_comment;
    RecyclerView comments;


    private String URL, URL_favs, URL_like, URL_close, URL_comments;
    private RequestQueue requestqueue;
    private CommentAdapter adapt;
    private ArrayList<Comment> Comments_post;

    private String mail_creador;

    private UserLoggedIn ul = UserLoggedIn.getUsuariLogejat("", "");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_post_lost);

        //Obtengo el ID del post
        Identificador = getIntent().getStringExtra("identificadorPost");

        titulo = findViewById(R.id.titulo_postPerd);
        fecha = findViewById(R.id.fecha_postPerd);
        especie = findViewById(R.id.especie_postPerd);
        tipo = findViewById(R.id.tipo_postPerd);
        raza = findViewById(R.id.raza_postPerd);
        contenido = findViewById(R.id.contenido_postPerd);
        num_comments = findViewById(R.id.permitir_comentario);

        foto_post = findViewById(R.id.foto_postPerd);
        foto_user = findViewById(R.id.imagen_coment_user);
        compartirRRSS = findViewById(R.id.CompartirRRSSPerd);
        organ_quedada = findViewById(R.id.organ_quedadaPerd);
        box_comment = findViewById(R.id.box_comment);

        cerrar_post = findViewById(R.id.boton_cerrar);
        crear_comment = findViewById(R.id.crear_comment);
        borrar_comment = findViewById(R.id.borrar_comment);

        comments = findViewById(R.id.contenedor_comments);

        //Gestión toolbar
        Toolbar tool = findViewById(R.id.toolbar_infoPostPerd);
        setSupportActionBar(tool);
        getSupportActionBar().setTitle("LOST");

        box_comment.setText("");

        //Recoger los datos de Back y cargarlos en la vista
        URL = "https://whip-api.herokuapp.com/contributions/lostposts/" + Identificador;
        URL_favs = "https://whip-api.herokuapp.com/contributions/" + Identificador + "/like/?type=lost";
        URL_like = "https://whip-api.herokuapp.com/contributions/" + Identificador + "/like/?type=lost";
        URL_close = "https://whip-api.herokuapp.com/contributions/close/" + Identificador + "/?type=lost";
        URL_comments = "https://whip-api.herokuapp.com/contributions/lostposts/" +Identificador+"/comments";

        requestqueue = Volley.newRequestQueue(this);


        cerrar_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tancar_post();
            }
        });

        crear_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                crear_comment();
            }
        });

        borrar_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                box_comment.setText("");
            }
        });


        JsonObjectRequest objectJsonrequest = new JsonObjectRequest(
                JsonRequest.Method.GET,
                URL,
                null,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONObject lostpost = response.getJSONObject("postInfo");
                            titulo.setText(lostpost.getString("title"));
                            String[] data = (lostpost.getString("createdAt")).split("T");
                            fecha.setText(data[0]);
                            especie.setText(lostpost.getString("specie"));
                            if (lostpost.getString("type").equals("F")) {
                                tipo.setText("Encontrado");
                            } else tipo.setText("Pérdida");

                            raza.setText(lostpost.getString("race"));
                            contenido.setText(lostpost.getString("text"));

                            mail_creador = lostpost.getString("userId");

                            //Fotografías con FIREBASE
                            foto_user.setBackgroundResource(R.drawable.icono_usuario); //TODO foto google

                            String urlFoto1 = lostpost.getString("photo_url_1"); //LAURA->
                            if (!urlFoto1.equals("")) retrieveImage(urlFoto1);
                            else foto_post.setBackgroundResource(R.drawable.perfilperro);

                            //Revisar si el post está cerrado para ocultar los iconos
                            if (lostpost.getBoolean("status")) {
                                cerrar_post.setVisibility(View.GONE);
                                compartirRRSS.setVisibility(View.GONE);
                                organ_quedada.setVisibility(View.GONE);
                                foto_user.setVisibility(View.GONE);
                                box_comment.setVisibility(View.GONE);
                                crear_comment.setVisibility(View.GONE);
                                borrar_comment.setVisibility(View.GONE);
                            }

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
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", ul.getAPI_KEY()); //valor de V ha de ser el de la var global
                return params;
            }
        };
        requestqueue.add(objectJsonrequest);
        
        carregar_comments();

    }

    private void crear_comment() {

        if (box_comment.getText().toString().equals("")) Toast.makeText(getApplicationContext(), "Debe escribir un comentario", Toast.LENGTH_SHORT).show();
        else {
            JSONObject post = new JSONObject();
            try {
                post.put("text", box_comment.getText().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest objectJsonrequest = new JsonObjectRequest(
                    JsonRequest.Method.POST,
                    URL_comments,
                    post,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            recreate();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), "Error al comentar", Toast.LENGTH_SHORT).show();

                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<>();
                    params.put("Content-Type", "application/json");
                    params.put("Authorization", ul.getAPI_KEY()); //valor de V ha de ser el de la var global
                    return params;
                }
            };
            requestqueue.add(objectJsonrequest);
        }

    }

    private void carregar_comments() {
        JsonArrayRequest arrayJsonrequest = new JsonArrayRequest(
                JsonRequest.Method.GET,
                URL_comments,
                null,
                new Response.Listener<JSONArray>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            num_comments.setText("Comentarios "+response.length());
                            Comments_post = new ArrayList<>();
                            LinearLayoutManager layout = new LinearLayoutManager(getApplicationContext());
                            layout.setOrientation(LinearLayoutManager.VERTICAL);
                            JSONObject comment;
                            for (int i = 0; i < response.length(); i++) {
                                comment = response.getJSONObject(i);
                                Comments_post.add(new Comment(comment.getString("id"), comment.getString("userId"), " ", comment.getString("text"),comment.getString("createdAt").split("T")[0]));
                            }
                            adapt = new CommentAdapter(Comments_post);
                            adapt.setOnCommentListener(new OnCommentListener() {
                                @Override
                                public void onEliminateClicked(int position, View vista) {
                                    Toast.makeText(getApplicationContext(), "Elimino este comentario", Toast.LENGTH_SHORT).show();
                                    eliminar_comentari(vista);
                                }

                                @Override
                                public void onVerCommentsClicked(View vista) {
                                    Toast.makeText(getApplicationContext(), "Viendo comentarios", Toast.LENGTH_SHORT).show();


                                }
                            });
                            comments.setAdapter(adapt);
                            comments.setLayoutManager(layout);

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
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", ul.getAPI_KEY()); //valor de V ha de ser el de la var global
                return params;
            }
        };
        requestqueue.add(arrayJsonrequest);
    }

    private void eliminar_comentari(View vista) {
        final String id_comment = Comments_post.get(comments.getChildAdapterPosition(vista)).getId();
        final String user_comment = Comments_post.get(comments.getChildAdapterPosition(vista)).getUser();
        if (user_comment.equals(ul.getCorreo_user())) {
            AlertDialog.Builder alert = new AlertDialog.Builder(InfoPostLost.this);
            alert.setMessage("¿Estás seguro que deseas eliminar este Comentario?")
                    .setCancelable(false)
                    .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            JsonObjectRequest objectJsonrequest = new JsonObjectRequest(
                                    JsonRequest.Method.DELETE,
                                    URL_comments+"/"+id_comment,
                                    null,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {

                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Toast.makeText(getApplicationContext(), "ERROOOOOOOR EN ELIMINAR COMENTARIO", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            ) {
                                @Override
                                public Map<String, String> getHeaders() {
                                    Map<String, String> params = new HashMap<>();
                                    params.put("Content-Type", "application/json");
                                    params.put("Authorization", ul.getAPI_KEY());
                                    return params;
                                }
                            };
                            requestqueue.add(objectJsonrequest);
                            recreate();

                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            AlertDialog title = alert.create();
            title.setTitle("ELIMINAR COMENTARIO");
            title.show();

        } else
          Toast.makeText(getApplicationContext(), "COMENTARIO NO CREADO POR TI, NO PUEDES BORRARLO", Toast.LENGTH_SHORT).show();
    }

    private void tancar_post() {
        //Toast.makeText(getApplicationContext(), "Cierro el post", Toast.LENGTH_SHORT).show();

        if (mail_creador.equals(ul.getCorreo_user())) {
            AlertDialog.Builder alert = new AlertDialog.Builder(InfoPostLost.this);
            alert.setMessage("¿Estás seguro que deseas cerrar este Post?")
                    .setCancelable(false)
                    .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            JsonObjectRequest objectJsonrequest = new JsonObjectRequest(
                                    JsonRequest.Method.PATCH,
                                    URL_close,
                                    null,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {

                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Toast.makeText(getApplicationContext(), "ERROOOOOOOR EN CERRAR POST", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            ) {
                                @Override
                                public Map<String, String> getHeaders() {
                                    Map<String, String> params = new HashMap<>();
                                    params.put("Authorization", ul.getAPI_KEY());
                                    return params;
                                }
                            };
                            requestqueue.add(objectJsonrequest);
                            recreate();

                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            AlertDialog title = alert.create();
            title.setTitle("CERRAR POST");
            title.show();

        } else
            Toast.makeText(getApplicationContext(), "POST NO CREADO POR EL TI, NO PUEDES BORRARLO", Toast.LENGTH_SHORT).show();

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
                                getMenuInflater().inflate(R.menu.menu_infopostlikeuser, menu);
                            } else {
                                Toast.makeText(getApplicationContext(), "MENU NO FAVORITO", Toast.LENGTH_SHORT).show();
                                getMenuInflater().inflate(R.menu.menu_infopostuser, menu);
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
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
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

            case R.id.icono_delete:

                BackDelete();

                break;
        }
        return true;
    }

    private void BackDelete() {
        if (mail_creador.equals(ul.getCorreo_user())) {

            AlertDialog.Builder alert = new AlertDialog.Builder(InfoPostLost.this);
            alert.setMessage("¿Estás seguro que deseas eliminar este Post?")
                    .setCancelable(false)
                    .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            JsonObjectRequest objectJsonrequest = new JsonObjectRequest(
                                    JsonRequest.Method.DELETE,
                                    URL,
                                    null,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            Toast.makeText(getApplicationContext(), "DELETE", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Toast.makeText(getApplicationContext(), "ERROR BORRAR", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            ) {
                                @Override
                                public Map<String, String> getHeaders() {
                                    Map<String, String> params = new HashMap<>();
                                    params.put("Authorization", ul.getAPI_KEY());
                                    return params;
                                }
                            };
                            requestqueue.add(objectJsonrequest);

                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            AlertDialog title = alert.create();
            title.setTitle("ELIMINAR POST");
            title.show();

        } else
            Toast.makeText(getApplicationContext(), "POST NO CREADO POR EL TI, NO PUEDES BORRARLO", Toast.LENGTH_SHORT).show();
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
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
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
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", ul.getAPI_KEY()); //valor de V ha de ser el de la var global
                return params;
            }
        };
        requestqueue.add(objectJsonrequest);
    }

    public void retrieveImage(String idImageFirebase) {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        //TODO: necessito recuperar l objecte desde el json. a child posarhi l indetificador guardat
        StorageReference storageReference = storage.getReferenceFromUrl("gs://whip-1553341713756.appspot.com/").child(idImageFirebase);

        //foto_post = (ImageView) findViewById(R.id.foto_postPerd);
        try {
            final File localFile = File.createTempFile("images", "jpg");
            storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    foto_post.setImageBitmap(bitmap);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                }
            });
        } catch (IOException ignored) {
        }
    }

}