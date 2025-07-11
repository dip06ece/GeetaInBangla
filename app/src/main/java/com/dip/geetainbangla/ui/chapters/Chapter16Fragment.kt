package com.dip.geetainbangla.ui.chapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dip.geetainbangla.R
import com.dip.geetainbangla.commonutils.commonlib

import com.dip.geetainbangla.databinding.FragmentChapter16Binding


class Chapter16Fragment : Fragment() {

    private var _binding: FragmentChapter16Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChapter16Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configure the Toolbar
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.menu_chapter16title)
        }
        // Call your reusable function, now adapted for Fragment:
        commonlib.loadAndDisplayChapter(
            fragment = this,
            jsonFileName = "chapter16.json",
            containerId = binding.verseContainer.id
        )
    }
}