package com.pmdm.virgen.rastreadorsueos.permission_sleep_api


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.pmdm.virgen.rastreadorsueos.R


const val PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 999;

/*
DEBEMOS DE CONCEDER LOS PERMISOS EN TIEMPO DE EJECUCIÓN.

- Si NO TENEMOS CONCEDIDO LOS PERMISOS, ES PORQUE NO NOS LO HA PEDIDO AÚN, LOS SOLICITAMOS POR PRIMERA VEZ.
- SINO, ES PORQUE YA NOS LOS PIDÍO Y SE LOS DENEGAMOS. EN ESTE CASO, PODEMOS VOLVER A PERMITIRLOS MEDIANTE UN FORMULARIO. LO MISMO QUE SETTINGS.
 */

fun Activity.requestPermission() {
    if (ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACTIVITY_RECOGNITION
        ).not()
    ) {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
            PERMISSION_REQUEST_ACTIVITY_RECOGNITION
        )

    }else{
        showRationalDialog(this);  //LOS DENEGAMOS Y VOLVEMOS A DARLE LA OPCIÓN DE CONCEDERLOS.
    }
}

/*
función que muestra un formulario personalizado.
 */
fun showRationalDialog(activity: Activity) {
    AlertDialog.Builder(activity).apply {
        setTitle(R.string.permission_dialog_title)
        setMessage(R.string.permission_dialog_message)
        setPositiveButton(R.string.permission_positive_button){ _, _ ->

                    //Volvemos a solicitarlos en tiempo de ejecución.
                    ActivityCompat.requestPermissions(
                            activity, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                            PERMISSION_REQUEST_ACTIVITY_RECOGNITION
                    )
            //fin expresión
        } //fin setPositive
        setNegativeButton(R.string.permission_negative_button){
                dialog, _ ->
                    dialog.dismiss();   //cerramos el formulario.

        } //fin setNegative

    }.run{
        create()
        show()
    }
}


/*
función que muestra un formulario personalizado para concesión de permisos, desde settings.
Este método se llamará siempre y cuando se haya denegado la opción de permisos tanto la primera vez
como con el formulario personalizado showRationalDialog. Llamará al método startAppSettings
que lanzará las preferencias del setting de la app. El usuario podrá aceptar el permiso y se
anotará por medio del
 */
fun showSettingsDialog(activity: Activity) {
    AlertDialog.Builder(activity).apply {
        setTitle(R.string.permission_dialog_title)
        setMessage(R.string.permission_dialog_message)
        setPositiveButton(R.string.permission_positive_button){
                _,_ ->

          startAppSettings(activity)
        //fin expresión
        } //fin setPositive
        setNegativeButton(R.string.permission_negative_button){
                dialog, _ ->
            dialog.dismiss();   //cerramos el formulario.

        } //fin setNegative

    }.run{
        create()
        show()
    }
}


/*
Este método abre un intent con los detalles de configuración de la app.
Aquí se podrá permitir de manera manual por el usuario la concesión del permiso.
 */
fun startAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri: Uri = Uri.fromParts("package", context.packageName, null)
    intent.data = uri
    context.startActivity(intent)

}


/*
Si la versión es anterior a la Q, no hay que pedir permisos en tiempo de ejecución
Si la versión es posterior o igual a Q, hay que Comprobar si se han dado los permisos en t. ejecución.
 */

fun Activity.isPermissionGranted():Boolean{
    val isAndroidQOrLater : Boolean = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    return if (isAndroidQOrLater.not()){
        true  //NO HACE FALTA PEDIRLOS, SÓLO HACE FALTA TENERLOS EN EL MANIFEST
    }else{
        //DEBEMOS COMPROBAR SI LOS PERMISOS YA SE LOS DIMOS EN T. EJECUCIÓN.
        PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACTIVITY_RECOGNITION
        //DEVOLVERÁ FALSO, SI DENEGAMOS LOS PERMISOS EN TIEMPO DE EJECUCIÓN.
        )
    }
}

