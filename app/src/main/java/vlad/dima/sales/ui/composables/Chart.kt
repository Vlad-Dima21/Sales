package vlad.dima.sales.ui.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.lang.Integer.max
import java.lang.Math.min


data class ChartData(
    val x: Int,
    val y: Int
)

@OptIn(ExperimentalTextApi::class)
@Composable
fun Chart(
    modifier: Modifier = Modifier,
    data: List<ChartData>,
    contentColor: Color = MaterialTheme.colors.primaryVariant,
    infoColor: Color = MaterialTheme.colors.onBackground,
    infoSize: TextUnit = 12.sp
) {
    if (data.isEmpty()) {
        return
    }
    val yMax = remember(data) {
        data.maxOfOrNull { it.y } ?: 10
    }
    val textMeasurer = rememberTextMeasurer()
    Canvas(modifier = modifier) {
        val chartPadding = 10
        val minLabelDistance = 50
        val yLabelSize = textMeasurer.measure(
            text = yMax.toString(),
            style = TextStyle(
                color = infoColor,
                fontSize = infoSize,
                textAlign = TextAlign.Center
            )
        ).size
        val xLabelSize = textMeasurer.measure(
            text = data.last().toString(),
            style = TextStyle(
                color = infoColor,
                fontSize = infoSize,
                textAlign = TextAlign.Center
            )
        ).size
        val yLabelCount =
            ((size.height - chartPadding - yLabelSize.height / 2 - xLabelSize.height) / (yLabelSize.height + minLabelDistance)).toInt()
                .coerceAtMost(5)
        val yLabelMax = run {
            var max = yMax
            var yStep = max(max / (yLabelCount - 1), 1)
            while ((max downTo 0 step yStep).last != 0) {
                max++
                yStep = max(max / (yLabelCount - 1), 1)
            }
            max
        }
        val yStep = max(yLabelMax / (yLabelCount - 1), 1)
        val yLabels = run {
            (yLabelMax downTo 0 step yStep).map {
                textMeasurer.measure(
                   text = it.toString().padStart(yLabelMax.toString().length),
                    style = TextStyle(
                        color = infoColor,
                        fontSize = infoSize,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
        val xLabelsXOffset = yLabelSize.width + 2 * chartPadding
        val xLabels = run {
            data.map {
                textMeasurer.measure(
                    text = it.x.toString().padStart(2, '0'),
                    style = TextStyle(
                        color = infoColor,
                        fontSize = infoSize,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
        val spaceBetweenYLabels = yLabelSize.height + minLabelDistance
        yLabels.forEachIndexed { index, result ->
            drawText(
                textLayoutResult = result,
                topLeft = Offset(
                    x = 0f,
                    y = (chartPadding + index * spaceBetweenYLabels).toFloat()
                )
            )
            drawLine(
                color = infoColor.copy(.2f),
                start = Offset(
                    x = xLabelsXOffset.toFloat(),
                    y = (chartPadding + index * spaceBetweenYLabels).toFloat() + yLabelSize.height / 2
                ),
                end = Offset(
                    x = (size.width - chartPadding),
                    y = (chartPadding + index * spaceBetweenYLabels).toFloat() + yLabelSize.height / 2
                )
            )
        }
        val xLabelsYOffset = run {
            chartPadding + (yLabels.size - 1) * spaceBetweenYLabels + yLabelSize.height / 2
        }
        val spaceBetweenXLabels = (size.width - xLabelsXOffset - chartPadding - xLabelSize.height) / (xLabels.size - 1)
        var xStep = 1
        when {
            data.size == 30 -> xStep = 3
            data.size > 7 -> xStep = 2
        }
        xLabels.forEachIndexed { index, result ->
            if ((xLabels.indices step xStep).contains(index)) {
                drawText(
                    textLayoutResult = result,
                    topLeft = Offset(
                        x = xLabelsXOffset + index * spaceBetweenXLabels,
                        y = xLabelsYOffset.toFloat() + chartPadding * 2
                    )
                )
            }
        }

        val contentOffset = chartPadding + yLabelSize.height / 2
        val contentHeight = xLabelsYOffset.toFloat() - contentOffset
        fun yValueToContent(value: Int): Float = contentHeight - (value * contentHeight / yLabelMax)
        val chartPath = Path()
        chartPath.moveTo(
            x = (xLabelsXOffset + xLabelSize.height / 2).toFloat(),
            y = contentOffset + yValueToContent(data.first().y)
        )

        data.forEachIndexed { index, data ->
            chartPath.lineTo(
                x = xLabelsXOffset + index * spaceBetweenXLabels + xLabels[index].size.height / 2,
                y = contentOffset + yValueToContent(data.y)
            )
        }

        drawLine(
            color = infoColor,
            start = Offset(
                x = xLabelsXOffset.toFloat(),
                y = chartPadding.toFloat()
            ),
            end = Offset(
                x = xLabelsXOffset.toFloat(),
                y = xLabelsYOffset.toFloat()
            )
        )
        drawLine(
            color = infoColor,
            start = Offset(
                x = xLabelsXOffset.toFloat(),
                y = xLabelsYOffset.toFloat()
            ),
            end = Offset(
                x = size.width - chartPadding,
                y = xLabelsYOffset.toFloat()
            )
        )

        drawPath(
            path = chartPath,
            color = contentColor,
            style = Stroke(
                width = 8f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

@Preview
@Composable
fun ChartPreview() {
    Column(
        modifier = Modifier.fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Spacer(modifier = Modifier.height(50.dp))
        Chart(
            data = listOf(
                ChartData(x = 1, y = 10),
                ChartData(x = 2, y = 12),
                ChartData(x = 3, y = 14),
                ChartData(x = 4, y = 9),
                ChartData(x = 5, y = 11)
            ),
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth(),
            infoSize = 14.sp
        )
        Spacer(modifier = Modifier.height(50.dp))
        Chart(
            data = listOf(
                ChartData(x = 1, y = 45),
                ChartData(x = 2, y = 101),
                ChartData(x = 3, y = 10),
                ChartData(x = 4, y = 75),
                ChartData(x = 5, y = 78),
                ChartData(x = 6, y = 90),
                ChartData(x = 7, y = 13),
            ),
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(50.dp))
    }
}