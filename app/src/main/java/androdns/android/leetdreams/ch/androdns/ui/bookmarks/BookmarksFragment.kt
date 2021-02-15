package androdns.android.leetdreams.ch.androdns.ui.bookmarks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androdns.android.leetdreams.ch.androdns.R

class BookmarksFragment : Fragment() {

    private lateinit var bookmarksViewModel: BookmarksViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        bookmarksViewModel =
                ViewModelProvider(this).get(BookmarksViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_bookmarks, container, false)
        val textView: TextView = root.findViewById(R.id.text_slideshow)
        bookmarksViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}