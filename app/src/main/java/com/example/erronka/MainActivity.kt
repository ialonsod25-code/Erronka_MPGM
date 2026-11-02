package com.example.erronka

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.erronka.ui.theme.ErronkaTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ErronkaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNabigazioa(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AppNabigazioa(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val navController = rememberNavController()
    var ekitaldiak by remember { mutableStateOf<List<Ekitaldia>>(emptyList()) }
    var kargatzen by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        ekitaldiak = withContext(Dispatchers.IO) {
            JsonIrakurlea(context).irakurriEkitaldiak()
        }
        kargatzen = false
    }

    NavHost(
        navController = navController,
        startDestination = "zerrenda",
        modifier = modifier
    ) {
        composable("zerrenda") {
            Ekitaldiak(
                ekitaldiak = ekitaldiak,
                kargatzen = kargatzen,
                onEkitaldiaClick = { e -> navController.navigate("xehetasuna/${e.id}") }
            )
        }
        composable(
            route = "xehetasuna/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString("id")
            val ekitaldia = ekitaldiak.find { it.id == id }
            if (ekitaldia != null) {
                XehetasunaPantaila(ekitaldia, onAtzera = { navController.popBackStack() })
            }
        }
    }
}

@Composable
fun Ekitaldiak(
    ekitaldiak: List<Ekitaldia>,
    kargatzen: Boolean,
    onEkitaldiaClick: (Ekitaldia) -> Unit,
    modifier: Modifier = Modifier
) {
    var bilatu by rememberSaveable { mutableStateOf("") }
    var aukeratutakoMota by rememberSaveable { mutableStateOf<String?>(null) }

    val motak = remember(ekitaldiak) { ekitaldiak.map { it.mota }.distinct().sorted() }

    val iragazita = remember(ekitaldiak, bilatu, aukeratutakoMota) {
        ekitaldiak.filter {
            it.izena.contains(bilatu, ignoreCase = true) &&
                    (aukeratutakoMota == null || it.mota == aukeratutakoMota)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Arrigorriaga Kanping",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(20.dp, 40.dp, 16.dp, 0.dp)
            )
            Text(
                text = "Agenda Kulturala",
                fontSize = 35.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(20.dp, 8.dp, 16.dp, 0.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = bilatu,
                onValueChange = { bilatu = it },
                placeholder = { Text("Bilatu ekitaldiak") },
                singleLine = true,
                shape = RoundedCornerShape(50),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(45.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(motak) { mota ->
                    FilterChip(
                        selected = aukeratutakoMota == mota,
                        onClick = {
                            // Si pulsas el que ya está elegido, se quita el filtro
                            aukeratutakoMota = if (aukeratutakoMota == mota) null else mota
                        },
                        label = { Text(mota) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = koloreaMota(mota),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Text(
                text = "Ekitaldi guztiak",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.padding(20.dp, 16.dp, 16.dp, 8.dp)
            )

            if (kargatzen) {
                CircularProgressIndicator(modifier = Modifier.padding(20.dp))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(iragazita, key = { it.id }) { e ->
                        EkitaldiaCard(e, onClick = { onEkitaldiaClick(e) })
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(100.dp)
                .background(Color(0xFFE8E4DC))
                .border(
                    width = 3.dp,
                    color = Color(0xFF1B5E57)
                )
        )
    }
}


@Composable
fun EkitaldiaCard(e: Ekitaldia, onClick: () -> Unit) {
    val kolorea = koloreaMota(e.mota)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cuadrado de la fecha
            Column(
                modifier = Modifier
                    .size(72.dp)
                    .background(kolorea, RoundedCornerShape(12.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = hilabetea(e.hasieraData),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = eguna(e.hasieraData),
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = e.mota,
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.End)
                        .background(kolorea, RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
                Text(
                    text = e.izena,
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = e.herria, color = Color.DarkGray, fontSize = 13.sp)
                Text(
                    text = listOf(e.lekua, e.ordua)
                        .filter { it.isNotEmpty() }
                        .joinToString(" · "),
                    color = Color.DarkGray,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


@Composable
fun XehetasunaPantaila(e: Ekitaldia, onAtzera: () -> Unit) {
    val context = LocalContext.current
    val kolorea = koloreaMota(e.mota)
    val data = if (e.hasieraData == e.amaieraData) e.hasieraData
    else "${e.hasieraData}  →  ${e.amaieraData}"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF3E8))
            .verticalScroll(rememberScrollState())
    ) {
        // Cabecera verde
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color(0xFF1B5E57),
                    RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
                .padding(20.dp, 40.dp, 20.dp, 28.dp)
        ) {
            Text(
                text = "← Atzera",
                color = Color.White,
                fontSize = 16.sp,
                modifier = Modifier.clickable(onClick = onAtzera)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = e.mota,
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier
                    .background(kolorea, RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = e.izena,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DatuLerroa("Data", data)
            DatuLerroa("Ordua", e.ordua)
            DatuLerroa("Prezioa", e.prezioa)
            DatuLerroa("Hizkuntza", e.hizkuntzak)
            DatuLerroa("Lekua", e.lekua)
            DatuLerroa("Helbidea", e.helbidea)
            DatuLerroa(
                "Herria",
                listOf(e.herria, e.lurraldea).filter { it.isNotEmpty() }.joinToString(", ")
            )

            if (e.esteka.isNotEmpty()) {
                Button(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(e.esteka)))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Informazio gehiago") }
            }
        }
    }
}

@Composable
fun DatuLerroa(izenburua: String, balioa: String) {
    if (balioa.isBlank()) return
    Column {
        Text(izenburua, color = Color(0xFF1B5E57), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(balioa, color = Color.Black, fontSize = 17.sp)
    }
}

fun eguna(data: String): String =
    data.substringAfterLast("-").toIntOrNull()?.toString() ?: ""

fun hilabetea(data: String): String {
    val hilabeteak = listOf(
        "Urtarrila", "Otsaila", "Martxoa", "Apirila", "Maiatza", "Ekaina",
        "Uztaila", "Abuztua", "Iraila", "Urria", "Azaroa", "Abendua"
    )
    val zenbakia = data.split("-").getOrNull(1)?.toIntOrNull() ?: return ""
    return hilabeteak.getOrNull(zenbakia - 1)?.take(4) ?: ""
}

fun koloreaMota(mota: String): Color {
    val koloreak = listOf(
        Color(0xFFE8575A), Color(0xFFF0A74A), Color(0xFF3B82F6),
        Color(0xFF3FA66B), Color(0xFF8E6BD1), Color(0xFF2A9DA6)
    )
    return koloreak[(mota.hashCode() and Int.MAX_VALUE) % koloreak.size]
}

@Preview(showBackground = true)
@Composable
fun EkitaldiakPreview() {
    ErronkaTheme {
        Ekitaldiak(ekitaldiak = emptyList(), kargatzen = false, onEkitaldiaClick = {})
    }
}