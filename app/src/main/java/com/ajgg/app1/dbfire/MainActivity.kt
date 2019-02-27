package com.ajgg.app1.dbfire

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    // para filtrar los logs
    val TAG = "Servicio"

    // referencia de la base de datos
    private var databasePreguntas: DatabaseReference? = null
    private var databaseJugadores: DatabaseReference? = null
    // Token del dispositivo
    private var FCMToken: String? = null
    // key unica creada automaticamente al añadir un child
    lateinit var key: String
    // para actualizar los datos necesito un hash map
    val hashRespuesta = HashMap<String, Any>()
    val hashPuntuacion = HashMap<String, Any>()
    lateinit var objPreguntas: Preguntas
    var n: Int = 0
    var arrayPreguntas: ArrayList<Preguntas> = ArrayList()


    private var databasePuntuacion: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        tiempo.max = 100

        boSi.isEnabled = false
        boNo.isEnabled = false
        // referencia a la base de datos del proyecto en firebase
        databasePreguntas = FirebaseDatabase.getInstance().getReference("/juego")
        databasePuntuacion = FirebaseDatabase.getInstance().getReference("/puntuacion")
        databaseJugadores = FirebaseDatabase.getInstance().getReference("/jugadores")
        // boton de la plantilla
        hashPuntuacion.put(Build.MODEL + "_" + Build.DEVICE, 0)
        databasePuntuacion!!.updateChildren(hashPuntuacion)


        fab.setOnClickListener { view ->
            tiempo.visibility= View.VISIBLE
            if (n < arrayPreguntas.size) {
                hashRespuesta.put(Build.MODEL + "_" + Build.DEVICE, Respuestas(""))
                databaseJugadores!!.updateChildren(hashRespuesta)
                txtPregunta.text = arrayPreguntas[n].id
                boSi.isEnabled = true
                boNo.isEnabled = true
                tiempo.progress=100
                tiempo.visibility=View.VISIBLE
            } else {
                boSi.isEnabled = false
                boNo.isEnabled = false
            }
            GlobalScope.launch {
                while (tiempo.progress != 0) {
                    tiempo.progress = tiempo.progress - 1
                    delay(1000)
                }
                tiempo.visibility=View.INVISIBLE
            }
        }

        boSi.setOnClickListener { view ->
            ponerScore("verdadero")
            boSi.isEnabled = false
            boNo.isEnabled = false
        }

        boNo.setOnClickListener { view ->
            ponerScore("falso")
            boSi.isEnabled = false
            boNo.isEnabled = false
        }

        // solo lo llamo cuando arranco la app
        // evito que cuando se pasa por el onCreate vuelva a ejecutarse
        if (savedInstanceState == null) {
            try {
                // Obtengo el token del dispositivo.
                FCMToken = FirebaseInstanceId.getInstance().token

            } catch (e: Exception) {
                e.printStackTrace()
                Log.d(TAG, "Error escribiendo datos ${e}")
            }
        }
        // inicializo el listener para los eventos de la basededatos
        initPreguntas()
        initJugadores()
    }


    fun ponerScore(resp: String) {
        if (resp.equals(arrayPreguntas[n].respuesta)) {
            txtPregunta.text = "Correcto!"
            hashRespuesta.put(Build.MODEL + "_" + Build.DEVICE, Respuestas("true"))
            databaseJugadores!!.updateChildren(hashRespuesta)
        } else {
            txtPregunta.text = "Comes caca"
            hashRespuesta.put(Build.MODEL + "_" + Build.DEVICE, Respuestas("false"))
            databaseJugadores!!.updateChildren(hashRespuesta)
        }
        n++
    }


    /**
     * Listener para los distintos eventos de la base de datos
     */
    private fun initJugadores() {
        val childEventListener = object : ChildEventListener {
            override fun onChildRemoved(p0: DataSnapshot) {
                Log.d(TAG, "Datos borrados: " + p0.key)
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                Log.d(TAG, "prueba: " + (p0.getValue() as HashMap<*, *>).toString())
                Log.d("prueba", p0.child("score").getValue().toString())
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                Log.d(TAG, "Datos movidos")
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "Error cancelacion")
            }
        }
        // attach el evenListener a la basededatos
        databaseJugadores!!.addChildEventListener(childEventListener)
    }

    private fun initPreguntas() {
        val childEventListener = object : ChildEventListener {
            override fun onChildRemoved(p0: DataSnapshot) {
                Log.d(TAG, "Datos borrados: " + p0.key)
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                Log.d(TAG, "Datos cambiados: " + (p0.getValue() as HashMap<*, *>).toString())
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                Log.d(TAG, "Datos movidos")
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                // onChildAdded() capturamos la key
                objPreguntas = p0.getValue(Preguntas::class.java)!!
                arrayPreguntas.add(objPreguntas)
                Log.d("preguntass", arrayPreguntas.toString())
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "Error cancelacion")
            }
        }
        // attach el evenListener a la basededatos
        databasePreguntas!!.addChildEventListener(childEventListener)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}