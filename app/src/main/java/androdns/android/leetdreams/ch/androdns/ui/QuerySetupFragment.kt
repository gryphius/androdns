package androdns.android.leetdreams.ch.androdns.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androdns.android.leetdreams.ch.androdns.R
import android.app.AlertDialog
import android.content.DialogInterface
import android.widget.Button
import com.google.android.material.snackbar.Snackbar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [QuerySetupFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class QuerySetupFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater!!.inflate(R.layout.fragment_query_setup, container, false)

        // type/class dialog
        val dialogTypeClassButton: Button = view.findViewById(R.id.buttonTypeClass)
        dialogTypeClassButton.setOnClickListener{
                view ->
            Snackbar.make(view, "opening the thing", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        // transport dialog
        val dialogTransportButton: Button = view.findViewById(R.id.buttonTransport)
        dialogTransportButton.setOnClickListener{
                view ->
            Snackbar.make(view, "opening the transport thing", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            showCustomDialog();
        }

        return view
    }

    private lateinit var alertDialog: AlertDialog
    fun showCustomDialog() {
        val inflater: LayoutInflater = this.getLayoutInflater()
        val dialogView: View = inflater.inflate(R.layout.fragment_transport_setup, null)

        /*
        val header_txt = dialogView.findViewById<TextView>(R.id.header)
        header_txt.text = "Header Message"
        val details_txt = dialogView.findViewById<TextView>(R.id.details)
        val custom_button: Button = dialogView.findViewById(R.id.customBtn)
        custom_button.setOnClickListener {
            //perform custom action
        }
        */

        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        dialogBuilder.setOnDismissListener(object : DialogInterface.OnDismissListener {
            override fun onDismiss(arg0: DialogInterface) {

            }
        })
        dialogBuilder.setView(dialogView)

        alertDialog = dialogBuilder.create();
        //alertDialog.window!!.getAttributes().windowAnimations = R.style.p
        alertDialog.show()
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment QuerySetupFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            QuerySetupFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}