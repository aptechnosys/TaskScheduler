package com.example.TaskScheduler.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.TaskScheduler.databinding.ItemAlarmBinding
import com.example.TaskScheduler.domain.model.Alarm
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Color
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import com.example.TaskScheduler.R

class AlarmAdapter : ListAdapter<Alarm, AlarmAdapter.AlarmViewHolder>(AlarmDiffCallback()) {
    private var lastPosition = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = ItemAlarmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(getItem(position))

        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.fade_slide_up)
            holder.itemView.startAnimation(animation)
            lastPosition = holder.bindingAdapterPosition
        }
    }

    class AlarmViewHolder(val binding: ItemAlarmBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(alarm: Alarm) {
            binding.tvTaskTitle.text = alarm.title
            binding.tvTaskDescription.text = alarm.description
            val date = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(alarm.timestamp))
            binding.tvTime.text = date

            if (alarm.isAlarmSet) {
                binding.imgTaskIcon.setImageResource(R.drawable.ic_alarm)
            } else {
                binding.imgTaskIcon.setImageResource(R.drawable.ic_notification)
            }

            val isExpired = alarm.timestamp < System.currentTimeMillis()

            if (isExpired) {
                binding.cardView.setCardBackgroundColor(ContextCompat.getColor(binding.root.context,R.color.grey_light))
                binding.tvTaskTitle.setTextColor(ContextCompat.getColor(binding.root.context,R.color.grey_medium))
                binding.tvTaskDescription.setTextColor(ContextCompat.getColor(binding.root.context,R.color.grey_medium))
                binding.tvTime.setTextColor(ContextCompat.getColor(binding.root.context,R.color.grey_medium))
                binding.imgTaskIcon.setColorFilter(ContextCompat.getColor(binding.root.context,R.color.grey_medium))
            } else {
                binding.root.setCardBackgroundColor(Color.WHITE)
                binding.tvTaskTitle.setTextColor(ContextCompat.getColor(binding.root.context,R.color.black))
                binding.tvTaskDescription.setTextColor(ContextCompat.getColor(binding.root.context,R.color.grey_dark))
                binding.tvTime.setTextColor(ContextCompat.getColor(binding.root.context,R.color.grey_dark))
                binding.imgTaskIcon.setColorFilter(ContextCompat.getColor(binding.root.context,R.color.light_blue))
            }

            // Apply a random light color to the CardView background
         //   binding.cardView.setCardBackgroundColor(generateRandomLightColor())
        }

        fun resetSwipe() {
            itemView.translationX = 0f
        }

        private fun generateRandomLightColor(): Int {
            val hue = (0..360).random().toFloat()
            val saturation = 0.1f   // Very low saturation
            val value = 0.97f       // Very high brightness
            return Color.HSVToColor(floatArrayOf(hue, saturation, value))
        }
    }

    class AlarmDiffCallback : DiffUtil.ItemCallback<Alarm>() {
        override fun areItemsTheSame(oldItem: Alarm, newItem: Alarm) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Alarm, newItem: Alarm) = oldItem == newItem
    }


}