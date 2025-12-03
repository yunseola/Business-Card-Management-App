// util/TextRecognizer.kt
package com.example.businesscardapp.util

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

fun recognizeTextFromImage(
    context: Context,
    imageUri: Uri,
    onSuccess: (String) -> Unit,
    onFailure: (Exception) -> Unit
) {
    try {
        val image = InputImage.fromFilePath(context, imageUri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                onSuccess(visionText.text)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "텍스트 인식 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                onFailure(e)
            }

    } catch (e: Exception) {
        e.printStackTrace()
        onFailure(e)
    }
} 