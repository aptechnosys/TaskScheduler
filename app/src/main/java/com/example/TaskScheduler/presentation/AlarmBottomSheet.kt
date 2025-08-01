package com.example.TaskScheduler.presentation

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.TaskScheduler.R
import com.example.TaskScheduler.databinding.BottomSheetAlarmBinding
import com.example.TaskScheduler.domain.model.Alarm
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.*

class AlarmBottomSheet(private val onSave: (Alarm) -> Unit) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAlarmBinding? = null
    private val binding get() = _binding
    private var selectedTime: Long = System.currentTimeMillis()

    override fun onStart() {
        super.onStart()
        dialog?.let {
            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false // Prevent dismiss on outside touch or back press
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): ConstraintLayout? {
        _binding = BottomSheetAlarmBinding.inflate(inflater, container, false)
        val date = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        val dateTime = "Select Date and Time"

        binding?.tvDate?.text = dateTime


        binding?.apply {
            checkboxSystemAlarm.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) checkboxSystemNotification.isChecked = false
            }

            checkboxSystemNotification.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) checkboxSystemAlarm.isChecked = false
            }
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding?.tvDate?.setOnClickListener {
            val calendar = Calendar.getInstance()

            // Show DatePickerDialog with current date as default
            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                // Show TimePickerDialog with current time as default
                TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)

                    selectedTime = calendar.timeInMillis

                    val date = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(selectedTime))
                    val dateTime = "Selected Date and Time: $date"
                    binding?.tvDate?.text = dateTime

                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()

            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding?.btnAddAlarm?.setOnClickListener {
            val title = binding?.etAlarmTitle?.text.toString().trim()
            val description = binding?.etAlarmDescription?.text.toString().trim()
            val isAlarmSet = binding?.checkboxSystemAlarm?.isChecked == true
            val isNotificationSet = binding?.checkboxSystemNotification?.isChecked == true

            // Validation checks
            if (title.isEmpty()) {
                binding?.etAlarmTitle?.error = "Title is required"
                return@setOnClickListener
            }

            if (!isAlarmSet && !isNotificationSet) {
                Toast.makeText(requireContext(), "Please select at least one option (Alarm or Notification)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedTime <= System.currentTimeMillis()) {
                Toast.makeText(requireContext(), "Please select a future time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val alarm = Alarm(
                title = title,
                description = description,
                timestamp = selectedTime,
                isAlarmSet = isAlarmSet,
                isNotificationSet = isNotificationSet,
                createdDate = System.currentTimeMillis()
            )
            onSave(alarm)
            dismiss()
        }


        binding?.btnClose?.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getTheme(): Int {
        return R.style.RoundedBottomSheetDialog
    }
}