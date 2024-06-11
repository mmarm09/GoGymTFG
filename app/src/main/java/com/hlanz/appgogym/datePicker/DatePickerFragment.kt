package com.hlanz.appgogym.datePicker

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.hlanz.appgogym.R

class DatePickerFragment : DialogFragment() {

    //Variable para almacenar la fecha seleccionada
    private var listener: DatePickerDialog.OnDateSetListener? = null

    //Llamar método para crear el cuadro de diálogo
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Devuelve un cuadro de diálogo para elegir la fecha con el año, mes y
        // día actual y asigna el listener para manejar la fecha seleccionada
        val datePickerDialog = DatePickerDialog(requireActivity(), listener, year, month, day)

        return datePickerDialog
    }

    //Objeto companion para definir métodos estáticos
    companion object {
        //Método para crear una instancia de DatePickerFragment con un listener
        fun newInstance(listener: DatePickerDialog.OnDateSetListener): DatePickerFragment {
            val fragment = DatePickerFragment()
            fragment.listener = listener
            return fragment
        }
    }
}