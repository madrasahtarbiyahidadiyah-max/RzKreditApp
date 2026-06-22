package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PeminjamEntity
import com.example.data.local.SetoranEntity
import com.example.data.repository.KreditRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class KreditViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = KreditRepository(database.peminjamDao(), database.setoranDao())
    private val sharedPrefs = application.getSharedPreferences("rzkredit_prefs", Context.MODE_PRIVATE)

    // UI States
    private val _isDarkTheme = MutableStateFlow(sharedPrefs.getBoolean("is_dark_theme", true))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(sharedPrefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // Form inputs: Peminjam
    val namaInput = MutableStateFlow("")
    val nominalInput = MutableStateFlow(1000000.0) // Default option
    val tenorInput = MutableStateFlow(12) // Default option: 12 months

    // Form inputs: Setoran
    val setoranNamaSelection = MutableStateFlow("")
    val setoranJumlahInput = MutableStateFlow("")

    // Success notifications / Toasts
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    // List of active nasabah
    val listNasabah: StateFlow<List<PeminjamEntity>> = repository.allPeminjamFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val listHistorySetoran: StateFlow<List<SetoranEntity>> = repository.allSetoranFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Dashboard Summed Stats
    val totalNasabah: StateFlow<Int> = listNasabah
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalUangDihutang: StateFlow<Double> = listNasabah
        .map { list -> list.sumOf { it.nominal } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalUangMasuk: StateFlow<Double> = listNasabah
        .map { list -> list.sumOf { it.totalSetoran } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalPiutang: StateFlow<Double> = listNasabah
        .map { list -> list.sumOf { it.sisaTagihan } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    init {
        viewModelScope.launch {
            // Populate sample data if DB is completely empty so it looks active
            repository.prepopulateIfEmpty()
        }

        // Automatic setoran amount pre-population depending on selected nasabah's monthly bill
        viewModelScope.launch {
            setoranNamaSelection.collect { name ->
                if (name.isNotEmpty()) {
                    val found = listNasabah.value.find { it.nama == name }
                    if (found != null) {
                        setoranJumlahInput.value = found.angsuranPerBulan.toInt().toString()
                    }
                } else {
                    setoranJumlahInput.value = ""
                }
            }
        }
    }

    fun toggleTheme() {
        val nextVal = !_isDarkTheme.value
        _isDarkTheme.value = nextVal
        sharedPrefs.edit().putBoolean("is_dark_theme", nextVal).apply()
    }

    fun hitungAngsuran(nominal: Double): Double {
        return repository.hitungAngsuran(nominal)
    }

    fun checkLogin(u: String, p: String, rememberMe: Boolean) {
        // Matches Google Apps Script login params EXACTLY: "rzkarim" & "rzkarim123"
        if (u == "rzkarim" && p == "rzkarim123") {
            _isLoggedIn.value = true
            _loginError.value = null
            if (rememberMe) {
                sharedPrefs.edit().putBoolean("is_logged_in", true).apply()
            }
        } else {
            _isLoggedIn.value = false
            _loginError.value = "Username atau Sandi Salah!"
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        sharedPrefs.edit().putBoolean("is_logged_in", false).apply()
        _loginError.value = null
    }

    fun simpanPeminjam() {
        viewModelScope.launch {
            val nameValue = namaInput.value.trim()
            if (nameValue.isEmpty()) {
                _toastMessage.emit("⚠️ Nama lengkap wajib diisi!")
                return@launch
            }

            val result = repository.simpanPeminjam(nameValue, nominalInput.value, tenorInput.value)
            result.onSuccess { msg ->
                _toastMessage.emit("✔ Kontrak Sudah Disimpan")
                // Clear state
                namaInput.value = ""
            }.onFailure { ex ->
                _toastMessage.emit("❌ " + (ex.message ?: "Gagal menyimpan"))
            }
        }
    }

    fun simpanSetoran() {
        viewModelScope.launch {
            val nameValue = setoranNamaSelection.value
            val amountStr = setoranJumlahInput.value.trim()

            if (nameValue.isEmpty()) {
                _toastMessage.emit("⚠️ Pilih debitur terlebih dahulu!")
                return@launch
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0.0) {
                _toastMessage.emit("⚠️ Nominal setoran tidak valid!")
                return@launch
            }

            val result = repository.simpanSetoran(nameValue, amount)
            result.onSuccess {
                _toastMessage.emit("💰 Terimakasih, Uang Sudah Masuk")
                // Clear state
                setoranNamaSelection.value = ""
                setoranJumlahInput.value = ""
            }.onFailure { ex ->
                val errorMsg = if (ex.message == "TIDAK_DITEMUKAN") {
                    "Gagal: Nama nasabah tidak terdaftar."
                } else {
                    ex.message ?: "Gagal menyimpan"
                }
                _toastMessage.emit("❌ $errorMsg")
            }
        }
    }

    fun getAngsuranWajibFormattedForLayar(): String {
        val selected = setoranNamaSelection.value
        if (selected.isEmpty()) return "0"
        val found = listNasabah.value.find { it.nama == selected } ?: return "0"
        return String.format("%,.0f", found.angsuranPerBulan)
    }

    // Handly delete function for full CRUD support
    fun deleteNasabah(peminjam: PeminjamEntity) {
        viewModelScope.launch {
            repository.deletePeminjam(peminjam)
            _toastMessage.emit("🗑 Nasabah berhasil dihapus")
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _toastMessage.emit("♻ Database berhasil direset")
        }
    }
}
