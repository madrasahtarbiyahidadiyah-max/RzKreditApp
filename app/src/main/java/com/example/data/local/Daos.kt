package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PeminjamDao {
    @Query("SELECT * FROM data_peminjam ORDER BY id DESC")
    fun getAllPeminjamFlow(): Flow<List<PeminjamEntity>>

    @Query("SELECT * FROM data_peminjam")
    suspend fun getAllPeminjamList(): List<PeminjamEntity>

    @Query("SELECT * FROM data_peminjam WHERE UPPER(TRIM(nama)) = UPPER(TRIM(:nama)) LIMIT 1")
    suspend fun getPeminjamByName(nama: String): PeminjamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeminjam(peminjam: PeminjamEntity): Long

    @Update
    suspend fun updatePeminjam(peminjam: PeminjamEntity)

    @Delete
    suspend fun deletePeminjam(peminjam: PeminjamEntity)

    @Query("DELETE FROM data_peminjam")
    suspend fun clearAll()
}

@Dao
interface SetoranDao {
    @Query("SELECT * FROM history_setoran ORDER BY tanggal DESC")
    fun getAllSetoranFlow(): Flow<List<SetoranEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetoran(setoran: SetoranEntity): Long

    @Query("DELETE FROM history_setoran")
    suspend fun clearAll()
}
