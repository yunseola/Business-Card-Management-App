package com.example.businesscardapp.ui.component

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.businesscardapp.R
import com.example.businesscardapp.ui.theme.pretendardMedium
import com.example.businesscardapp.ui.theme.pretendardSemiBold
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

// 기존 함수 유지
suspend fun downloadImageAndSave(context: Context, imageUrl: String): File? = withContext(Dispatchers.IO) {
    return@withContext try {
        val input = URL(imageUrl).openStream()
        val file = File(context.cacheDir, "shared_image.jpg")
        file.outputStream().use { output -> input.copyTo(output) }
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/** 공용 액션/옵션 타입 */
sealed class ShareAction {
    data class CopyLink(val link: String): ShareAction()
    data class ShareText(val link: String, val packageName: String? = null): ShareAction()
    data class ShareImage(val imageUrl: String, val packageName: String? = null): ShareAction()
    data class Callback(val run: () -> Unit): ShareAction()
}
data class ShareItem(val iconRes: Int, val label: String, val action: ShareAction)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    sheetState: SheetState,
    scope: CoroutineScope,
    onDismiss: () -> Unit,

    // 기존 파라미터(하위호환)
    shareLink: String,
    imageUrl: String,

    // 선택 파라미터: 넘기면 3x2 그리드로 렌더
    options: List<ShareItem> = emptyList(),
    footerText: String? = null
) {
    val context = LocalContext.current
    val localScope = rememberCoroutineScope()

    fun copyLink(link: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("mycard", link))
        Toast.makeText(context, "링크가 복사되었습니다", Toast.LENGTH_SHORT).show()
    }

    fun shareText(link: String, pkg: String? = null) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, link)
            if (!pkg.isNullOrBlank()) setPackage(pkg)
        }
        try { context.startActivity(send) }
        catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "앱이 설치되어 있지 않습니다", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareImage(url: String, pkg: String? = null) {
        localScope.launch {
            val file = if (url.startsWith("http")) {
                downloadImageAndSave(context, url)
            } else {
                // content/file URI도 허용
                try {
                    val f = File(context.cacheDir, "shared_image.jpg")
                    context.contentResolver.openInputStream(Uri.parse(url))?.use { ins ->
                        f.outputStream().use { outs -> ins.copyTo(outs) }
                    }
                    f
                } catch (e: Exception) { null }
            }
            if (file == null) {
                Toast.makeText(context, "이미지 준비 실패", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.file_provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (!pkg.isNullOrBlank()) setPackage(pkg)
            }
            try { context.startActivity(intent) }
            catch (_: ActivityNotFoundException) {
                Toast.makeText(context, "앱이 설치되어 있지 않습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun handle(action: ShareAction) {
        when (action) {
            is ShareAction.CopyLink  -> copyLink(action.link)
            is ShareAction.ShareText -> shareText(action.link, action.packageName)
            is ShareAction.ShareImage-> shareImage(action.imageUrl, action.packageName)
            is ShareAction.Callback  -> action.run()
        }
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    @Composable
    fun RowOf(items: List<ShareItem>) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            items.forEach { item ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(63.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, Color(0xFF4C3924), CircleShape)
                            .clickable { handle(item.action) },
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            painter = painterResource(item.iconRes),
                            contentDescription = item.label,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(Modifier.size(8.dp))
                    Text(item.label, fontFamily = pretendardMedium, fontSize = 16.sp, color = Color.Black)
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
        },
        sheetState = sheetState,
        containerColor = Color(0xFFF6F3ED),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "공유 방법 선택",
                fontFamily = pretendardSemiBold,
                fontSize = 24.sp,
                color = Color.Black
            )
            Spacer(Modifier.size(24.dp))

            if (options.isNotEmpty()) {
                // 3x2(두 줄) 렌더링
                val row1 = options.take(3)
                val row2 = options.drop(3).take(3)
                RowOf(row1)
                Spacer(Modifier.size(18.dp))
                RowOf(row2)
            } else {
                // 하위호환: 카카오/매터모스트/인스타 기본 1행
                val defaultOptions = listOf(
                    ShareItem(
                        R.drawable.ic_kakaotalk, "카카오톡",
                        ShareAction.ShareText(shareLink, "com.kakao.talk")
                    ),
                    ShareItem(
                        R.drawable.ic_mattermost, "Mattermost",
                        ShareAction.ShareText(shareLink, "com.mattermost.rn")
                    ),
                    ShareItem(
                        R.drawable.ic_instagram, "인스타그램",
                        if (imageUrl.isNotBlank())
                            ShareAction.ShareImage(imageUrl, "com.instagram.android")
                        else
                            ShareAction.ShareText(shareLink, "com.instagram.android")
                    )
                )
                RowOf(defaultOptions)
            }

            if (!footerText.isNullOrBlank()) {
                Spacer(Modifier.size(20.dp))
                Text(
                    text = footerText,
                    fontFamily = pretendardMedium,
                    fontSize = 12.sp,
                    color = Color(0xFF4C3924),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.size(16.dp))
        }
    }
}
