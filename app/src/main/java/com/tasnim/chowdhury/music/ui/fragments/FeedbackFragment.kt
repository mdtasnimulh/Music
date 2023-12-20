package com.tasnim.chowdhury.music.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.databinding.FragmentFeedbackBinding

class FeedbackFragment : Fragment() {

    private var _binding: FragmentFeedbackBinding? = null
    private val binding get() = _binding!!

    private var feedbackMessage = ""
    private var topic = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedbackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
        setupClicks()
    }

    private fun setupClicks() {
        binding.sendFeedbackButton.setOnClickListener {
            Toast.makeText(requireContext(), feedbackMessage + topic, Toast.LENGTH_SHORT).show()
        }
    }

    private fun initData() {
        feedbackMessage = "User Email: ${binding.feedbackEmailEt.text}\nUser Feedback: ${binding.feedbackDescriptionEt.text}"
        topic = "Title: ${binding.feedbackTopicEt.text}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}