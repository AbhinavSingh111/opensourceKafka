package com.kafka.user.feature.downloads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kafka.user.R
import kotlinx.android.synthetic.main.fragment_reviews.*

/**
 * @author Vipul Kumar; dated 05/02/19.
 *
 * Fragment to show reviews.
 */
class BookDownloadFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reviews, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvReviews.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
//            setController(controller)
        }

//        controller.setData(Book(bookId = ""))
    }

    companion object {
        fun newInstance(data: String): BookDownloadFragment {
            return BookDownloadFragment().also { fragment ->
                fragment.arguments = Bundle().also { it.putString("data", data) }
            }
        }
    }
}
