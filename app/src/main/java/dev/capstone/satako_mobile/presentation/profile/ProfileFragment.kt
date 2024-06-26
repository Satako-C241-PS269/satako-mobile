package dev.capstone.satako_mobile.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder

import dev.capstone.satako_mobile.R
import dev.capstone.satako_mobile.databinding.FragmentProfileBinding
import dev.capstone.satako_mobile.presentation.ViewModelFactory
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            profileViewModel.getUsername().observe(viewLifecycleOwner) { username ->
                if (username != null) {
                    usernameTextView.text = username
                }
            }

            profileViewModel.getEmail().observe(viewLifecycleOwner) { email ->
                if (email != null) {
                    emailTextView.text = email
                }
            }

            btnSettings.setOnClickListener {
                view.findNavController().navigate(R.id.action_profile_fragment_to_settingsFragment)
            }

            btnLogout.setOnClickListener {
                showLogoutDialog()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { dialog, which ->
                profileViewModel.logout() {
                    findNavController().navigate(
                        R.id.action_profile_fragment_to_onboardingFragment, null,
                        NavOptions.Builder().setPopUpTo(R.id.main_navigation, true).build()
                    )
                }
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

}