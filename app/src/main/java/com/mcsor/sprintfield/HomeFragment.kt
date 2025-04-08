package com.mcsor.sprintfield

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mcsor.sprintfield.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!  // safe access to non-null binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
