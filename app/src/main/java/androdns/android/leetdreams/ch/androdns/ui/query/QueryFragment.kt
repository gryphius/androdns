package androdns.android.leetdreams.ch.androdns.ui.query

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androdns.android.leetdreams.ch.androdns.R

class QueryFragment : Fragment() {

    private lateinit var queryViewModel: QueryViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        queryViewModel =
                ViewModelProvider(this).get(QueryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_query, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        queryViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}