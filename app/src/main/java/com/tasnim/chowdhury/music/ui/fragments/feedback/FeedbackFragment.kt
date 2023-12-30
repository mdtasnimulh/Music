package com.tasnim.chowdhury.music.ui.fragments.feedback

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.databinding.FragmentFeedbackBinding

class FeedbackFragment : Fragment() {

    private var _binding: FragmentFeedbackBinding? = null
    private val binding get() = _binding!!

    private var feedbackMessage = ""
    private var subject = ""
    private var email = ""

    private val emailResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        when (result.resultCode) {
            RESULT_OK -> {
                Toast.makeText(requireContext(), "Thanks for your feedback.", Toast.LENGTH_LONG).show()
                findNavController().previousBackStackEntry?.savedStateHandle?.set("result_key", "Email Sent Successfully!")
                findNavController().popBackStack(R.id.nav_graph, false)
                findNavController().navigate(R.id.mainFragment)
            }
            RESULT_CANCELED -> {
                Toast.makeText(requireContext(), "Thanks for your feedback.", Toast.LENGTH_LONG).show()
                findNavController().previousBackStackEntry?.savedStateHandle?.set("result_key", "Email Sent Successfully!")
                findNavController().popBackStack(R.id.nav_graph, false)
                findNavController().navigate(R.id.mainFragment)
            }
            else -> {
                Toast.makeText(requireContext(), "Email sending failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedbackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClicks()
    }

    private fun setupClicks() {
        binding.sendFeedbackButton.setOnClickListener {
            subject = "Subject: ${binding.feedbackTopicEt.text}"
            email = "tasnimulhasan3@gmail.com"
            feedbackMessage = "User Email: ${binding.feedbackEmailEt.text}\n\nUser Feedback: ${binding.feedbackDescriptionEt.text}"
            sendEmail(email, subject, feedbackMessage)
        }

        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun sendEmail(recipient: String, subject: String, message: String) {
        val mIntent = Intent(Intent.ACTION_SEND)
        mIntent.data = Uri.parse("mailto:")
        mIntent.type = "text/plain"
        mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        mIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        mIntent.putExtra(Intent.EXTRA_TEXT, message)
        try {
            emailResultLauncher.launch(mIntent)
        }
        catch (e: Exception){
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}