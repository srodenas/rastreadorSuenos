# Ejemplo de broadcast receiver con tratamiento de permisos

Versión no acabada (BY SANTI - PMDM 22/23)
```
INFORMACIÓN DE LA APP.
1.- Consumiremos una Api Sleep. Queremos saber la probabilidad de sueño que nos da la app. Recibiremos notificaciones cada 6 minutos.
2.- Trataremos los permisos dependiendo de la API
3.- Interpretaremos datos de ActivityRecognition. Con estas notificaciones, sacaremos una probabilidad del sueño.
4.- Trabajaremos con Broadcast Receivers. Importante para recibir mensajes de otras app.

```

- `1.- GESTIÓN DE PERMISOS`
'''
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

Por tanto:
    Si el usuario después de la primera solicitud mediante requestPermission, concedió el permiso, mostramos un mensaje y trabamos.
    Si el usuario denegó la solicitud de permiso que mostró el diálogo showRationalDialog(this). Volvemos a solicitarlo mediante settings de manera manual. Invocamos
    al método showSettingsDialog(this)
'''
- `2.- Suscribirse a Sleep Data API mediante Broadcast Receiver.`
- `3.- Actualizar UI desde Broadcast Receiver.`
- `4.- Testear la UI sin necesitad de eventos Sleep.`  

