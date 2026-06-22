package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "data_peminjam")
data class PeminjamEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nama: String,
    val nominal: Double,
    val tenor: Int,
    val angsuranPerBulan: Double,
    val totalTagihan: Double,
    val totalSetoran: Double = 0.0,
    val sisaTagihan: Double,
    val sisaTenor: Int
)

@Entity(tableName = "history_setoran")
data class SetoranEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tanggal: Long = System.currentTimeMillis(),
    val nama: String,
    val jumlahSetoran: Double
)
