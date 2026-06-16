package com.example.hanger

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen() {
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                FeedHeader(onWeatherClick = { showBottomSheet = true })
                SearchBar()
                CategoryTabs()
                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
            }
        },
        bottomBar = {
            BottomNavBar()
        }
    ) { innerPadding ->
        FeedList(modifier = Modifier.padding(innerPadding))

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
                dragHandle = null
            ) {
                WeatherDetailSheet(onClose = { showBottomSheet = false })
            }
        }
    }
}

@Composable
fun FeedHeader(onWeatherClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "São Paulo · 28°C",
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable { onWeatherClick() }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Text(
                    text = "HANGER",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
                // Small red accent above H
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .offset(x = (-2).dp, y = (-2).dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE91E63))
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF0EFEF))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clickable { onWeatherClick() }
                ) {
                    Text(text = "28°C", fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4A394A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "MC", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar() {
    TextField(
        value = "",
        onValueChange = {},
        placeholder = { Text("Buscar looks, marcas, pessoas...", fontSize = 14.sp) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(50.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF0EFEF),
            unfocusedContainerColor = Color(0xFFF0EFEF),
            disabledContainerColor = Color(0xFFF0EFEF),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun CategoryTabs() {
    val categories = listOf("Todos", "Seguindo", "Casual", "Trabalho", "Festa")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val isSelected = category == "Festa" // Just for visual matching
            Surface(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                color = if (isSelected) Color.Black else Color.White,
                border = if (isSelected) null else BorderStroke(0.5.dp, Color.LightGray)
            ) {
                Text(
                    text = category,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = if (isSelected) Color.White else Color.Black,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun FeedList(modifier: Modifier = Modifier) {
    val samplePosts = listOf(
        Post(
            username = "julia.looks",
            location = "São Paulo",
            time = "agora",
            likes = 284,
            comments = 31,
            caption = "Esse vestido midi com sandália rasteira está perfeito para o calor de São Paulo! ☀️",
            tags = listOf("casual", "vestido", "verão"),
            userInitials = "JL"
        ),
        Post(
            username = "rafael.moda",
            location = "Rio de Janeiro",
            time = "há 1h",
            likes = 150,
            comments = 12,
            caption = "Estilo executivo para o dia a dia.",
            tags = listOf("trabalho", "executivo"),
            userInitials = "RM"
        )
    )

    LazyColumn(modifier = modifier) {
        items(samplePosts) { post ->
            PostCard(post = post)
            HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
        }
    }
}

@Composable
fun BottomNavBar() {
    Column {
        HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem("Feed", true)
            NavBarItem("Buscar", false)
            Box(
                modifier = Modifier
                    .size(40.dp, 20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE91E63))
            )
            NavBarItem("Notif.", false)
            NavBarItem("Perfil", false)
        }
    }
}

@Composable
fun NavBarItem(label: String, isSelected: Boolean) {
    Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        color = if (isSelected) Color.Black else Color.Gray
    )
}

@Composable
fun WeatherDetailSheet(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SÃO PAULO",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                Text(
                    text = "✕",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Text(text = "28", fontSize = 72.sp, fontWeight = FontWeight.Bold)
                Text(text = "°C", fontSize = 24.sp, modifier = Modifier.padding(top = 16.dp))
            }
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(bottom = 8.dp)) {
                Text(text = "Ensolarado", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "Umidade 72%", fontSize = 12.sp, color = Color.Gray)
                Text(text = "Vento 14 km/h", fontSize = 12.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            color = Color.Black,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(text = "🌡️", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = buildAnnotatedString {
                        append("Quente! Para hoje, ")
                        withStyle(style = SpanStyle(color = Color(0xFFFFC107), fontWeight = FontWeight.Bold)) {
                            append("Hanger sugere")
                        }
                        append(" roupas leves em tecidos naturais — linho, algodão ou viscose. Evite peças escuras que absorvam calor. Sandálias abertas são a pedida certa.")
                    },
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ForecastItem("Manhã", "24°C", "☀️")
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = Color.LightGray)
        ForecastItem("Tarde", "31°C", "☀️")
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = Color.LightGray)
        ForecastItem("Noite", "26°C", "🌙")
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = Color.LightGray)
        ForecastItem("Amanhã", "27°C", "☀️")
    }
}

@Composable
fun ForecastItem(period: String, temp: String, icon: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = period, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = temp, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = icon)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FeedScreenPreview() {
    FeedScreen()
}
