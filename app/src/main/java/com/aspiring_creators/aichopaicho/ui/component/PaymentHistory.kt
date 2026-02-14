package com.aspiring_creators.aichopaicho.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aspiring_creators.aichopaicho.data.entity.Repayment
import com.aspiring_creators.aichopaicho.ui.theme.PaidColor
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

@Composable
fun PaymentHistory(
    repayments: List<Repayment>,
    modifier: Modifier = Modifier
) {
    if (repayments.isEmpty()) return

    Column(modifier = modifier) {
        Text(
            text = "Payment History",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn {
            items(repayments) { repayment ->
                PaymentItem(repayment)
            }
        }
    }
}

@Composable
fun PaymentItem(repayment: Repayment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(repayment.date)),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (!repayment.description.isNullOrBlank()) {
                    Text(
                        text = repayment.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(repayment.amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = PaidColor
            )
        }
    }
}
