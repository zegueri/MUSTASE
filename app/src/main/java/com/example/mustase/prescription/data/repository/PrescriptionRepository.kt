package com.example.mustase.prescription.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.mustase.prescription.data.local.PrescriptionDao
import com.example.mustase.prescription.data.model.PrescriptionEntity
import com.example.mustase.prescription.data.model.Resource
import com.example.mustase.prescription.data.remote.OcrWebService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class PrescriptionRepository(
    private val ocrWebService: OcrWebService,
    private val prescriptionDao: PrescriptionDao,
    private val context: Context
) {
    companion object {
        private const val TAG = "PrescriptionRepository"
        private const val API_KEY = "K81090087688957"
        private const val MAX_FILE_SIZE_KB = 1024 // Limite de 1024 KB pour l'API OCR
    }

    /**
     * Scanne une image et sauvegarde le résultat en base de données
     */
    suspend fun scanAndSave(imageUri: Uri): Resource<PrescriptionEntity> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Copier l'image dans le stockage interne de l'app pour la conserver
                val localUri = copyImageToInternalStorage(imageUri)
                    ?: return@withContext Resource.Error("Impossible de copier l'image")

                // 2. Préparer le fichier pour l'upload (avec compression si nécessaire)
                val file = getCompressedFileFromUri(imageUri)
                    ?: return@withContext Resource.Error("Impossible de lire l'image")

                Log.d(TAG, "Taille du fichier après compression: ${file.length() / 1024} KB")

                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val languagePart = "fre".toRequestBody("text/plain".toMediaTypeOrNull())
                val overlayPart = "false".toRequestBody("text/plain".toMediaTypeOrNull())

                // 3. Appeler l'API OCR
                Log.d(TAG, "Envoi de l'image à l'API OCR...")
                val response = ocrWebService.parseImage(
                    apiKey = API_KEY,
                    file = filePart,
                    language = languagePart,
                    isOverlayRequired = overlayPart
                )

                // Nettoyer le fichier temporaire
                file.delete()

                if (!response.isSuccessful) {
                    Log.e(TAG, "Erreur HTTP: ${response.code()} - ${response.message()}")
                    return@withContext Resource.Error("Erreur serveur: ${response.message()}")
                }

                val ocrResponse = response.body()
                    ?: return@withContext Resource.Error("Réponse vide du serveur")

                Log.d(TAG, "OCRExitCode: ${ocrResponse.ocrExitCode}")

                // 4. Vérifier le succès de l'OCR
                if (ocrResponse.ocrExitCode != 1 || ocrResponse.isErroredOnProcessing) {
                    val errorMsg = ocrResponse.errorMessage?.joinToString(", ")
                        ?: ocrResponse.parsedResults?.firstOrNull()?.errorMessage
                        ?: "Erreur lors de l'analyse de l'image"
                    Log.e(TAG, "Erreur OCR: $errorMsg")
                    return@withContext Resource.Error(errorMsg)
                }

                // 5. Extraire le texte
                val extractedText = ocrResponse.parsedResults
                    ?.firstOrNull()
                    ?.parsedText
                    ?: ""

                if (extractedText.isBlank()) {
                    return@withContext Resource.Error("Aucun texte détecté dans l'image")
                }

                Log.d(TAG, "Texte extrait: ${extractedText.take(100)}...")

                // 6. Sauvegarder en base de données
                val prescription = PrescriptionEntity(
                    imageUri = localUri,
                    extractedText = extractedText,
                    timestamp = System.currentTimeMillis()
                )
                val id = prescriptionDao.insertPrescription(prescription)

                Resource.Success(prescription.copy(id = id))
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ${e.message}", e)
                Resource.Error(e.message ?: "Une erreur inattendue s'est produite")
            }
        }
    }

    /**
     * Copie l'image dans le stockage interne de l'application
     */
    private fun copyImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = "prescription_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)

            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()

            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la copie de l'image: ${e.message}")
            null
        }
    }

    /**
     * Crée un fichier temporaire compressé à partir de l'URI
     * Si le fichier dépasse MAX_FILE_SIZE_KB, il est compressé automatiquement
     */
    private fun getCompressedFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null

            // Décoder l'image en Bitmap
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) {
                Log.e(TAG, "Impossible de décoder l'image")
                return null
            }

            // Compresser l'image pour qu'elle soit sous la limite
            val compressedFile = compressImageToFile(originalBitmap)

            // Libérer la mémoire du bitmap original
            originalBitmap.recycle()

            compressedFile
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la compression de l'image: ${e.message}")
            null
        }
    }

    /**
     * Compresse un Bitmap jusqu'à ce qu'il soit sous la limite de taille
     */
    private fun compressImageToFile(bitmap: Bitmap): File {
        val tempFile = File.createTempFile("ocr_upload_", ".jpg", context.cacheDir)

        var quality = 90 // Commencer avec une qualité de 90%
        var scaleFactor = 1.0f
        var currentBitmap = bitmap

        do {
            // Si on a déjà réduit la qualité au minimum, réduire aussi la taille
            if (quality <= 20 && scaleFactor > 0.1f) {
                scaleFactor -= 0.1f
                quality = 90 // Réinitialiser la qualité

                val newWidth = (bitmap.width * scaleFactor).toInt()
                val newHeight = (bitmap.height * scaleFactor).toInt()

                if (newWidth > 0 && newHeight > 0) {
                    currentBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
                    Log.d(TAG, "Redimensionnement: ${newWidth}x${newHeight} (facteur: $scaleFactor)")
                }
            }

            // Compresser en JPEG
            val outputStream = ByteArrayOutputStream()
            currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val byteArray = outputStream.toByteArray()

            val fileSizeKB = byteArray.size / 1024
            Log.d(TAG, "Compression: qualité=$quality%, taille=${fileSizeKB}KB")

            if (fileSizeKB <= MAX_FILE_SIZE_KB) {
                // Écrire dans le fichier
                FileOutputStream(tempFile).use { fos ->
                    fos.write(byteArray)
                }

                // Libérer le bitmap redimensionné s'il est différent de l'original
                if (currentBitmap != bitmap) {
                    currentBitmap.recycle()
                }

                return tempFile
            }

            outputStream.close()
            quality -= 10

        } while (quality > 0 || scaleFactor > 0.1f)

        // Si on arrive ici, on écrit quand même le fichier avec la compression maximale
        val outputStream = ByteArrayOutputStream()
        currentBitmap.compress(Bitmap.CompressFormat.JPEG, 10, outputStream)
        FileOutputStream(tempFile).use { fos ->
            fos.write(outputStream.toByteArray())
        }

        if (currentBitmap != bitmap) {
            currentBitmap.recycle()
        }

        Log.w(TAG, "Image compressée au maximum, taille finale: ${tempFile.length() / 1024}KB")
        return tempFile
    }

    /**
     * Crée un fichier temporaire à partir de l'URI (sans compression)
     */
    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("ocr_upload_", ".jpg", context.cacheDir)

            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()

            tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la création du fichier temporaire: ${e.message}")
            null
        }
    }

    /**
     * Récupère toutes les prescriptions depuis la base de données
     */
    fun getAllPrescriptions(): Flow<List<PrescriptionEntity>> {
        return prescriptionDao.getAllPrescriptions()
    }

    /**
     * Récupère une prescription par son ID
     */
    suspend fun getPrescriptionById(id: Long): PrescriptionEntity? {
        return withContext(Dispatchers.IO) {
            prescriptionDao.getPrescriptionById(id)
        }
    }

    /**
     * Supprime une prescription
     */
    suspend fun deletePrescription(id: Long) {
        withContext(Dispatchers.IO) {
            val prescription = prescriptionDao.getPrescriptionById(id)
            prescription?.let {
                // Supprimer aussi le fichier image local
                try {
                    File(it.imageUri).delete()
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur lors de la suppression du fichier: ${e.message}")
                }
                prescriptionDao.deletePrescriptionById(id)
            }
        }
    }

    /**
     * Met à jour une prescription (titre, etc.)
     */
    suspend fun updatePrescription(prescription: PrescriptionEntity) {
        withContext(Dispatchers.IO) {
            prescriptionDao.updatePrescription(prescription)
        }
    }
}

