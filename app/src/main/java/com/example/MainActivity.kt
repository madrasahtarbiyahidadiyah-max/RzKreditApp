package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.PeminjamEntity
import com.example.ui.KreditViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
  private val viewModel: KreditViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val isDarkTheme by viewModel.isDarkTheme.collectAsState()
      
      MyApplicationTheme(darkTheme = isDarkTheme) {
        val context = LocalContext.current
        
        // Listen to View Model toast / broadcast events
        LaunchedEffect(Unit) {
          viewModel.toastMessage.collectLatest { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
          }
        }

        val isLoggedIn by viewModel.isLoggedIn.collectAsState()

        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          if (!isLoggedIn) {
            LoginScreen(viewModel = viewModel)
          } else {
            DashboardScreen(viewModel = viewModel)
          }
        }
      }
    }
  }
}

// FORMATTER FOR INDONESIA RUPIAH
fun formatRupiah(value: Double): String {
  val formatter = java.text.DecimalFormat.getCurrencyInstance(java.util.Locale("id", "ID"))
  val decimalFormatSymbols = (formatter as java.text.DecimalFormat).decimalFormatSymbols
  decimalFormatSymbols.currencySymbol = "Rp "
  formatter.decimalFormatSymbols = decimalFormatSymbols
  formatter.maximumFractionDigits = 0
  return formatter.format(value)
}

// LOGO CONFIGURATION STRINGS
const val APP_LOGO_URL = "https://i.postimg.cc/sfNDS3Mn/Whats-App-Image-2026-06-02-at-10-33-41.jpg"

@Composable
fun LoginScreen(viewModel: KreditViewModel) {
  val surfaceColor = MaterialTheme.colorScheme.surface
  val isDark by viewModel.isDarkTheme.collectAsState()

  var usernameInput by remember { mutableStateOf("") }
  var passwordInput by remember { mutableStateOf("") }
  var isPasswordVisible by remember { mutableStateOf(false) }
  var rememberMe by remember { mutableStateOf(true) }

  val loginError by viewModel.loginError.collectAsState()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Card(
      modifier = Modifier
        .widthIn(max = 400.dp)
        .fillMaxWidth(),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = surfaceColor),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      border = BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outline
      )
    ) {
      Column(
        modifier = Modifier
          .padding(32.dp)
          .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // WhatsApp logo
        AsyncImage(
          model = APP_LOGO_URL,
          contentDescription = "RzKredit Logo",
          modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
          contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Login RzKredit",
          fontSize = 22.sp,
          fontWeight = FontWeight.Black,
          color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Portal Administrasi Kredit Mobile",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Username Input
        Text(
          text = "USERNAME",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
          modifier = Modifier.fillMaxWidth(),
          textAlign = TextAlign.Start
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = usernameInput,
          onValueChange = { usernameInput = it },
          placeholder = { Text("Contoh: rzkarim") },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth(),
          leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
          )
        )

        Spacer(modifier = Modifier.height(16.dp))

        //Password Input
        Text(
          text = "KATA SANDI",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
          modifier = Modifier.fillMaxWidth(),
          textAlign = TextAlign.Start
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = passwordInput,
          onValueChange = { passwordInput = it },
          placeholder = { Text("••••••••") },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth(),
          leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
          trailingIcon = {
            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
              Icon(
                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = null
              )
            }
          },
          visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
          )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Remember Me checkbox
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { rememberMe = !rememberMe },
          verticalAlignment = Alignment.CenterVertically
        ) {
          Checkbox(
            checked = rememberMe,
            onCheckedChange = { rememberMe = it },
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
          )
          Text(
            text = "Ingat Login",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground
          )
        }

        if (loginError != null) {
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = loginError ?: "",
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
          onClick = {
            viewModel.checkLogin(usernameInput, passwordInput, rememberMe)
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text(
            text = "MASUK DASHBOARD",
            fontWeight = FontWeight.Black,
            fontSize = 14.sp
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Demo credentials helper
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "Kredensial Admin:\nUsername: rzkarim  |  Sandi: rzkarim123",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
              .fillMaxWidth()
              .padding(8.dp)
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: KreditViewModel) {
  val isDarkTheme by viewModel.isDarkTheme.collectAsState()
  val totalNasabah by viewModel.totalNasabah.collectAsState()
  val totalUangDihutang by viewModel.totalUangDihutang.collectAsState()
  val totalUangMasuk by viewModel.totalUangMasuk.collectAsState()
  val totalPiutang by viewModel.totalPiutang.collectAsState()
  val listNasabah by viewModel.listNasabah.collectAsState()

  // Layout screen adaptivity check
  val configuration = LocalConfiguration.current
  val isLargeScreen = configuration.screenWidthDp >= 900

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            AsyncImage(
              model = APP_LOGO_URL,
              contentDescription = "Avatar",
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.secondary, CircleShape),
              contentScale = ContentScale.Crop
            )
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "RzKredit",
                  fontWeight = FontWeight.Black,
                  fontSize = 18.sp,
                  color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Mobile",
                  fontWeight = FontWeight.Black,
                  fontSize = 16.sp,
                  color = MaterialTheme.colorScheme.secondary
                )
              }
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(ColorPayEmerald)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                  text = "Dashboard Input Kredit",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
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
    if (isLargeScreen) {
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .background(MaterialTheme.colorScheme.background)
          .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
      ) {
        // Left Column (7/12 width approximation)
        Column(
          modifier = Modifier
            .weight(7f)
            .fillMaxHeight(),
          verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
          // Stats Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
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

          // Input Forms Side-by-Side
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
          ) {
            Box(modifier = Modifier.weight(1f)) {
              CardFormPeminjam(viewModel = viewModel)
            }
            Box(modifier = Modifier.weight(1f)) {
              CardFormSetoran(viewModel = viewModel)
            }
          }
        }

        // Right Column (5/12 width approximation) - Database Nasabah
        Column(
          modifier = Modifier
            .weight(5f)
            .fillMaxHeight()
        ) {
          CardActiveDatabase(viewModel = viewModel, modifier = Modifier.fillMaxSize())
        }
      }
    } else {
      // Mobile compact view with beautiful tab ergonomics
      var selectedTab by remember { mutableStateOf(0) }

      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .background(MaterialTheme.colorScheme.background)
      ) {
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = MaterialTheme.colorScheme.surface,
          contentColor = MaterialTheme.colorScheme.primary
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = { Text("📊 INPUT & STATS", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = { Text("📜 DATABASE AKTIF", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
          )
        }

        if (selectedTab == 0) {
          // Stats Grid & Forms Stack inside Scrollable Layout
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            item {
              // Grid-like dynamic Stats
              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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

            item {
              CardFormPeminjam(viewModel = viewModel)
            }

            item {
              CardFormSetoran(viewModel = viewModel)
            }
          }
        } else {
          // Active customer interactive list
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(16.dp)
          ) {
            CardActiveDatabase(viewModel = viewModel, modifier = Modifier.fillMaxSize())
          }
        }
      }
    }
  }
}

// RENDER A POLISHED GLOWING STAT CARD
@Composable
fun StatWidget(
  title: String,
  value: String,
  icon: ImageVector,
  tintColor: Color
) {
  val surfaceColor = MaterialTheme.colorScheme.surface
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = surfaceColor),
    modifier = Modifier
      .fillMaxWidth()
      .heightIn(min = 100.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(
      width = 1.dp,
      color = MaterialTheme.colorScheme.outline
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(14.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          fontSize = 10.sp,
          fontWeight = FontWeight.Black,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          letterSpacing = 0.5.sp
        )
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(tintColor.copy(alpha = 0.12f))
            .border(1.dp, tintColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tintColor,
            modifier = Modifier.size(14.dp)
          )
        }
      }
      Text(
        text = value,
        fontSize = 18.sp,
        fontWeight = FontWeight.Black,
        color = tintColor,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        modifier = Modifier.padding(top = 10.dp)
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardFormPeminjam(viewModel: KreditViewModel) {
  val surfaceColor = MaterialTheme.colorScheme.surface
  val isDarkTheme by viewModel.isDarkTheme.collectAsState()
  
  val inputNama by viewModel.namaInput.collectAsState()
  val inputNominal by viewModel.nominalInput.collectAsState()
  val inputTenor by viewModel.tenorInput.collectAsState()

  // Dropdown states
  var nominalExpanded by remember { mutableStateOf(false) }
  var tenorExpanded by remember { mutableStateOf(false) }

  val nominalOptions = listOf(
    1000000.0, 1500000.0, 2000000.0, 2500000.0,
    3000000.0, 3500000.0, 4000000.0, 4500000.0, 5000000.0
  )
  val tenorOptions = listOf(12, 24)

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = surfaceColor),
    modifier = Modifier.fillMaxWidth(),
    border = BorderStroke(
      width = 1.dp,
      color = MaterialTheme.colorScheme.outline
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "1. Pendaftaran Peminjam Baru",
          fontWeight = FontWeight.Black,
          fontSize = 14.sp,
          color = if (isDarkTheme) ColorHoldSky else ColorHoldSkyDark
        )
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text("KONTRAK BARU", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }

      // Nama Lengkap input
      Column {
        Text(
          text = "Nama Lengkap",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = inputNama,
          onValueChange = { viewModel.namaInput.value = it },
          placeholder = { Text("Contoh: Lutfia Karim") },
          singleLine = true,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isDarkTheme) ColorHoldSky else ColorHoldSkyDark,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
          )
        )
      }

      // Nominal Selection
      Column {
        Text(
          text = "Nominal Peminjaman",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
          OutlinedTextField(
            value = "${formatRupiah(inputNominal)} (Angsuran: ${formatRupiahShort(viewModel.hitungAngsuran(inputNominal))})",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { nominalExpanded = true }) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { nominalExpanded = true },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = if (isDarkTheme) ColorHoldSky else ColorHoldSkyDark,
              unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
          )
          DropdownMenu(
            expanded = nominalExpanded,
            onDismissRequest = { nominalExpanded = false },
            modifier = Modifier.fillMaxWidth(0.95f)
          ) {
            nominalOptions.forEach { nominal ->
              val angsuranVal = viewModel.hitungAngsuran(nominal)
              DropdownMenuItem(
                text = { Text("${formatRupiah(nominal)} (Angsuran: ${formatRupiahShort(angsuranVal)})") },
                onClick = {
                  viewModel.nominalInput.value = nominal
                  nominalExpanded = false
                }
              )
            }
          }
        }
      }

      // Tenor Selection
      Column {
        Text(
          text = "Kontrak Tenor",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
          OutlinedTextField(
            value = "$inputTenor Bulan (${inputTenor / 12} Tahun)",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { tenorExpanded = true }) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { tenorExpanded = true },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = if (isDarkTheme) ColorHoldSky else ColorHoldSkyDark,
              unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
          )
          DropdownMenu(
            expanded = tenorExpanded,
            onDismissRequest = { tenorExpanded = false }
          ) {
            tenorOptions.forEach { tenor ->
              DropdownMenuItem(
                text = { Text("$tenor Bulan (${tenor / 12} Tahun)") },
                onClick = {
                  viewModel.tenorInput.value = tenor
                  tenorExpanded = false
                }
              )
            }
          }
        }
      }

      // Monthly Estimate calculations
      val estAngsuran = viewModel.hitungAngsuran(inputNominal)
      val totalKontrak = estAngsuran * inputTenor
      Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Angsuran per Bulan:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatRupiah(estAngsuran), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDarkTheme) ColorHoldSky else ColorHoldSkyDark)
          }
          Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Tagihan akhir:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatRupiah(totalKontrak), fontSize = 11.sp, fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }

      Button(
        onClick = { viewModel.simpanPeminjam() },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isDarkTheme) ColorHoldSky else ColorHoldSkyDark,
          contentColor = if (isDarkTheme) Color(0xFF060913) else Color.White
        )
      ) {
        Text("SIMPAN KONTRAK BARU", fontWeight = FontWeight.Black, fontSize = 13.sp)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardFormSetoran(viewModel: KreditViewModel) {
  val surfaceColor = MaterialTheme.colorScheme.surface
  val listNasabah by viewModel.listNasabah.collectAsState()
  val isDarkTheme by viewModel.isDarkTheme.collectAsState()

  val selectedNasabahName by viewModel.setoranNamaSelection.collectAsState()
  val inputJumlahSetoran by viewModel.setoranJumlahInput.collectAsState()

  var dropdownExpanded by remember { mutableStateOf(false) }

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = surfaceColor),
    modifier = Modifier.fillMaxWidth(),
    border = BorderStroke(
      width = 1.dp,
      color = if (isDarkTheme) ColorPayEmerald.copy(alpha = 0.6f) else ColorPayEmeraldDark
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "2. Input Setoran Angsuran",
          fontWeight = FontWeight.Black,
          fontSize = 14.sp,
          color = if (isDarkTheme) ColorPayEmerald else ColorPayEmeraldDark
        )
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isDarkTheme) ColorPayEmerald.copy(alpha = 0.15f) else ColorPayEmeraldDark.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text("SETORAN", fontSize = 9.sp, fontWeight = FontWeight.Black, color = if (isDarkTheme) ColorPayEmerald else ColorPayEmeraldDark)
        }
      }

      // GLOOWING CASHIER TERMINAL SCREEN
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(if (isDarkTheme) ObsidianScreen else PearlScreen)
          .border(
            width = 2.dp,
            color = if (isDarkTheme) ColorPayEmerald else ColorPayEmeraldDark,
            shape = RoundedCornerShape(12.dp)
          )
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "ANGSURAN PER BULAN (RP)",
            color = if (isDarkTheme) ColorPayEmerald.copy(alpha = 0.7f) else Color(0xFF34D399),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
          )
          Text(
            text = viewModel.getAngsuranWajibFormattedForLayar(),
            color = if (isDarkTheme) ColorPayEmerald else Color(0xFF10b981),
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.End,
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 4.dp)
          )
        }
      }

      // 选择 Borrower Dropdown
      Column {
        Text(
          text = "Pilih Nama Peminjam",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
          OutlinedTextField(
            value = if (selectedNasabahName.isEmpty()) "-- Pilih Nasabah --" else selectedNasabahName,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.clickable { dropdownExpanded = true }) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { dropdownExpanded = true },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = if (isDarkTheme) ColorPayEmerald else ColorPayEmeraldDark,
              unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
          )
          DropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false },
            modifier = Modifier.fillMaxWidth(0.95f)
          ) {
            if (listNasabah.isEmpty()) {
              DropdownMenuItem(
                text = { Text("Tidak ada nasabah aktif") },
                onClick = { dropdownExpanded = false }
              )
            } else {
              listNasabah.forEach { nasabah ->
                DropdownMenuItem(
                  text = { Text("${nasabah.nama} (Sisa: ${nasabah.sisaTenor} bln)") },
                  onClick = {
                    viewModel.setoranNamaSelection.value = nasabah.nama
                    dropdownExpanded = false
                  }
                )
              }
            }
          }
        }
      }

      // Quantity amount input
      Column {
        Text(
          text = "Jumlah Setoran (Rp)",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = inputJumlahSetoran,
          onValueChange = { viewModel.setoranJumlahInput.value = it },
          placeholder = { Text("Akan terisi otomatis atau ketik manual") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isDarkTheme) ColorPayEmerald else ColorPayEmeraldDark,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
          )
        )
      }

      Button(
        onClick = { viewModel.simpanSetoran() },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isDarkTheme) ColorPayEmerald else ColorPayEmeraldDark,
          contentColor = Color.White
        )
      ) {
        Text("SIMPAN SETORAN TUNAI", fontWeight = FontWeight.Black, fontSize = 13.sp)
      }
    }
  }
}

@Composable
fun CardActiveDatabase(viewModel: KreditViewModel, modifier: Modifier = Modifier) {
  val listNasabah by viewModel.listNasabah.collectAsState()
  val isDarkTheme by viewModel.isDarkTheme.collectAsState()

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    modifier = modifier,
    border = BorderStroke(
      width = 1.dp,
      color = MaterialTheme.colorScheme.outline
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(20.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "📜 DATABASE NASABAH AKTIF",
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = "Terhubung secara Real-Time",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ColorDangerRose.copy(alpha = 0.15f))
            .border(1.dp, ColorDangerRose.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text(
            text = "SINKRON",
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = ColorDangerRose
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // TABLE HEADER ROW
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
          .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Debitur", modifier = Modifier.weight(2.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Plafon", modifier = Modifier.weight(2f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Sisa Tenor", modifier = Modifier.weight(1.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Tunggakan", modifier = Modifier.weight(2.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("", modifier = Modifier.width(36.dp), fontSize = 10.sp) // Action spot
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outline)

      if (listNasabah.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Inbox,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Database Kosong",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "Gunakan formulir disamping untuk mendaftarkan nasabah baru.",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(horizontal = 24.dp)
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        ) {
          items(listNasabah) { n ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  // Direct UX flow: clicking a row auto-populates setoran name selection!
                  viewModel.setoranNamaSelection.value = n.nama
                }
                .padding(vertical = 14.dp, horizontal = 12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Name column
              Text(
                text = n.nama,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                color = if (isDarkTheme) ColorHoldSky else ColorHoldSkyDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(2.5f)
              )
              
              // Plafon columns
              Text(
                text = formatRupiahShort(n.nominal),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(2f)
              )

              // Sisa Tenor Badge
              Box(
                modifier = Modifier
                  .weight(1.8f)
                  .padding(end = 4.dp),
                contentAlignment = Alignment.CenterStart
              ) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(ColorPayEmerald.copy(alpha = 0.15f))
                    .border(1.dp, ColorPayEmerald.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "${n.sisaTenor} bln",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isDarkTheme) ColorPayEmerald else ColorPayEmeraldDark
                  )
                }
              }

              // Sisa Tagihan column
              Text(
                text = formatRupiahShort(n.sisaTagihan),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                color = if (isDarkTheme) ColorDangerRose else ColorDangerRoseDark,
                modifier = Modifier.weight(2.5f)
              )

              // Quick delete icon
              IconButton(
                onClick = { viewModel.deleteNasabah(n) },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = "Hapus Nasabah",
                  tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                  modifier = Modifier.size(18.dp)
                )
              }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      
      Text(
        text = "*Sisa tenor otomatis berkurang saat menerima pembayaran angsuran per bulan. Klik baris nama debitur untuk langsung menginput setoran.",
        fontSize = 10.sp,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

// FORMAT RUPIAH SHORT (E.G. Rp 1.500k OR Rp 1,5M AS DESIRED)
fun formatRupiahShort(value: Double): String {
  return when {
    value >= 1000000.0 -> {
      val million = value / 1000000.0
      val formattedStr = if (million % 1 == 0.0) {
        String.format("%.0f", million)
      } else {
        String.format("%.1f", million)
      }
      "Rp ${formattedStr}jt"
    }
    value >= 1000.0 -> {
      val k = value / 1000.0
      "Rp ${k.toInt()}rb"
    }
    else -> "Rp ${value.toInt()}"
  }
}
