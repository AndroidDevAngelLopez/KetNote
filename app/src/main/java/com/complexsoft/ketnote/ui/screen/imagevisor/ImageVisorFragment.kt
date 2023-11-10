package com.complexsoft.ketnote.ui.screen.imagevisor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.databinding.ImageVisorLayoutBinding

class ImageVisorFragment : Fragment(R.layout.image_visor_layout) {

    private lateinit var binding: ImageVisorLayoutBinding
    private val args: ImageVisorFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = ImageVisorLayoutBinding.inflate(layoutInflater)
        ViewCompat.setOnApplyWindowInsetsListener(binding.imageVisorCoordinatorLayout) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                topMargin = insets.top
                rightMargin = insets.right
                bottomMargin = insets.bottom
            }
            WindowInsetsCompat.CONSUMED
        }
        binding.imageVisorTopAppBar.title = args.title
        binding.imageVisorTopAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        Glide.with(this).load(args.image).into(binding.imageVisorImage)
        return binding.root
    }
}