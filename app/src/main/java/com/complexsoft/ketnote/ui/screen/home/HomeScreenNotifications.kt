package com.complexsoft.ketnote.ui.screen.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.databinding.HomeNotificationsLayoutBinding
import com.complexsoft.ketnote.ui.screen.utils.NotificationsUiState
import com.complexsoft.ketnote.ui.screen.utils.adapters.NotificationItemAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeScreenNotifications : BottomSheetDialogFragment(R.layout.home_notifications_layout) {

    private lateinit var binding: HomeNotificationsLayoutBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = HomeNotificationsLayoutBinding.bind(view)
        val viewModel by viewModels<HomeScreenNotificationsViewModel>()
        val notificationsItemAdapter = NotificationItemAdapter(emptyList()) {

        }

        binding.dragHandle.setOnClickListener {
            viewModel.insertNotification("nueva noti", "dsad")
        }


        binding.clearAllNotificationsButton.setOnClickListener {
            viewModel.clearAllNotifications()
            this.dismiss()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.notificationsFlow.collectLatest { state ->
                        when (state) {
                            is NotificationsUiState.Success -> {
                                if (state.data.isNotEmpty()) {
                                    binding.clearAllNotificationsButton.visibility = View.VISIBLE
                                    binding.notificationsRecycler.visibility = View.VISIBLE
                                    notificationsItemAdapter.updateList(
                                        state.data
                                    )
                                    viewModel.updateNotificationsButtonState(false)
                                } else {
                                    binding.notificationsRecycler.visibility = View.GONE
                                    binding.clearAllNotificationsButton.visibility = View.GONE
                                    notificationsItemAdapter.updateList(
                                        emptyList()
                                    )
                                    viewModel.updateNotificationsButtonState(true)
                                }
                            }

                            is NotificationsUiState.Error -> {


                            }

                            is NotificationsUiState.Loading -> {


                            }
                        }
                    }
                }
            }
        }

        binding.notificationsRecycler.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = notificationsItemAdapter
        }
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }
}