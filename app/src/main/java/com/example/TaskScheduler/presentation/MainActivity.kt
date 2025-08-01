package com.example.TaskScheduler.presentation

import AlarmScheduler
import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.BillingClient
import com.example.TaskScheduler.R
import com.example.TaskScheduler.databinding.ActivityMainBinding
import com.example.TaskScheduler.domain.model.Alarm
import com.example.TaskScheduler.domain.model.Payment
import com.example.TaskScheduler.domain.usecase.GetPaymentUseCase
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryPurchasesParams

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<AlarmViewModel>()
    private val alarmAdapter = AlarmAdapter()
    private val notificationAdapter = AlarmAdapter()

    private var latestAlarmList: List<Alarm> = emptyList()
    private var latestNotificationList: List<Alarm> = emptyList()
    private var paymentStatusOnlyList: List<Payment> = emptyList()

   // private lateinit var upiPaymentLauncher: ActivityResultLauncher<Intent>
    var bypassPaymentFlag: Boolean? = false

   // lateinit var billingClient: BillingClient

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchasesList ->

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchasesList?.forEach { purchase ->
                    val isValid = isThreeMonthSubscriptionValid(purchase)

                    if (isValid) {
                        Log.d("SUBSCRIPTION", "User has a valid 3-month subscription")
                        // Grant premium access

                    } else {
                        Log.d("SUBSCRIPTION", "Subscription expired or invalid")
                        // Restrict access
                        lifecycleScope.launch {
                            viewModel.paymentList.collectLatest {
                                paymentStatusOnlyList = it
                                paymentStatusOnlyList.forEach { payment ->
                                    viewModel.deletePayment(payment)
                                    bypassPaymentFlag = false
                                }
                            }
                        }
                    }
                }
            }
        }*/

        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.recyclerViewAlarms.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewAlarms.adapter = alarmAdapter


        binding.recyclerViewNotifications.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewNotifications.adapter = notificationAdapter

        lifecycleScope.launch {
            viewModel.alarmOnlyList.collectLatest {
                latestAlarmList = it
                binding.recyclerViewAlarms.visibility =
                    if (it.isEmpty()) View.GONE else View.VISIBLE
                binding.tvAlarmSection.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
                alarmAdapter.submitList(it)
                updateNoDataVisibility()
            }
        }

        lifecycleScope.launch {
            viewModel.notificationOnlyList.collectLatest {
                latestNotificationList = it
                binding.recyclerViewNotifications.visibility =
                    if (it.isEmpty()) View.GONE else View.VISIBLE
                binding.tvNotificationSection.visibility =
                    if (it.isEmpty()) View.GONE else View.VISIBLE
                notificationAdapter.submitList(it)
                updateNoDataVisibility()
            }
        }

        binding.fabAddAlarm.setOnClickListener {

            lifecycleScope.launch {
                viewModel.paymentList.collectLatest {
                    paymentStatusOnlyList = it
                    paymentStatusOnlyList.forEach { payment ->
                        Log.d("Payment", "Was Successful? ${payment.isSuccessful}")
                        bypassPaymentFlag = payment.isSuccessful
                    }
                }
            }

            if (latestAlarmList.count() + latestNotificationList.count() == 2 && bypassPaymentFlag == false) {
                val inflater: LayoutInflater = LayoutInflater.from(this)
                val view: View = inflater.inflate(R.layout.payment_dialog, null)

                val builder = AlertDialog.Builder(this)
                builder.setView(view)

                val dialog: AlertDialog = builder.create()

                val cancelButton: Button = view.findViewById(R.id.buttonCancel)
                val payButton: Button = view.findViewById(R.id.buttonPay)

                cancelButton.setOnClickListener {
                    dialog.dismiss()
                }

                payButton.setOnClickListener {
                    // Handle payment process here
                    dialog.dismiss()
                    startPayment()
                }

                dialog.show()
            } else {

                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (!alarmManager.canScheduleExactAlarms()) {
                        showExactAlarmPermissionDialog(this, alarmManager)
                        return@setOnClickListener // 🔴 Don't add alarm if permission not granted
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        )
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                            NOTIFICATION_PERMISSION_REQUEST_CODE
                        )
                        return@setOnClickListener
                    }
                }

                val bottomSheet = AlarmBottomSheet { alarm ->
                    val selectedDateTime = alarm.timestamp
                    AlarmScheduler.scheduleAlarm(this, selectedDateTime, alarm)
                    viewModel.addAlarm(alarm)
                }
                bottomSheet.show(supportFragmentManager, "AlarmBottomSheet")
            }
        }

        attachSwipeToDelete(binding.recyclerViewAlarms, alarmAdapter) { alarm ->
            viewModel.deleteAlarm(alarm)
        }

        attachSwipeToDelete(binding.recyclerViewNotifications, notificationAdapter) { alarm ->
            viewModel.deleteAlarm(alarm)
        }
    }

    fun startPayment() {
        payUsingUpi(
            name = "RASHID ALAM",
            upiId = "alam.rashid039@oksbi",
            amount = "49.00",
            note = "ALAM SCHEDULER QUARTERLY SUBSCRIPTION FEE",
            context = this
        )
    }

    fun isGPayInstalled(context: Context): Boolean {
        val packageManager = context.packageManager
        return try {
            packageManager.getPackageInfo("com.google.android.apps.nbu.paisa.user", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun payUsingUpi(
        name: String,
        upiId: String,
        amount: String,
        note: String,
        context: Context
    ) {
        if (!isGPayInstalled(context)) {
            Toast.makeText(context, "Google Pay is not installed", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", upiId)  // Payee address
            .appendQueryParameter("pn", name)   // Payee name
            .appendQueryParameter("tn", note)   // Transaction note
            .appendQueryParameter("am", amount) // Amount
            .appendQueryParameter("cu", "INR")  // Currency code
            .build()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = uri
            setPackage("com.google.android.apps.nbu.paisa.user")  // Google Pay package name
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            (context as Activity).startActivityForResult(intent, UPI_PAYMENT_REQUEST_CODE)
        } else {
            Toast.makeText(context, "Google Pay app is not available to handle the transaction", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == UPI_PAYMENT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK || resultCode == 11) {

                val response = data?.getStringExtra("response")

                if (response != null) {
                    Log.d("UPI", "UPI Response: $response")

                    if (response.contains("SUCCESS", ignoreCase = true)) {

                        val payment = Payment(
                            timestamp = System.currentTimeMillis(),
                            isSuccessful = true
                        )

                        viewModel.addPayment(payment)

                        Toast.makeText(this, "Payment Successful. Your subscription is now active for next 3 months.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Payment Failed or Cancelled", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(this, "No response from UPI app", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Payment cancelled or failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showExactAlarmPermissionDialog(context: Context, alarmManager: AlarmManager) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Permission Required")
            .setMessage("Please enable 'Schedule Exact Alarms' permission in system settings to allow alarms to trigger on time.")
            .setPositiveButton("Open Settings") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (!alarmManager.canScheduleExactAlarms()) {
                        // Optional: Open system settings for user to allow it
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        startActivity(intent)

                        return@setPositiveButton // 🔴 Don't add alarm if permission not granted
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun attachSwipeToDelete(
        recyclerView: RecyclerView,
        adapter: AlarmAdapter,
        deleteCallback: (Alarm) -> Unit
    ) {
        val itemTouchHelperCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ) = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.bindingAdapterPosition
                    val alarm = adapter.currentList.getOrNull(position)
                    alarm?.let { deleteCallback(it) }
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float, dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    val itemView = viewHolder.itemView
                    val paint = Paint().apply {
                        color = "#F44336".toColorInt()
                    }

                    c.drawRect(
                        itemView.right.toFloat() + dX, itemView.top.toFloat(),
                        itemView.right.toFloat(), itemView.bottom.toFloat(),
                        paint
                    )

                    val icon = ContextCompat.getDrawable(
                        recyclerView.context,
                        android.R.drawable.ic_menu_delete
                    )
                    icon?.let {
                        val iconMargin = (itemView.height - it.intrinsicHeight) / 2
                        val iconTop = itemView.top + iconMargin
                        val iconLeft = itemView.right - iconMargin - it.intrinsicWidth
                        val iconRight = itemView.right - iconMargin
                        val iconBottom = iconTop + it.intrinsicHeight

                        it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        it.draw(c)
                    }

                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }
            }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    fun isThreeMonthSubscriptionValid(purchase: Purchase): Boolean {
        // The purchase time in milliseconds since epoch
        val purchaseTimeMillis = purchase.purchaseTime
        val currentTimeMillis = System.currentTimeMillis()

        // Approximate 3 months = 90 days
        val threeMonthsInMillis = 90L * 24 * 60 * 60 * 1000

        // Check if current time is within 3 months of purchase time
        val isWithinThreeMonths = (currentTimeMillis - purchaseTimeMillis) < threeMonthsInMillis

        // Acknowledged purchase means user has accepted the subscription
        return isWithinThreeMonths && purchase.isAcknowledged
    }

    private fun updateNoDataVisibility() {
        val noData = latestAlarmList.isEmpty() && latestNotificationList.isEmpty()
        binding.ivNoDataFound.visibility = if (noData) View.VISIBLE else View.GONE
        binding.tvNoDataFound.visibility = if (noData) View.VISIBLE else View.GONE
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1002
        private const val UPI_PAYMENT_REQUEST_CODE = 123
    }
}