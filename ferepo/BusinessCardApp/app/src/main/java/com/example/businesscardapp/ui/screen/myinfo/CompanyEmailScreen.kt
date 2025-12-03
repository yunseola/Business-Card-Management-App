package com.example.businesscardapp.ui.screen.myinfo

import android.net.Uri
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
import androidx.navigation.NavController
import com.example.businesscardapp.ui.viewmodel.CompanyVerifyEmailViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyEmailScreen(
    navController: NavController,
    cardId: Int,
    vm: CompanyVerifyEmailViewModel,
    toCodeInputRoute: (Int) -> String // ex) { id -> "company/verify/code/$id" }
) {
    val ui by vm.ui.collectAsState()
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val emailValid = remember(ui.email) {
        Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$").matches(ui.email)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("회사 인증") },
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
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("회사 이메일을 입력하여 인증을 진행해 주세요", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = ui.email,
                onValueChange = vm::onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("회사 이메일 입력") },
                singleLine = true,
                isError = ui.notCompany,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { if (emailValid && !ui.loading) vm.requestCode(cardId) }
                )
            )

            if (ui.loading) {
                LinearProgressIndicator(
                    Modifier.fillMaxWidth().padding(top = 12.dp)
                )
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { vm.requestCode(cardId) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = emailValid && !ui.loading
            ) { Text("인증 이메일 보내기") }

            // 스낵바
            LaunchedEffect(ui.toast) { ui.toast?.let { scope.launch { snack.showSnackbar(it) } } }
            // 발송 성공 → 코드 입력 화면으로 이동
            LaunchedEffect(ui.sent) {
                if (ui.sent) {
                    val encoded = Uri.encode(ui.email)       // ← 중요!
                    navController.navigate("company/verify/code/$cardId?email=$encoded")
                }
            }


            if (ui.notCompany) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "회사 이메일이 아닙니다. 회사 도메인 메일을 입력해 주세요.",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
