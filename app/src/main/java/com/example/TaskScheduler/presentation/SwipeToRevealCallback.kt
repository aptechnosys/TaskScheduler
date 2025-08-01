package com.example.TaskScheduler.presentation

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class SwipeToRevealCallback(
    private val onDeleteClicked: (position: Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val backgroundColor = Color.parseColor("#F44336")
    private val deleteIcon = android.R.drawable.ic_delete // your icon

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // NO ACTION — wait for delete icon click
        // Reset swipe
        (viewHolder as? AlarmAdapter.AlarmViewHolder)?.resetSwipe()
    }

    override fun onChildDraw(
        canvas: Canvas, recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView

        val paint = Paint().apply {
            color = backgroundColor
        }

        // Draw red background
        canvas.drawRect(
            itemView.right + dX,
            itemView.top.toFloat(),
            itemView.right.toFloat(),
            itemView.bottom.toFloat(),
            paint
        )

        // Draw delete icon
        val icon = ContextCompat.getDrawable(recyclerView.context, deleteIcon)
        icon?.let {
            val iconMargin = (itemView.height - it.intrinsicHeight) / 2
            val iconTop = itemView.top + iconMargin
            val iconLeft = itemView.right - iconMargin - it.intrinsicWidth
            val iconRight = itemView.right - iconMargin
            val iconBottom = iconTop + it.intrinsicHeight

            it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            it.draw(canvas)

            // Handle click on icon area
            itemView.setOnClickListener {
                val touchX = recyclerView.context.resources.displayMetrics.density * 80 // approx icon area
                if (dX < -touchX) {
                    onDeleteClicked(viewHolder.bindingAdapterPosition)
                }
            }
        }

        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}