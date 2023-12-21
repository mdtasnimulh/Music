package com.tasnim.chowdhury.music.ui.fragments

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.tasnim.chowdhury.music.databinding.FragmentSettingsBinding
import com.tasnim.chowdhury.music.ui.MainActivity


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when(MainActivity.themeIndex){
            0 -> {
                binding.colorRedDot.visibility = View.VISIBLE
            }
            1 -> {
                binding.colorGreenDot.visibility = View.VISIBLE
            }
            2 -> {
                binding.colorOrangeDot.visibility = View.VISIBLE
            }
            3 -> {
                binding.colorBlueDot.visibility = View.VISIBLE
            }
            4 -> {
                binding.colorBlackDot.visibility = View.VISIBLE
            }
            5 -> {
                binding.colorPurpleDot.visibility = View.VISIBLE
            }
        }

        setupClicks()
    }

    private fun setupClicks() {
        binding.colorRed.setOnClickListener {
            saveTheme(0)
        }
        binding.colorGreen.setOnClickListener {
            saveTheme(1)
        }
        binding.colorOrange.setOnClickListener {
            saveTheme(2)
        }
        binding.colorBlue.setOnClickListener {
            saveTheme(3)
        }
        binding.colorBlack.setOnClickListener {
            saveTheme(4)
        }
        binding.colorPurple.setOnClickListener {
            saveTheme(5)
        }
    }

    private fun saveTheme(index: Int) {
        if (MainActivity.themeIndex != index){
            val editor = activity?.getSharedPreferences("THEME", MODE_PRIVATE)?.edit()
            editor?.putInt("themeIndex", index)
            editor?.apply()

            val themeDialog = AlertDialog.Builder(requireContext())
                .setTitle("Apply Theme")
                .setMessage("Are you sure you want to apply this theme?")
                .setPositiveButton("Yes") { dialog, _ ->
                    val i = requireActivity().packageManager.getLaunchIntentForPackage(requireActivity().packageName)
                    i?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    i?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    requireActivity().finish()
                    startActivity(i!!)
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
            themeDialog.create()
            themeDialog.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}