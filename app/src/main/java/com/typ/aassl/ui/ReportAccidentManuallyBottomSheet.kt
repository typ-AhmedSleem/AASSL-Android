package com.typ.aassl.ui

import android.content.Context
import android.content.Intent
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.typ.aassl.R
import com.typ.aassl.data.Accidents
import com.typ.aassl.data.models.Accident
import com.typ.aassl.data.models.CarInfo
import com.typ.aassl.data.models.MapLocation
import com.typ.aassl.services.AccidentWatcherService
import kotlin.random.Random

class ReportAccidentManuallyBottomSheet(private val context: Context) : BottomSheetDialog(context) {

	init {
		setContentView(R.layout.bs_add_accident_manually)
		// Inner views
		try {
			val inputChassisId = findViewById<TextInputLayout>(R.id.til_car_chassis_id)!!.editText!!
			val inputModel = findViewById<TextInputLayout>(R.id.til_car_model)!!.editText!!
			val inputOwner = findViewById<TextInputLayout>(R.id.til_car_owner)!!.editText!!
			val inputContacts = findViewById<TextInputLayout>(R.id.til_car_emergency_contacts)!!.editText!!
			findViewById<MaterialButton>(R.id.btn_report)!!.setOnClickListener {
				// Check inputs at first
				if (inputChassisId.text.isNullOrEmpty()) {
					inputChassisId.requestFocus()
					return@setOnClickListener
				}
				if (inputModel.text.isNullOrEmpty()) {
					inputModel.requestFocus()
					return@setOnClickListener
				}
				if (inputOwner.text.isNullOrEmpty()) {
					inputOwner.requestFocus()
					return@setOnClickListener
				}
				if (inputContacts.text.isNullOrEmpty()) {
					inputContacts.requestFocus()
					return@setOnClickListener
				}
				// Report the accident
				val accident = Accident(
					location = MapLocation(
						Random.nextDouble(40.0, 45.0),
						Random.nextDouble(40.0, 45.0)
					),
					timestamp = System.currentTimeMillis(),
					carInfo = CarInfo(
						carId = inputChassisId.text.toString(),
						carModel = inputModel.text.toString(),
						carOwner = inputOwner.text.toString(),
						emergencyContacts = inputContacts.text.toString().split(",").toTypedArray()
					)
				)
				// Save the accident to database
				try {
					Accidents.save(context, accident)
				} catch (e: Throwable) {
					e.printStackTrace()
				}
				// Send broadcast with the accident
				context.sendBroadcast(Intent(AccidentWatcherService.ACTION_NEW_ACCIDENT).apply {
					putExtra("accident", accident)
				})
				// Dismiss the bs
				cancel()
			}
		} catch (e: Throwable) {
			e.printStackTrace()
		} finally {
			cancel()
		}
	}

}
