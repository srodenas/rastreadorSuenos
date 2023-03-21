package com.pmdm.virgen.rastreadorsueos

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.pmdm.virgen.rastreadorsueos.permission_sleep_api.isPermissionGranted
import com.pmdm.virgen.rastreadorsueos.permission_sleep_api.requestPermission
import com.pmdm.virgen.rastreadorsueos.permission_sleep_api.showSettingsDialog
import android.Manifest
import com.pmdm.virgen.rastreadorsueos.permission_sleep_api.PERMISSION_REQUEST_ACTIVITY_RECOGNITION

/*
1.- Consumiremos una Api Sleep.
2.- Interpretaremos datos de ActivityRecognition
3.- Trabajaremos con Broadcast Receivers

Android Sleep Api
- Ayyda a las aplicaciones a conocer cuando el usuario es´´a dormido o despierto
- A partir de Android Api 29 (Android 10)
- Necesitamos el permiso de ACTIVITY_RECOGNITION
- Usar requestSleepSegmentUpdates() para recibir actualizaciones del comportamiento del sueño.

Eventos:
 - SleepClasifyEvent:  Representa el estado del sueño. Se envía cada 6 minutos. Se vasa a partir del nivel
 de luz, movimiento y da una confidencialidad entre 0 y 100% de si el usuario está dormido o no.
 - SleepSegmentEvent: Representa un resumen de los datos de sueño una vez que el usuario despierta.

 PERMISOS
 - API Dispositivo menor de Android 10 (UTILIZAREMOS UN PERMISO DIFERENTE AL DE MAYOR O IGUAL QUE ANDROID 10

 PASOS:
 1.- GESTIÓN DE PERMISOS
 2.- Suscribirse a Sleep Data API mediante Broadcast Receiver.
 3.- Actualizar UI desde Broadcast Receiver.
 4.- Testear la UI sin necesitad de eventos Sleep.

 Debemos tener un targetSdk >= 29. Ponemos una 33

 //No es necesario cerrar las sentencias con ; pero por semenjanza a java los pongo.
 */



class MainActivity : AppCompatActivity() {

    companion object{
        private const val TAG = "SleepTrackerActoivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission();
    }


    /*
    Comprobamos los permisos.
    1.- Si estan dados los permisos, ya que podemos trabajar con la app
    2.- Si no están dados los permisos, hay que solicitarlos en tiempo de ejecución.
     */
    private fun checkPermission(){
        if (isPermissionGranted()){  //Si los permisos ya están dados, simplemente pedidos datos a sleep api
            requestSleepTracking();
        }else{
            requestPermission();  //Si no se concedieron los permisos o se los cancelamos, los pedimos en tiempo de ejecución.
        }
    }

    private fun requestSleepTracking() {
        Log.d(TAG, "Comenzamos a pedir datos a nuestro Sleep API");
    }



    /*
    Cuando un usuario acepta/deniega una solicitud, el array grantResults tiene un valor en la posicíon 0 que dice si está aceptado o denegado.
    Con -1, nos dice que está denegado.

    Cuando el usuario selecciona una opción en la petición del permiso, invoca a onRequestPermissionsResult.
    1.- Este método se llama automáticamente cuando se acepta/deniega el permiso controlado por el diálogo showRationalDialog(this);
    Esto pasó, cuando el sistema mediante un requestPermissions, pidió el permiso y el usuario lo canceló. Se volvió a solicitar pero
    mediante un formulario del tipo sowhRationalDialog(this). En este caso, al estar de nuevo cancelado, se volverá a pedir mediante settings
    con el método swhoeSettingsDialog(this). Esto abrirá la configuración de la app, para que el usuario pueda aceptar el permiso. De lo contrario,
    siempre se mostrará el settings de la app. En el momento que el usuario acepte de manera manual mediante el settings, quedará registrado por
    el PackageManager y se podrá utilizar la app sin problema.

    2.- Tambén, se llamará a este método automáticamente cuando se acepta/deniega el permiso solicitado mediante ActivityCompat.requestPermissions.
    ________________ Por tanto:
    Si el usuario denegó la solicitud de permiso que mostró el diálogo showRationalDialog(this). Volvemos a solicitarlo mediante settings de manera manual
    Si el usuario después de la primera solicitud mediante requestPermission, concedió el permiso, mostramos un mensaje y trabamos.

     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            //Si Se denegó el formulario del showRationalDialog. Método administrado por tu cuenta.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
            Manifest.permission.ACTIVITY_RECOGNITION).not() &&
                grantResults.size == 1 &&
                grantResults[0] == PackageManager.PERMISSION_DENIED){
            showSettingsDialog(this)
        }else if(requestCode == PERMISSION_REQUEST_ACTIVITY_RECOGNITION &&
                permissions.contains(Manifest.permission.ACTIVITY_RECOGNITION) &&
                grantResults.size == 1 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED){
            //Si se concedió los permisos la primera vez, por medio del sistema.
            Log.d(TAG, "Permisos concedidos")
            requestSleepTracking()  //empieza la fiesta
        }

    }


}