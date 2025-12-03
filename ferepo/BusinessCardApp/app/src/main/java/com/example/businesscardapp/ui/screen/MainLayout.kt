package com.example.businesscardapp.ui.screen

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.businesscardapp.ui.component.BottomNavigationBar
import com.example.businesscardapp.ui.screen.add.AddCardDialog
import com.example.businesscardapp.ui.screen.add.AddCardScreen
import com.example.businesscardapp.ui.screen.cardbox.CardBoxScreen
import com.example.businesscardapp.ui.screen.group.GroupScreen
import com.example.businesscardapp.ui.screen.mycard.MyCardScreen
import com.example.businesscardapp.ui.screen.mycard.MyCardViewModel
import com.example.businesscardapp.ui.screen.myinfo.AppVersionScreen
import com.example.businesscardapp.ui.screen.myinfo.CompanyVerificationScreen
import com.example.businesscardapp.ui.screen.myinfo.CustomerServiceScreen
import com.example.businesscardapp.ui.screen.myinfo.FAQScreen
import com.example.businesscardapp.ui.screen.myinfo.MyInfoScreen
import com.example.businesscardapp.ui.screen.myinfo.NoticeScreen
import com.example.businesscardapp.ui.screen.myinfo.SettingScreen
import com.example.businesscardapp.ui.screen.share.RegisterFromShareScreen

@Composable
fun MainLayout(appNavController: NavHostController, initialTab: String = "card_box") {
    val mainNavController = rememberNavController()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(initialTab) {
        if (initialTab != "card_box") {
            mainNavController.navigate(initialTab) {
                popUpTo(mainNavController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val currentRoute = mainNavController
        .currentBackStackEntryAsState().value?.destination?.route

    val showBottomBar = currentRoute in listOf(
        "card_box", "group", "my_card", "my_info"
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    navController = mainNavController,
                    onAddClick = { showDialog = true }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = mainNavController,
                startDestination = "card_box"
            ) {
                composable("card_box") { CardBoxScreen(navController = appNavController) }
                composable("group") { GroupScreen(navController = appNavController) }
                composable("add") { AddCardScreen() }

                composable("my_card") {
                    val parentEntry = remember(
                        appNavController,
                        appNavController.currentBackStackEntry
                    ) {
                        appNavController.getBackStackEntry("main")
                    }

                    val myCardVm: MyCardViewModel =
                        androidx.lifecycle.viewmodel.compose.viewModel(
                            viewModelStoreOwner = parentEntry
                        )

                    MyCardScreen(
                        navController = appNavController,
                        myCardVm = myCardVm
                    )
                }

                composable("my_info") { MyInfoScreen(rootNavController = appNavController, navController = mainNavController) }
                composable("settings") { SettingScreen(navController = mainNavController) }
                composable("faq") { FAQScreen(navController = mainNavController) }
                composable("notice") { NoticeScreen(navController = mainNavController) }
                composable("app_version") { AppVersionScreen(navController = mainNavController) }
                composable("customer_service") { CustomerServiceScreen(navController = mainNavController) }
                composable("company_verification") { CompanyVerificationScreen(navController = appNavController) }

                composable(
                    route = "cards/{cardId}?name={name}&img={img}",
                    arguments = listOf(
                        navArgument("cardId") { type = NavType.StringType },
                        navArgument("name") { type = NavType.StringType; defaultValue = "" },
                        navArgument("img") { type = NavType.StringType; nullable = true }
                    ),
                    deepLinks = listOf(
                        navDeepLink { uriPattern = "https://i13e201.p.ssafy.io/cards/{cardId}" }
                    )
                ) { entry ->
                    val cardId = entry.arguments?.getString("cardId").orEmpty()
                    val name = entry.arguments?.getString("name").orEmpty()
                    val imgString = entry.arguments?.getString("img")
                    val imageUri = imgString?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }

                    RegisterFromShareScreen(
                        navController = appNavController,
                        imageUri = imageUri,
                        displayName = name,
                        cardId = cardId.toIntOrNull() ?: 0
                    )
                }
            }

            if (showDialog) {
                AddCardDialog(
                    onDismiss = { showDialog = false },
                    navController = appNavController // 상위 NavGraph의 NavController 전달
                )
            }
        }
    }
}