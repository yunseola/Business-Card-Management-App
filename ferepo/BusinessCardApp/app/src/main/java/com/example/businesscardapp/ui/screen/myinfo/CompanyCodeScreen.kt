// app/src/main/java/com/example/businesscardapp/ui/screen/myinfo/CompanyCodeScreen.kt
package com.example.businesscardapp.ui.screen.myinfo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.businesscardapp.ui.viewmodel.CompanyVerifyCodeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyCodeScreen(
    navController: NavController,
    cardId: Int,
    email: String,
    vm: CompanyVerifyCodeViewModel = viewModel()
) {
    val ui by vm.ui.collectAsState()
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(ui.toast) {
        ui.toast?.let { msg ->
            scope.launch { snack.showSnackbar(msg) }
            vm.consumeToast()
        }
    }
    LaunchedEffect(ui.success) {
        if (ui.success) {
            navController.navigate("company/verify/done")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("인증 코드 입력") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snack) }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "아래 이메일로 전송된 6자리 코드를 입력해 주세요.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = ui.code,
                onValueChange = vm::onCodeChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("예: 123456") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (ui.code.length == 6 && !ui.loading) {
                            vm.submit(cardId, email)
                        }
                    }
                )
            )

            if (ui.loading) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { vm.submit(cardId, email) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = ui.code.length == 6 && !ui.loading
            ) {
                Text(if (ui.loading) "확인 중..." else "확인")
            }
        }
    }
}
