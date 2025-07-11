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

import com.dip.geetainbangla.databinding.FragmentChapter7Binding


class Chapter7Fragment : Fragment() {

    private var _binding: FragmentChapter7Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChapter7Binding.inflate(inflater, container, false)
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
            title = getString(R.string.menu_chapter7title)
        }
        // Call your reusable function, now adapted for Fragment:
        commonlib.loadAndDisplayChapter(
            fragment = this,
            jsonFileName = "chapter7.json",
            containerId = binding.verseContainer.id
        )
    }
}