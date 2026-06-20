package com.hanger.app.ui.feed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hanger.ui.theme.HangerGold
import com.example.hanger.ui.theme.HangerInk
import com.example.hanger.ui.theme.HangerPink
import com.example.hanger.ui.theme.HangerPlum
import com.example.hanger.ui.theme.HangerSurfaceAlt
import com.example.hanger.ui.theme.HangerTextMuted

@Composable
fun FeedTopBar(
    temperatureLabel: String,
    userInitials: String,
    onMenuClick: () -> Unit,
    onTemperatureClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = HangerInk)
                }
                Text(
                    text = "HANGER",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = HangerInk
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                TemperatureChip(label = temperatureLabel, onClick = onTemperatureClick)

                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(HangerPlum)
                        .clickable(onClick = onProfileClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userInitials,
                        color = HangerGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(HangerSurfaceAlt)
                .clickable(onClick = onSearchClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Buscar",
                tint = Color(0xFF888888),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Buscar looks, marcas, pessoas...",
                fontSize = 13.sp,
                color = HangerTextMuted,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun TemperatureChip(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(HangerInk)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Thermostat,
            contentDescription = null,
            tint = HangerPink,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = label,
            color = HangerGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}