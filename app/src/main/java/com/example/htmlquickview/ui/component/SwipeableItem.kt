package com.example.htmlquickview.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun SwipeableItem(
    modifier: Modifier = Modifier,
    swipeThreshold: Float = 120f,
    leftActions: @Composable () -> Unit = {},
    rightActions: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        // 背景层：操作按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .matchParentSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧操作按钮（右滑时显示）
            Row(
                modifier = Modifier.alpha(
                    if (offsetX.value > 0) (offsetX.value / swipeThreshold).coerceIn(0f, 1f) else 0f
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leftActions()
            }

            // 右侧操作按钮（左滑时显示）
            Row(
                modifier = Modifier.alpha(
                    if (offsetX.value < 0) (-offsetX.value / swipeThreshold).coerceIn(0f, 1f) else 0f
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                rightActions()
            }
        }

        // 前景内容：可滑动
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.toInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                val newOffset = offsetX.value + dragAmount
                                offsetX.snapTo(newOffset.coerceIn(-swipeThreshold * 1.5f, swipeThreshold * 1.5f))
                            }
                        },
                        onDragEnd = {
                            coroutineScope.launch {
                                if (offsetX.value.absoluteValue > swipeThreshold * 0.5f) {
                                    // 滑动超过50%，保持打开状态
                                    val target = if (offsetX.value > 0) swipeThreshold else -swipeThreshold
                                    offsetX.animateTo(target)
                                    // 3秒后自动回弹
                                    delay(3000)
                                    offsetX.animateTo(0f, tween(300))
                                } else {
                                    // 回弹
                                    offsetX.animateTo(0f, tween(300))
                                }
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}
