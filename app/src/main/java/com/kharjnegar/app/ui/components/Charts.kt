package com.kharjnegar.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kharjnegar.app.domain.AppLanguage
import com.kharjnegar.app.domain.CategoryBreakdown
import com.kharjnegar.app.domain.CurrencyUnit
import com.kharjnegar.app.domain.DailySpending
import com.kharjnegar.app.domain.MonthlyTrend
import com.kharjnegar.app.domain.PersianNormalizer
import com.kharjnegar.app.ui.theme.ExpenseRed
import com.kharjnegar.app.ui.theme.IncomeGreen

@Composable
fun CategoryDonutChart(
    breakdowns: List<CategoryBreakdown>,
    totalExpenseRial: Long,
    unit: CurrencyUnit,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.size(144.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(136.dp)) {
                val strokeWidth = 22.dp.toPx()
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                if (breakdowns.isEmpty() || totalExpenseRial <= 0L) {
                    drawArc(
                        color = trackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                } else {
                    var startAngle = -90f
                    val gap = if (breakdowns.size > 1) 2.5f else 0f
                    for (item in breakdowns) {
                        val sweep = (item.percentage * 360f - gap).coerceAtLeast(1f)
                        drawArc(
                            color = Color(item.category.colorHex),
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        startAngle += item.percentage * 360f
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = PersianNormalizer.formatCompactAmount(totalExpenseRial, unit, language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (unit == CurrencyUnit.TOMAN) {
                        if (language == AppLanguage.FA) "تومان" else "Toman"
                    } else {
                        if (language == AppLanguage.FA) "ریال" else "IRR"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            breakdowns.take(5).forEach { item ->
                val pctInt = (item.percentage * 100).toInt()
                val pctStr = if (language == AppLanguage.FA) {
                    "${PersianNormalizer.toPersianDigits(pctInt.toString())}٪"
                } else {
                    "$pctInt%"
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(item.category.colorHex), CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = item.category.title(language),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = pctStr,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DailySpendingBarChart(
    dailySpending: List<DailySpending>,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val maxExpense = dailySpending.maxOfOrNull { it.expenseRial }?.coerceAtLeast(1L) ?: 1L
    val barColor = MaterialTheme.colorScheme.primary
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            if (dailySpending.isEmpty()) return@Canvas
            val count = dailySpending.size
            val slotWidth = size.width / count
            val barWidth = (slotWidth * 0.62f).coerceAtLeast(3f)
            val maxHeight = size.height - 8f

            dailySpending.forEachIndexed { index, item ->
                val ratio = (item.expenseRial.toFloat() / maxExpense.toFloat()).coerceIn(0f, 1f)
                val barHeight = if (item.expenseRial > 0L) (ratio * maxHeight).coerceAtLeast(6f) else 4f
                val left = index * slotWidth + (slotWidth - barWidth) / 2f
                val top = size.height - barHeight
                drawRoundRect(
                    color = if (item.expenseRial > 0L) barColor else emptyColor,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(1, 8, 15, 22, dailySpending.size.coerceAtLeast(29)).forEach { day ->
                val label = if (language == AppLanguage.FA) {
                    PersianNormalizer.toPersianDigits(day.toString())
                } else {
                    day.toString()
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MonthlyTrendBarChart(
    trends: List<MonthlyTrend>,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val maxVal = trends.maxOfOrNull { maxOf(it.incomeRial, it.expenseRial) }?.coerceAtLeast(1L) ?: 1L

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            if (trends.isEmpty()) return@Canvas
            val groupCount = trends.size
            val groupWidth = size.width / groupCount
            val barWidth = (groupWidth * 0.28f).coerceAtLeast(8f)
            val gap = 4.dp.toPx()
            val maxH = size.height - 8f

            trends.forEachIndexed { idx, trend ->
                val center = idx * groupWidth + groupWidth / 2f

                val incH = ((trend.incomeRial.toFloat() / maxVal) * maxH).coerceAtLeast(if (trend.incomeRial > 0) 6f else 3f)
                drawRoundRect(
                    color = IncomeGreen,
                    topLeft = Offset(center - barWidth - gap / 2f, size.height - incH),
                    size = Size(barWidth, incH),
                    cornerRadius = CornerRadius(6f, 6f)
                )

                val expH = ((trend.expenseRial.toFloat() / maxVal) * maxH).coerceAtLeast(if (trend.expenseRial > 0) 6f else 3f)
                drawRoundRect(
                    color = ExpenseRed,
                    topLeft = Offset(center + gap / 2f, size.height - expH),
                    size = Size(barWidth, expH),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            trends.forEach { trend ->
                Text(
                    text = trend.yearMonth.shortLabel(language).take(5),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}
