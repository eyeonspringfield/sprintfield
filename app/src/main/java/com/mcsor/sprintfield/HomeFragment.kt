package com.mcsor.sprintfield

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.mcsor.sprintfield.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        val currentUser = auth.currentUser

        if (currentUser != null && currentUser.email != null) {
            binding.textViewGreeting.text = getString(R.string.logged_in_as, currentUser.email)
        } else {
            binding.textViewGreeting.text = getString(R.string.not_logged_in)
        }

        binding.buttonTeams.setOnClickListener {
            findNavController().navigate(R.id.teamsFragment)
        }

        binding.buttonProjects.setOnClickListener {
            findNavController().navigate(R.id.projectsFragment)
        }

        binding.buttonSprints.setOnClickListener {
            findNavController().navigate(R.id.sprintsFragment)
        }

        binding.buttonTasks.setOnClickListener {
            findNavController().navigate(R.id.tasksFragment)
        }
    }
}

