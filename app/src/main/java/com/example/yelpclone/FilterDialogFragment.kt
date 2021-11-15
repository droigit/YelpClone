package com.example.yelpclone

import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView


class FilterDialogFragment: DialogFragment(){
    private lateinit var bCancel: Button
    private lateinit var bSubmit: Button
    private lateinit var ratingRadioGroup: RadioGroup

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?{
        var rootView: View = inflater.inflate(R.layout.fragment_dialog_filter, container, false)

        bCancel = rootView.findViewById(R.id.bCancel)
        bCancel.setOnClickListener{
            dismiss()
        }

        bSubmit = rootView.findViewById(R.id.bSubmit)
        bSubmit.setOnClickListener{
            ratingRadioGroup = rootView.findViewById(R.id.ratingRadioGroup)

            if(ratingRadioGroup.checkedRadioButtonId == -1) dismiss()
            else {
                val selectedPrice = ratingRadioGroup.checkedRadioButtonId
                val radio = rootView.findViewById<RadioButton>(selectedPrice)
                var ratingPrice = radio.text.toString()

                val mActivity = activity as MainActivity
                mActivity.desiredPrice = ratingPrice
                mActivity.putData()
                dismiss()
            }
        }

        return rootView
    }
}