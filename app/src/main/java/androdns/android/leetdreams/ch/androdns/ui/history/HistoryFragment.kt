package androdns.android.leetdreams.ch.androdns.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androdns.android.leetdreams.ch.androdns.R

class HistoryFragment : Fragment() {

    private lateinit var historyViewModel: HistoryViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        historyViewModel =
                ViewModelProvider(this).get(HistoryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_history, container, false)
        val textView: TextView = root.findViewById(R.id.text_gallery)
        historyViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}