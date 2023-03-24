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
   INFORMACIÓN DE LA APP.
1.- Consumiremos una Api Sleep. Queremos saber la probabilidad de sueño que nos da la app. Recibiremos notificaciones cada 6 minutos.
2.- Interpretaremos datos de ActivityRecognition. Con estas notificaciones, sacaremos una probabilidad del sueño.
3.- Trabajaremos con Broadcast Receivers. Importante para recibir mensajes de otras app.

    INFORMACIÓN DE LA API. Android Sleep Api
1 Ayuda a las aplicaciones a conocer cuando el usuario está dormido o despierto
2 A partir de Android Api 29 (Android 10). Diferenciaremos los permisos menor api 29 y mayor o igual.
3 Necesitamos el permiso de ACTIVITY_RECOGNITION para api mayor o igual a 29
4 Usar requestSleepSegmentUpdates() para recibir actualizaciones del comportamiento del sueño.

    INFORMACIÓN DE LOS EVENTOS QUE TENDREMOS QUE GESTIONAR:
 - SleepClasifyEvent:  Representa el estado del sueño. Se envía cada 6 minutos. Se vasa a partir del nivel
 de luz, movimiento y da una confidencialidad entre 0 y 100% de si el usuario está dormido o no.
 - SleepSegmentEvent: Representa un resumen de los datos de sueño una vez que el usuario despierta.

    INFORMACIÓN DE LOS PERMISOS
 - API Dispositivo menor de Android 10 (UTILIZAREMOS UN PERMISO DIFERENTE AL DE MAYOR O IGUAL QUE ANDROID 10. Mirar el manifest

 *******************************PASOS********************************:

 1.- GESTIÓN DE PERMISOS

 Debemos tener un targetSdk >= 29. Ponemos una 33


  ------- INFORMACIÓN IMPORTANTE SOBRE LA PETICIÓN DE PERMISOS. ESTO TE VALDRÁ PARA CUALQUIER PERMISO --------
    Cuando un usuario acepta/deniega una solicitud, el array grantResults tiene un valor en la posicíon 0 que dice si está aceptado o denegado.
    Con -1, nos dice que está denegado.

    La primera vez de petición de permisos, será el sistema el encargado de solicitarlos. Si los denegamos, tendremos que ser nosotros mediante
    una interfaz de usuario, el que tenga que volver a pedirlos. La idea es:

    1.- El sistema nos pide permisos. Si se los concedemos, ya podemos utilizar la app.
    2.- Si no hemos concedido los permisos, tendremos que recurrir a solicitarselos mediante un Dialogo. Si se los concedemos, ya podemos
    utilizar la app. Si volvemos a denegarlos, tendremos la opción y obligación de habilitarle los permisos de manera manual con el setting.


 ¿Cómo funciona la petición y respuesta de permisos?

    Cuando el usuario selecciona una opción en la petición del permiso, invoca al método onRequestPermissionsResult.

    1.- Se llamará a este método automáticamente cuando se acepta/deniega el permiso solicitado mediante ActivityCompat.requestPermissions. Esta petición de
    permisos, se hace a cuenta del sistema. La primera vez.
    2.- Este método se llama automáticamente cuando se acepta/deniega el permiso controlado por el diálogo showRationalDialog(this);
    Esto pasó, cuando el sistema en la primera vez y mediante un requestPermissions, pidió el permiso y el usuario lo canceló. Se volvió a solicitar pero
    mediante un formulario llamandoa showRationalDialog(this). En este caso, al estar de nuevo cancelado, se volverá a pedir mediante settings
    con el método showSettingsDialog(this). Esto abrirá la configuración de la app, para que el usuario pueda aceptar el permiso. De lo contrario,
    siempre se mostrará el settings de la app. En el momento que el usuario acepte de manera manual mediante el settings, quedará registrado por
    el PackageManager y se podrá utilizar la app sin problema.

    ________________ Por tanto:
    Si el usuario después de la primera solicitud mediante requestPermission, concedió el permiso, mostramos un mensaje y trabamos.
    Si el usuario denegó la solicitud de permiso que mostró el diálogo showRationalDialog(this). Volvemos a solicitarlo mediante settings de manera manual. Invocamos
    al método showSettingsDialog(this)

________________________________SIN TERMINAR--------------------------------------------
 2.- Suscribirse a Sleep Data API mediante Broadcast Receiver.
 3.- Actualizar UI desde Broadcast Receiver.
 4.- Testear la UI sin necesitad de eventos Sleep.

 */



class MainActivity : AppCompatActivity() {

    //En Kotlin, hay que empaquetar las constantes en un objeto de tipo object.
    companion object{
        private const val TAG = "SleepTrackerActoivity" //para filtrar los mensages del Log
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission();  //comprobamos los permisos.
    }


    /*
    Comprobamos los permisos.
    1.- Si estan dados los permisos, ya podemos trabajar con la app recibiendo la actividad de sueño.
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
    ------- INFORMACIÓN IMPORTANTE SOBRE LA PETICIÓN DE PERMISOS. ESTO TE VALDRÁ PARA CUALQUIER PERMISO --------
    Cuando un usuario acepta/deniega una solicitud, el array grantResults tiene un valor en la posicíon 0 que dice si está aceptado o denegado.
    Con -1, nos dice que está denegado.

    La primera vez de petición de permisos, será el sistema el encargado de solicitarlos. Si los denegamos, tendremos que ser nosotros mediante
    una interfaz de usuario, el que tenga que volver a pedirlos. La idea es:

    1.- El sistema nos pide permisos. Si se los concedemos, ya podemos utilizar la app.
    2.- Si no hemos concedido los permisos, tendremos que recurrir a solicitarselos mediante un Dialogo. Si se los concedemos, ya podemos
    utilizar la app. Si volvemos a denegarlos, tendremos la opción y obligación de habilitarle los permisos de manera manual con el setting.


 ¿Cómo funciona la petición y respuesta de permisos?

    Cuando el usuario selecciona una opción en la petición del permiso, invoca al método onRequestPermissionsResult.

    1.- Se llamará a este método automáticamente cuando se acepta/deniega el permiso solicitado mediante ActivityCompat.requestPermissions. Esta petición de
    permisos, se hace a cuenta del sistema. La primera vez.
    2.- Este método se llama automáticamente cuando se acepta/deniega el permiso controlado por el diálogo showRationalDialog(this);
    Esto pasó, cuando el sistema en la primera vez y mediante un requestPermissions, pidió el permiso y el usuario lo canceló. Se volvió a solicitar pero
    mediante un formulario llamandoa showRationalDialog(this). En este caso, al estar de nuevo cancelado, se volverá a pedir mediante settings
    con el método showSettingsDialog(this). Esto abrirá la configuración de la app, para que el usuario pueda aceptar el permiso. De lo contrario,
    siempre se mostrará el settings de la app. En el momento que el usuario acepte de manera manual mediante el settings, quedará registrado por
    el PackageManager y se podrá utilizar la app sin problema.

    ________________ Por tanto:
    Si el usuario después de la primera solicitud mediante requestPermission, concedió el permiso, mostramos un mensaje y trabamos.
    Si el usuario denegó la solicitud de permiso que mostró el diálogo showRationalDialog(this). Volvemos a solicitarlo mediante settings de manera manual. Invocamos
    al método showSettingsDialog(this)

     */




    /*
    MÉTODO QUE COMPRUEBA EL RESULTADO DE LA ELECCIÓN DE ACEPTACIÓN/DENEGACIÓN DE PERMISOS.
    SE LLAMARÁ CUANDO:
     1.- CUADNO SE ACEPTE/DENIEGUE LOS PERMISOS DESDE EL DIÁLOGO DE IU.
     2.- CUANDO SE ACEPTE/DENIEGUE LOS PERMISOS SOLICITADOS POR EL SISTEMA.
     */

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //SI LA RESPUESTA VIENE DE LA IU Y SE DENEGARON LOS PERMISOS, MOSTRAMOS SETTINGS.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACTIVITY_RECOGNITION).not() &&
            grantResults.size == 1 &&
            grantResults[0] == PackageManager.PERMISSION_DENIED){
            showSettingsDialog(this)
        }else if(requestCode == PERMISSION_REQUEST_ACTIVITY_RECOGNITION &&
            permissions.contains(Manifest.permission.ACTIVITY_RECOGNITION) &&
            grantResults.size == 1 &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED){
            //SI LA RESPUESTA VIENE DE LA SOLICITADA POR EL SISTEMA Y LOS PERMISOS FUERON CONCEDIDOS.
            Log.d(TAG, "Permisos concedidos")
            requestSleepTracking()  //empieza la fiesta
        }

    }

}