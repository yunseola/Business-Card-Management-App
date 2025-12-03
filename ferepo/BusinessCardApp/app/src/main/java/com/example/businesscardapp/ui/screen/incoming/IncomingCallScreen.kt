import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.businesscardapp.data.model.CardCallInfoResponse

@Composable
fun IncomingCallScreen(cardInfo: CardCallInfoResponse?) {
    Log.d(
        "IncomingUI",
        "render name=${cardInfo?.name}, company=${cardInfo?.company}, pos=${cardInfo?.position}, img=${cardInfo?.cardImageForDisplay}, memo=${cardInfo?.memoSummary}"
    )
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AsyncImage(
            model = cardInfo?.cardImageForDisplay,
            contentDescription = "명함 이미지",
            modifier = Modifier.size(120.dp)
        )
        Text(cardInfo?.name ?: "이름 없음", fontSize = 20.sp)
        Text(cardInfo?.company ?: "회사 없음", fontSize = 16.sp)
        Text(cardInfo?.position ?: "", fontSize = 16.sp)
        Text(cardInfo?.memoSummary ?: "메모 없음", fontSize = 14.sp)
    }
}
