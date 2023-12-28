package com.tasnim.chowdhury.music.ui.fragments

import android.app.Service
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.databinding.FragmentSettingsBinding
import com.tasnim.chowdhury.music.ui.MainActivity
import com.tasnim.chowdhury.music.utilities.closeApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess


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
                binding.systemDefaultTheme.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.palette1Orange)
                binding.systemDefaultTheme.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.white))
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
            6 -> {
                binding.colorRedDot.visibility = View.VISIBLE
            }
            7 -> {
                binding.musicImageColor.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.palette1Orange)
                binding.musicImageColor.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.white))
            }
        }

        setupClicks()
    }

    private fun setupClicks() {
        binding.colorRed.setOnClickListener {
            saveTheme(6)
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
        binding.systemDefaultTheme.setOnClickListener {
            saveTheme(0)
        }
        binding.musicImageColor.setOnClickListener {
            saveTheme(7)
        }

        binding.sortBtn.setOnClickListener {
            val sortList = arrayOf(
                "Recently Added",
                "Song Title (Name)",
                "File Size"
            )
            var currentSort = MainFragment.sortOrder
            val sortDialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle("Sort by...")
                .setSingleChoiceItems(sortList, currentSort){ _, itemClick ->
                    currentSort = itemClick
                }
                .setPositiveButton("Yes") { dialog, _ ->
                    val sortEditor = activity?.getSharedPreferences("SORT_ORDER", MODE_PRIVATE)?.edit()
                    sortEditor?.putInt("sortOrder", currentSort)
                    sortEditor?.apply()
                    dialog.dismiss()
                    /*val i = activity?.packageManager?.getLaunchIntentForPackage(requireActivity().packageName)
                    i?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    i?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    activity?.finishAffinity()
                    startActivity(i!!)*/
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
            sortDialog.create()
            sortDialog.show()
        }

        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
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