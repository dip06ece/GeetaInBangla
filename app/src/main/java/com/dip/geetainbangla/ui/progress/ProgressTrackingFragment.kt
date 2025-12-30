package com.dip.geetainbangla.ui.chapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dip.geetainbangla.R
import com.dip.geetainbangla.commonutils.commonlib
import com.dip.geetainbangla.databinding.FragmentProgressTrackingBinding

class ProgressTrackingFragment : Fragment() {

    private var _binding: FragmentProgressTrackingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgressTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.menu_progress)
        }

        // Reuse bookmark loader (or create a new progress loader later)
        commonlib.loadAndDisplayProgress(
            fragment = this,
            containerId = binding.progressContainer.id
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onResume() {
        super.onResume()

        commonlib.loadAndDisplayProgress(
            fragment = this,
            containerId = binding.progressContainer.id
        )
    }
}
