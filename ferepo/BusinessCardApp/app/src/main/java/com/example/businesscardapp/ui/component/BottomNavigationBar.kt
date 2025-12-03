package com.example.businesscardapp.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomNavigation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.businesscardapp.R
import com.example.businesscardapp.ui.theme.pretendardMedium
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.Divider


data class NavItem(
    val label: String?,
    val route: String,
    val icon: Int,
    val selectedIcon: Int
)

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    onAddClick: () -> Unit,
    isDialogOpen: Boolean = false
) {
    val items = listOf(
        NavItem("명함첩", "card_box", R.drawable.ic_nav_cardbox, R.drawable.ic_nav_cardbox_touch),
        NavItem("그룹", "group", R.drawable.ic_nav_group, R.drawable.ic_nav_group_touch),
        NavItem("내 명함", "my_card", R.drawable.ic_nav_mycard, R.drawable.ic_nav_mycard_touch),
        NavItem("내 정보", "my_info", R.drawable.ic_nav_person, R.drawable.ic_nav_person_touch)
    )
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .height(64.dp) // 네비게이션 높이
    ) {
        // ✅ 상단 구분선
        Divider(
            color = Color(0xFF4C3924), // 회색 선
            thickness = 1.dp,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                if (index == 2) Spacer(modifier = Modifier.weight(1f)) // add 자리 비움

                val isSelected = currentRoute == item.route
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            // 다이얼로그가 열려있으면 네비게이션을 막음
                            if (!isDialogOpen && !isSelected) {
                                navController.navigate(item.route) {
                                    popUpTo("card_box") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = if (isSelected) item.selectedIcon else item.icon),
                        contentDescription = item.label,
                        tint = Color(0xFF4C3924),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = item.label ?: "",
                        fontSize = 12.sp,
                        fontFamily = pretendardMedium,
                        color = Color(0xFF4C3924)
                    )
                }
            }
        }

        // ✅ 중앙 add 버튼 (더 아래로 내리기)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 12.dp) // ↓ 더 아래로 이동
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onAddClick() }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_nav_add),
                contentDescription = "추가 버튼",
                modifier = Modifier.size(32.dp),
                tint = Color.Unspecified
            )
        }
    }
}
