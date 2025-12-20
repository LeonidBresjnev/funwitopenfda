package com.openfda.funwitopenfda.openfda
/*
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember


@Composable
fun myRememberStaggeredGridScrollbarAdapter(state: LazyStaggeredGridState): ScrollbarAdapter {
    return remember(state) {
        object : ScrollbarAdapter {
            private val itemValue = 100.0

            override val scrollOffset: Double
                get() {
                    val layoutInfo = state.layoutInfo
                    val visibleItems = layoutInfo.visibleItemsInfo
                    if (visibleItems.isEmpty()) return 0.0

                    // Find the item with the smallest index currently visible
                    val minIndex = visibleItems.minOf { it.index }
                    val firstItem = visibleItems.first { it.index == minIndex }

                    // Calculate how far into the item we have scrolled
                    // offset is usually negative in layoutInfo (distance from top of viewport)
                    val offsetPixels = -firstItem.offset.y.toDouble()
                    val itemHeight = firstItem.size.height.toDouble()

                    val progressInItem = if (itemHeight > 0) offsetPixels / itemHeight else 0.0

                    return (minIndex.toDouble() + progressInItem) * itemValue
                }

            override val contentSize: Double
                get() {
                    val totalItems = state.layoutInfo.totalItemsCount
                    return totalItems.toDouble() * itemValue
                }

            override val viewportSize: Double
                get() {
                    val layoutInfo = state.layoutInfo
                    val visibleCount = layoutInfo.visibleItemsInfo.size.toDouble()
                    // Adjust viewport size based on number of columns to keep thumb size sane
                    return visibleCount * itemValue
                }

            override suspend fun scrollTo(scrollOffset: Double) {
                val totalItems = state.layoutInfo.totalItemsCount
                if (totalItems <= 0) return

                val targetItemIndex = (scrollOffset / itemValue).toInt().coerceIn(0, totalItems - 1)
                val progressInItem = (scrollOffset % itemValue) / itemValue

                // We don't know the future height of the item we're scrolling to,
                // so we use a standard guess or the current average height
                val estimatedHeight = 300.0
                val pixelOffset = (progressInItem * estimatedHeight).toInt()

                state.scrollToItem(targetItemIndex, pixelOffset)
            }

        }
    }
}*/