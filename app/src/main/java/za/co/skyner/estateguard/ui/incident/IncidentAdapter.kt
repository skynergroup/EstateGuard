package za.co.skyner.estateguard.ui.incident

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import za.co.skyner.estateguard.R
import za.co.skyner.estateguard.data.model.Incident
import za.co.skyner.estateguard.databinding.ItemIncidentBinding
import java.text.SimpleDateFormat
import java.util.*

class IncidentAdapter : ListAdapter<Incident, IncidentAdapter.IncidentViewHolder>(IncidentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncidentViewHolder {
        val binding = ItemIncidentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IncidentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IncidentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class IncidentViewHolder(private val binding: ItemIncidentBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(incident: Incident) {
            binding.apply {
                // Set incident description
                textIncidentDescription.text = incident.description
                
                // Format and set timestamp
                val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                textIncidentTimestamp.text = dateFormat.format(Date(incident.timestamp))
                
                // Set location
                textIncidentLocation.text = incident.location ?: "Location not available"
                
                // Set severity indicator
                val severityColor = when (incident.severity) {
                    za.co.skyner.estateguard.data.model.IncidentSeverity.LOW -> R.color.severity_low
                    za.co.skyner.estateguard.data.model.IncidentSeverity.MEDIUM -> R.color.severity_medium
                    za.co.skyner.estateguard.data.model.IncidentSeverity.HIGH -> R.color.severity_high
                    za.co.skyner.estateguard.data.model.IncidentSeverity.CRITICAL -> R.color.severity_critical
                }
                
                severityIndicator.setBackgroundColor(
                    binding.root.context.getColor(severityColor)
                )
                
                // Set status
                textIncidentStatus.text = incident.status.name.replace("_", " ")
                
                // Handle photo
                if (!incident.photoPath.isNullOrEmpty()) {
                    imageIncidentPhoto.visibility = View.VISIBLE
                    Glide.with(binding.root.context)
                        .load(incident.photoPath)
                        .placeholder(R.drawable.ic_incident)
                        .error(R.drawable.ic_incident)
                        .centerCrop()
                        .into(imageIncidentPhoto)
                } else {
                    imageIncidentPhoto.visibility = View.GONE
                }
            }
        }
    }

    class IncidentDiffCallback : DiffUtil.ItemCallback<Incident>() {
        override fun areItemsTheSame(oldItem: Incident, newItem: Incident): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Incident, newItem: Incident): Boolean {
            return oldItem == newItem
        }
    }
}
