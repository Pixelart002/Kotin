package com.luviio.upialert.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luviio.upialert.R
import com.luviio.upialert.database.Transaction

class TransactionAdapter : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {
    
    private var transactions: List<Transaction> = emptyList()
    
    fun submitList(list: List<Transaction>) {
        transactions = list
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction)
    }
    
    override fun getItemCount() = transactions.size
    
    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvSender: TextView = itemView.findViewById(R.id.tvSender)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvApp: TextView = itemView.findViewById(R.id.tvApp)
        
        fun bind(transaction: Transaction) {
            tvAmount.text = "₹${transaction.amount}"
            tvSender.text = transaction.sender
            tvTime.text = transaction.getFormattedTime()
            tvApp.text = transaction.upiApp
        }
    }
}
