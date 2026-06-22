package com.example.data.repository

import com.example.data.local.PeminjamDao
import com.example.data.local.SetoranDao
import com.example.data.local.PeminjamEntity
import com.example.data.local.SetoranEntity
import kotlinx.coroutines.flow.Flow

class KreditRepository(
    private val peminjamDao: PeminjamDao,
    private val setoranDao: SetoranDao
) {
    val allPeminjamFlow: Flow<List<PeminjamEntity>> = peminjamDao.getAllPeminjamFlow()
    val allSetoranFlow: Flow<List<SetoranEntity>> = setoranDao.getAllSetoranFlow()

    fun hitungAngsuran(nominal: Double): Double {
        return when (nominal.toInt()) {
            1000000 -> 110000.0
            1500000 -> 170000.0
            2000000 -> 220000.0
            2500000 -> 270000.0
            3000000 -> 320000.0
            3500000 -> 370000.0
            4000000 -> 420000.0
            4500000 -> 470000.0
            5000000 -> 530000.0
            else -> 0.0
        }
    }

    suspend fun simpanPeminjam(nama: String, nominal: Double, tenor: Int): Result<String> {
        val trimmedName = nama.trim()
        if (trimmedName.isEmpty()) {
            return Result.failure(Exception("Nama wajib diisi!"))
        }

        val existing = peminjamDao.getPeminjamByName(trimmedName)
        if (existing != null) {
            return Result.failure(Exception("Nama nasabah sudah terdaftar."))
        }

        val angsuranPerBulan = hitungAngsuran(nominal)
        val totalTagihan = angsuranPerBulan * tenor

        val peminjam = PeminjamEntity(
            nama = trimmedName,
            nominal = nominal,
            tenor = tenor,
            angsuranPerBulan = angsuranPerBulan,
            totalTagihan = totalTagihan,
            totalSetoran = 0.0,
            sisaTagihan = totalTagihan,
            sisaTenor = tenor
        )

        peminjamDao.insertPeminjam(peminjam)
        return Result.success("Data peminjam baru berhasil disimpan!")
    }

    suspend fun simpanSetoran(nama: String, jumlahSetoran: Double): Result<String> {
        val trimmedName = nama.trim()
        if (trimmedName.isEmpty()) {
            return Result.failure(Exception("Pilih debitur!"))
        }
        if (jumlahSetoran <= 0.0) {
            return Result.failure(Exception("Jumlah setoran harus lebih besar dari 0!"))
        }

        val peminjam = peminjamDao.getPeminjamByName(trimmedName)
            ?: return Result.failure(Exception("TIDAK_DITEMUKAN"))

        val currentSetoran = peminjam.totalSetoran
        val totalTagihan = peminjam.totalTagihan
        val angsuranWajib = peminjam.angsuranPerBulan
        val sisaTenorSekarang = peminjam.sisaTenor

        val newSetoranVal = currentSetoran + jumlahSetoran
        val newSisa = (totalTagihan - newSetoranVal).coerceAtLeast(0.0)

        // Sisa tenor berkurang 1 jika jumlahSetoran >= angsuranWajib
        var newTenor = sisaTenorSekarang
        if (jumlahSetoran >= angsuranWajib && sisaTenorSekarang > 0) {
            newTenor = sisaTenorSekarang - 1
        }

        val updatedPeminjam = peminjam.copy(
            totalSetoran = newSetoranVal,
            sisaTagihan = newSisa,
            sisaTenor = newTenor
        )

        peminjamDao.updatePeminjam(updatedPeminjam)

        // History Log
        val setoranLog = SetoranEntity(
            nama = peminjam.nama,
            jumlahSetoran = jumlahSetoran
        )
        setoranDao.insertSetoran(setoranLog)

        return Result.success("Sukses")
    }

    suspend fun clearAllData() {
        peminjamDao.clearAll()
        setoranDao.clearAll()
    }

    suspend fun deletePeminjam(peminjam: PeminjamEntity) {
        peminjamDao.deletePeminjam(peminjam)
    }

    // Helper method to pre-populate dummy/sample data if the tables are empty
    suspend fun prepopulateIfEmpty() {
        if (peminjamDao.getAllPeminjamList().isEmpty()) {
            simpanPeminjam("Nasabah Contoh 1", 2000000.0, 12)
            simpanPeminjam("Nasabah Contoh 2", 3000000.0, 24)
            simpanSetoran("Nasabah Contoh 1", 220000.0)
        }
    }
}
