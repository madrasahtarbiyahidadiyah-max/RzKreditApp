package com.example

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.KreditViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: KreditViewModel) {
  val isDarkTheme by viewModel.isDarkTheme.collectAsState()
  val totalNasabah by viewModel.totalNasabah.collectAsState()
  val totalUangDihutang by viewModel.totalUangDihutang.collectAsState()
  val totalUangMasuk by viewModel.totalUangMasuk.collectAsState()
  val totalPiutang by viewModel.totalPiutang.collectAsState()

  // State to toggle the interactive floating simulation modal / calculator card
  var showSimulationCalculator by remember { mutableStateOf(false) }

  // Simulator state parameters
  var simNominalStr by remember { mutableStateOf("10000000") }
  var simTenorBulanStr by remember { mutableStateOf("12") }
  var simBungaPersenStr by remember { mutableStateOf("1.5") }
  var simHasilAngsuran by remember { mutableStateOf(0.0) }

  // Calculation trigger helper
  fun hitungSimulasi() {
    val nominal = simNominalStr.toDoubleOrNull() ?: 0.0
    val tenor = simTenorBulanStr.toIntOrNull() ?: 1
    val bungaPersen = simBungaPersenStr.toDoubleOrNull() ?: 0.0
    
    val totalBunga = nominal * (bungaPersen / 100) * tenor
    val totalBayar = nominal + totalBunga
    simHasilAngsuran = if (tenor > 0) totalBayar / tenor else 0.0
  }

  // Live recalculation when inputs modify
  LaunchedEffect(simNominalStr, simTenorBulanStr, simBungaPersenStr) {
    hitungSimulasi()
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            AsyncImage(
              model = APP_LOGO_URL,
              contentDescription = "RzKredit Logo Avatar",
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.secondary, CircleShape),
              contentScale = ContentScale.Crop
            )
            Text(
              text = "Portal RzKredit",
              fontWeight = FontWeight.Black,
              fontSize = 18.sp,
              color = MaterialTheme.colorScheme.onBackground
            )
          }
        },
        actions = {
          IconButton(onClick = { viewModel.toggleTheme() }) {
            Icon(
              imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
              contentDescription = "Ganti Tema",
              tint = MaterialTheme.colorScheme.onBackground
            )
          }
          IconButton(onClick = { viewModel.logout() }) {
            Icon(
              imageVector = Icons.Default.ExitToApp,
              contentDescription = "Keluar",
              tint = MaterialTheme.colorScheme.error
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.border(width = 1.dp, color = MaterialTheme.colorScheme.outline)
      )
    }
  ) { innerPadding ->
    val configuration = LocalConfiguration.current
    val isLargeScreen = configuration.screenWidthDp >= 900

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(MaterialTheme.colorScheme.background)
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. GREETING HERO SECTION
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
          ),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Halo, RzKarim",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Selamat datang kembali di portal administrasi kredit premium.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Box(
              modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(ColorPayEmerald.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = "Verified Admin",
                tint = ColorPayEmerald,
                modifier = Modifier.size(28.dp)
              )
            }
          }
        }
      }

      // 2. SIMULASI KALKULATOR KREDIT BUTTON & CONTAINER
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
          ),
          border = BorderStroke(
            width = 1.5.dp,
            color = if (showSimulationCalculator) ColorPayEmerald else MaterialTheme.colorScheme.outline
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Button(
              onClick = { 
                showSimulationCalculator = !showSimulationCalculator 
                if (showSimulationCalculator) {
                  hitungSimulasi()
                }
              },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (showSimulationCalculator) ColorPayEmerald else MaterialTheme.colorScheme.primary
              )
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = if (showSimulationCalculator) Icons.Default.KeyboardArrowUp else Icons.Default.Calculate,
                  contentDescription = null
                )
                Text(
                  text = "Simulasi Kalkulator Kredit",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp
                )
              }
            }

            AnimatedVisibility(
              visible = showSimulationCalculator,
              enter = expandVertically() + fadeIn(),
              exit = shrinkVertically() + fadeOut()
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                
                Text(
                  text = "🧮 SIMULATOR INSTAN PREMIUM",
                  fontWeight = FontWeight.Black,
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.secondary
                )

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  OutlinedTextField(
                    value = simNominalStr,
                    onValueChange = { simNominalStr = it },
                    label = { Text("Besar Pinjaman (Rupiah)", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(8.dp)
                  )
                  OutlinedTextField(
                    value = simTenorBulanStr,
                    onValueChange = { simTenorBulanStr = it },
                    label = { Text("Tenor (Bulan)", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                  )
                }

                OutlinedTextField(
                  value = simBungaPersenStr,
                  onValueChange = { simBungaPersenStr = it },
                  label = { Text("Suku Bunga Flat per Bulan (%)", fontSize = 11.sp) },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(8.dp)
                )

                Card(
                  colors = CardDefaults.cardColors(
                    containerColor = ColorPayEmerald.copy(alpha = if (isDarkTheme) 0.15f else 0.1f)
                  ),
                  shape = RoundedCornerShape(12.dp),
                  border = BorderStroke(1.dp, ColorPayEmerald.copy(alpha = 0.4f)),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                    Text(
                      text = "ESTIMASI ANGSURAN BULANAN",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isDarkTheme) ColorPayEmerald else ColorPayEmeraldDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = formatRupiah(simHasilAngsuran),
                      fontSize = 22.sp,
                      fontWeight = FontWeight.Black,
                      color = if (isDarkTheme) ColorPayEmerald else ColorPayEmeraldDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                      text = "Pinjaman Utama: " + formatRupiah(simNominalStr.toDoubleOrNull() ?: 0.0) + " @ " + simTenorBulanStr + " bulan (" + simBungaPersenStr + "% flat bunga/bln)",
                      fontSize = 10.sp,
                      textAlign = TextAlign.Center,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
              }
            }
          }
        }
      }

      // 3. STATS HEADERS BLOCK
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "📊 STATISTIK KEUANGAN PORTAL",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
          )
          
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1f)) {
              StatWidget(
                title = "DEBITUR",
                value = "$totalNasabah",
                icon = Icons.Default.People,
                tintColor = if (isDarkTheme) ColorHoldSky else ColorHoldSkyDark
              )
            }
            Box(modifier = Modifier.weight(1f)) {
              StatWidget(
                title = "PIUTANG",
                value = formatRupiah(totalUangDihutang),
                icon = Icons.Default.Analytics,
                tintColor = if (isDarkTheme) ColorWarnAmber else ColorWarnAmberDark
              )
            }
          }
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1f)) {
              StatWidget(
                title = "MASUK",
                value = formatRupiah(totalUangMasuk),
                icon = Icons.Default.Paid,
                tintColor = if (isDarkTheme) ColorPayEmerald else ColorPayEmeraldDark
              )
            }
            Box(modifier = Modifier.weight(1f)) {
              StatWidget(
                title = "SALDO",
                value = formatRupiah(totalPiutang),
                icon = Icons.Default.TrendingDown,
                tintColor = if (isDarkTheme) ColorDangerRose else ColorDangerRoseDark
              )
            }
          }
        }
      }

      // 4. TABBED ADMINISTRATION CONTROLS OR SIDE-BY-SIDE
      item {
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
      }

      item {
        Text(
          text = "⚙️ FORMULIR ADMINISTRASI KREDIT",
          fontSize = 12.sp,
          fontWeight = FontWeight.Black,
          color = MaterialTheme.colorScheme.secondary
        )
      }

      item {
        if (isLargeScreen) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Box(modifier = Modifier.weight(1f)) {
              CardFormPeminjam(viewModel = viewModel)
            }
            Box(modifier = Modifier.weight(1f)) {
              CardFormSetoran(viewModel = viewModel)
            }
          }
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CardFormPeminjam(viewModel = viewModel)
            CardFormSetoran(viewModel = viewModel)
          }
        }
      }

      item {
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
      }

      // 5. DATA PERMANEN LIST (DATABASE AKTIF)
      item {
        Text(
          text = "📜 DAFTAR DEBITUR AKTIF (LOCAL ROOM DB)",
          fontSize = 12.sp,
          fontWeight = FontWeight.Black,
          color = MaterialTheme.colorScheme.secondary
        )
      }

      item {
        CardActiveDatabase(
          viewModel = viewModel,
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 350.dp, max = 600.dp)
        )
      }

      item {
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
      }

      // 6. RIWAYAT TRANSAKSI & CETAK STRUK JPG
      item {
        Text(
          text = "🧾 RIWAYAT SETORAN & CETAK STRUK (JPG)",
          fontSize = 12.sp,
          fontWeight = FontWeight.Black,
          color = MaterialTheme.colorScheme.secondary
        )
      }

      item {
        CardHistorySetoran(
          viewModel = viewModel,
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 350.dp, max = 500.dp)
        )
      }
    }
  }
}
