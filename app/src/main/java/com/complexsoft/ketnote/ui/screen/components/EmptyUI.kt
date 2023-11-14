package com.complexsoft.ketnote.ui.screen.components

import android.view.View
import androidx.fragment.app.Fragment
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.databinding.HomeScreenLayoutBinding
import com.complexsoft.ketnote.databinding.SearchViewLayoutBinding

fun EmptyUI(
    fragment: Fragment,
    loading: Boolean = false,
    message: String = fragment.getString(R.string.no_notes_for_display),
    homeScreenLayoutBinding: HomeScreenLayoutBinding? = null,
    searchScreenLayoutBinding: SearchViewLayoutBinding? = null
) {
    if (homeScreenLayoutBinding != null) {
        if (loading) {
            homeScreenLayoutBinding.homeScreenProgressIndicator.visibility = View.VISIBLE
            homeScreenLayoutBinding.homeScreenMessage.visibility = View.GONE
        } else {
            homeScreenLayoutBinding.homeScreenProgressIndicator.visibility = View.GONE
            homeScreenLayoutBinding.homeScreenMessage.visibility = View.VISIBLE
            homeScreenLayoutBinding.homeScreenMessage.text = message
        }
    }
    if (searchScreenLayoutBinding != null) {
        if (loading) {
            searchScreenLayoutBinding.searchScreenProgressIndicator.visibility = View.VISIBLE
            searchScreenLayoutBinding.searchScreenMessage.visibility = View.GONE
        } else {
            searchScreenLayoutBinding.searchScreenProgressIndicator.visibility = View.GONE
            searchScreenLayoutBinding.searchScreenMessage.visibility = View.VISIBLE
            searchScreenLayoutBinding.searchScreenMessage.text = message
        }
    }
}