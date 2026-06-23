package com.example

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.PeminjamEntity
import com.example.data.local.SetoranEntity
import com.example.ui.KreditViewModel
import com.example.ui.theme.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

// Helper to format currency for receipt drawing (static environment)
private fun formatRupiahRaw(value: Double): String {
  val formatter = java.text.NumberFormat.getCurrencyInstance(Locale("id", "ID"))
  return formatter.format(value).replace("Rp", "Rp ").replace(",00", "")
}

// 1. GENERATE GORGEOUS CASH RECEPT BITMAP (JPG CONVERTIBLE)
fun generateReceiptBitmap(
  context: Context,
  setoran: SetoranEntity,
  peminjam: PeminjamEntity?
): Bitmap {
  val width = 600
  val height = 820
  val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
  val canvas = Canvas(bitmap)

  // Clean slate white background
  canvas.drawColor(android.graphics.Color.WHITE)

  val paint = Paint().apply {
    isAntiAlias = true
  }

  // Double external boundaries
  paint.color = android.graphics.Color.LTGRAY
  paint.style = Paint.Style.STROKE
  paint.strokeWidth = 4f
  canvas.drawRect(Rect(10, 10, width - 10, height - 10), paint)

  // Thermal dashed internal boundary line
  paint.strokeWidth = 2f
  paint.pathEffect = DashPathEffect(floatArrayOf(12f, 10f), 0f)
  canvas.drawRect(Rect(22, 22, width - 22, height - 22), paint)
  paint.pathEffect = null // Reset path effect

  // Header Title
  paint.textAlign = Paint.Align.CENTER
  paint.color = android.graphics.Color.parseColor("#0C6B46") // Safe deep emerald logo color
  paint.textSize = 28f
  paint.isFakeBoldText = true
  canvas.drawText("PORTAL RZKREDIT", (width / 2).toFloat(), 75f, paint)

  // Subtitle
  paint.textSize = 14f
  paint.isFakeBoldText = false
  paint.color = android.graphics.Color.DKGRAY
  canvas.drawText("Sistem Administrasi Setoran Tunai Premium", (width / 2).toFloat(), 105f, paint)

  // Helper dashed drawer
  fun drawSeparatorLine(y: Float) {
    paint.color = android.graphics.Color.GRAY
    paint.strokeWidth = 2f
    paint.pathEffect = DashPathEffect(floatArrayOf(8f, 8f), 0f)
    canvas.drawLine(40f, y, (width - 40).toFloat(), y, paint)
    paint.pathEffect = null
  }

  drawSeparatorLine(124f)

  // Waktu format
  val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale("id", "ID"))
  val dateStr = sdf.format(Date(setoran.tanggal))

  // Utility row printing
  fun drawValueRow(label: String, value: String, y: Float, isBoldValue: Boolean = false) {
    paint.textAlign = Paint.Align.LEFT
    paint.isFakeBoldText = false
    paint.color = android.graphics.Color.DKGRAY
    paint.textSize = 15f
    canvas.drawText(label, 50f, y, paint)

    paint.textAlign = Paint.Align.RIGHT
    paint.isFakeBoldText = isBoldValue
    paint.color = if (isBoldValue) android.graphics.Color.parseColor("#0C6B46") else android.graphics.Color.BLACK
    paint.textSize = 15f
    canvas.drawText(value, (width - 50).toFloat(), y, paint)
  }

  // Metadata block
  var targetY = 165f
  drawValueRow("No. Struk Resi", "STK-2026-${setoran.id}", targetY)
  targetY += 30f
  drawValueRow("Waktu Setor", dateStr, targetY)
  targetY += 30f
  drawValueRow("Tipe Transaksi", "SETORAN TUNAI INSTAN", targetY, isBoldValue = true)

  targetY += 25f
  drawSeparatorLine(targetY)
  targetY += 35f

  // Customer transactions block
  paint.textAlign = Paint.Align.LEFT
  paint.textSize = 17f
  paint.isFakeBoldText = true
  paint.color = android.graphics.Color.parseColor("#1E293B")
  canvas.drawText("RINCIAN TRANSAKSI DEBITUR", 50f, targetY, paint)
  targetY += 40f

  drawValueRow("Nama Penerima", "RzKarim (Admin Portal)", targetY)
  targetY += 32f
  drawValueRow("Nama Debitur", setoran.nama, targetY, isBoldValue = true)
  targetY += 32f
  drawValueRow("Besar Angsuran Masuk", formatRupiahRaw(setoran.jumlahSetoran), targetY, isBoldValue = true)

  targetY += 25f
  drawSeparatorLine(targetY)
  targetY += 35f

  // Extra dynamic contract records if debitor context exists
  if (peminjam != null) {
    paint.textAlign = Paint.Align.LEFT
    paint.textSize = 17f
    paint.isFakeBoldText = true
    paint.color = android.graphics.Color.parseColor("#1E293B")
    canvas.drawText("STATUS KONTRAK TER-UPDATE", 50f, targetY, paint)
    targetY += 40f

    drawValueRow("Total Plafon Pinjaman", formatRupiahRaw(peminjam.nominal), targetY)
    targetY += 32f
    drawValueRow("Sisa Tenor Berjalan", "${peminjam.sisaTenor} Bulan", targetY)
    targetY += 32f
    drawValueRow("Sisa Outstanding Tagihan", formatRupiahRaw(peminjam.sisaTagihan), targetY, isBoldValue = true)

    targetY += 25f
    drawSeparatorLine(targetY)
    targetY += 35f
  }

  // End Note
  paint.textAlign = Paint.Align.CENTER
  paint.textSize = 13f
  paint.color = android.graphics.Color.GRAY
  paint.isFakeBoldText = false
  canvas.drawText("Simpan struk JPG ini sebagai bukti pembayaran sah.", (width / 2).toFloat(), targetY, paint)
  targetY += 20f
  canvas.drawText("RzKredit - Kemudahan Kredit Terpercaya.", (width / 2).toFloat(), targetY, paint)

  // Stamp Seal Decoration (LUNAS Official seal)
  paint.style = Paint.Style.STROKE
  paint.strokeWidth = 3f
  paint.color = android.graphics.Color.parseColor("#E11D48") // Rose Stamp red
  val boundaryStamp = RectF((width - 190).toFloat(), 125f, (width - 40).toFloat(), 205f)
  canvas.drawRoundRect(boundaryStamp, 8f, 8f, paint)

  paint.style = Paint.Style.FILL
  paint.textSize = 19f
  paint.isFakeBoldText = true
  paint.textAlign = Paint.Align.CENTER
  canvas.drawText("LUNAS", boundaryStamp.centerX(), boundaryStamp.centerY() - 2f, paint)
  paint.textSize = 10f
  canvas.drawText("PORTAL RZKARIM", boundaryStamp.centerX(), boundaryStamp.centerY() + 16f, paint)

  // Decorative barcode
  targetY += 30f
  paint.color = android.graphics.Color.BLACK
  paint.strokeWidth = 2f
  var startX = (width / 2) - 120f
  val limitX = startX + 240f
  var indexCounter = 0
  while (startX < limitX) {
    val barWeight = if (indexCounter % 3 == 0) 2f else if (indexCounter % 4 == 0) 5f else 3f
    canvas.drawLine(startX, targetY, startX, targetY + 22f, paint)
    startX += barWeight + 3f
    indexCounter++
  }

  return bitmap
}

// 2. MEDIASTORE SAVING HELPER
fun saveReceiptToGallery(context: Context, bitmap: Bitmap, setoranId: Int): Uri? {
  val resolver = context.contentResolver
  val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
  } else {
    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
  }

  val displayTitle = "Struk_RzKredit_Setoran_${setoranId}"
  val infoSet = ContentValues().apply {
    put(MediaStore.Images.Media.DISPLAY_NAME, "$displayTitle.jpg")
    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/RzKredit_Struk")
      put(MediaStore.Images.Media.IS_PENDING, 1)
    }
  }

  val uri = resolver.insert(imageCollection, infoSet)
  if (uri != null) {
    try {
      resolver.openOutputStream(uri).use { stream ->
        if (stream == null) throw IOException("Failed to open output stream")
        if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
          throw IOException("Failed to compress receipt bitmap")
        }
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        infoSet.clear()
        infoSet.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, infoSet, null, null)
      }
      return uri
    } catch (e: Exception) {
      resolver.delete(uri, null, null)
      return null
    }
  }
  return null
}

// 3. INTENT SHARING HELPER
fun shareReceiptImage(context: Context, uri: Uri) {
  val shareIntent = Intent(Intent.ACTION_SEND).apply {
    type = "image/jpeg"
    putExtra(Intent.EXTRA_STREAM, uri)
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
  }
  context.startActivity(Intent.createChooser(shareIntent, "Bagikan Resi JPG Melalui"))
}

// 4. JETPACK COMPOSE PREVIEW DIALOG
@Composable
fun ReceiptPreviewDialog(
  setoran: SetoranEntity,
  peminjam: PeminjamEntity?,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val receiptBitmap = remember(setoran, peminjam) {
    generateReceiptBitmap(context, setoran, peminjam)
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surface,
      modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "🔍 PREVIEW RESI JPG",
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
          )
          IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

        // Receipt content bitmap rendering
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f, fill = false)
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(4.dp)
        ) {
          Image(
            bitmap = receiptBitmap.asImageBitmap(),
            contentDescription = "Receipt Image Result",
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(600f / 820f)
              .clip(RoundedCornerShape(6.dp))
          )
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Button(
            onClick = {
              val uri = saveReceiptToGallery(context, receiptBitmap, setoran.id)
              if (uri != null) {
                Toast.makeText(context, "Resi berhasil disimpan ke Galeri Pictures/RzKredit_Struk!", Toast.LENGTH_LONG).show()
              } else {
                Toast.makeText(context, "Gagal menyimpan resi.", Toast.LENGTH_SHORT).show()
              }
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
              containerColor = ColorPayEmerald,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(Icons.Default.Download, contentDescription = "Download Logo")
              Text("Unduh JPG", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }

          OutlinedButton(
            onClick = {
              val uri = saveReceiptToGallery(context, receiptBitmap, setoran.id)
              if (uri != null) {
                shareReceiptImage(context, uri)
              } else {
                Toast.makeText(context, "Gagal memproses sharing.", Toast.LENGTH_SHORT).show()
              }
            },
            modifier = Modifier.weight(1.0f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(Icons.Default.Share, contentDescription = "Share Logo")
              Text("Bagikan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
      }
    }
  }
}

// 5. TRANSACTION HISTORY CARD COMPOSABLE (SINKRON DI DALAM DATABASE)
@Composable
fun CardHistorySetoran(viewModel: KreditViewModel, modifier: Modifier = Modifier) {
  val listHistorySetoran by viewModel.listHistorySetoran.collectAsState()
  val listNasabah by viewModel.listNasabah.collectAsState()
  val isDarkTheme by viewModel.isDarkTheme.collectAsState()

  // Preview dialog controller
  var activeSetoranToPrint by remember { mutableStateOf<SetoranEntity?>(null) }

  if (activeSetoranToPrint != null) {
    val setoran = activeSetoranToPrint!!
    val correlatedDebitur = listNasabah.find { it.nama.lowercase() == setoran.nama.lowercase() }
    ReceiptPreviewDialog(
      setoran = setoran,
      peminjam = correlatedDebitur,
      onDismiss = { activeSetoranToPrint = null }
    )
  }

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    modifier = modifier,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
            text = "🧾 RIWAYAT SETORAN & CETAK STRUK",
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = "Klik logo printer untuk simpan/bagikan struk JPG",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ColorPayEmerald.copy(alpha = 0.15f))
            .border(1.dp, ColorPayEmerald.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text(
            text = "TRANSAKSI",
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = if (isDarkTheme) ColorPayEmerald else ColorPayEmeraldDark
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Content list history
      if (listHistorySetoran.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Receipt,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Belum Ada Setoran",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "Setoran yang disimpan akan tercatat otomatis di sini.",
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
          items(listHistorySetoran.reversed()) { setoran ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
              ) {
                Box(
                  modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(ColorPayEmerald.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success check",
                    tint = ColorPayEmerald,
                    modifier = Modifier.size(20.dp)
                  )
                }

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = setoran.nama,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID")).format(Date(setoran.tanggal)),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Text(
                  text = formatRupiahShort(setoran.jumlahSetoran),
                  fontWeight = FontWeight.Black,
                  fontSize = 13.sp,
                  color = if (isDarkTheme) ColorPayEmerald else ColorPayEmeraldDark
                )

                IconButton(
                  onClick = { activeSetoranToPrint = setoran },
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ColorHoldSky.copy(alpha = 0.15f))
                    .border(1.dp, ColorHoldSky.copy(alpha = 0.3f), CircleShape)
                ) {
                  Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = "Cetak Resi JPG",
                    tint = if (isDarkTheme) ColorHoldSky else ColorHoldSkyDark,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
          }
        }
      }
    }
  }
}
