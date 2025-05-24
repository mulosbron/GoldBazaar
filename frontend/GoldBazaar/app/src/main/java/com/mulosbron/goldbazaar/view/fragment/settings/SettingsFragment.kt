package com.mulosbron.goldbazaar.view.fragment.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import com.mulosbron.goldbazaar.R
import com.mulosbron.goldbazaar.util.ThemeManager

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        val radioGroup = view.findViewById<RadioGroup>(R.id.rgTheme)

        // Önceki seçimi işaretle
        when (ThemeManager.getCurrentPalette(requireContext())) {
            ThemeManager.Palette.PALETTE1 -> view.findViewById<RadioButton>(R.id.rbPalette1).isChecked = true
            ThemeManager.Palette.PALETTE4 -> view.findViewById<RadioButton>(R.id.rbPalette4).isChecked = true
            ThemeManager.Palette.PALETTE5 -> view.findViewById<RadioButton>(R.id.rbPalette5).isChecked = true
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val newPalette = when (checkedId) {
                R.id.rbPalette1 -> ThemeManager.Palette.PALETTE1
                R.id.rbPalette4 -> ThemeManager.Palette.PALETTE4
                R.id.rbPalette5 -> ThemeManager.Palette.PALETTE5
                else -> ThemeManager.Palette.PALETTE1
            }
            ThemeManager.savePalette(requireContext(), newPalette)
            requireActivity().recreate()
        }

        return view
    }
}
