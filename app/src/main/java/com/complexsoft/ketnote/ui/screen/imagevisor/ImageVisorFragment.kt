package com.complexsoft.ketnote.ui.screen.imagevisor

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.databinding.ImageVisorLayoutBinding
import kotlin.math.max
import kotlin.math.min

class ImageVisorFragment : Fragment(R.layout.image_visor_layout) {

    private lateinit var binding: ImageVisorLayoutBinding
    private lateinit var mScaleGestureDetector: ScaleGestureDetector
    private var mScaleFactor = 1.0f
    private val args: ImageVisorFragmentArgs by navArgs()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = ImageVisorLayoutBinding.inflate(layoutInflater)
        Glide.with(this).load(args.image).into(binding.imageVisorLayout)
        mScaleGestureDetector = context?.let { ScaleGestureDetector(it, ScaleListener()) }!!
        binding.imageVisorLayout.setOnTouchListener { _, event ->
            mScaleGestureDetector.onTouchEvent(event)
            true
        }
        return binding.root
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
            mScaleFactor *= scaleGestureDetector.scaleFactor
            mScaleFactor = max(0.1f, min(mScaleFactor, 5.0f))
            binding.imageVisorLayout.scaleX = mScaleFactor
            binding.imageVisorLayout.scaleY = mScaleFactor
            return true
        }
    }
}