package com.artscry.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: Int = 0,
    crossAxisSpacing: Int = 0,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val sequences = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        val crossAxisSizes = mutableListOf<Int>()
        val crossAxisPositions = mutableListOf<Int>()

        var crossAxisSpace = 0

        val currentSequence = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentMainAxisSize = 0
        var currentCrossAxisSize = 0

        for (measurable in measurables) {
            val placeable = measurable.measure(constraints)

            if (currentSequence.isNotEmpty() &&
                currentMainAxisSize + mainAxisSpacing + placeable.width > constraints.maxWidth) {

                sequences.add(currentSequence.toList())
                crossAxisSizes.add(currentCrossAxisSize)
                crossAxisPositions.add(crossAxisSpace)

                crossAxisSpace += currentCrossAxisSize + crossAxisSpacing
                currentSequence.clear()

                currentMainAxisSize = 0
                currentCrossAxisSize = 0
            }

            currentSequence.add(placeable)
            currentMainAxisSize += placeable.width + if (currentSequence.size > 1) mainAxisSpacing else 0
            currentCrossAxisSize = maxOf(currentCrossAxisSize, placeable.height)
        }

        if (currentSequence.isNotEmpty()) {
            sequences.add(currentSequence.toList())
            crossAxisSizes.add(currentCrossAxisSize)
            crossAxisPositions.add(crossAxisSpace)
            crossAxisSpace += currentCrossAxisSize
        }

        layout(
            width = constraints.maxWidth,
            height = crossAxisSpace
        ) {
            sequences.forEachIndexed { i, placeables ->
                var mainAxisPosition = 0

                placeables.forEach { placeable ->
                    placeable.placeRelative(
                        x = mainAxisPosition,
                        y = crossAxisPositions[i]
                    )

                    mainAxisPosition += placeable.width + mainAxisSpacing
                }
            }
        }
    }
}
