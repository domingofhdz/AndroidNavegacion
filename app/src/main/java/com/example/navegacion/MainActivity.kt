package com.example.navegacion

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.navegacion.ui.theme.NavegacionTheme
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NavegacionTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppContent(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}



class Preferencias(context: Context) {
    private val preferences = context.getSharedPreferences("app", Context.MODE_PRIVATE)

    fun guardar(prop: String, value: String) {
        preferences.edit()
            .putString(prop, value)
            .apply()
    }

    fun obtener(prop: String, def: String): String? {
        return preferences.getString(prop, def)
    }

    fun remover(prop: String) {
        preferences.edit()
            .remove(prop)
            .apply()
    }
}



@Composable
fun AppContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val preferencias = Preferencias(context)
    var destination = "LoginContent"

    if (preferencias.obtener("id", "") != "") {
        destination = "MenuContent"
    }



    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = destination) {
        composable("LoginContent") { LoginContent(navController, modifier) }
        composable("MenuContent") { MenuContent(navController, modifier) }
        composable("ProductosContent") { ProductosContent(navController, modifier) }
    }
}



data class ModeloCategoria(val id: Int, val nombre: String)
data class ModeloProducto(val id: Int, val nombre: String, val precio: Double, val idCategoria: Int, val nombreCategoria: String)

interface ApiService {
    @POST("iniciarSesion.php")
    @FormUrlEncoded
    suspend fun iniciarSesion(
        @Field("nombreUsuario") nombreUsuario: String,
        @Field("contrasena") contrasena: String
    ): Response<String>

    @GET("categorias.php?buscar&busqueda")
    suspend fun buscarCategorias(): List<ModeloCategoria>

    @POST("productos.php?registrar")
    @FormUrlEncoded
    suspend fun registrarProducto(
        @Field("nombre") nombre: String,
        @Field("precio") precio: Double,
        @Field("categoria") categoria: Int
    ): Response<Unit>

    @GET("productos.php?buscar&busqueda")
    suspend fun buscarProductos(): List<ModeloProducto>

    @POST("productos.php?modificar")
    @FormUrlEncoded
    suspend fun modificarProducto(
        @Field("id") id: Int,
        @Field("nombre") nombre: String,
        @Field("precio") precio: Double,
        @Field("categoria") categoria: Int
    ): Response<Unit>

    @POST("productos.php?eliminar")
    @FormUrlEncoded
    suspend fun eliminarProducto(
        @Field("id") id: Int
    ): Response<Unit>
}

val retrofit = Retrofit.Builder()
    .baseUrl("https://monitored-pays-archive-asks.trycloudflare.com/api/")
    .addConverterFactory(ScalarsConverterFactory.create())
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val api = retrofit.create(ApiService::class.java)



@Composable
fun LoginContent(navController: NavHostController, modifier: Modifier = Modifier) {
    val context = LocalContext.current



    val preferencias = Preferencias(context)



    val scope = rememberCoroutineScope()

    var usuario: String by remember { mutableStateOf("") }
    var contrasena: String by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {

        // Aquí irán nuestras cajas de texto y botones.
        Text(
            text = "Inicio de Sesión",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Usuario:")
        TextField(
            value = usuario,
            onValueChange = { usuario = it },
            placeholder = { Text("Ingresa tu usuario") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Contraseña:")
        TextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            placeholder = { Text("Ingresa tu contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {



                scope.launch {
                    try {
                        val respuesta : Response<String> = api.iniciarSesion(usuario, contrasena)
                        val id : String = respuesta.body().toString()

                        if (id.toDoubleOrNull() != null) {
                            preferencias.guardar("id", id)
                            navController.navigate("MenuContent")
                        }
                        else {
                            Toast.makeText(context, "Usuario y/o contraseña incorrecto(s).", Toast.LENGTH_SHORT).show()
                        }
                    }
                    catch (e: Exception) {
                        Log.e("API", "Error al iniciar sesión: ${e.message}")
                    }
                }



            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Iniciar sesión")
        }

    }
}

@Composable
fun MenuContent(navController: NavHostController, modifier: Modifier = Modifier) {
    val preferencias = Preferencias(LocalContext.current)



    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {

        // Aquí irán nuestras cajas de texto y botones.
        Text(
            text = "Menú",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                preferencias.remover("id")



                navController.navigate("LoginContent")
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Blue
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Cerrar sesión",
                style = TextStyle(textDecoration = TextDecoration.Underline),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Button(
            onClick = {
                navController.navigate("ProductosContent")
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Blue
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Productos",
                style = TextStyle(textDecoration = TextDecoration.Underline),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosContent(navController: NavHostController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()


    var id: String by remember { mutableStateOf("") }
    var nombre: String by remember { mutableStateOf("") }
    var precio: String by remember { mutableStateOf("") }

    val categorias = remember {
        mutableStateListOf<ModeloCategoria>()
    }
    var categoriaExpandido by remember { mutableStateOf(false) }
    var categoria by remember { mutableStateOf(ModeloCategoria(1, "")) }

    val productos = remember {
        mutableStateListOf<ModeloProducto>()
    }



    LaunchedEffect(Unit) {
        try {
            categorias.clear()
            categorias.addAll(api.buscarCategorias())
            categoria = categorias[0]

            productos.clear()
            productos.addAll(api.buscarProductos())
        }
        catch (e: Exception) {
            Log.e("API", "Error al buscar categorias: ${e.message}")
        }
    }



    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .horizontalScroll(scrollState),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {

        // Aquí irán nuestras cajas de texto y botones.
        Button(
            onClick = {
                navController.navigate("MenuContent")
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Blue
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Menú",
                style = TextStyle(textDecoration = TextDecoration.Underline),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Producto",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Id:")
        TextField(
            value = id,
            onValueChange = { id = it },
            placeholder = { Text("") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Nombre:")
        TextField(
            value = nombre,
            onValueChange = { nombre = it },
            placeholder = { Text("Ingresa el nombre") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Precio:")
        TextField(
            value = precio,
            onValueChange = { precio = it },
            placeholder = { Text("Ingresa el precio") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Categoría:")
        ExposedDropdownMenuBox(
            expanded = categoriaExpandido,
            onExpandedChange = { categoriaExpandido = !categoriaExpandido }
        ) {
            TextField(
                value = categoria.nombre,
                onValueChange = {},
                readOnly = true,
                label = { Text("Selecciona una opción") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriaExpandido)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            DropdownMenu(
                expanded = categoriaExpandido,
                onDismissRequest = { categoriaExpandido = false }
            ) {
                categorias.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion.nombre) },
                        onClick = {
                            categoria = opcion
                            categoriaExpandido = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {



                scope.launch {
                    try {
                        if (id == "") {
                            api.registrarProducto(nombre, precio.toDouble(), categoria.id)
                            Toast.makeText(context, "Producto registrado con éxito.", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            api.modificarProducto(id.toInt(), nombre, precio.toDouble(), categoria.id)
                            Toast.makeText(context, "Producto modificado con éxito.", Toast.LENGTH_SHORT).show()
                        }

                        id = ""
                        nombre = ""
                        precio = ""
                        categoria = categorias[0]

                        productos.clear()
                        productos.addAll(api.buscarProductos())
                    }
                    catch (e: Exception) {
                        Log.e("API", "Error al registrar producto: ${e.message}")
                    }
                }



            }
        ) {
            Text("Enviar")
        }
        Button(
            onClick = {
                id = ""
                nombre = ""
                precio = ""
                categoria = categorias[0]
            }
        ) {
            Text("Cancelar")
        }

        Row {
            Text("Nombre", modifier = Modifier.width(150.dp), fontWeight = FontWeight.Bold)
            Text("Precio", modifier = Modifier.width(100.dp), fontWeight = FontWeight.Bold)
            Text("Categoria", modifier = Modifier.width(100.dp), fontWeight = FontWeight.Bold)
            Text("Editar", modifier = Modifier.width(100.dp), fontWeight = FontWeight.Bold)
            Text("Eliminar", modifier = Modifier.width(100.dp), fontWeight = FontWeight.Bold)
        }
        Divider()
        productos.forEachIndexed { index, producto ->
            val bgColor = if (index % 2 == 0) Color(0xFFF5F5F5) else Color.White

            Row (
                modifier = Modifier
                    .background(bgColor)
            ) {
                Text(producto.nombre, modifier = Modifier
                    .width(150.dp)
                )
                Text("$ ${producto.precio}", modifier = Modifier
                    .width(100.dp)
                )
                Text(producto.nombreCategoria, modifier = Modifier
                    .width(100.dp)
                )
                Button(onClick = {
                    id = productos[index].id.toString()
                    nombre = productos[index].nombre
                    precio = productos[index].precio.toString()
                    categoria = categorias.find { it.id == producto.idCategoria } ?: categorias[0]
                }) {
                    Text("Editar")
                }
                Button(onClick = {



                    scope.launch {
                        try {
                            api.eliminarProducto(producto.id)
                            Toast.makeText(context, "Producto eliminado con éxito.", Toast.LENGTH_SHORT).show()
                            productos.removeAt(index)
                        }
                        catch (e: Exception) {
                            Log.e("API", "Error al registrar producto: ${e.message}")
                        }
                    }



                }) {
                    Text("Eliminar")
                }
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun AppContentPreview() {
    NavegacionTheme {
        AppContent()
    }
}