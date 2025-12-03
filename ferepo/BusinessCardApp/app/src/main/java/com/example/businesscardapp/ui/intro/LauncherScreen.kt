import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.businesscardapp.util.PrefUtil
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

@Composable
fun LauncherScreen(navController: NavHostController) {
    val context = LocalContext.current
    val userId = remember { PrefUtil.getUserId(context) }

    LaunchedEffect(Unit) {
        if (userId.isNullOrEmpty()) {
            navController.navigate("intro") {
                popUpTo("launcher") { inclusive = true }
            }
        } else {
            navController.navigate("main") {
                popUpTo("launcher") { inclusive = true }
            }
        }
    }
}
