package com.hanger.app.ui.feed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hanger.app.data.model.WeatherSnapshot
import com.example.hanger.ui.theme.HangerCream
import com.example.hanger.ui.theme.HangerInk
import com.example.hanger.ui.theme.HangerPink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherBottomSheet(
    snapshot: WeatherSnapshot,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = HangerCream,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 40.dp)
        ) {
            // Cabeçalho: cidade + botão fechar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = HangerPink,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = snapshot.cityLabel.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .weight(1f)
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Fechar",
                        tint = Color(0xFFAAAAAA),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Temperatura + condição + dados
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${snapshot.currentTempC}",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = HangerInk,
                    lineHeight = 64.sp
                )
                Text(
                    text = "°C",
                    fontSize = 22.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Text(
                        text = "${snapshot.conditionEmoji} ${snapshot.condition}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF555555)
                    )
                    Text(
                        text = "Umidade ${snapshot.humidityPercent}%",
                        fontSize = 12.sp,
                        color = Color(0xFFAAAAAA)
                    )
                    Text(
                        text = "Vento ${snapshot.windKmh} km/h",
                        fontSize = 12.sp,
                        color = Color(0xFFAAAAAA)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Sugestão de look
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(HangerInk)
                    .padding(14.dp)
            ) {
                Text(
                    text = snapshot.suggestion,
                    fontSize = 12.sp,
                    color = Color.White,
                    lineHeight = 19.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Previsão por período
            val rows = listOf(
                "Manhã"   to "${snapshot.morningTempC}°C ☀️",
                "Tarde"   to "${snapshot.afternoonTempC}°C 🌤️",
                "Noite"   to "${snapshot.nightTempC}°C 🌙",
                "Amanhã"  to "${snapshot.tomorrowTempC}°C ${snapshot.tomorrowEmoji}"
            )

            rows.forEachIndexed { index, (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = Color(0xFF555555),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = value,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HangerInk
                    )
                }
                if (index < rows.lastIndex) {
                    HorizontalDivider(color = Color(0x12000000), thickness = 0.5.dp)
                }
            }
        }
    }
}
